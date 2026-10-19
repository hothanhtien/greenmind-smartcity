"use client";

import { useEffect, useRef, useState, useCallback } from "react";
import { io, Socket } from "socket.io-client";
import { getAccessToken } from "@/lib/auth";

export interface ChatSender {
  id: string;
  fullName: string;
  role: string;
}

export interface ChatMessage {
  id: string;
  campaignId: string;
  sender: ChatSender;
  content: string;
  createdAt: string;
}

export type ConnectionStatus =
  | "idle"
  | "connecting"
  | "connected"
  | "reconnecting"
  | "failed";

const API_URL =
  process.env.NEXT_PUBLIC_API_URL || "https://vodang-api.gauas.com";
const HTTP_API_URL =
  typeof window !== "undefined" ? "/api/backend" : API_URL;
const PAGE_SIZE = 20;
const HISTORY_SYNC_INTERVAL_MS = 5000;

function sortMessagesAsc(messages: ChatMessage[]) {
  return [...messages].sort(
    (a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
  );
}

function mergeMessages(current: ChatMessage[], incoming: ChatMessage[]) {
  const byId = new Map<string, ChatMessage>();

  for (const msg of current) {
    byId.set(msg.id, msg);
  }

  for (const msg of incoming) {
    byId.set(msg.id, msg);
  }

  return sortMessagesAsc(Array.from(byId.values()));
}

export function useCampaignChat(campaignId: string | null) {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [status, setStatus] = useState<ConnectionStatus>("idle");
  const [error, setError] = useState<string | null>(null);
  const [loadingHistory, setLoadingHistory] = useState(false);
  const [hasMore, setHasMore] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [typingUsers, setTypingUsers] = useState<string[]>([]);

  // ── Refs (stable across renders) ───────────────────────────────────────
  const socketRef = useRef<Socket | null>(null);
  const campaignIdRef = useRef<string | null>(null);
  const isActiveRef = useRef(false);
  const reconnectAttemptRef = useRef(0);
  const typingTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  // FIX #3: Store the current messages count as a ref to avoid stale closure
  // in loadMore. Updated every time messages state changes.
  const messagesCountRef = useRef(0);
  // FIX: Track whether a message send is in-flight to prevent double-send
  const isSendingRef = useRef(false);

  // Keep messagesCountRef in sync whenever messages changes
  useEffect(() => {
    messagesCountRef.current = messages.length;
  }, [messages]);

  // ── Helpers ────────────────────────────────────────────────────────────────
  const fetchMessages = useCallback(async (id: string, skip = 0, take = 50) => {
    const token = getAccessToken();
    const res = await fetch(
      `${HTTP_API_URL}/campaigns/${id}/messages?skip=${skip}&take=${take}`,
      {
        headers: token ? { Authorization: `Bearer ${token}` } : {},
        cache: "no-store",
      }
    );

    if (!res.ok) {
      throw res;
    }

    const data = await res.json();
    return sortMessagesAsc(data?.data ?? []);
  }, []);

  // ── Fetch history ────────────────────────────────────────────────────────
  const fetchHistory = useCallback(async (id: string, mode: "replace" | "merge" = "replace") => {
    if (mode === "replace") setLoadingHistory(true);
    if (mode === "replace") setError(null);
    try {
      const list = await fetchMessages(id, 0, 50);
      if (mode === "replace") {
        setMessages(list);
      } else {
        setMessages((prev) => mergeMessages(prev, list));
      }
      if (list.length < 50) setHasMore(false);
    } catch (err) {
      if (mode === "replace") {
        const status = err instanceof Response ? err.status : 0;
        if (status === 403)
          setError("Bạn không có quyền xem chat của chiến dịch này.");
        else if (status === 401)
          setError("Vui lòng đăng nhập để xem chat.");
        else setError("Không thể tải lịch sử chat.");
      }
    } finally {
      if (mode === "replace") setLoadingHistory(false);
    }
  }, [fetchMessages]);

  // ── Load more ────────────────────────────────────────────────────────────
  const loadMore = useCallback(async () => {
    if (!campaignId || loadingMore || !hasMore) return;
    setLoadingMore(true);

    try {
      // FIX #1: Read from ref instead of stale closure.
      // messagesCountRef.current is always the latest value.
      const skipOffset = messagesCountRef.current;
      const older = await fetchMessages(campaignId, skipOffset, PAGE_SIZE);

      if (older.length === 0) {
        setHasMore(false);
      } else {
        setMessages((prev) => mergeMessages(prev, older));
        if (older.length < PAGE_SIZE) setHasMore(false);
      }
    } catch {
      // Silently fail — user can retry
    } finally {
      setLoadingMore(false);
    }
  }, [campaignId, loadingMore, hasMore, fetchMessages]);

  // ── Socket.IO effect ─────────────────────────────────────────────────────
  useEffect(() => {
    if (!campaignId) return;

    isActiveRef.current = true;
    campaignIdRef.current = campaignId;
    reconnectAttemptRef.current = 0;
    setMessages([]);
    setError(null);
    setStatus("idle");
    setHasMore(true);
    setTypingUsers([]);

    fetchHistory(campaignId);

    const token = getAccessToken();
    const socket = io(API_URL, {
      auth: {
        token: token ? `Bearer ${token}` : "",
        accessToken: token ?? "",
        authorization: token ? `Bearer ${token}` : "",
      },
      reconnectionAttempts: 5,
      reconnectionDelay: 2000,
      reconnectionDelayMax: 10000,
    });

    socketRef.current = socket;
    setStatus("connecting");

    // ── Connection handlers ──────────────────────────────────────────────
    socket.on("connect", () => {
      if (!isActiveRef.current) return;
      setStatus("connected");
      setError(null);
      reconnectAttemptRef.current = 0;
      socket.emit("join_campaign", { campaignId });
    });

    socket.on("disconnect", () => {
      if (!isActiveRef.current) return;
      setStatus("reconnecting");
    });

    socket.on("connect_error", () => {
      if (!isActiveRef.current) return;
      reconnectAttemptRef.current += 1;
      if (reconnectAttemptRef.current >= 5) {
        setStatus("failed");
        setError("Không thể kết nối tới server chat.");
      } else {
        setStatus("reconnecting");
      }
    });

    socket.on("reconnect_attempt", () => {
      if (!isActiveRef.current) return;
      setStatus("reconnecting");
    });

    socket.on("reconnect_failed", () => {
      if (!isActiveRef.current) return;
      setStatus("failed");
      setError("Mất kết nối. Vui lòng nhấn Thử lại.");
    });

    // ── Message handler with full deduplication ────────────────────────────
    socket.on("new_message", (msg: ChatMessage) => {
      // FIX #3: Block events from stale sockets or wrong campaigns
      if (
        !isActiveRef.current ||
        campaignIdRef.current !== campaignId ||
        (msg.campaignId && msg.campaignId !== campaignId)
      )
        return;

      setMessages((prev) => {
        // Primary dedup: by message UUID (set in DB, guaranteed unique)
        const byId = prev.some((m) => m.id === msg.id);
        if (byId) return prev;

        return sortMessagesAsc([...prev, msg]);
      });
    });

    const historySyncInterval = setInterval(() => {
      if (!isActiveRef.current || campaignIdRef.current !== campaignId) return;
      void fetchHistory(campaignId, "merge");
    }, HISTORY_SYNC_INTERVAL_MS);

    // ── Typing indicator ────────────────────────────────────────────────
    socket.on(
      "user_typing",
      (payload: { userId: string; fullName: string }) => {
        if (!isActiveRef.current) return;
        setTypingUsers((prev) => {
          if (prev.includes(payload.fullName)) return prev;
          return [...prev, payload.fullName];
        });
        setTimeout(() => {
          setTypingUsers((prev) => prev.filter((n) => n !== payload.fullName));
        }, 3000);
      }
    );

    socket.on("error", (payload: { message: string }) => {
      if (!isActiveRef.current) return;
      setError(payload?.message || "Đã xảy ra lỗi trong phòng chat.");
    });

    return () => {
      // FIX #3: Mark inactive FIRST (synchronously) — blocks any in-flight
      // events before disconnect() is called. This prevents race between
      // cleanup and arriving socket events.
      isActiveRef.current = false;
      clearInterval(historySyncInterval);
      socket.disconnect();
      socketRef.current = null;
    };
  }, [campaignId, fetchHistory]);

  // ── Send message ────────────────────────────────────────────────────────
  const sendMessage = useCallback(
    (content: string) => {
      if (!socketRef.current || !campaignId || !content.trim()) return;
      // FIX: Prevent rapid double-send (double-click, Enter key bounce)
      if (isSendingRef.current) return;
      isSendingRef.current = true;
      socketRef.current.emit("send_message", {
        campaignId,
        content: content.trim(),
      });
      socketRef.current.emit("stop_typing", { campaignId });
      if (typingTimerRef.current) clearTimeout(typingTimerRef.current);
      setTimeout(() => {
        void fetchHistory(campaignId, "merge");
      }, 300);
      // Reset guard after a short delay — protects against rapid sends
      setTimeout(() => {
        isSendingRef.current = false;
      }, 500);
    },
    [campaignId, fetchHistory]
  );

  // ── Typing indicator ────────────────────────────────────────────────────
  const startTyping = useCallback(() => {
    if (!socketRef.current || !campaignId) return;
    socketRef.current.emit("typing", { campaignId });
    if (typingTimerRef.current) clearTimeout(typingTimerRef.current);
    typingTimerRef.current = setTimeout(() => {
      socketRef.current?.emit("stop_typing", { campaignId });
    }, 2000);
  }, [campaignId]);

  // ── Manual retry ────────────────────────────────────────────────────────
  const retryConnection = useCallback(() => {
    // FIX #3: Check that the socket is still valid (not already cleaned up)
    // and that this hook is still active before attempting reconnect.
    if (!socketRef.current || !isActiveRef.current || !campaignIdRef.current)
      return;
    reconnectAttemptRef.current = 0;
    socketRef.current.connect();
    setStatus("connecting");
  }, []);

  // ── Public API ─────────────────────────────────────────────────────────
  const connected = status === "connected";

  return {
    messages,
    connected,
    status,
    error,
    loadingHistory,
    loadingMore,
    hasMore,
    typingUsers,
    sendMessage,
    loadMore,
    startTyping,
    retryConnection,
  };
}
