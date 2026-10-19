"use client";

import { Suspense, useState, useEffect, useMemo } from "react";
import { useSearchParams, useRouter } from "next/navigation";
import { Campaign } from "@/types/campaign";
import { Card } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { reverseGeocode } from "@/lib/geocode";
import {
  Loader2,
  Users,
  Calendar,
  Clock,
  CheckCircle2,
  MessageCircle,
  BarChart3,
  MapPin,
  ArrowLeft,
  UserCircle,
  X,
  FileText,
  CalendarDays,
  Activity,
} from "lucide-react";
import { ScrollArea } from "@/components/ui/scroll-area";
import { apiGet } from "@/lib/auth";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from "recharts";
import { CampaignChatPanel } from "@/components/campaign/CampaignChatPanel";
import { PendingParticipantsCard } from "@/components/campaign/PendingParticipantsCard";
import { CampaignLocationMap } from "@/components/campaign/CampaignLocationMap";
import dynamic from "next/dynamic";

// Dynamic import for map to avoid SSR issues
const CampaignLocationMapDynamic = dynamic(
  () => import("@/components/campaign/CampaignLocationMap").then((m) => m.CampaignLocationMap),
  { ssr: false, loading: () => <div className="w-full h-full bg-slate-100 animate-pulse rounded-xl" /> }
);

// ── Suspense wrapper vì useSearchParams cần boundary trong App Router ─────────
export default function CampaignManagementPage() {
  return (
    <Suspense fallback={<CampaignPageLoading />}>
      <CampaignManagementContent />
    </Suspense>
  );
}

function CampaignPageLoading() {
  return (
    <div className="p-6 h-[calc(100vh-64px)] flex flex-col gap-6 overflow-hidden bg-slate-50">
      <div className="animate-pulse space-y-4">
        <div className="h-8 bg-slate-200 rounded w-64" />
        <div className="h-4 bg-slate-200 rounded w-96" />
        <div className="h-[600px] bg-slate-200 rounded-xl" />
      </div>
    </div>
  );
}

