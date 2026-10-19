import { useState, useEffect } from "react";
import { MessageCircle } from "lucide-react";
import type { WasteReport, ReportStatus, UrbanArea } from "@/types/waste-report";
import { reverseGeocode } from "@/lib/geocode";
import { getAccessToken } from "@/lib/auth";

// Minimum AI pollution score for a pending report to be suggested for a campaign (set in .env.local)
const CAMPAIGN_POLLUTION_THRESHOLD = Number(process.env.NEXT_PUBLIC_CAMPAIGN_POLLUTION_THRESHOLD ?? 0);

interface ReportListProps {
  wasteReports: WasteReport[];
  loading?: boolean;
  onReportClick: (report: WasteReport) => void;
  onCreateCampaign?: (report: WasteReport) => void;
  onNavigateToCampaign?: (campaignId: string) => void;
  onViewReportDetail?: (report: WasteReport) => void;
  selectedArea: UrbanArea | null;
  filter?: "no-campaign" | "approved" | "all";
  onFilterChange?: (filter: "no-campaign" | "approved" | "all") => void;
  focusedReportId?: string | null;
  onClearFocusedReport?: () => void;
}

const STATUS_CFG: Record<ReportStatus, { label: string; bg: string; text: string; dot: string; border: string }> = {
  pending: { label: "Pending", bg: "bg-red-50", text: "text-red-700", dot: "bg-red-400", border: "border-red-200" },
  approved: { label: "Approved", bg: "bg-blue-50", text: "text-blue-700", dot: "bg-blue-400", border: "border-blue-200" },
  done: { label: "Completed", bg: "bg-emerald-50", text: "text-emerald-700", dot: "bg-emerald-400", border: "border-emerald-200" },
};

export type FilterMode = "no-campaign" | "approved" | "all";

const FILTERS: { label: string; value: FilterMode }[] = [
  { label: "Awaiting Campaign", value: "no-campaign" },
  { label: "Approved", value: "approved" },
  { label: "All Reports", value: "all" },
];

const TARGET_WARD_KEY = "hoa khanh";

function normalizeWardName(value: string | null | undefined): string {
  return (value ?? "")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/đ/g, "d")
    .replace(/Đ/g, "D")
    .toLowerCase()
    .trim();
}

function isTargetWard(wardName: string | null | undefined): boolean {
  return normalizeWardName(wardName).includes(TARGET_WARD_KEY);
}

function reportHasCampaign(report: WasteReport): boolean {
  return !!report.campaignId;
}

// ─── Report Address (reverse geocode) ────────────────────────────────────────
function ReportAddress({ lat, lng }: { lat: number; lng: number }) {
  const [address, setAddress] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setAddress(null);
    setLoading(false);

    if (!lat || !lng || (lat === 0 && lng === 0)) {
      return;
    }

    const cachedKey = `addr_${lat.toFixed(6)}_${lng.toFixed(6)}`;
    const cached = sessionStorage.getItem(cachedKey);
    if (cached) {
      setAddress(cached);
      setLoading(false);
      return;
    }

    setLoading(true);
    reverseGeocode(lat, lng).then((addr) => {
      if (addr) sessionStorage.setItem(cachedKey, addr);
      setAddress(addr);
      setLoading(false);
    });
  }, [lat, lng]);

  if (!lat || !lng || (lat === 0 && lng === 0)) return null;

  return (
    <p className="text-[11px] text-gray-400 mb-0.5 truncate flex items-center gap-1">
      <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
        <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" /><circle cx="12" cy="10" r="3" />
      </svg>
      {loading ? (
        <span className="animate-pulse">Loading...</span>
      ) : address ? (
        <span>{address}</span>
      ) : (
        <span className="text-gray-300">{lat.toFixed(4)}, {lng.toFixed(4)}</span>
      )}
    </p>
  );
}

