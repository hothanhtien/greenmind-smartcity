"use client";

import { useState, useEffect, useCallback, useRef } from "react";
import type { WasteReport, UrbanArea, EnvAlert } from "@/types/waste-report";
import { MapView } from "./MapView";
import { getAccessToken } from "@/lib/auth";

function normalizeReportStatus(status: unknown): WasteReport["status"] {
  const normalized = String(status ?? "").toLowerCase();

  if (normalized === "approved") return "approved";
  if (normalized === "done") return "done";
  return "pending";
}

interface MapContainerProps {
  areas: UrbanArea[];
  envAlerts: EnvAlert[];
  selectedWardName: string | null;
  selectedAreaId: number | null;
  highlightAreaName: string | null;
  onAreaSelect: (area: UrbanArea) => void;
  onReportSelect?: (report: WasteReport) => void;
  onClearSelection?: () => void;
  loading: boolean;
  focusedReport?: WasteReport | null;
  onViewReportDetail?: (report: WasteReport) => void;
  onClearFocusedReport?: () => void;
  // Callback to sync reports with parent (for ReportList)
  onReportsUpdate?: (reports: WasteReport[]) => void;
}

export default function MapContainer({
  areas,
  envAlerts,
  selectedWardName,
  selectedAreaId,
  highlightAreaName,
  onAreaSelect,
  onReportSelect,
  onClearSelection,
  loading,
  focusedReport,
  onViewReportDetail,
  onClearFocusedReport,
  onReportsUpdate,
}: MapContainerProps) {
  // Use useRef to hold reports so MapView doesn't re-render on data refresh
  const reportsRef = useRef<WasteReport[]>([]);
  const envAlertsRef = useRef<EnvAlert[]>(envAlerts);
  const initialReportsSetRef = useRef(false);
  const [initialReports, setInitialReports] = useState<WasteReport[]>([]);
  const [isInitialLoad, setIsInitialLoad] = useState(true);
  const [reportsVersion, setReportsVersion] = useState(0);

  // Sync envAlerts to ref
  useEffect(() => {
    envAlertsRef.current = envAlerts;
  }, [envAlerts]);

  const fetchReports = useCallback(async (showLoader: boolean = false) => {
    try {
      const token = getAccessToken();
      const reportsRes = await fetch(`${process.env.NEXT_PUBLIC_API_URL || "https://vodang-api.gauas.com"}/waste-monitoring?limit=100`, {
        headers: token ? { Authorization: `Bearer ${token}` } : undefined,
        cache: "no-store",
      }).catch(() => null);

      let validReports: WasteReport[] = [];

      if (reportsRes && reportsRes.ok) {
        const apiData = await reportsRes.json();
        const rawList: any[] = Array.isArray(apiData) ? apiData : (apiData?.data ?? []);

        validReports = rawList.map((r: any) => ({
          id: r.id,
          code: r.code || "",
          reportedBy: r.reportedBy || null,
          reportedByUserId: r.reportedByUserId || null,
          reportedByName: r.reportedBy?.fullName || r.reportedBy?.username || r.reportedByName || null,
          wardName: r.wardName || "Unknown",
          lat: r.lat ?? 0,
          lng: r.lng ?? 0,
          wasteType: r.wasteType || "mixed",
          description: r.description || null,
          status: normalizeReportStatus(r.status),
          createdAt: r.createdAt || new Date().toISOString(),
          resolvedAt: r.resolvedAt || null,
          imageUrl: r.imageUrl || null,
          imageEvidenceUrl: r.imageEvidenceUrl || null,
          segmentedImageUrl: r.segmentedImageUrl || null,
          depthImageUrl: r.depthImageUrl || null,
          heatmapUrl: r.heatmapUrl || null,
          segmentRatio: r.segmentRatio != null ? Number(r.segmentRatio) : null,
          pollutionScore: r.pollutionScore != null ? Number(r.pollutionScore) : null,
          pollutionLevel: r.pollutionLevel || null,
          campaignId: r.campaignId || null,
          campaignName: r.campaign?.name || r.campaignName || null,
        } as WasteReport));
      }

      // Update ref for internal map updates (no re-render)
      reportsRef.current = validReports;
      // Only seed MapView state once. Later polling updates reportsRef + version only,
      // so Leaflet keeps the user's current center/zoom.
      if (!initialReportsSetRef.current) {
        setInitialReports(validReports);
        initialReportsSetRef.current = true;
      }
      setReportsVersion((version) => version + 1);
      // Sync reports with parent for ReportList
      onReportsUpdate?.(validReports);
    } catch (err) {
      console.error("Failed to fetch monitoring data:", err);
    } finally {
      setIsInitialLoad(false);
    }
  }, []);

  // Initial load
  useEffect(() => {
    fetchReports(true);
  }, [fetchReports]);

  // Background refresh every 5 seconds
  useEffect(() => {
    if (isInitialLoad) return;

    const interval = setInterval(() => {
      fetchReports(false);
    }, 5000);

    return () => clearInterval(interval);
  }, [fetchReports, isInitialLoad]);

  return (
    <MapView
      areas={areas}
      reports={initialReports}
      reportsRef={reportsRef}
      reportsVersion={reportsVersion}
      envAlerts={envAlerts}
      envAlertsRef={envAlertsRef}
      selectedWardName={selectedWardName}
      selectedAreaId={selectedAreaId}
      highlightAreaName={highlightAreaName}
      onAreaSelect={onAreaSelect}
      onReportSelect={onReportSelect}
      onClearSelection={onClearSelection}
      loading={loading || isInitialLoad}
      focusedReport={focusedReport}
      onViewReportDetail={onViewReportDetail}
      onClearFocusedReport={onClearFocusedReport}
    />
  );
}
