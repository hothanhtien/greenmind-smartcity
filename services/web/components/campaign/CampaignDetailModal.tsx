"use client";

import { useEffect, useState, useCallback } from "react";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Campaign, CampaignParticipant } from "@/types/campaign";
import { apiGet } from "@/lib/auth";
import { Badge } from "@/components/ui/badge";
import { Calendar, CheckCircle2, MapPin, UserCircle, Users, AlertCircle, Loader2, Activity, Clock, X, MessageCircle } from "lucide-react";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Sheet, SheetContent, SheetTitle } from "@/components/ui/sheet";
import { CampaignChatPanel } from "@/components/campaign/CampaignChatPanel";
import { PendingParticipantsCard } from "./PendingParticipantsCard";

interface CampaignDetailModalProps {
  isOpen: boolean;
  onClose: () => void;
  campaignId: string | null;
}

export function CampaignDetailModal({ isOpen, onClose, campaignId }: CampaignDetailModalProps) {
  const [loading, setLoading] = useState(false);
  const [detail, setDetail] = useState<Campaign | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isChatOpen, setIsChatOpen] = useState(false);
  const [initialLoadDone, setInitialLoadDone] = useState(false);

  const fetchDetail = useCallback(async (isSilent: boolean = false) => {
    if (!campaignId) return;
    if (!isSilent) {
      setLoading(true);
    }
    setError(null);
    try {
      const data = await apiGet(`/campaigns/${campaignId}`);
      setDetail(data);
      if (!initialLoadDone) {
        setInitialLoadDone(true);
      }
    } catch (err: any) {
      setError(err.message);
    } finally {
      if (!isSilent) {
        setLoading(false);
      }
    }
  }, [campaignId, initialLoadDone]);

  useEffect(() => {
    if (!isOpen || !campaignId) return;
    fetchDetail();
  }, [isOpen, campaignId, fetchDetail]);

  const approvedParticipants = detail?.participants?.filter(p => p.status === "APPROVED" || p.status === "CHECKED_IN" || p.status === "COMPLETED") || [];

  const renderStatus = (status?: string) => {
    switch (status?.toUpperCase()) {
      case "COMPLETED":
        return <Badge className="bg-emerald-100 text-emerald-700 border-none">Đã hoàn thành</Badge>;
      case "ONGOING":
        return <Badge className="bg-blue-100 text-blue-700 border-none">Đang diễn ra</Badge>;
      case "PENDING":
        return <Badge className="bg-amber-100 text-amber-700 border-none">Sắp diễn ra</Badge>;
      case "CANCELLED":
        return <Badge className="bg-red-100 text-red-600 border-none">Đã hủy</Badge>;
      default:
        return <Badge className="bg-slate-100 text-slate-600 border-none">{status ?? "Không rõ"}</Badge>;
    }
  };

  const renderParticipantStatus = (status: "PENDING" | "APPROVED" | "REJECTED" | "CHECKED_IN" | "COMPLETED") => {
    switch (status) {
      case "COMPLETED":
        return <span className="inline-flex items-center gap-1.5 text-xs font-medium text-emerald-700 bg-emerald-50 px-2.5 py-1 rounded-full"><CheckCircle2 className="w-3.5 h-3.5" /> Hoàn thành</span>;
      case "CHECKED_IN":
        return <span className="inline-flex items-center gap-1.5 text-xs font-medium text-blue-700 bg-blue-50 px-2.5 py-1 rounded-full"><MapPin className="w-3.5 h-3.5" /> Đã điểm danh</span>;
      case "APPROVED":
        return <span className="inline-flex items-center gap-1.5 text-xs font-medium text-cyan-700 bg-cyan-50 px-2.5 py-1 rounded-full"><CheckCircle2 className="w-3.5 h-3.5" /> Đã duyệt</span>;
      case "REJECTED":
        return <span className="inline-flex items-center gap-1.5 text-xs font-medium text-red-700 bg-red-50 px-2.5 py-1 rounded-full"><X className="w-3.5 h-3.5" /> Từ chối</span>;
      default:
        return <span className="inline-flex items-center gap-1.5 text-xs font-medium text-amber-700 bg-amber-50 px-2.5 py-1 rounded-full"><Clock className="w-3.5 h-3.5" /> Chờ duyệt</span>;
    }
  };

  const finalParticipants: CampaignParticipant[] = detail?.participants || [];

  // Show loading overlay only on first load
  const showInitialLoading = loading && !initialLoadDone;

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="sm:max-w-7xl w-[97vw] p-0 overflow-hidden flex flex-col max-h-[90vh] bg-slate-50">
        <DialogHeader className="px-6 py-4 border-b border-slate-100 bg-white shrink-0">
          <DialogTitle className="text-xl font-bold flex items-center justify-between mt-1">
            <span>Thông tin Chiến dịch</span>
            {loading && initialLoadDone && (
              <span className="text-xs text-slate-400 flex items-center gap-1.5">
                <Loader2 className="w-3.5 h-3.5 animate-spin" />
                Đang cập nhật...
              </span>
            )}
          </DialogTitle>
        </DialogHeader>

        {showInitialLoading ? (
           <div className="flex-1 flex flex-col items-center justify-center py-20 bg-white">
              <Loader2 className="w-8 h-8 animate-spin text-slate-400 mb-4" />
              <p className="text-sm text-slate-500">Đang tải chi tiết...</p>
           </div>
        ) : error ? (
           <div className="flex-1 flex flex-col items-center justify-center py-20 bg-white">
              <AlertCircle className="w-10 h-10 text-red-400 mb-4" />
              <p className="text-red-600 font-medium">{error}</p>
              <button
                onClick={onClose}
                className="mt-4 px-4 py-2 bg-slate-100 rounded-lg text-sm text-slate-600 font-medium hover:bg-slate-200"
              >
                 Đóng
              </button>
           </div>
        ) : detail ? (
           <>
           <div className="flex-1 min-h-0 flex overflow-hidden">

             {/* ── LEFT: Info + Participants ──────────────────── */}
             <div className="flex flex-col min-h-0 overflow-hidden border-r border-slate-100" style={{ width: "58%" }}>

               {/* Header Info Banner */}
               <div className="px-6 py-4 bg-white border-b border-slate-100 shrink-0">
                 <div className="flex items-start justify-between mb-3 mt-1">
                   <div className="pr-6">
                     <h2 className="text-xl font-bold text-slate-900 mb-1">{detail.name}</h2>
                     <p className="text-sm text-slate-500 leading-relaxed line-clamp-2">{detail.description || "Không có mô tả chi tiết."}</p>
                   </div>
                   <div className="shrink-0">{renderStatus(detail.status)}</div>
                 </div>

                 <div className="grid grid-cols-2 gap-3 mt-3">
                   {detail.createdBy?.fullName && (
                     <div className="flex items-center gap-2.5 bg-slate-50 border border-slate-100 rounded-xl p-3">
                       <div className="w-8 h-8 rounded-full bg-slate-200 flex items-center justify-center shrink-0">
                         <UserCircle className="w-5 h-5 text-slate-500" />
                       </div>
                       <div className="min-w-0">
                         <p className="text-[10px] text-slate-400 font-medium uppercase tracking-wider">Người tổ chức</p>
                         <p className="font-semibold text-slate-800 text-sm truncate">{detail.createdBy.fullName}</p>
                       </div>
                     </div>
                   )}

                   <div className="flex items-center gap-2.5 bg-blue-50 border border-blue-100 rounded-xl p-3">
                     <div className="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center shrink-0">
                       <Calendar className="w-4 h-4 text-blue-600" />
                     </div>
                     <div className="min-w-0">
                       <p className="text-[10px] text-blue-400 font-medium uppercase tracking-wider">Thời gian</p>
                       <p className="font-semibold text-blue-800 text-xs">
                         {new Date(detail.startDate).toLocaleDateString("vi-VN")} – {new Date(detail.endDate).toLocaleDateString("vi-VN")}
                       </p>
                     </div>
                   </div>

                   <div className="flex items-center gap-2.5 bg-purple-50 border border-purple-100 rounded-xl p-3">
                     <div className="w-8 h-8 rounded-full bg-purple-100 flex items-center justify-center shrink-0">
                       <MapPin className="w-4 h-4 text-purple-600" />
                     </div>
                     <div className="min-w-0">
                       <p className="text-[10px] text-purple-400 font-medium uppercase tracking-wider">Vị trí</p>
                       <p className="font-semibold text-purple-800 text-xs">{detail.lat.toFixed(4)}, {detail.lng.toFixed(4)}</p>
                     </div>
                   </div>

                   <div className="flex items-center gap-2.5 bg-emerald-50 border border-emerald-100 rounded-xl p-3">
                     <div className="w-8 h-8 rounded-full bg-emerald-100 flex items-center justify-center shrink-0">
                       <Users className="w-4 h-4 text-emerald-600" />
                     </div>
                     <div className="min-w-0">
                       <p className="text-[10px] text-emerald-400 font-medium uppercase tracking-wider">Người tham gia</p>
                       <p className="font-bold text-emerald-800 text-lg">{detail.participantsCount ?? finalParticipants.length}</p>
                     </div>
                   </div>
                 </div>
               </div>

               {/* Pending Participants Card */}
               <div className="px-6 py-4 border-b border-slate-100 shrink-0">
                 <PendingParticipantsCard
                   campaignId={campaignId || ""}
                   onUpdate={fetchDetail}
                   onUpdateSilent={() => fetchDetail(true)}
                 />
               </div>

               {/* Participants Table */}
               <div className="flex-1 min-h-0 flex flex-col bg-white">
                 <div className="px-6 py-3 border-b border-slate-100 bg-slate-50/50 shrink-0">
                   <h3 className="font-semibold text-slate-800 text-sm">
                     Tất cả Người tham gia ({approvedParticipants.length})
                   </h3>
                 </div>
                 <ScrollArea className="flex-1">
                   <Table>
                     <TableHeader className="bg-slate-50 sticky top-0 z-10 shadow-sm">
                       <TableRow>
                         <TableHead className="w-[240px] pl-6">Người dùng</TableHead>
                         <TableHead>Trạng thái</TableHead>
                         <TableHead>Thay đổi lần cuối</TableHead>
                       </TableRow>
                     </TableHeader>
                     <TableBody>
                       {approvedParticipants.length === 0 ? (
                         <TableRow>
                           <TableCell colSpan={3} className="text-center py-12 text-slate-400">
                             <Users className="w-10 h-10 mx-auto mb-2 opacity-50" />
                             <p>Chưa có người tham gia được duyệt</p>
                           </TableCell>
                         </TableRow>
                       ) : (
                         approvedParticipants.map((p) => (
                           <TableRow key={p.id}>
                             <TableCell className="pl-6">
                               <div className="flex items-center gap-3 py-1">
                                 <UserCircle className="w-8 h-8 text-slate-300" />
                                 <div>
                                   <p className="font-semibold text-slate-900 text-sm">{p.user?.fullName || "Người dùng ẩn danh"}</p>
                                   <p className="text-xs text-slate-500">{p.user?.email || p.user?.phoneNumber || p.user?.username || "Chưa cập nhật"}</p>
                                 </div>
                               </div>
                             </TableCell>
                             <TableCell>{renderParticipantStatus(p.status)}</TableCell>
                             <TableCell className="text-xs text-slate-500">
                               {(() => {
                                 const dateObj = p.checkOutTime || p.checkInTime || p.updatedAt || p.createdAt;
                                 if (!dateObj) return "N/A";
                                 try {
                                   return new Date(dateObj).toLocaleString("vi-VN", {
                                     hour: "2-digit", minute: "2-digit",
                                     day: "2-digit", month: "2-digit", year: "numeric"
                                   });
                                 } catch { return "N/A"; }
                               })()}
                             </TableCell>
                           </TableRow>
                         ))
                       )}
                     </TableBody>
                   </Table>
                 </ScrollArea>
               </div>
             </div>

             {/* ── RIGHT: Campaign Stats Overview ───────────────────── */}
             <div className="flex flex-col min-h-0 bg-slate-50" style={{ width: "42%" }}>
               <div className="px-5 pt-4 pb-2 bg-white border-b border-slate-100 shrink-0">
                 <h3 className="font-semibold text-slate-800 text-sm">Tổng quan Chiến dịch</h3>
               </div>
               <ScrollArea className="flex-1 px-5 py-4">
                 <div className="space-y-4">
                   {/* Stats Grid */}
                   <div className="grid grid-cols-2 gap-3">
                     <div className="bg-white rounded-xl p-4 border border-slate-100">
                       <div className="flex items-center gap-2 mb-2">
                         <div className="w-8 h-8 rounded-lg bg-blue-100 flex items-center justify-center">
                           <Users className="w-4 h-4 text-blue-600" />
                         </div>
                         <span className="text-xs text-slate-500 font-medium">Tổng cộng</span>
                       </div>
                       <p className="text-2xl font-bold text-slate-800">{detail.participantsCount ?? detail.participants?.length ?? 0}</p>
                     </div>
                     <div className="bg-white rounded-xl p-4 border border-slate-100">
                       <div className="flex items-center gap-2 mb-2">
                         <div className="w-8 h-8 rounded-lg bg-amber-100 flex items-center justify-center">
                           <Clock className="w-4 h-4 text-amber-600" />
                         </div>
                         <span className="text-xs text-slate-500 font-medium">Chờ duyệt</span>
                       </div>
                       <p className="text-2xl font-bold text-amber-600">{detail.participants?.filter(p => p.status === "PENDING").length ?? 0}</p>
                     </div>
                   </div>

                   {/* Participant Status Breakdown */}
                   <div className="bg-white rounded-xl p-4 border border-slate-100">
                     <h4 className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-3">Trạng thái tham gia</h4>
                     <div className="space-y-2.5">
                       {[
                         { status: "Đã duyệt", count: approvedParticipants.filter(p => p.status === "APPROVED").length, color: "bg-cyan-500", textColor: "text-cyan-700" },
                         { status: "Đã điểm danh", count: approvedParticipants.filter(p => p.status === "CHECKED_IN").length, color: "bg-blue-500", textColor: "text-blue-700" },
                         { status: "Hoàn thành", count: approvedParticipants.filter(p => p.status === "COMPLETED").length, color: "bg-emerald-500", textColor: "text-emerald-700" },
                         { status: "Từ chối", count: detail.participants?.filter(p => p.status === "REJECTED").length ?? 0, color: "bg-red-500", textColor: "text-red-700" },
                       ].map((item) => (
                         <div key={item.status} className="flex items-center justify-between">
                           <div className="flex items-center gap-2">
                             <div className={`w-2 h-2 rounded-full ${item.color}`} />
                             <span className={`text-sm ${item.textColor}`}>{item.status}</span>
                           </div>
                           <span className={`text-sm font-semibold ${item.textColor}`}>{item.count}</span>
                         </div>
                       ))}
                     </div>
                   </div>

                   {/* Campaign Info */}
                   <div className="bg-white rounded-xl p-4 border border-slate-100">
                     <h4 className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-3">Thông tin</h4>
                     <div className="space-y-3">
                       <div className="flex items-start gap-3">
                         <div className="w-8 h-8 rounded-lg bg-slate-100 flex items-center justify-center shrink-0">
                           <MapPin className="w-4 h-4 text-slate-500" />
                         </div>
                         <div>
                           <p className="text-xs text-slate-400">Vị trí</p>
                           <p className="text-sm font-medium text-slate-700">{detail.lat.toFixed(4)}, {detail.lng.toFixed(4)}</p>
                         </div>
                       </div>
                       <div className="flex items-start gap-3">
                         <div className="w-8 h-8 rounded-lg bg-slate-100 flex items-center justify-center shrink-0">
                           <Activity className="w-4 h-4 text-slate-500" />
                         </div>
                         <div>
                           <p className="text-xs text-slate-400">Bán kính</p>
                           <p className="text-sm font-medium text-slate-700">{detail.radius} mét</p>
                         </div>
                       </div>
                       <div className="flex items-start gap-3">
                         <div className="w-8 h-8 rounded-lg bg-slate-100 flex items-center justify-center shrink-0">
                           <Calendar className="w-4 h-4 text-slate-500" />
                         </div>
                         <div>
                           <p className="text-xs text-slate-400">Thời gian</p>
                           <p className="text-sm font-medium text-slate-700">
                             {new Date(detail.startDate).toLocaleDateString("vi-VN")} – {new Date(detail.endDate).toLocaleDateString("vi-VN")}
                           </p>
                         </div>
                       </div>
                     </div>
                   </div>

                   {/* Quick Action - Chat Button */}
                   <button
                     onClick={() => setIsChatOpen(true)}
                     className="w-full flex items-center justify-center gap-3 px-4 py-4 bg-emerald-500 hover:bg-emerald-600 text-white text-base font-semibold rounded-xl transition-all active:scale-[0.98] shadow-lg hover:shadow-xl"
                   >
                     <MessageCircle className="w-6 h-6" />
                     Chat Nhóm
                   </button>
                 </div>
               </ScrollArea>
             </div>

           </div>
          </>
        ) : null}
      </DialogContent>

      {/* Chat Sheet */}
      <Sheet open={isChatOpen} onOpenChange={setIsChatOpen}>
        <SheetContent className="p-0 w-full sm:w-[400px] md:w-[450px] flex flex-col h-full max-h-screen">
          <SheetTitle className="sr-only">Chat Nhóm</SheetTitle>
          <div className="flex items-center justify-between px-4 py-3 border-b border-slate-100 bg-white shrink-0">
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 rounded-full bg-emerald-100 flex items-center justify-center">
                <MessageCircle className="w-4 h-4 text-emerald-600" />
              </div>
              <span className="font-semibold text-slate-800">Chat Nhóm</span>
            </div>
          </div>
          <div className="flex-1 min-h-0">
            {campaignId && <CampaignChatPanel campaignId={campaignId} />}
          </div>
        </SheetContent>
      </Sheet>
    </Dialog>
  );
}