// ─── Detail Modal ────────────────────────────────────────────────────────────
export function ReportDetailModal({
  report,
  onClose,
  onCreateCampaign,
  onNavigateToCampaign,
}: {
  report: WasteReport;
  onClose: () => void;
  onCreateCampaign?: (report: WasteReport) => void;
  onNavigateToCampaign?: (campaignId: string) => void;
}) {
  const scfg = STATUS_CFG[report.status] ?? STATUS_CFG["pending"];

  const [address, setAddress] = useState<string | null>(null);
  const [loadingAddress, setLoadingAddress] = useState(false);
  const [campaignName, setCampaignName] = useState<string | null>(report.campaignName ?? null);
  const [loadingCampaignName, setLoadingCampaignName] = useState(false);

  useEffect(() => {
    if (report.lat !== 0 || report.lng !== 0) {
      const cachedKey = `addr_${report.lat.toFixed(6)}_${report.lng.toFixed(6)}`;
      const cached = sessionStorage.getItem(cachedKey);
      if (cached) {
        setAddress(cached);
        setLoadingAddress(false);
        return;
      }

      setLoadingAddress(true);
      reverseGeocode(report.lat, report.lng).then((addr) => {
        if (addr) sessionStorage.setItem(cachedKey, addr);
        setAddress(addr);
        setLoadingAddress(false);
      });
    }
  }, [report.lat, report.lng]);

  useEffect(() => {
    setCampaignName(report.campaignName ?? null);
    setLoadingCampaignName(false);

    if (!report.campaignId || report.campaignName) return;

    const cachedKey = `campaign_name_${report.campaignId}`;
    const cached = sessionStorage.getItem(cachedKey);
    if (cached) {
      setCampaignName(cached);
      return;
    }

    let cancelled = false;
    setLoadingCampaignName(true);

    const token = getAccessToken();
    fetch(`${process.env.NEXT_PUBLIC_API_URL || "https://vodang-api.gauas.com"}/campaigns/${report.campaignId}`, {
      headers: token ? { Authorization: `Bearer ${token}` } : undefined,
      cache: "no-store",
    })
      .then((res) => (res.ok ? res.json() : null))
      .then((data) => {
        if (cancelled) return;
        const name = data?.name || data?.data?.name || null;
        if (name) {
          sessionStorage.setItem(cachedKey, name);
          setCampaignName(name);
        }
      })
      .catch(() => {
        if (!cancelled) setCampaignName(null);
      })
      .finally(() => {
        if (!cancelled) setLoadingCampaignName(false);
      });

    return () => {
      cancelled = true;
    };
  }, [report.campaignId, report.campaignName]);

  const reportedTime = (() => {
    try { return new Date(report.createdAt).toLocaleString("vi-VN"); } catch { return "N/A"; }
  })();

  const hasAiImages = !!(report.segmentedImageUrl || report.depthImageUrl || report.heatmapUrl);
  const hasAiData = report.pollutionScore != null || report.pollutionLevel || report.segmentRatio != null;

  const reporterName = typeof report.reportedBy === "string" ? report.reportedBy : report.reportedBy?.fullName || report.reportedByName || "Unidentified";
  const campaignDisplayName = campaignName || (loadingCampaignName ? "Loading campaign..." : "Campaign assigned");

  return (
    <div
      className="fixed inset-0 z-[2000] flex items-center justify-center p-3 md:p-5"
      onClick={onClose}
    >
      {/* Blur overlay */}
      <div className="absolute inset-0 bg-black/50 backdrop-blur-sm" />

      {/* Modal card — wide enough for 2-col layout */}
      <div
        className="relative z-10 bg-white rounded-2xl shadow-2xl w-full max-w-6xl max-h-[94vh] overflow-hidden flex flex-col"
        onClick={(e) => e.stopPropagation()}
      >
        {/* ── Header ── */}
        <div className="shrink-0 bg-white border-b border-gray-100 px-8 pt-6 pb-5 flex items-start justify-between gap-4">
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-3 mb-2 flex-wrap">
              <span className="text-sm font-mono text-gray-400 bg-gray-50 px-3 py-1 rounded-md">
                {report.code || report.id.slice(0, 8).toUpperCase()}
              </span>
              <span className={`inline-flex items-center gap-2 px-3 py-1.5 rounded-full text-sm font-semibold ${scfg.bg} ${scfg.text}`}>
                <span className={`w-2 h-2 rounded-full ${scfg.dot} ${report.status === "pending" ? "animate-pulse" : ""}`} />
                {scfg.label}
              </span>
            </div>
            <h3 className="text-xl font-bold text-gray-900">{report.wardName}</h3>
            {(report.lat !== 0 || report.lng !== 0) && (
              <p className="text-sm text-gray-500 mt-1 flex items-center gap-1.5">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" /><circle cx="12" cy="10" r="3" /></svg>
                {loadingAddress ? (
                  <span className="text-gray-400 animate-pulse">Đang tải địa chỉ...</span>
                ) : address ? (
                  <span>{address}</span>
                ) : (
                  <span className="font-mono text-gray-400">{report.lat.toFixed(6)}, {report.lng.toFixed(6)}</span>
                )}
              </p>
            )}
          </div>
          <button
            onClick={onClose}
            className="w-10 h-10 rounded-full bg-gray-100 hover:bg-gray-200 flex items-center justify-center transition-colors shrink-0"
          >
            <svg width="14" height="14" viewBox="0 0 12 12" fill="none" stroke="currentColor" strokeWidth="2.2">
              <path d="M2 2l8 8M10 2l-8 8" />
            </svg>
          </button>
        </div>

        {/* ── Body — scrollable ── */}
        <div className="flex-1 overflow-y-auto">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-0 divide-y lg:divide-y-0 lg:divide-x divide-gray-100">

            {/* ═══ LEFT COLUMN: Images ═══ */}
            <div className="p-6 space-y-5">

              {/* Main report image */}
              {report.imageUrl ? (
                <div>
                  <p className="text-xs font-bold text-gray-400 uppercase tracking-wider mb-2">Report Image</p>
                  <img
                    src={report.imageUrl}
                    alt="Report waste image"
                    className="w-full rounded-xl object-cover max-h-80 border border-gray-100 shadow-sm bg-gray-50"
                    loading="lazy"
                    onError={(e) => { (e.target as HTMLImageElement).style.display = "none"; }}
                  />
                </div>
              ) : (
                <div className="flex items-center justify-center h-48 bg-gray-50 rounded-xl border border-dashed border-gray-200">
                  <p className="text-sm text-gray-400">No report image</p>
                </div>
              )}

              {/* Evidence image (after collection) */}
              {report.status === "done" && report.imageEvidenceUrl && (
                <div>
                  <p className="text-xs font-bold text-emerald-500 uppercase tracking-wider mb-2 flex items-center gap-1.5">
                    <span className="w-5 h-5 rounded-full bg-emerald-500 text-white flex items-center justify-center text-xs">✓</span>
                    Post-Collection Image
                  </p>
                  <img
                    src={report.imageEvidenceUrl}
                    alt="Collection evidence image"
                    className="w-full rounded-xl object-cover max-h-64 border border-emerald-100 shadow-sm bg-emerald-50/20"
                    loading="lazy"
                    onError={(e) => { (e.target as HTMLImageElement).style.display = "none"; }}
                  />
                </div>
              )}

              {/* AI-generated images */}
              {hasAiImages && (
                <div>
                  <p className="text-xs font-bold text-indigo-500 uppercase tracking-wider mb-2 flex items-center gap-1.5">
                    AI Analysis Images
                  </p>
                  <div className="grid grid-cols-3 gap-2">
                    {report.segmentedImageUrl && (
                      <div className="bg-gray-50 rounded-lg p-1.5 border border-gray-100">
                        <p className="text-xs text-gray-500 font-semibold text-center mb-1">Segmentation</p>
                        <img
                          src={report.segmentedImageUrl}
                          alt="Segment mask"
                          className="w-full aspect-square object-cover rounded-md bg-black/5"
                          loading="lazy"
                          onError={(e) => { (e.target as HTMLImageElement).style.display = "none"; }}
                        />
                      </div>
                    )}
                    {report.depthImageUrl && (
                      <div className="bg-gray-50 rounded-lg p-1.5 border border-gray-100">
                        <p className="text-xs text-gray-500 font-semibold text-center mb-1">Depth</p>
                        <img
                          src={report.depthImageUrl}
                          alt="Depth map"
                          className="w-full aspect-square object-cover rounded-md bg-black/5"
                          loading="lazy"
                          onError={(e) => { (e.target as HTMLImageElement).style.display = "none"; }}
                        />
                      </div>
                    )}
                    {report.heatmapUrl && (
                      <div className="bg-gray-50 rounded-lg p-1.5 border border-gray-100">
                        <p className="text-xs text-gray-500 font-semibold text-center mb-1">Heatmap</p>
                        <img
                          src={report.heatmapUrl}
                          alt="Heatmap"
                          className="w-full aspect-square object-cover rounded-md bg-black/5"
                          loading="lazy"
                          onError={(e) => { (e.target as HTMLImageElement).style.display = "none"; }}
                        />
                      </div>
                    )}
                  </div>
                </div>
              )}
            </div>

            {/* ═══ RIGHT COLUMN: Info ═══ */}
            <div className="p-6 space-y-4">

              {/* Description */}
              {report.description && (
                <div>
                  <p className="text-xs font-bold text-gray-400 uppercase tracking-wider mb-2">Description</p>
                  <p className="text-base text-gray-700 leading-relaxed bg-gray-50 rounded-xl p-4 border border-gray-100">
                    {report.description}
                  </p>
                </div>
              )}

              {/* AI Analysis data */}
              {hasAiData && (
                <div className="bg-indigo-50/60 rounded-xl p-5 border border-indigo-100/60">
                  <p className="text-xs font-bold text-indigo-700 uppercase tracking-wider mb-4 flex items-center gap-1.5">
                    <span>AI Analysis</span>
                  </p>
                  <div className="grid grid-cols-3 gap-3">
                    {report.pollutionScore != null && (
                      <div className="bg-white rounded-xl p-4 border border-indigo-100 shadow-sm text-center">
                        <p className="text-sm font-bold text-gray-800 mb-2">Pollution Score</p>
                        <div className="flex items-baseline justify-center gap-0.5">
                          <p className="text-2xl font-black text-indigo-600 leading-none">
                            {(Number(report.pollutionScore) * 10).toFixed(1)}
                          </p>
                          <p className="text-sm text-gray-500 font-medium">/10</p>
                        </div>
                      </div>
                    )}
                    {report.pollutionLevel && (
                      <div className="bg-white rounded-xl p-4 border border-indigo-100 shadow-sm text-center">
                        <p className="text-sm font-bold text-gray-800 mb-2">Pollution Level</p>
                        <p className="text-lg font-bold text-red-600 capitalize">{report.pollutionLevel}</p>
                      </div>
                    )}
                    {report.segmentRatio != null && (
                      <div className="bg-white rounded-xl p-4 border border-indigo-100 shadow-sm text-center">
                        <p className="text-sm font-bold text-gray-800 mb-2">Segment Ratio</p>
                        <p className="text-lg font-bold text-indigo-700">
                          {(Number(report.segmentRatio) * 100).toFixed(1)}%
                        </p>
                      </div>
                    )}
                  </div>
                </div>
              )}

              {/* Reporter */}
              <div className="flex items-center gap-4 bg-slate-50 rounded-xl p-4 border border-slate-100">
                <div className="w-11 h-11 rounded-full bg-slate-200 flex items-center justify-center shrink-0">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#64748b" strokeWidth="2">
                    <circle cx="12" cy="8" r="4" /><path d="M4 20c0-4 3.6-7 8-7s8 3 8 7" />
                  </svg>
                </div>
                <div>
                  <p className="text-xs text-gray-400 font-medium mb-0.5">Reporter</p>
                  <p className="text-base font-semibold text-slate-800">{reporterName}</p>
                </div>
              </div>

              {/* Timeline */}
              <div className="rounded-xl border border-gray-100 p-5 bg-white">
                <p className="text-xs font-bold text-gray-400 uppercase tracking-wider mb-4">Timeline</p>
                <div className="relative pl-6 border-l-2 border-gray-100">
                  <div className="relative">
                    <div className="absolute w-3 h-3 bg-indigo-500 rounded-full -left-[19px] top-1 ring-4 ring-indigo-50" />
                    <p className="text-xs text-gray-400 font-medium mb-0.5">Reported at</p>
                    <p className="text-sm font-semibold text-gray-700">{reportedTime}</p>
                  </div>
                </div>
              </div>

              {/* Campaign */}
              {report.campaignId && (
                <div className="flex items-center gap-4 bg-amber-50 rounded-xl p-4 border border-amber-100">
                  <div className="w-10 h-10 rounded-full bg-amber-100 flex items-center justify-center shrink-0 text-xl" />
                  <div className="min-w-0 flex-1">
                    <p className="text-xs text-amber-500 font-medium mb-0.5">Campaign</p>
                    <p className="text-sm font-semibold text-amber-700 break-words">{campaignDisplayName}</p>
                  </div>
                  <button
                    onClick={() => report.campaignId && onNavigateToCampaign?.(report.campaignId)}
                    className="shrink-0 flex items-center gap-2 bg-amber-500 hover:bg-amber-600 active:scale-95 text-white font-semibold px-4 py-2 rounded-xl text-sm transition-all whitespace-nowrap"
                  >
                    <MessageCircle className="w-4 h-4" />
                    View Campaign
                  </button>
                </div>
              )}

              {/* All raw fields table */}
              <div className="rounded-xl border border-gray-100 overflow-hidden">
                <div className="bg-gray-50 px-5 py-3 border-b border-gray-100">
                  <p className="text-xs font-bold text-gray-400 uppercase tracking-wider">Details</p>
                </div>
                <table className="w-full text-sm">
                  <tbody className="divide-y divide-gray-50">
                    {[
                      { label: "Report Code", value: report.code || "—" },
                      { label: "Ward", value: report.wardName },
                      { label: "Status", value: scfg.label },
                      { label: "Reporter", value: reporterName },
                      { label: "Created Date", value: reportedTime },
                      { label: "Coordinates", value: address || `${report.lat.toFixed(6)}, ${report.lng.toFixed(6)}` },
                      { label: "Pollution Score", value: report.pollutionScore != null ? (report.pollutionScore * 10).toFixed(4) : "—" },
                      { label: "Pollution Level", value: report.pollutionLevel || "—" },
                      { label: "Segment Ratio", value: report.segmentRatio != null ? (report.segmentRatio * 100).toFixed(2) + "%" : "—" },
                    ].map(({ label, value }) => (
                      <tr key={label} className="hover:bg-gray-50/50">
                        <td className="px-5 py-3 text-gray-500 font-medium whitespace-nowrap w-36">{label}</td>
                        <td className="px-5 py-3 text-gray-800 text-sm break-words min-w-0">{value}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>

        {/* ── Sticky footer: Nút tạo chiến dịch ── */}
        {report.status === "pending" && !report.campaignId && onCreateCampaign && (
          <div className="shrink-0 sticky bottom-0 bg-gradient-to-r from-indigo-50 to-purple-50 border-t border-indigo-100 px-8 py-4 flex items-center justify-between gap-4">
            <div className="min-w-0">
              <p className="text-sm font-semibold text-indigo-800">This report has high pollution level</p>
              <p className="text-xs text-indigo-400 mt-0.5">Create collection campaign to handle this area</p>
            </div>
            <button
              onClick={() => { onCreateCampaign(report); onClose(); }}
              className="flex items-center gap-2.5 bg-indigo-600 hover:bg-indigo-700 active:scale-95 text-white font-semibold px-6 py-3 rounded-xl shadow-md hover:shadow-lg transition-all whitespace-nowrap"
            >
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                <circle cx="12" cy="12" r="10" />
                <path d="M12 8v8M8 12h8" />
              </svg>
              Create Campaign
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

// ─── Main Component ───────────────────────────────────────────────────────────
export function ReportList({
  wasteReports,
  loading,
  onReportClick,
  onCreateCampaign,
  onNavigateToCampaign,
  onViewReportDetail,
  selectedArea,
  filter = "all",
  onFilterChange,
  focusedReportId,
  onClearFocusedReport,
}: ReportListProps) {

  // Báo cáo chờ tạo chiến dịch: pending, chưa có campaign, pollutionScore vượt ngưỡng cấu hình
  // Sắp xếp từ cao → thấp theo pollutionScore
  const noCampaignReports = wasteReports
    .filter(r => isTargetWard(r.wardName) && r.status === "pending" && !r.campaignId && (r.pollutionScore ?? 0) > CAMPAIGN_POLLUTION_THRESHOLD)
    .sort((a, b) => (b.pollutionScore ?? 0) - (a.pollutionScore ?? 0));

  // Báo cáo đã được duyệt
  const approvedReports = wasteReports
    .filter(r => isTargetWard(r.wardName) && r.status === "approved")
    .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());

  const filtered =
    filter === "no-campaign" ? noCampaignReports :
      filter === "approved" ? approvedReports :
        wasteReports;

  const noCampaignCount = noCampaignReports.length;
  const approvedCount = approvedReports.length;
  const totalCount = wasteReports.length;

  return (
    <>
      <div className="flex flex-col h-full bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
        {/* Panel header */}
        <div className="shrink-0 px-4 pt-4 pb-3 border-b border-gray-100">
          <div className="flex items-center justify-between mb-3">
            <h2 className="text-base font-bold text-gray-800">Waste Reports</h2>
          </div>

          {/* Mini stats */}
          <div className="grid grid-cols-3 gap-2 mb-3">
            <div
              onClick={() => onFilterChange?.("no-campaign")}
              className={`rounded-lg px-2 py-1.5 text-center cursor-pointer transition-all ${filter === "no-campaign" ? "bg-red-100 ring-2 ring-red-300" : "bg-red-50 hover:bg-red-100"
                }`}
            >
              <p className="text-lg font-bold text-red-600">{noCampaignCount}</p>
              <p className="text-xs text-red-500/70 leading-tight">Await Campaign</p>
            </div>
            <div
              onClick={() => onFilterChange?.("approved")}
              className={`rounded-lg px-2 py-1.5 text-center cursor-pointer transition-all ${filter === "approved" ? "bg-blue-100 ring-2 ring-blue-300" : "bg-blue-50 hover:bg-blue-100"
                }`}
            >
              <p className="text-lg font-bold text-blue-600">{approvedCount}</p>
              <p className="text-xs text-blue-500/70 leading-tight">Approved</p>
            </div>
            <div
              onClick={() => onFilterChange?.("all")}
              className={`rounded-lg px-2 py-1.5 text-center cursor-pointer transition-all ${filter === "all" ? "bg-gray-200 ring-2 ring-gray-400" : "bg-gray-50 hover:bg-gray-100"
                }`}
            >
              <p className="text-lg font-bold text-gray-700">{totalCount}</p>
              <p className="text-xs text-gray-500 leading-tight">All</p>
            </div>
          </div>

          {/* Filter tabs */}
          <div className="flex gap-1 pb-1">
            {FILTERS.map(f => (
              <button
                key={f.value}
                onClick={() => onFilterChange?.(f.value)}
                className={`flex-1 py-1.5 text-[11px] rounded-lg font-medium transition-all duration-150 whitespace-nowrap ${filter === f.value ? "bg-gray-900 text-white" : "text-gray-500 hover:bg-gray-100"
                  }`}
              >
                {f.label}
              </button>
            ))}
          </div>
          {filter === "no-campaign" && (
            <div className="mt-2.5 bg-indigo-50/80 border border-indigo-100 p-2.5 rounded-lg flex items-start gap-2.5">
              <div className="text-indigo-500 mt-0.5 shrink-0">
                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5"><circle cx="12" cy="12" r="10" /><path d="M12 16v-4M12 8h.01" /></svg>
              </div>
              <p className="text-xs font-medium text-indigo-800 leading-relaxed">
                These are high pollution areas, <strong className="font-bold">priority should be given to create collection campaigns</strong> as soon as possible.
              </p>
            </div>
          )}
          {filter === "approved" && (
            <div className="mt-2.5 bg-blue-50/80 border border-blue-100 p-2.5 rounded-lg flex items-start gap-2.5">
              <div className="text-blue-500 mt-0.5 shrink-0">
                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" /><polyline points="22 4 12 14.01 9 11.01" /></svg>
              </div>
              <p className="text-xs font-medium text-blue-800 leading-relaxed">
                Reports have been <strong className="font-bold">approved</strong>. Campaign chat opens when a campaign is assigned.
              </p>
            </div>
          )}
        </div>

        {/* List */}
        <div className="flex-1 overflow-y-auto min-h-0">
          {loading ? (
            <div className="p-4 space-y-3">
              {[...Array(5)].map((_, i) => (
                <div key={i} className="animate-pulse space-y-2">
                  <div className="h-3 bg-gray-100 rounded w-1/3" />
                  <div className="h-2 bg-gray-100 rounded w-full" />
                  <div className="h-2 bg-gray-100 rounded w-2/3" />
                </div>
              ))}
            </div>
          ) : filtered.length === 0 ? (
            <div className="flex items-center justify-center h-32 text-sm text-gray-400">
              No reports
            </div>
          ) : (
            <div className="divide-y divide-gray-50">
              {filtered.map(report => {
                const scfg = STATUS_CFG[report.status] ?? STATUS_CFG["pending"];
                const isHL = selectedArea?.name === report.wardName;

                let time = "N/A";
                try { time = new Date(report.createdAt).toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit" }); } catch { }

                // Navigate to campaign if report has campaignId (approved with campaign)
                // Otherwise open detail modal
                const hasCampaign = reportHasCampaign(report);

                return (
                  <div
                    key={report.id}
                    onClick={() => {
                      if (report.campaignId) {
                        onNavigateToCampaign?.(report.campaignId);
                      } else {
                        onReportClick(report);
                      }
                    }}
                    className={`w-full text-left px-4 py-3.5 transition-all duration-150 hover:bg-gray-50 group cursor-pointer ${isHL || focusedReportId === report.id
                      ? "bg-indigo-50 border-l-2 border-indigo-400"
                      : ""
                      }`}
                  >
                    {/* Row 1: status + time */}
                    <div className="flex items-center justify-between gap-2 mb-1.5">
                      <span className={`inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full text-xs font-semibold ${scfg.bg} ${scfg.text}`}>
                        <span className={`w-1.5 h-1.5 rounded-full ${scfg.dot} ${report.status === "pending" ? "animate-pulse" : ""}`} />
                        {scfg.label}
                      </span>
                      <div className="flex items-center gap-1.5">
                        {hasCampaign ? (
                          <span className="text-[10px] text-amber-500 group-hover:text-amber-600 font-medium transition-colors flex items-center gap-1">
                            <MessageCircle className="w-3 h-3" />
                            View Campaign →
                          </span>
                        ) : focusedReportId === report.id ? (
                          <span className="text-[10px] text-indigo-600 font-medium flex items-center gap-1">
                            <svg className="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10" /><path strokeWidth="2" d="M12 8v4M12 16h.01" /></svg>
                            Focused
                          </span>
                        ) : (
                          <span className="text-[10px] text-gray-300 group-hover:text-gray-400 transition-colors hidden group-hover:inline">
                            View on Map →
                          </span>
                        )}
                        <span className="text-xs text-gray-400 shrink-0 tabular-nums">{time}</span>
                      </div>
                    </div>

                    {/* Row 2: ward */}
                    <div className="flex items-center gap-1.5 mb-1">
                      <p className="text-sm font-semibold text-gray-800">{report.wardName}</p>
                      {hasCampaign && (
                        <span className="text-xs px-1.5 py-0.5 rounded-full font-medium bg-amber-50 text-amber-600 flex items-center gap-1">
                          <MessageCircle className="w-3 h-3" />
                          has chat
                        </span>
                      )}
                    </div>

                    {/* Row 3: reporter name */}
                    <p className="text-xs text-gray-500 mb-0.5 truncate">
                      {report.reportedBy || "Anonymous"}
                    </p>

                    {/* Row 4: description */}
                    {report.description && (
                      <p className="text-xs text-gray-400 leading-relaxed line-clamp-1">{report.description}</p>
                    )}
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>
    </>
  );
}
