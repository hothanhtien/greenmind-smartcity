"use client";

import { useEffect, useRef, useState, useCallback, useLayoutEffect } from "react";
import {
  useCampaignChat,
  ChatMessage,
  ConnectionStatus,
} from "@/hooks/useCampaignChat";
import { getStoredUser } from "@/lib/auth";
import {
  Send,
  Wifi,
  WifiOff,
  Loader2,
  MessageCircle,
  AlertCircle,
  RefreshCw,
  ChevronUp,
} from "lucide-react";
import { ScrollArea } from "@/components/ui/scroll-area";

interface CampaignChatPanelProps {
  campaignId: string;
}

// ── Helpers ──────────────────────────────────────────────────────────────────

function formatTime(dateStr: string) {
  try {
    return new Date(dateStr).toLocaleTimeString("vi-VN", {
      hour: "2-digit",
      minute: "2-digit",
    });
  } catch {
    return "";
  }
}

function formatDateLabel(dateStr: string) {
  try {
    return new Date(dateStr).toLocaleDateString("vi-VN", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
    });
  } catch {
    return "";
  }
}

function groupByDate(messages: ChatMessage[]) {
  const groups: { date: string; items: ChatMessage[] }[] = [];
  let lastDate = "";
  for (const msg of messages) {
    const d = formatDateLabel(msg.createdAt);
    if (d !== lastDate) {
      groups.push({ date: d, items: [] });
      lastDate = d;
    }
    groups[groups.length - 1].items.push(msg);
  }
  return groups;
}

// ── Sub-components ────────────────────────────────────────────────────────────

function ConnectionBadge({ status }: { status: ConnectionStatus }) {
  if (status === "connected") {
    return (
      <div className="flex items-center gap-1.5">
        <span className="relative flex h-2 w-2">
          <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75" />
          <span className="relative inline-flex rounded-full h-2 w-2 bg-emerald-500" />
        </span>
        <span className="text-xs text-emerald-600 font-medium">Đã kết nối</span>
        <Wifi className="w-3.5 h-3.5 text-emerald-500" />
      </div>
    );
  }
  if (status === "connecting") {
    return (
      <div className="flex items-center gap-1.5">
        <Loader2 className="w-3.5 h-3.5 text-slate-400 animate-spin" />
        <span className="text-xs text-slate-400 font-medium">Đang kết nối...</span>
      </div>
    );
  }
  if (status === "reconnecting") {
    return (
      <div className="flex items-center gap-1.5">
        <span className="relative flex h-2 w-2">
          <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-amber-400 opacity-75" />
          <span className="relative inline-flex rounded-full h-2 w-2 bg-amber-500" />
        </span>
        <span className="text-xs text-amber-600 font-medium">Kết nối lại...</span>
      </div>
    );
  }
  return (
    <div className="flex items-center gap-1.5">
      <span className="h-2 w-2 rounded-full bg-red-500" />
      <span className="text-xs text-red-500 font-medium">Mất kết nối</span>
    </div>
  );
}

function DateSeparator({ date }: { date: string }) {
  return (
    <div className="flex items-center gap-3 my-4">
      <div className="flex-1 h-px bg-slate-200" />
      <span className="text-xs text-slate-400 font-medium px-3 py-1 bg-slate-100 rounded-full">
        {date}
      </span>
      <div className="flex-1 h-px bg-slate-200" />
    </div>
  );
}

// ── Main Component ─────────────────────────────────────────────────────────────

