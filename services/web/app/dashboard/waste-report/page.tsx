"use client";

import { useState, useEffect, useCallback } from "react";
import { useRouter } from "next/navigation";
import dynamic from "next/dynamic";
import type { UrbanArea, WasteReport, ReportStatus, CampaignRegion } from "@/types/waste-report";
import { ReportList, ReportDetailModal } from "@/components/waste-report/ReportList";
import type { FilterMode } from "@/components/waste-report/ReportList";
import { AreaDrawer } from "@/components/waste-report/AreaDrawer";
import { CampaignModal } from "@/components/campaign/CampaignModal";
import { WARDS } from "@/data/wardData";
import { ENV_ALERTS } from "@/data/envAlertData";
import { getAccessToken } from "@/lib/auth";

const MapContainer = dynamic(
  () => import("@/components/waste-report/MapContainer").then((m) => m.default),
  { ssr: false }
);

const MapWard = dynamic(
  () => import("@/components/waste-report/MapWard").then((m) => m.MapWard),
  { ssr: false }
);

export default function MonitoringPage() {
  const router = useRouter();
  const [areas] = useState<UrbanArea[]>(WARDS);
  const [reports, setReports] = useState<WasteReport[]>([]);
  const [loading, setLoading] = useState(false);

  const [selectedArea, setSelectedArea] = useState<UrbanArea | null>(null);
  const [highlightAreaName, setHighlightAreaName] = useState<string | null>(null);
  const [selectedWardName, setSelectedWardName] = useState<string | null>(null);
  const [selectedReportPopup, setSelectedReportPopup] = useState<WasteReport | null>(null);
  const [focusedReport, setFocusedReport] = useState<WasteReport | null>(null);
  const [campaignRegion, setCampaignRegion] = useState<CampaignRegion | null>(null);
  const [reportFilter, setReportFilter] = useState<FilterMode>("no-campaign");

  // Handle reports update from MapContainer (when MapContainer fetches new data)
  const handleReportsUpdate = useCallback((newReports: WasteReport[]) => {
    setReports(newReports);
  }, []);

  // Listen for viewReportDetail event from map popup
  useEffect(() => {
    const handleEvent = (e: Event) => {
      const customEvent = e as CustomEvent<string>;
      const reportId = customEvent.detail;
      const report = reports.find((r) => r.id === reportId);
      if (report) {
        setSelectedReportPopup(report);
      }
    };

    window.addEventListener("viewReportDetail", handleEvent);
    return () => window.removeEventListener("viewReportDetail", handleEvent);
  }, [reports]);

  const handleAreaSelect = useCallback((area: UrbanArea) => {
    setSelectedArea(area);
    setSelectedWardName(area.name);
    setHighlightAreaName(null);
  }, []);

  const handleDrawerClose = useCallback(() => {
    setSelectedArea(null);
    setSelectedWardName(null);
  }, []);

  const handleReportClick = useCallback((report: WasteReport) => {
    setFocusedReport(report);
  }, []);

  const handleViewReportDetail = useCallback((report: WasteReport) => {
    setSelectedReportPopup(report);
  }, []);

  const handleClearFocusedReport = useCallback(() => {
    setFocusedReport(null);
    setSelectedReportPopup(null);
  }, []);

  // Chuyển 1 report thành CampaignRegion rồi mở CampaignModal
  const handleCreateCampaign = useCallback((report: WasteReport) => {
    const reporterName = typeof report.reportedBy === "string"
      ? report.reportedBy
      : report.reportedBy?.fullName || report.reportedByName || "Báo cáo";

    const region: CampaignRegion = {
      id: report.id,
      name: `${report.wardName} — ${reporterName}`,
      center: { lat: report.lat, lng: report.lng },
      reports: [report],
    };
    setCampaignRegion(region);
  }, []);

  // Điều hướng đến campaign management + chọn đúng campaign để xem chat
  const handleNavigateToCampaign = useCallback((campaignId: string) => {
    router.push(`/dashboard/campaign-management?campaignId=${campaignId}`);
  }, [router]);

  const handleClearSelection = useCallback(() => {
    setSelectedWardName(null);
    setSelectedArea(null);
    setHighlightAreaName(null);
  }, []);

  return (
    <div className="flex flex-col h-[calc(100vh-64px)] overflow-hidden bg-gray-50">
      {/* Header — fixed */}
      <div className="shrink-0 px-6 pt-4 pb-3 bg-white border-b border-gray-100">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
          <div>
              <h1 className="text-xl font-bold text-gray-900 tracking-tight">
                {focusedReport ? focusedReport.wardName : "Waste Report"}
              </h1>
              <p className="text-sm text-gray-400 mt-0.5">
                {focusedReport ? "Focused view" : "Da Nang City — Real-time Dashboard"}
              </p>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <span className="flex items-center gap-1.5 text-xs text-emerald-600 font-semibold bg-emerald-50 px-3 py-1.5 rounded-full">
              <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
              Live
            </span>
            <span className="text-xs text-gray-400 font-medium">
              Updated: {new Date().toLocaleTimeString("vi-VN", {
                hour: "2-digit",
                minute: "2-digit",
              })}
            </span>
          </div>
        </div>
      </div>

      {/* Map + Right panel — fills all remaining space */}
      <div className="flex-1 min-h-0 px-6 pb-6">
        <div className="grid grid-cols-12 gap-5 h-full">
          {/* Map 80% */}
          <div className="col-span-8 h-full">
            <MapContainer
              areas={areas}
              envAlerts={ENV_ALERTS}
              selectedWardName={selectedWardName}
              selectedAreaId={selectedArea?.id ?? null}
              highlightAreaName={highlightAreaName}
              onAreaSelect={handleAreaSelect}
              onReportSelect={handleReportClick}
              onClearSelection={handleClearSelection}
              loading={loading}
              focusedReport={focusedReport}
              onViewReportDetail={handleViewReportDetail}
              onClearFocusedReport={handleClearFocusedReport}
              onReportsUpdate={handleReportsUpdate}
            />
          </div>

          {/* Right panel 20%:
              Level 1 (không chọn phường) → ReportList
              Level 2 (đã chọn phường)    → AreaDrawer
          */}
          <div className="col-span-4 h-full overflow-hidden">
            {selectedArea ? (
              <AreaDrawer
                area={selectedArea}
                wasteReports={reports}
                onClose={handleDrawerClose}
              />
            ) : (
              <ReportList
                wasteReports={reports}
                loading={loading}
                onReportClick={handleReportClick}
                onCreateCampaign={handleCreateCampaign}
                onNavigateToCampaign={handleNavigateToCampaign}
                onViewReportDetail={handleViewReportDetail}
                selectedArea={null}
                filter={reportFilter}
                onFilterChange={setReportFilter}
                focusedReportId={focusedReport?.id ?? null}
                onClearFocusedReport={handleClearFocusedReport}
              />
            )}
          </div>
        </div>
      </div>

      {/* Detail Modal cho map marker click */}
      {selectedReportPopup && (
        <ReportDetailModal
          report={selectedReportPopup}
          onClose={handleClearFocusedReport}
          onCreateCampaign={handleCreateCampaign}
          onNavigateToCampaign={handleNavigateToCampaign}
        />
      )}

      {/* Campaign creation modal (from waste report) */}
      <CampaignModal
        isOpen={!!campaignRegion}
        onClose={() => setCampaignRegion(null)}
        region={campaignRegion}
      />
    </div>
  );
}
