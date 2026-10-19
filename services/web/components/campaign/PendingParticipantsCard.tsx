"use client";

import { useState, useEffect } from "react";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import { CampaignParticipant } from "@/types/campaign";
import { Check, X, Clock, ChevronDown, ChevronUp, Loader2, AlertCircle } from "lucide-react";
import { getPendingParticipants, approveCampaignParticipant, rejectCampaignParticipant } from "@/lib/auth";
import { toast } from "sonner";

interface PendingParticipantsCardProps {
  campaignId: string;
  onUpdate: () => void;
  onUpdateSilent?: () => void; // Silent update - no loading indicator
}

function formatTimeAgo(dateStr: string): string {
  const now = new Date();
  const date = new Date(dateStr);
  const diffMs = now.getTime() - date.getTime();
  const diffMins = Math.floor(diffMs / 60000);
  const diffHours = Math.floor(diffMins / 60);
  const diffDays = Math.floor(diffHours / 24);

  if (diffMins < 1) return "Vừa xong";
  if (diffMins < 60) return `${diffMins} phút trước`;
  if (diffHours < 24) return `${diffHours} giờ trước`;
  return `${diffDays} ngày trước`;
}

function getInitials(name: string): string {
  return name
    ?.split(" ")
    .map((n) => n[0])
    .join("")
    .toUpperCase()
    .slice(0, 2) || "?";
}

export function PendingParticipantsCard({
  campaignId,
  onUpdate,
  onUpdateSilent,
}: PendingParticipantsCardProps) {
  const [participants, setParticipants] = useState<CampaignParticipant[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [expanded, setExpanded] = useState(true);
  const [loadingId, setLoadingId] = useState<string | null>(null);

  useEffect(() => {
    async function fetchPending() {
      if (!campaignId) return;
      setLoading(true);
      setError(null);
      try {
        const response = await getPendingParticipants(campaignId);
        const data = Array.isArray(response) ? response : (response?.data ?? []);
        setParticipants(data);
      } catch (err: any) {
        setError(err.message || "Không thể tải danh sách");
      } finally {
        setLoading(false);
      }
    }
    fetchPending();
  }, [campaignId]);

  // Background refresh every 3 seconds (silent update)
  useEffect(() => {
    const interval = setInterval(async () => {
      try {
        const response = await getPendingParticipants(campaignId);
        const data = Array.isArray(response) ? response : (response?.data ?? []);
        setParticipants(data);
        onUpdateSilent?.();
      } catch {
        // Silently fail on background refresh
      }
    }, 3000);

    return () => clearInterval(interval);
  }, [campaignId, onUpdateSilent]);

  const handleApprove = async (participantId: string) => {
    setLoadingId(participantId);
    try {
      await approveCampaignParticipant(campaignId, participantId);
      toast.success("Đã phê duyệt tham gia");
      setParticipants(prev => prev.filter(p => p.id !== participantId));
      onUpdate();
    } catch (error) {
      toast.error("Không thể phê duyệt. Vui lòng thử lại.");
    } finally {
      setLoadingId(null);
    }
  };

  const handleReject = async (participantId: string) => {
    setLoadingId(participantId);
    try {
      await rejectCampaignParticipant(campaignId, participantId);
      toast.success("Đã từ chối tham gia");
      setParticipants(prev => prev.filter(p => p.id !== participantId));
      onUpdate();
    } catch (error) {
      toast.error("Không thể từ chối. Vui lòng thử lại.");
    } finally {
      setLoadingId(null);
    }
  };

  if (error) {
    return null; // Silently hide if user doesn't have permission
  }

  if (participants.length === 0) return null;

  return (
    <div className="border border-amber-200 bg-amber-50/50 rounded-xl overflow-hidden">
      <button
        onClick={() => setExpanded(!expanded)}
        className="w-full px-4 py-3 flex items-center justify-between bg-amber-50 hover:bg-amber-100 transition-colors"
      >
        <div className="flex items-center gap-2">
          <div className="w-7 h-7 rounded-full bg-amber-100 flex items-center justify-center">
            <Clock className="w-3.5 h-3.5 text-amber-600" />
          </div>
          <span className="text-sm font-semibold text-amber-800">
            Pending approval
          </span>
          <span className="text-xs font-bold bg-amber-200 text-amber-800 px-2 py-0.5 rounded-full">
            {participants.length}
          </span>
        </div>
        {expanded ? (
          <ChevronUp className="w-4 h-4 text-amber-600" />
        ) : (
          <ChevronDown className="w-4 h-4 text-amber-600" />
        )}
      </button>

      {expanded && (
        <div className="p-3 space-y-2">
          {participants.map((participant) => (
            <div
              key={participant.id}
              className="flex items-center gap-3 p-2.5 bg-white rounded-lg border border-amber-100 hover:border-amber-200 transition-colors"
            >
              <Avatar className="w-10 h-10">
                <AvatarFallback className="bg-emerald-100 text-emerald-700 text-sm font-semibold">
                  {getInitials(participant.user?.fullName || "U")}
                </AvatarFallback>
              </Avatar>

              <div className="flex-1 min-w-0">
                <p className="font-medium text-slate-800 text-sm truncate">
                  {participant.user?.fullName || "Người dùng"}
                </p>
                <div className="flex items-center gap-1.5 text-xs text-slate-500">
                  <Clock className="w-3 h-3" />
                  {formatTimeAgo(participant.createdAt)}
                </div>
              </div>

              <div className="flex items-center gap-2">
                <Button
                  size="sm"
                  onClick={() => handleApprove(participant.id)}
                  disabled={loadingId === participant.id}
                  className="bg-emerald-500 hover:bg-emerald-600 text-white rounded-lg gap-1.5"
                >
                  {loadingId === participant.id ? (
                    <Loader2 className="w-3.5 h-3.5 animate-spin" />
                  ) : (
                    <Check className="w-3.5 h-3.5" />
                  )}
                  <span className="hidden sm:inline">Approve</span>
                </Button>
                <Button
                  size="sm"
                  variant="destructive"
                  onClick={() => handleReject(participant.id)}
                  disabled={loadingId === participant.id}
                  className="bg-red-500 hover:bg-red-600 text-white rounded-lg gap-1.5"
                >
                  {loadingId === participant.id ? (
                    <Loader2 className="w-3.5 h-3.5 animate-spin" />
                  ) : (
                    <X className="w-3.5 h-3.5" />
                  )}
                  <span className="hidden sm:inline">Reject</span>
                </Button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}