export function CampaignChatPanel({ campaignId }: CampaignChatPanelProps) {
  const {
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
  } = useCampaignChat(campaignId);

  const [input, setInput] = useState("");
  const scrollRef = useRef<HTMLDivElement>(null);
  const topRef = useRef<HTMLDivElement>(null);
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const inputRef = useRef(input);
  const currentUser = getStoredUser();
  const currentUserId = currentUser?.id ?? "";

  // Auto-scroll to BOTTOM when new messages arrive — uses useLayoutEffect for sync scroll
  useLayoutEffect(() => {
    if (scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    }
  }, [messages]);

  const handleInputChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    inputRef.current = e.target.value;
    setInput(e.target.value);
    startTyping();
    e.target.style.height = "auto";
    e.target.style.height = Math.min(e.target.scrollHeight, 120) + "px";
  };

  // Use ref to avoid stale closure — sendMessage reads from inputRef, not the closure
  const handleSend = useCallback(() => {
    const content = inputRef.current;
    if (!content?.trim()) return;
    sendMessage(content);
    setInput("");
    inputRef.current = "";
    if (textareaRef.current) {
      textareaRef.current.style.height = "auto";
      textareaRef.current.style.height = "28px";
    }
  }, [sendMessage]);

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  const groups = groupByDate(messages);
  const canSend = connected && input.trim().length > 0;
  const charCount = input.length;
  const showCharCount = charCount > 500;

  return (
    <div className="flex flex-col h-full min-h-0 bg-slate-50">
      {/* ── Header ────────────────────────────────────────────────────────────── */}
      <div className="flex items-center justify-between px-4 py-3 bg-white border-b border-slate-100 shrink-0">
        <div className="flex items-center gap-2.5">
          <div className="w-9 h-9 rounded-full bg-blue-50 border border-blue-100 flex items-center justify-center">
            <MessageCircle className="w-4 h-4 text-blue-500" />
          </div>
          <div>
            <p className="text-sm font-bold text-slate-800 leading-tight">Campaign Chat</p>
            {typingUsers.length > 0 ? (
              <p className="text-[10px] text-blue-500 leading-tight italic">
                {typingUsers[0]}
                {typingUsers.length > 1 ? ` and ${typingUsers.length - 1} other(s)` : ""} is typing...
              </p>
            ) : (
              <p className="text-[10px] text-slate-400 leading-tight">
                {messages.length} messages
              </p>
            )}
          </div>
        </div>
        <ConnectionBadge status={status} />
      </div>

      {/* ── Error Banner ─────────────────────────────────────────────────────── */}
      {error && (
        <div className="mx-3 mt-3 flex items-center gap-3 bg-red-50 border border-red-200 text-red-700 rounded-xl px-4 py-3 text-sm shrink-0">
          <AlertCircle className="w-4 h-4 shrink-0 text-red-500" />
          <span className="flex-1">{error}</span>
          {status === "failed" && (
            <button
              onClick={retryConnection}
              className="flex items-center gap-1.5 text-xs font-semibold text-red-600 hover:text-red-800 shrink-0 transition-colors"
            >
              <RefreshCw className="w-3 h-3" />
              Thử lại
            </button>
          )}
        </div>
      )}

      {/* ── Messages ────────────────────────────────────────────────────────── */}
      <ScrollArea className="flex-1 min-h-0 px-4 py-3" ref={topRef} viewportRef={scrollRef}>
        {loadingHistory ? (
          <div className="flex flex-col items-center justify-center py-16 gap-3 text-slate-400">
            <Loader2 className="w-7 h-7 animate-spin" />
            <p className="text-sm">Đang tải tin nhắn...</p>
          </div>
        ) : messages.length === 0 && !error ? (
          <div className="flex flex-col items-center justify-center py-16 gap-3">
            <div className="w-16 h-16 rounded-full bg-slate-100 flex items-center justify-center">
              <MessageCircle className="w-8 h-8 text-slate-300" />
            </div>
            <p className="text-sm font-medium text-slate-500">Chưa có tin nhắn nào</p>
            <p className="text-xs text-slate-400">Hãy là người đầu tiên gửi tin!</p>
          </div>
        ) : (
          <div className="space-y-1 pb-2">
            {/* Load more */}
            {hasMore && !loadingMore && (
              <div className="flex justify-center py-2">
                <button
                  onClick={loadMore}
                  className="flex items-center gap-1.5 px-4 py-1.5 text-xs font-medium text-blue-600 bg-blue-50 hover:bg-blue-100 rounded-full border border-blue-100 transition-colors"
                >
                  <ChevronUp className="w-3.5 h-3.5" />
                  Tải thêm tin nhắn
                </button>
              </div>
            )}
            {loadingMore && (
              <div className="flex justify-center py-2">
                <Loader2 className="w-4 h-4 animate-spin text-blue-400" />
              </div>
            )}

            {groups.map((group) => (
              <div key={group.date}>
                <DateSeparator date={group.date} />

                {group.items.map((msg, idx) => {
                  const isMe = msg.sender?.id === currentUserId;
                  const prevMsg = idx > 0 ? group.items[idx - 1] : null;
                  const isSameAuthor = prevMsg?.sender?.id === msg.sender?.id;
                  const isFirstInGroup = !isSameAuthor;
                  const isLastInGroup =
                    idx === group.items.length - 1 ||
                    group.items[idx + 1]?.sender?.id !== msg.sender?.id;
                  // Show avatar only for the first message of each author's group
                  const showAvatar = isFirstInGroup;

                  return (
                    <div
                      key={msg.id}
                      className={`flex items-end gap-2 ${isMe ? "flex-row-reverse" : "flex-row"} ${
                        isFirstInGroup ? "mt-3" : "mt-1"
                      } message-in`}
                    >
                      {/* Avatar */}
                      {showAvatar ? (
                        <div
                          className={`w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold shrink-0 text-white ${
                            isMe
                              ? "bg-gradient-to-br from-emerald-400 to-emerald-600"
                              : "bg-gradient-to-br from-blue-400 to-blue-600"
                          }`}
                        >
                          {isMe
                            ? currentUser?.fullName?.charAt(0).toUpperCase() || "T"
                            : msg.sender?.fullName?.charAt(0).toUpperCase() || "?"}
                        </div>
                      ) : (
                        <div className="w-8 shrink-0" />
                      )}

                      <div className={`flex flex-col max-w-[75%] ${isMe ? "items-end" : "items-start"}`}>
                        {/* Sender name — hiển thị trên MỌI tin nhắn */}
                        <span className="text-xs font-semibold mb-1 ml-1">
                          {isMe
                            ? "Bạn"
                            : msg.sender?.fullName || "Ẩn danh"}
                        </span>

                        {/* Bubble */}
                        <div
                          className={`relative px-4 py-2.5 rounded-2xl text-sm leading-relaxed break-words shadow-sm ${
                            isMe
                              ? "bg-gradient-to-br from-blue-500 to-blue-600 text-white rounded-br-sm"
                              : "bg-white text-slate-800 border border-slate-100 rounded-bl-sm"
                          }`}
                          style={{ wordBreak: "break-word" }}
                        >
                          {msg.content}
                          {/* Bubble tail */}
                          {isLastInGroup && (
                            <div
                              className="absolute bottom-0 w-0 h-0"
                              style={
                                isMe
                                  ? {
                                      right: "-6px",
                                      borderTop: "8px solid transparent",
                                      borderBottom: "8px solid transparent",
                                      borderLeft: "8px solid #3b82f6",
                                    }
                                  : {
                                      left: "-6px",
                                      borderTop: "8px solid transparent",
                                      borderBottom: "8px solid transparent",
                                      borderRight: "8px solid #fff",
                                    }
                              }
                            />
                          )}
                        </div>

                        {/* Timestamp */}
                        <span className="text-[10px] text-slate-400 mt-0.5 mx-1">
                          {formatTime(msg.createdAt)}
                        </span>
                      </div>
                    </div>
                  );
                })}
              </div>
            ))}
            <div />
          </div>
        )}
      </ScrollArea>

      {/* ── Input ────────────────────────────────────────────────────────────── */}
      <div className="shrink-0 px-4 py-3 bg-white border-t border-slate-100">
        <div
          className={`flex items-end gap-2 bg-slate-50 border rounded-2xl px-4 py-2 transition-all ${
            connected
              ? "border-slate-200 focus-within:border-blue-300 focus-within:ring-2 focus-within:ring-blue-50"
              : "border-slate-100 opacity-60"
          }`}
        >
          <textarea
            ref={textareaRef}
            rows={1}
            value={input}
            onChange={handleInputChange}
            onKeyDown={handleKeyDown}
            disabled={!connected}
            placeholder={connected ? "Nhập tin nhắn..." : "Đang kết nối..."}
            className="flex-1 bg-transparent resize-none outline-none text-sm text-slate-800 placeholder:text-slate-400 py-1 max-h-[120px] overflow-y-auto leading-5"
            style={{ height: "28px" }}
          />
          <button
            onClick={handleSend}
            disabled={!canSend}
            className="shrink-0 w-9 h-9 rounded-xl flex items-center justify-center transition-all duration-150 active:scale-95 mb-0.5 disabled:bg-slate-200 disabled:text-slate-400 disabled:cursor-not-allowed bg-blue-500 hover:bg-blue-600 text-white"
            aria-label="Gửi tin nhắn"
          >
            <Send className="w-4 h-4" />
          </button>
        </div>

        <div className="flex items-center justify-between mt-1.5 ml-1">
          <p className="text-[10px] text-slate-400">Shift+Enter để xuống dòng</p>
          {showCharCount && (
            <p className={`text-[10px] ${charCount > 1000 ? "text-red-500" : "text-amber-500"}`}>
              {charCount}/1000
            </p>
          )}
        </div>
      </div>

      <style jsx>{`
        @keyframes message-in {
          from {
            opacity: 0;
            transform: translateY(8px);
          }
          to {
            opacity: 1;
            transform: translateY(0);
          }
        }
        .message-in {
          animation: message-in 0.2s ease-out;
        }
      `}</style>
    </div>
  );
}