// ── Main content — dùng useSearchParams bên trong Suspense ──────────────────
function CampaignManagementContent() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const [campaigns, setCampaigns] = useState<Campaign[]>([]);
  const [loading, setLoading] = useState(true);

  // Campaign được chọn để chat — null = hiển thị biểu đồ
  // Ưu tiên từ URL query, fallback vào state user chọn
  const urlCampaignId = searchParams.get("id") || searchParams.get("campaignId");
  const [userSelectedCampaign, setUserSelectedCampaign] = useState<Campaign | null>(null);

  // View mode: list = hiển thị danh sách campaign, detail = hiển thị chi tiết campaign
  const [viewMode, setViewMode] = useState<"list" | "detail">("list");
  const [campaignDetail, setCampaignDetail] = useState<Campaign | null>(null);
  const [loadingDetail, setLoadingDetail] = useState(false);
  const [campaignAddress, setCampaignAddress] = useState<string | null>(null);
  const [initialLoadDone, setInitialLoadDone] = useState(false);

  // selectedCampaign = campaign từ URL (ưu tiên cao nhất) hoặc user tự chọn
  const selectedCampaign = (() => {
    if (viewMode !== "detail") return null;
    if (urlCampaignId) {
      return campaigns.find((c) => c.id === urlCampaignId) ?? null;
    }
    return userSelectedCampaign;
  })();

  // Khi URL có campaignId, đồng bộ campaigns list → auto-select
  useEffect(() => {
    if (urlCampaignId) {
      const found = campaigns.find((c) => c.id === urlCampaignId);
      if (found) {
        setUserSelectedCampaign(found);
        setViewMode("detail");
        fetchCampaignDetail(urlCampaignId);
      }
    }
  }, [urlCampaignId, campaigns]);

  // Chart state
  const [chartMode, setChartMode] = useState<"day" | "month" | "year">("day");

  // View mode for right panel when campaign is selected: map or chat
  const [rightPanelMode, setRightPanelMode] = useState<"map" | "chat">("map");

  useEffect(() => {
    async function fetchCampaigns() {
      setLoading(true);
      try {
        const apiData = await apiGet("/campaigns/my-created-and-joined");
        // Handle new API response format with created and participated arrays
        const createdCampaigns = apiData?.created ?? [];
        const participatedCampaigns = apiData?.participated ?? [];
        const rawList: any[] = [...createdCampaigns, ...participatedCampaigns];
        setCampaigns(rawList);
      } catch (error) {
        console.error("Failed to fetch campaigns:", error);
      } finally {
        setLoading(false);
      }
    }
    fetchCampaigns();
  }, []);

  // Fetch chi tiết campaign từ API
  async function fetchCampaignDetail(id: string, isSilent: boolean = false) {
    if (!isSilent) {
      setLoadingDetail(true);
      setCampaignDetail(null);
      setCampaignAddress(null);
    }
    try {
      const data = await apiGet(`/campaigns/${id}`);
      setCampaignDetail(data);
      if (!initialLoadDone) {
        setInitialLoadDone(true);
      }
      // Reverse geocode the address
      if (data.lat && data.lng) {
        const addr = await reverseGeocode(data.lat, data.lng);
        setCampaignAddress(addr);
      }
    } catch (error) {
      console.error("Failed to fetch campaign detail:", error);
    } finally {
      if (!isSilent) {
        setLoadingDetail(false);
      }
    }
  }

  // Background refresh every 10 seconds (silent update) when in detail view
  useEffect(() => {
    if (viewMode !== "detail" || !userSelectedCampaign) return;

    const interval = setInterval(() => {
      if (userSelectedCampaign) {
        fetchCampaignDetail(userSelectedCampaign.id, true);
      }
    }, 3000);

    return () => clearInterval(interval);
  }, [viewMode, userSelectedCampaign, initialLoadDone]);

  const renderStatus = (status?: string) => {
    switch (status?.toUpperCase()) {
      case "COMPLETED":
        return (
          <span className="px-2.5 py-1 rounded-full text-[10px] font-bold bg-gradient-to-r from-emerald-500 to-emerald-600 text-white shadow-sm">
            Completed
          </span>
        );
      case "ONGOING":
        return (
          <span className="px-2.5 py-1 rounded-full text-[10px] font-bold bg-gradient-to-r from-blue-500 to-blue-600 text-white shadow-sm">
            Ongoing
          </span>
        );
      case "PENDING":
        return (
          <span className="px-2.5 py-1 rounded-full text-[10px] font-bold bg-gradient-to-r from-amber-500 to-orange-500 text-white shadow-sm">
            Upcoming
          </span>
        );
      case "CANCELLED":
        return (
          <span className="px-2.5 py-1 rounded-full text-[10px] font-bold bg-gradient-to-r from-red-500 to-red-600 text-white shadow-sm">
            Cancelled
          </span>
        );
      default:
        return (
          <span className="px-2.5 py-1 rounded-full text-[10px] font-medium bg-slate-200 text-slate-600">
            Unknown
          </span>
        );
    }
  };

  const campaignStats = useMemo(() => {
    const total = campaigns.length;
    const upcoming = campaigns.filter((campaign) => campaign.status?.toUpperCase() === "PENDING").length;
    const ongoing = campaigns.filter((campaign) => campaign.status?.toUpperCase() === "ONGOING").length;
    const completed = campaigns.filter((campaign) => campaign.status?.toUpperCase() === "COMPLETED").length;

    return { total, upcoming, ongoing, completed };
  }, [campaigns]);

  // ─── CHART AGGREGATION ──────────────────────────────────────────────────────
  const chartData = useMemo(() => {
    const counts: Record<string, number> = {};
    if (campaigns.length === 0) return [];

    campaigns.forEach((c) => {
      if (!c.startDate) return;
      const d = new Date(c.startDate);
      let dateStr = "";
      if (chartMode === "day") {
        dateStr = `${String(d.getDate()).padStart(2, "0")}/${String(d.getMonth() + 1).padStart(2, "0")}/${d.getFullYear()}`;
      } else if (chartMode === "month") {
        dateStr = `${String(d.getMonth() + 1).padStart(2, "0")}/${d.getFullYear()}`;
      } else {
        dateStr = `${d.getFullYear()}`;
      }
      counts[dateStr] = (counts[dateStr] || 0) + 1;
    });

    const entries = Object.entries(counts).map(([date, count]) => ({ date, count }));
    entries.sort((a, b) => {
      if (chartMode === "day") {
        const [d1, m1, y1] = a.date.split("/");
        const [d2, m2, y2] = b.date.split("/");
        if (y1 !== y2) return parseInt(y1) - parseInt(y2);
        if (m1 !== m2) return parseInt(m1) - parseInt(m2);
        return parseInt(d1) - parseInt(d2);
      } else if (chartMode === "month") {
        const [m1, y1] = a.date.split("/");
        const [m2, y2] = b.date.split("/");
        if (y1 !== y2) return parseInt(y1) - parseInt(y2);
        return parseInt(m1) - parseInt(m2);
      } else {
        return parseInt(a.date) - parseInt(b.date);
      }
    });
    return entries;
  }, [campaigns, chartMode]);

  // Sort campaigns: future campaigns first (by startDate ascending), then past campaigns (by startDate descending)
  const sortedCampaigns = useMemo(() => {
    const now = new Date();
    now.setHours(0, 0, 0, 0);

    const sorted = [...campaigns].sort((a, b) => {
      const dateA = new Date(a.startDate);
      const dateB = new Date(b.startDate);
      const isFutureA = dateA >= now;
      const isFutureB = dateB >= now;

      if (isFutureA && !isFutureB) return -1;
      if (!isFutureA && isFutureB) return 1;

      if (isFutureA) {
        return dateA.getTime() - dateB.getTime();
      } else {
        return dateB.getTime() - dateA.getTime();
      }
    });
    return sorted;
  }, [campaigns]);

  const resetToCampaignList = () => {
    setUserSelectedCampaign(null);
    setViewMode("list");
    setCampaignDetail(null);
    setCampaignAddress(null);
    setLoadingDetail(false);
    setRightPanelMode("map");

    if (urlCampaignId) {
      router.replace("/dashboard/campaign-management", { scroll: false });
    }
  };

  // ─── LEFT PANEL CONTENT ─────────────────────────────────────────────────────
  const renderLeftPanel = () => {
    // Show detail or list view
    if (viewMode === "detail" && userSelectedCampaign) {
      return (
        <>
          {/* Header với nút quay lại */}
          <div className="p-4 border-b border-slate-100 bg-gradient-to-r from-blue-50 to-white shrink-0">
            <div className="space-y-3 mb-3">
              <button
                onClick={resetToCampaignList}
                className="w-[140px] h-9 flex items-center justify-center gap-2 bg-emerald-500 hover:bg-emerald-600 text-white text-sm font-semibold rounded-lg shadow-sm transition-all"
              >
                <ArrowLeft className="w-4 h-4" />
                Back to list
              </button>
              <div className="min-w-0">
                <h2 className="font-bold text-slate-800 text-base leading-snug break-words">
                  {userSelectedCampaign?.name || campaignDetail?.name}
                </h2>
                <p className="text-xs text-slate-400 mt-0.5 break-words">
                  Created by: <span className="font-medium text-slate-600">{campaignDetail?.createdBy?.fullName || userSelectedCampaign?.createdBy?.fullName}</span>
                </p>
              </div>
            </div>
          </div>

          {/* Nội dung chi tiết */}
          <ScrollArea className="flex-1 min-h-0 p-4">
            <div className="space-y-2">
              {/* Description */}
              {(campaignDetail || userSelectedCampaign)?.description && (
                <div className="bg-blue-50 border border-blue-100 rounded-xl p-4">
                  <div className="flex items-center justify-between mb-2">
                    <div className="flex items-center gap-2">
                      <FileText className="w-4 h-4 text-blue-500" />
                      <span className="text-xs font-semibold text-blue-600 uppercase tracking-wide">Description</span>
                    </div>
                    <div className="flex items-center gap-1 text-xs text-slate-500">
                      <Calendar className="w-3 h-3 text-amber-500" />
                      <span>
                        {new Date((campaignDetail || userSelectedCampaign)?.startDate!).toLocaleDateString("vi-VN", { day: "2-digit", month: "2-digit", year: "numeric" })}
                        <span className="text-slate-400 mx-0.5">→</span>
                        {new Date((campaignDetail || userSelectedCampaign)?.endDate!).toLocaleDateString("vi-VN", { day: "2-digit", month: "2-digit", year: "numeric" })}
                      </span>
                    </div>
                  </div>
                  <p className="text-sm text-slate-700 leading-relaxed">
                    {(campaignDetail || userSelectedCampaign)?.description}
                  </p>
                </div>
              )}

              {/* Thống kê - inline horizontal */}
              <div className="flex items-center gap-2 text-xs">
                <div className="flex items-center gap-1.5 bg-emerald-50 border border-emerald-100 rounded-lg px-3 py-2">
                  <Users className="w-4 h-4 text-emerald-500" />
                  <span className="font-bold text-emerald-700">
                    {campaignDetail?.participantsCount ?? campaignDetail?.participants?.length ?? userSelectedCampaign?.participantsCount ?? 0}
                  </span>
                  <span className="text-emerald-600">Participants</span>
                </div>
                <div className="flex items-center gap-1.5 bg-rose-50 border border-rose-100 rounded-lg px-3 py-2">
                  <FileText className="w-4 h-4 text-rose-500" />
                  <span className="font-bold text-rose-700">
                    {campaignDetail?.reports?.length ?? 0}
                  </span>
                  <span className="text-rose-600">Reports</span>
                </div>
              </div>

              {/* Pending Participants Card - only show when campaignDetail is loaded */}
              {campaignDetail && (
                <PendingParticipantsCard
                  campaignId={campaignDetail.id}
                  onUpdate={async () => {
                    await fetchCampaignDetail(campaignDetail.id);
                  }}
                  onUpdateSilent={async () => {
                    await fetchCampaignDetail(campaignDetail.id, true);
                  }}
                />
              )}

              {/* Participant list - only show when campaignDetail is loaded */}
              {campaignDetail?.participants && campaignDetail.participants.length > 0 && (
                <div className="bg-slate-50 border border-slate-200 rounded-xl p-4">
                  <div className="flex items-center gap-2 mb-3">
                    <Users className="w-4 h-4 text-slate-500" />
                    <span className="text-xs font-semibold text-slate-600 uppercase tracking-wide">Participant list</span>
                  </div>
                  <div className="space-y-2">
                    {campaignDetail.participants.map((participant) => (
                      <div key={participant.id} className="flex items-center gap-3 bg-white rounded-lg p-2.5 border border-slate-100">
                        <div className="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center shrink-0">
                          <UserCircle className="w-5 h-5 text-blue-500" />
                        </div>
                        <div className="min-w-0 flex-1">
                          <p className="font-medium text-slate-800 text-sm truncate">
                            {participant.user?.fullName || "User"}
                          </p>
                          <p className="text-xs text-slate-400 truncate">
                            {participant.user?.email || participant.user?.phoneNumber || ""}
                          </p>
                        </div>
                        <Badge className={`shrink-0 text-[10px] ${participant.status === "COMPLETED" ? "bg-emerald-100 text-emerald-700 border-none" :
                          participant.status === "CHECKED_IN" ? "bg-blue-100 text-blue-700 border-none" :
                            participant.status === "APPROVED" ? "bg-cyan-100 text-cyan-700 border-none" :
                              participant.status === "PENDING" ? "bg-amber-100 text-amber-700 border-none" :
                                "bg-red-100 text-red-600 border-none"
                          }`}>
                          {participant.status === "COMPLETED" ? "Completed" :
                            participant.status === "CHECKED_IN" ? "Checked in" :
                              participant.status === "APPROVED" ? "Approved" :
                                participant.status === "PENDING" ? "Pending" : "Rejected"}
                        </Badge>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          </ScrollArea>
        </>
      );
    }

    // List view (default)
    return (
      <>
        <div className="p-4 border-b border-slate-100 bg-gradient-to-r from-blue-50 to-white shrink-0">
          <div className="flex items-center justify-between">
            <h2 className="font-bold text-slate-800 flex items-center gap-2 text-sm">
              <div className="w-7 h-7 rounded-lg bg-primary/10 flex items-center justify-center">
                <Calendar className="w-4 h-4 text-primary" />
              </div>
              All Campaigns
            </h2>
            <span className="px-2.5 py-1 bg-gradient-to-r from-blue-500 to-blue-600 text-white text-[10px] font-bold rounded-full shadow-sm">
              {campaigns.length}
            </span>
          </div>
        </div>

        <ScrollArea className="flex-1 min-h-0 p-3">
          {loading ? (
            <div className="space-y-3 p-2">
              {[1, 2, 3, 4, 5].map((i) => (
                <div key={i} className="h-20 bg-slate-100 rounded-xl animate-pulse" />
              ))}
            </div>
          ) : campaigns.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-12 text-center">
              <div className="w-16 h-16 bg-slate-100 rounded-full flex items-center justify-center mb-4">
                <Calendar className="w-8 h-8 text-slate-300" />
              </div>
              <p className="text-slate-500 text-sm font-medium">No campaigns yet</p>
              <p className="text-slate-400 text-xs mt-1">Create a campaign to get started</p>
            </div>
          ) : (
            <div className="space-y-2.5 pb-4">
              {sortedCampaigns.map((campaign) => {
                const isSelected = selectedCampaign?.id === campaign.id;
                return (
                  <div
                    key={campaign.id}
                    onClick={() => {
                      if (!isSelected) {
                        // Immediately set the campaign and view mode
                        setUserSelectedCampaign(campaign);
                        setViewMode("detail");
                        // Fetch detail in background
                        fetchCampaignDetail(campaign.id);
                      } else {
                        setUserSelectedCampaign(null);
                        setViewMode("list");
                        setCampaignDetail(null);
                      }
                    }}
                    className={`group p-4 rounded-xl cursor-pointer transition-all duration-200 relative ${isSelected
                      ? "bg-gradient-to-br from-blue-50 to-blue-100 border-2 border-blue-400 shadow-lg ring-4 ring-blue-100"
                      : "bg-white border border-slate-200 hover:border-blue-300 hover:shadow-lg hover:ring-4 ring-blue-50"
                      }`}
                    style={{ transform: isSelected ? 'scale(1.02)' : 'scale(1)', transition: 'all 0.2s ease' }}
                  >
                    {/* Status indicator bar */}
                    <div className={`absolute left-0 top-0 bottom-0 w-1 rounded-l-xl ${campaign.status?.toUpperCase() === "COMPLETED" ? "bg-gradient-to-b from-emerald-400 to-emerald-600" :
                      campaign.status?.toUpperCase() === "ONGOING" ? "bg-gradient-to-b from-blue-400 to-blue-600" :
                        campaign.status?.toUpperCase() === "PENDING" ? "bg-gradient-to-b from-amber-400 to-orange-500" :
                          "bg-gradient-to-b from-slate-400 to-slate-500"
                      }`} />

                    <div className="flex items-start justify-between mb-3 gap-2 pl-2">
                      <h3
                        className={`font-bold text-sm leading-snug transition-colors ${isSelected ? "text-blue-700" : "text-slate-800 group-hover:text-blue-600"
                          }`}
                      >
                        {campaign.name}
                      </h3>
                      <div className="shrink-0">{renderStatus(campaign.status)}</div>
                    </div>

                    <div className="flex items-center gap-3 pl-2">
                      <span className="flex items-center gap-1.5 bg-slate-100 text-slate-600 px-2.5 py-1 rounded-lg text-xs font-medium">
                        <Clock className="w-3 h-3 text-slate-400" />
                        {new Date(campaign.startDate).toLocaleDateString("vi-VN")}
                      </span>
                      <span className="flex items-center gap-1.5 bg-emerald-50 text-emerald-700 px-2.5 py-1 rounded-lg text-xs font-medium">
                        <Users className="w-3 h-3 text-emerald-500" />
                        {campaign.participantsCount ?? campaign.participants?.length ?? 0}
                      </span>
                    </div>

                    {/* Hover hint */}
                    {!isSelected && (
                      <div className="mt-3 pl-2 flex items-center gap-1 text-[10px] text-blue-400 opacity-0 group-hover:opacity-100 transition-opacity font-medium">
                        <MessageCircle className="w-3 h-3" />
                        Click to view details
                      </div>
                    )}
                    {isSelected && (
                      <div className="mt-3 pl-2 flex items-center gap-1 text-[10px] text-blue-500 font-medium">
                        <MessageCircle className="w-3 h-3" />
                        Viewing details
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          )}
        </ScrollArea>
      </>
    );
  };

  // ─── RIGHT PANEL CONTENT ─────────────────────────────────────────────────────
  const renderRightPanel = () => {
    if (selectedCampaign) {
      return (
        <>
          {/* Header campaign */}
          <div className="px-5 py-3.5 border-b border-slate-100 bg-white shrink-0">
            <div className="flex items-center justify-between mb-3">
              <div className="flex items-center gap-3 min-w-0">
                <div className="w-9 h-9 rounded-xl bg-emerald-100 flex items-center justify-center shrink-0">
                  <MapPin className="w-4.5 h-4.5 text-emerald-600" />
                </div>
                <div className="min-w-0">
                  <p className="font-bold text-slate-800 text-sm truncate leading-tight">
                    {selectedCampaign.name}
                  </p>
                  <div className="flex items-center gap-2 mt-0.5 flex-wrap">
                    {renderStatus(selectedCampaign.status)}
                    <span className="text-[11px] text-slate-400 flex items-center gap-1">
                      <Clock className="w-3 h-3" />
                      {new Date(selectedCampaign.startDate).toLocaleDateString("vi-VN")}
                    </span>
                    <span className="text-[11px] text-slate-400 flex items-center gap-1">
                      <Users className="w-3 h-3" />
                      {selectedCampaign.participantsCount ?? selectedCampaign.participants?.length ?? 0} people
                    </span>
                  </div>
                </div>
              </div>

              {/* Toggle buttons: Map / Chat */}
              <div className="flex items-center gap-2">
                <div className="flex bg-slate-100 p-1 rounded-lg border border-slate-200 shrink-0">
                  <button
                    onClick={() => setRightPanelMode("map")}
                    className={`px-3 py-1.5 text-xs font-semibold rounded-md transition-all ${rightPanelMode === "map"
                      ? "bg-white text-slate-800 shadow-sm"
                      : "text-slate-500 hover:text-slate-700"
                      }`}
                  >
                    <MapPin className="w-3.5 h-3.5 inline-block mr-1" />
                    Map
                  </button>
                  <button
                    onClick={() => setRightPanelMode("chat")}
                    className={`px-3 py-1.5 text-xs font-semibold rounded-md transition-all ${rightPanelMode === "chat"
                      ? "bg-white text-slate-800 shadow-sm"
                      : "text-slate-500 hover:text-slate-700"
                      }`}
                  >
                    <MessageCircle className="w-3.5 h-3.5 inline-block mr-1" />
                    Chat
                  </button>
                </div>
              </div>
            </div>
          </div>

          {/* Content: Map or Chat */}
          <div className="flex-1 min-h-0">
            {rightPanelMode === "map" ? (
              <CampaignLocationMapDynamic
                lat={Number((selectedCampaign as any).lat) || 16.065}
                lng={Number((selectedCampaign as any).lng) || 108.220}
                radius={selectedCampaign.radius || 500}
                campaignName={selectedCampaign.name}
              />
            ) : (
              <CampaignChatPanel campaignId={selectedCampaign.id} />
            )}
          </div>
        </>
      );
    }

    // Chart view (default)
    return (
      <div className="flex flex-col h-full p-6">
        <div className="grid grid-cols-1 sm:grid-cols-4 gap-3 mb-4 shrink-0">
          <div className="rounded-xl border border-slate-200 bg-slate-50 px-4 py-3">
            <div className="flex items-center justify-between gap-3">
              <div>
                <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">Total</p>
                <p className="mt-1 text-2xl font-black text-slate-800">{campaignStats.total}</p>
              </div>
              <div className="h-10 w-10 rounded-lg bg-slate-200/80 flex items-center justify-center">
                <CalendarDays className="h-5 w-5 text-slate-600" />
              </div>
            </div>
          </div>
          <div className="rounded-xl border border-amber-100 bg-amber-50 px-4 py-3">
            <div className="flex items-center justify-between gap-3">
              <div>
                <p className="text-xs font-semibold uppercase tracking-wide text-amber-600">Upcoming</p>
                <p className="mt-1 text-2xl font-black text-amber-700">{campaignStats.upcoming}</p>
              </div>
              <div className="h-10 w-10 rounded-lg bg-amber-100 flex items-center justify-center">
                <Clock className="h-5 w-5 text-amber-600" />
              </div>
            </div>
          </div>
          <div className="rounded-xl border border-blue-100 bg-blue-50 px-4 py-3">
            <div className="flex items-center justify-between gap-3">
              <div>
                <p className="text-xs font-semibold uppercase tracking-wide text-blue-600">Ongoing</p>
                <p className="mt-1 text-2xl font-black text-blue-700">{campaignStats.ongoing}</p>
              </div>
              <div className="h-10 w-10 rounded-lg bg-blue-100 flex items-center justify-center">
                <Activity className="h-5 w-5 text-blue-600" />
              </div>
            </div>
          </div>
          <div className="rounded-xl border border-emerald-100 bg-emerald-50 px-4 py-3">
            <div className="flex items-center justify-between gap-3">
              <div>
                <p className="text-xs font-semibold uppercase tracking-wide text-emerald-600">Completed</p>
                <p className="mt-1 text-2xl font-black text-emerald-700">{campaignStats.completed}</p>
              </div>
              <div className="h-10 w-10 rounded-lg bg-emerald-100 flex items-center justify-center">
                <CheckCircle2 className="h-5 w-5 text-emerald-600" />
              </div>
            </div>
          </div>
        </div>

        <div className="flex flex-col sm:flex-row sm:items-center justify-between mb-4 gap-4 shrink-0">
          <div>
            <h2 className="font-bold text-lg text-slate-800">
              Campaign Frequency by{" "}
              {chartMode === "day" ? "day" : chartMode === "month" ? "month" : "year"}
            </h2>
          </div>
          <div className="flex bg-slate-100 p-1 rounded-lg border border-slate-200 shrink-0">
            {(["day", "month", "year"] as const).map((mode) => (
              <button
                key={mode}
                onClick={() => setChartMode(mode)}
                className={`px-3 py-1.5 text-xs font-semibold rounded-md transition-all ${chartMode === mode
                  ? "bg-white text-slate-800 shadow-sm"
                  : "text-slate-500 hover:text-slate-700"
                  }`}
              >
                {mode === "day" ? "Day" : mode === "month" ? "Month" : "Year"}
              </button>
            ))}
          </div>
        </div>

        {/* Chart container - full width/height */}
        <div className="flex-1 min-h-0">
          {chartData.length === 0 ? (
            <div className="flex flex-col items-center justify-center h-full text-center">
              <div className="w-20 h-20 bg-slate-100 rounded-full flex items-center justify-center mb-4">
                <BarChart3 className="w-10 h-10 text-slate-300" />
              </div>
              <p className="text-slate-500 font-medium">No campaign data yet</p>
              <p className="text-slate-400 text-sm mt-1">Create a campaign to see the chart</p>
            </div>
          ) : (
            <ResponsiveContainer width="100%" height="100%">
              <BarChart
                data={chartData}
                margin={{ top: 28, right: 16, left: 0, bottom: 40 }}
                barCategoryGap="8%"
                barGap={4}
              >
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                <XAxis
                  dataKey="date"
                  tickLine={false}
                  axisLine={false}
                  tickMargin={6}
                  tick={{ fill: "#64748b", fontSize: 12, fontWeight: 600 }}
                  interval={0}
                  angle={-45}
                  textAnchor="end"
                  height={28}
                />
                <YAxis
                  tickLine={false}
                  axisLine={false}
                  tickMargin={8}
                  tick={{ fill: "#64748b", fontSize: 12, fontWeight: 600 }}
                  allowDecimals={false}
                />
                <Tooltip
                  cursor={{ fill: "#f1f5f9" }}
                  contentStyle={{
                    borderRadius: "12px",
                    border: "none",
                    boxShadow: "0 10px 15px -3px rgb(0 0 0 / 0.1)",
                    fontWeight: "bold",
                  }}
                  labelStyle={{ color: "#64748b", fontSize: "12px", marginBottom: "4px", fontWeight: "normal" }}
                  formatter={(value: any) => [`${value} units`, "Quantity"]}
                />
                <Bar
                  dataKey="count"
                  fill="#3b82f6"
                  radius={[4, 4, 0, 0]}
                  maxBarSize={72}
                  label={{ position: "top", fill: "#3b82f6", fontSize: 12, fontWeight: "bold", dy: -6 }}
                  animationDuration={1500}
                />
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>
      </div>
    );
  };

  // ─── RENDER ─────────────────────────────────────────────────────────────────
  return (
    <div className="p-6 h-[calc(100vh-64px)] flex flex-col gap-4 overflow-hidden bg-slate-50">
      {/* Page header */}
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 shrink-0">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">Campaign Management</h1>
          <p className="text-slate-500 mt-1 text-sm">
            Monitor campaign frequency and coordinate volunteer deployments.
          </p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-5 gap-4 flex-1 min-h-0 overflow-hidden">
        {/* Left panel - 40% width */}
        <div className="lg:col-span-2">
          <Card className="h-[calc(100vh-180px)] shadow-sm border-slate-200 flex flex-col overflow-hidden bg-white">
            {renderLeftPanel()}
          </Card>
        </div>

        {/* Right panel - 60% width */}
        <Card className="lg:col-span-3 shadow-sm border-slate-200 flex flex-col bg-white flex-1 min-h-0">
          {renderRightPanel()}
        </Card>
      </div>
    </div>
  );
}
