"use client";

import { useEffect, useRef, useState, useCallback } from "react";
import type { MutableRefObject } from "react";
import L from "leaflet";
import { createRoot } from "react-dom/client";
import { WardChartPopup } from "./WardChartPopup";
import { reverseGeocode } from "@/lib/geocode";
import type { UrbanArea, WasteReport, EnvAlert } from "@/types/waste-report";
import { VIETNAM_ISLANDS_GEOJSON, HOANG_SA_ISLANDS, TRUONG_SA_ISLANDS } from "@/data/vietnam-islands";

// Fix Leaflet's default icon path resolving issue in Next.js
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
});

// ---------------------------------------------------------------------------
// Nominatim boundary cache — 2 tầng:
//   1. localStorage  → tồn tại qua reload, đóng/mở tab
//   2. _memCache     → Map in-memory, truy cập O(1) trong session
//
// ⚠ Bump CACHE_VERSION để invalidate cache cũ khi cần
// ---------------------------------------------------------------------------
const CACHE_VERSION = "v2";                              // v1 → v2: xoá null poisoned entries
const LS_PREFIX = `nominatim_boundary_${CACHE_VERSION}_`;
const LS_OLD_PREFIXES = ["nominatim_boundary_v1_"];       // các version cũ cần dọn
const _memCache = new Map<string, GeoJSON.GeoJsonObject | null>();

// Dọn dẹp các key thuộc version cũ (chạy 1 lần khi module load)
if (typeof window !== "undefined") {
  try {
    const oldKeys = Object.keys(localStorage).filter((k) =>
      LS_OLD_PREFIXES.some((prefix) => k.startsWith(prefix))
    );
    oldKeys.forEach((k) => localStorage.removeItem(k));
    if (oldKeys.length > 0) {
      console.info(`[boundary-cache] Cleaned ${oldKeys.length} stale keys from old versions`);
    }
  } catch { /* ignore */ }
}

/** Đọc từ localStorage → geo object hoặc undefined nếu chưa có */
function lsGet(ward: string): GeoJSON.GeoJsonObject | null | undefined {
  try {
    const raw = localStorage.getItem(LS_PREFIX + ward);
    if (raw === null) return undefined;          // chưa có trong LS
    const parsed = JSON.parse(raw);
    // Paranoia check: nếu parse ra null thì coi như chưa có (không trust)
    // Chỉ trust null nếu đây là v2+ (proxy hoạt động)
    return parsed as GeoJSON.GeoJsonObject | null;
  } catch {
    return undefined;
  }
}

/** Ghi vào localStorage */
function lsSet(ward: string, geo: GeoJSON.GeoJsonObject | null): void {
  try {
    localStorage.setItem(LS_PREFIX + ward, JSON.stringify(geo));
  } catch { /* quota exceeded — ignore */ }
}

/** Xoá toàn bộ cache boundary (dùng khi cần refresh) */
export function clearBoundaryCache(): void {
  _memCache.clear();
  Object.keys(localStorage)
    .filter((k) => k.startsWith(LS_PREFIX))
    .forEach((k) => localStorage.removeItem(k));
}

async function fetchWardBoundary(
  ward: string
): Promise<{ ward: string; geo: GeoJSON.GeoJsonObject | null }> {
  // 1️⃣ In-memory cache
  if (_memCache.has(ward)) return { ward, geo: _memCache.get(ward)! };

  // 2️⃣ localStorage cache
  const cached = lsGet(ward);
  if (cached !== undefined) {
    _memCache.set(ward, cached);          // warm in-memory
    return { ward, geo: cached };
  }

  // 3️⃣ Gọi qua Next.js proxy (tránh CORS với Nominatim)
  try {
    const q = `${ward}, Đà Nẵng, Việt Nam`;
    const res = await fetch(`/api/nominatim?q=${encodeURIComponent(q)}`);
    if (!res.ok) throw new Error(`Proxy error ${res.status}`);
    const data = await res.json();
    const geo: GeoJSON.GeoJsonObject | null =
      Array.isArray(data) && data.length > 0 && data[0].geojson
        ? data[0].geojson
        : null;

    _memCache.set(ward, geo);
    lsSet(ward, geo);                     // persist vào localStorage
    return { ward, geo };
  } catch {
    _memCache.set(ward, null);
    lsSet(ward, null);
    return { ward, geo: null };
  }
}

// Danh sách phường cần vẽ boundary
const WARD_NAMES = [
  "An Hải", "An Khê", "An Thắng", "Avyương", "Bà Nà", "Thanh Khê", "Liên Chiểu",
  "Bến Giằng", "Bến Hiên", "Cẩm Lệ", "Chiên Đàn", "Duy Nghĩa", "Duy Xuyên", "Hòa Xuân", "Ngũ Hành Sơn",
  "Đại Lộc", "Đắc Pring", "Điện Bàn", "Điện Bàn Bắc", "Điện Bàn Đông",
  "Điện Bàn Tây", "Đồng Dương", "Đông Giang", "Đức Phú", "Gò Nổi", "Hà Nha",
  "Hải Châu", "Hải Vân", "Hiệp Đức", "Hòa Cường", "Hòa Khánh", "Hòa Tiến", "Sơn Trà",
  "Hòa Vang", "Hoàng Sa", "Hội An", "Hội An Đông", "Hội An Tây",
  "Hùng Sơn", "Hương Trà", "Khâm Đức", "La Dêeê", "La Êeê", "Lãnh Ngọc", "Nam Giang", "Nam Phước", "Nam Trà My",
  "Nông Sơn", "Núi Thành", "Phú Ninh", "Phú Thuận", "Phước Chánh",
  "Phước Hiệp", "Phước Năng", "Phước Thành", "Phước Trà", "Quảng Phú",
  "Quế Phước", "Quế Sơn", "Quế Sơn Trung", "Sông Kôn", "Sông Vàng",
  "Sơn Cẩm Hà", "Tam Anh", "Tam Hải", "Tam Kỳ", "Tam Mỹ",
  "Tam Xuân", "Tân Hiệp", "Tây Giang", "Tây Hồ", "Thạnh Bình",
  "Thạnh Mỹ", "Thăng An", "Thăng Bình", "Thăng Điền", "Thăng Phú",
  "Thăng Trường", "Thu Bồn", "Thượng Đức", "Tiên Phước", "Trà Đốc",
  "Trà Giáp", "Trà Leng", "Trà Liên", "Trà Linh", "Trà My", "Trà Tân",
  "Trà Tập", "Trà Vân", "Việt An", "Vu Gia", "Xuân Phú"
];

const BOUNDARY_DEFAULT: L.PathOptions = {
  color: "#3b82f6", weight: 1.5,
  fillColor: "#3b82f6", fillOpacity: 0.08, opacity: 0.7,
};
const BOUNDARY_HOVER: L.PathOptions = {
  color: "#1d4ed8", weight: 2.5,
  fillColor: "#1d4ed8", fillOpacity: 0.22, opacity: 1,
};

interface MapViewProps {
  areas: UrbanArea[];
  reports: WasteReport[];
  reportsRef?: MutableRefObject<WasteReport[]>;
  reportsVersion?: number;
  envAlerts: EnvAlert[];
  envAlertsRef?: MutableRefObject<EnvAlert[]>;
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
}

// ---------------------------------------------------------------------------
// Alert level config
// ---------------------------------------------------------------------------
const ALERT_CFG: Record<
  string,
  { color: string; border: string; label: string; pulse: boolean }
> = {
  normal: { color: "#10b981", border: "#059669", label: "Bình thường", pulse: false },
  warning: { color: "#f59e0b", border: "#d97706", label: "Cảnh báo", pulse: false },
  critical: { color: "#ef4444", border: "#b91c1c", label: "Nguy hiểm", pulse: true },
};

// Ward-level status → badge color (derived from UrbanArea.status)
const WARD_STATUS_CFG: Record<string, { color: string; border: string; label: string }> = {
  red: { color: "#ef4444", border: "#b91c1c", label: "Nguy hiểm" },
  yellow: { color: "#f59e0b", border: "#d97706", label: "Cảnh báo" },
  green: { color: "#10b981", border: "#059669", label: "Bình thường" },
};

const REPORT_COLORS: Record<string, { bg: string; border: string; pulse: boolean }> = {
  pending: { bg: "#ef4444", border: "#b91c1c", pulse: true },
  approved: { bg: "#3b82f6", border: "#1d4ed8", pulse: false },
  done: { bg: "#10b981", border: "#059669", pulse: false },
};

const TARGET_REPORT_WARD_KEY = "hoa khanh";
const INITIAL_REPORT_BOUNDS_PADDING = 0.35;
const INITIAL_REPORT_MAX_ZOOM = 14;

function normalizeReportWardName(value: string | null | undefined): string {
  return (value ?? "")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/đ/g, "d")
    .replace(/Đ/g, "D")
    .toLowerCase()
    .trim();
}

function isTargetReportWard(wardName: string | null | undefined): boolean {
  return normalizeReportWardName(wardName).includes(TARGET_REPORT_WARD_KEY);
}

function normalizeWardMatchName(value: string | null | undefined): string {
  return normalizeReportWardName(value)
    .replace(/^(phuong|xa|thi tran)\s+/, "")
    .replace(/\s+/g, " ")
    .trim();
}

function isSameWardName(left: string | null | undefined, right: string | null | undefined): boolean {
  return normalizeWardMatchName(left) === normalizeWardMatchName(right);
}

// Island styling
const ISLAND_STYLE: L.PathOptions = {
  color: "#dc2626",
  weight: 2.5,
  fillColor: "#f97316",
  fillOpacity: 0.25,
  dashArray: "6, 4",
};

// Small island point styling
const ISLAND_POINT_STYLE: L.CircleMarkerOptions = {
  radius: 4,
  color: "#dc2626",
  fillColor: "#fbbf24",
  fillOpacity: 0.8,
  weight: 1.5,
};

// Reef/shoal styling
const REEF_STYLE: L.CircleMarkerOptions = {
  radius: 3,
  color: "#dc2626",
  fillColor: "#fef3c7",
  fillOpacity: 0.6,
  weight: 1,
};

// Island group label style - rounded badge with shadow
const ISLAND_GROUP_STYLE: L.DivIconOptions = {
  className: "island-group-label",
  iconSize: [180, 36],
  iconAnchor: [90, 18],
};

// ---------------------------------------------------------------------------
// Create island group labels (Quần đảo Hoàng Sa, Quần đảo Trường Sa)
// ---------------------------------------------------------------------------
function createIslandGroupLabels(): L.LayerGroup {
  const group = L.layerGroup();

  // Trường Sa label - tâm quần đảo
  const truongSaLabel = L.marker([10.35, 113.5], {
    icon: L.divIcon({
      className: "island-group-label",
      iconSize: [200, 30],
      iconAnchor: [100, 15],
      html: `<div style="
        font-size: 13px;
        font-weight: 700;
        color: #b91c1c;
        text-shadow: 0 1px 2px rgba(255,255,255,0.8);
        white-space: nowrap;
      ">QUẦN ĐẢO TRƯỜNG SA</div>`,
    }),
  });
  group.addLayer(truongSaLabel);

  // Hoàng Sa label - tâm quần đảo
  const hoangSaLabel = L.marker([16.5, 112.5], {
    icon: L.divIcon({
      className: "island-group-label",
      iconSize: [200, 30],
      iconAnchor: [100, 15],
      html: `<div style="
        font-size: 13px;
        font-weight: 700;
        color: #b91c1c;
        text-shadow: 0 1px 2px rgba(255,255,255,0.8);
        white-space: nowrap;
      ">QUẦN ĐẢO HOÀNG SA</div>`,
    }),
  });
  group.addLayer(hoangSaLabel);

  return group;
}

// Label cover style - ocean blue, solid opaque to cover labels from OSM
const LABEL_COVER_STYLE: L.PathOptions = {
  color: "#AAD3DF",
  fillColor: "#AAD3DF",
  fillOpacity: 1,
  opacity: 1,
  interactive: false,
};

// ---------------------------------------------------------------------------
// Create label cover overlays to hide labels from OSM tiles
// ---------------------------------------------------------------------------
function createVietnamLabelCovers(): L.LayerGroup {
  const group = L.layerGroup();

  // Hoàng Sa cover polygon - hình tròn tâm cách đều, bán kính ~2.5°
  // Tâm lệch về đất liền: 16°N, 112.5°E
  const hoangSaCover = L.polygon(
    [
      [18.5, 112.5],   // Bắc
      [17.3, 114.9],   // Đông Bắc
      [16.0, 115.0],   // Đông
      [14.7, 114.9],   // Đông Nam
      [13.5, 112.5],   // Nam
      [14.7, 110.1],   // Nam Tây
      [16.0, 110.0],   // Tây
      [17.3, 110.1],   // Tây Bắc
      [18.5, 112.5],   // Đóng polygon
    ],
    LABEL_COVER_STYLE
  );
  group.addLayer(hoangSaCover);

  // Trường Sa cover polygon - hình tròn tâm cách đều, bán kính ~3.5° (giảm 0.5°)
  // Tâm lệch về đất liền: 10.35°N, 113.5°E
  const truongSaCover = L.polygon(
    [
      [13.85, 113.5],  // Bắc
      [12.82, 115.97], // Đông Bắc
      [10.35, 117],    // Đông
      [7.88, 115.97],   // Đông Nam
      [6.85, 113.5],   // Nam
      [7.88, 111.03],   // Nam Tây
      [10.35, 110],    // Tây
      [12.82, 111.03],  // Tây Bắc
      [13.85, 113.5],  // Đóng polygon
    ],
    LABEL_COVER_STYLE
  );
  group.addLayer(truongSaCover);

  return group;
}

// ---------------------------------------------------------------------------
// Build ward-level marker (label pin with ward name)
// ---------------------------------------------------------------------------
function buildWardMarkerIcon(ward: UrbanArea, pendingCount: number): L.DivIcon {
  const cfg = WARD_STATUS_CFG[ward.status] ?? WARD_STATUS_CFG.green;
  const shortName = ward.name.replace(/^Phường /, "");

  return L.divIcon({
    className: "",
    iconSize: [140, 48],
    iconAnchor: [70, 48],
    html: `
      <div style="display:flex;flex-direction:column;align-items:center;pointer-events:auto;cursor:pointer;">
        <!-- label card -->
        <div style="
          position:relative;
          background:white;
          border:2.5px solid ${cfg.color};
          border-radius:12px;
          padding:5px 12px;
          box-shadow:0 3px 12px rgba(0,0,0,0.15);
          text-align:center;
          transition:transform 0.15s;
        ">
          <span style="font-size:12px;font-weight:700;color:#1f2937;display:block;white-space:nowrap;">
            ${shortName}
          </span>
          <span style="font-size:10px;font-weight:600;color:${cfg.color};">
            ${cfg.label}
          </span>
          ${pendingCount > 0 ? `
            <span style="
              position:absolute;top:-7px;right:-7px;
              background:#ef4444;color:white;
              font-size:9px;font-weight:700;
              padding:1px 5px;border-radius:99px;
              border:2px solid white;
              white-space:nowrap;
            ">${pendingCount} báo cáo</span>
          ` : ""}
        </div>
        <!-- tail -->
        <div style="
          width:0;height:0;
          border-left:7px solid transparent;
          border-right:7px solid transparent;
          border-top:8px solid ${cfg.color};
          margin-top:-1px;
        "></div>
      </div>
    `,
  });
}

// ---------------------------------------------------------------------------
// Build env alert detail marker (teardrop)
// ---------------------------------------------------------------------------
function buildAlertIcon(level: string): L.DivIcon {
  const cfg = ALERT_CFG[level] ?? ALERT_CFG.normal;
  const pulseStyle = cfg.pulse
    ? `animation:alert-pin-pulse 1.4s ease-in-out infinite;`
    : "";

  return L.divIcon({
    className: "",
    iconSize: [24, 30],
    iconAnchor: [12, 30],
    popupAnchor: [0, -32],
    html: `
      <div style="position:relative;width:24px;height:30px;${pulseStyle}">
        <div style="position:absolute;bottom:0;left:50%;transform:translateX(-50%);width:8px;height:3px;background:rgba(0,0,0,0.15);border-radius:50%;"></div>
        <div style="
          position:absolute;top:0;left:0;
          width:24px;height:24px;
          background:${cfg.color};
          border:2.5px solid ${cfg.border};
          border-radius:50% 50% 50% 0;
          transform:rotate(-45deg);
          box-shadow:0 3px 8px rgba(0,0,0,0.25);
        "></div>
        <div style="
          position:absolute;top:5px;left:5px;
          width:10px;height:10px;
          background:white;border-radius:50%;opacity:0.75;
        "></div>
      </div>
    `,
  });
}

// ---------------------------------------------------------------------------
// Build report pin icon
// ---------------------------------------------------------------------------
function buildReportIcon(status: string): L.DivIcon {
  const cfg = REPORT_COLORS[status] ?? REPORT_COLORS.pending;
  const pulseStyle = cfg.pulse ? `animation:alert-pin-pulse 1.4s ease-in-out infinite;` : "";
  return L.divIcon({
    className: "",
    iconSize: [22, 28],
    iconAnchor: [11, 28],
    popupAnchor: [0, -30],
    html: `
      <div style="position:relative;width:22px;height:28px;${pulseStyle}">
        <div style="position:absolute;bottom:0;left:50%;transform:translateX(-50%);width:7px;height:3px;background:rgba(0,0,0,0.15);border-radius:50%;"></div>
        <div style="position:absolute;top:0;left:0;width:22px;height:22px;background:${cfg.bg};border:2px solid ${cfg.border};border-radius:50% 50% 50% 0;transform:rotate(-45deg);box-shadow:0 2px 6px rgba(0,0,0,0.22);"></div>
      </div>
    `,
  });
}

// ---------------------------------------------------------------------------
// Main component
// ---------------------------------------------------------------------------
export function MapView({
  areas,
  reports,
  reportsRef: externalReportsRef,
  reportsVersion = 0,
  envAlerts,
  envAlertsRef: externalEnvAlertsRef,
  selectedWardName,
  highlightAreaName,
  onAreaSelect,
  onReportSelect,
  onClearSelection,
  loading,
  focusedReport,
  onViewReportDetail,
  onClearFocusedReport,
}: MapViewProps) {
  const mapContainerRef = useRef<HTMLDivElement>(null);
  const mapRef = useRef<L.Map | null>(null);
  // Layer for ward-overview markers (level 1)
  const wardLayerRef = useRef<L.LayerGroup | null>(null);
  // Layer for ward boundary polygons from Nominatim (level 1)
  const boundaryLayerRef = useRef<L.LayerGroup | null>(null);
  // Layer for Vietnam islands (Hoàng Sa & Trường Sa) - always visible
  const islandsLayerRef = useRef<L.GeoJSON | null>(null);
  // Layer for island name labels (separate layer for z-index control)
  const islandLabelsLayerRef = useRef<L.LayerGroup | null>(null);
  // Layer for env-alert detail markers (level 2)
  const alertLayerRef = useRef<L.LayerGroup | null>(null);
  // Layer for report pins (always on top)
  const reportLayerRef = useRef<L.LayerGroup | null>(null);
  // Layer for focused report marker (single point with detail button)
  const focusedReportLayerRef = useRef<L.LayerGroup | null>(null);

  const [mapLoaded, setMapLoaded] = useState(false);
  const [boundaryLoading, setBoundaryLoading] = useState(false);
  const [boundaryLoaded, setBoundaryLoaded] = useState(0);

  const [popupWardData, setPopupWardData] = useState<{ wardName: string; reports: WasteReport[], allReports?: WasteReport[] } | null>(null);

  // State for reverse geocoded address in focused report popup
  const [focusedAddress, setFocusedAddress] = useState<string | null>(null);

  // Internal ref to track reports
  const internalReportsRef = useRef<WasteReport[]>(reports);

  // Use external reportsRef if provided, otherwise use internal ref
  const reportsRef = externalReportsRef ?? internalReportsRef;

  // If using external ref, sync internal ref on first load
  if (externalReportsRef) {
    internalReportsRef.current = reports;
  }

  // Sync reports prop to internal ref - no re-render when data refreshes
  useEffect(() => {
    internalReportsRef.current = reports;
  }, [reports]);

  // Internal ref for envAlerts
  const internalEnvAlertsRef = useRef<EnvAlert[]>(envAlerts);

  // Use external envAlertsRef if provided, otherwise use internal ref
  const envAlertsRef = externalEnvAlertsRef ?? internalEnvAlertsRef;

  // Sync envAlerts prop to internal ref
  useEffect(() => {
    internalEnvAlertsRef.current = envAlerts;
  }, [envAlerts]);

  // Track if initial map render is complete (to avoid re-rendering map on data refresh)
  const mapInitialRenderRef = useRef(false);
  const initialViewportFitRef = useRef(false);
  const previousSelectedWardNameRef = useRef<string | null>(null);
  const previousFocusedReportRef = useRef<WasteReport | null>(null);

  // ── Inject keyframe once ────────────────────────────────────────────────
  useEffect(() => {
    const id = "alert-pin-style";
    if (!document.getElementById(id)) {
      const s = document.createElement("style");
      s.id = id;
      s.textContent = `
        @keyframes alert-pin-pulse {
          0%,100% { transform: scale(1) translateY(0); }
          50%      { transform: scale(1.2) translateY(-3px); }
        }
      `;
      document.head.appendChild(s);
    }
  }, []);

  // ── Reverse geocode address when focused report changes ──────────────────
  useEffect(() => {
    if (!focusedReport) {
      setFocusedAddress(null);
      return;
    }

    const cachedKey = `addr_${focusedReport.lat.toFixed(6)}_${focusedReport.lng.toFixed(6)}`;
    const cached = sessionStorage.getItem(cachedKey);
    if (cached) {
      setFocusedAddress(cached);
      return;
    }

    reverseGeocode(focusedReport.lat, focusedReport.lng).then((address) => {
      if (address) {
        sessionStorage.setItem(cachedKey, address);
        setFocusedAddress(address);
      } else {
        setFocusedAddress("Không xác định được địa chỉ");
      }
    });
  }, [focusedReport]);

  // ── Init map once ────────────────────────────────────────────────────────
  useEffect(() => {
    if (!mapContainerRef.current || mapRef.current) return;

    const map = L.map(mapContainerRef.current, {
      center: [16.065, 108.220],
      zoom: 13,
      zoomControl: false,
      attributionControl: false,
    });

    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
      attribution: "&copy; OpenStreetMap contributors",
    }).addTo(map);

    L.control.zoom({ position: "topleft" }).addTo(map);
    L.control.attribution({ position: "bottomleft", prefix: false }).addTo(map);

    wardLayerRef.current = L.layerGroup().addTo(map);
    boundaryLayerRef.current = L.layerGroup().addTo(map); // polygons dưới ward markers

    // Label cover overlays (add first - bottom layer)
    const labelCoverLayer = createVietnamLabelCovers();
    labelCoverLayer.addTo(map);

    // Vietnam islands layer (on top of cover)
    islandsLayerRef.current = L.geoJSON(VIETNAM_ISLANDS_GEOJSON, {
      pointToLayer: (feature, latlng) => {
        const type = feature.properties?.type as string;
        const style = type === "reef" || type === "shoal" ? REEF_STYLE : ISLAND_POINT_STYLE;
        return L.circleMarker(latlng, style);
      },
      onEachFeature: (feature, layer) => {
        const name = feature.properties?.name as string;
        const nameEn = feature.properties?.nameEn as string;

        if (layer instanceof L.CircleMarker) {
          // Store island names in options for later use
          (layer.options as any)._islandName = name;
          (layer.options as any)._islandNameEn = nameEn;
        }
      },
    }).addTo(map);

    // Show island labels when zoomed in enough (zoom >= 10)
    const updateIslandLabels = () => {
      if (!islandsLayerRef.current) return;
      const zoom = map.getZoom();

      islandsLayerRef.current.eachLayer((layer) => {
        if (!(layer instanceof L.CircleMarker)) return;

        const name = (layer.options as any)._islandName;
        const nameEn = (layer.options as any)._islandNameEn;
        if (!name) return;

        const label = `${name} (${nameEn})`;

        if (zoom >= 10) {
          layer.bindTooltip(label, {
            permanent: true,
            direction: "top",
            className: "island-name-label",
            offset: [0, -10],
          });
        } else {
          layer.bindTooltip(label, {
            permanent: false,
            direction: "top",
            className: "monitoring-leaflet-tooltip",
            offset: [0, -8],
          });
        }
      });
    };

    map.on("zoomend", updateIslandLabels);
    updateIslandLabels(); // Initial check

    // Island group labels (on top of islands) - e.g. "QUẦN ĐẢO HOÀNG SA"
    const islandGroupLabelLayer = createIslandGroupLabels();
    islandGroupLabelLayer.addTo(map);

    alertLayerRef.current = L.layerGroup().addTo(map);
    reportLayerRef.current = L.layerGroup().addTo(map);
    focusedReportLayerRef.current = L.layerGroup().addTo(map);

    mapRef.current = map;
    setMapLoaded(true);
    mapInitialRenderRef.current = true;

    // Listen for clear focused report event from popup
    const handleClearFocused = () => onClearFocusedReport?.();
    window.addEventListener("_clearFocusedReport", handleClearFocused);

    return () => {
      map.remove();
      mapRef.current = null;
      window.removeEventListener("_clearFocusedReport", handleClearFocused);
    };
  }, [onClearFocusedReport]);

  // ── LEVEL 1: Draw one marker per ward ───────────────────────────────────
  const drawWardOverview = useCallback(() => {
    if (!wardLayerRef.current) return;
    wardLayerRef.current.clearLayers();

    areas.forEach((ward) => {
      // Use reportsRef for dynamic pending count
      const pendingCount = reportsRef.current.filter(
        (r) => isSameWardName(r.wardName, ward.name) && r.status === "pending"
      ).length;
      const icon = buildWardMarkerIcon(ward, pendingCount);

      L.marker([ward.lat, ward.lng], { icon, zIndexOffset: 100 })
        .addTo(wardLayerRef.current!)
        .on("click", () => onAreaSelect(ward));
    });
  }, [areas, reportsRef, onAreaSelect]);

  // ── LEVEL 2: Draw env-alert detail markers for one ward ─────────────────
  const drawAlertDetail = useCallback(
    (wardName: string) => {
      if (!alertLayerRef.current) return;
      alertLayerRef.current.clearLayers();

      // Use envAlertsRef to avoid dependency changes
      const filtered = envAlertsRef.current.filter((a) => a.wardName === wardName);

      filtered.forEach((alert) => {
        const cfg = ALERT_CFG[alert.level] ?? ALERT_CFG.normal;
        const icon = buildAlertIcon(alert.level);

        const marker = L.marker([alert.lat, alert.lng], {
          icon,
          zIndexOffset: 300,
        }).addTo(alertLayerRef.current!);

        marker.bindPopup(
          `<div style="font-family:system-ui;min-width:200px;max-width:240px;padding:2px 0">
            <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:6px;">
              <span style="font-size:13px;font-weight:700;color:#111;">${alert.wardName}</span>
              <span style="
                font-size:10px;font-weight:700;padding:2px 8px;border-radius:99px;
                background:${cfg.color}22;color:${cfg.color};border:1px solid ${cfg.color}44;
              ">${cfg.label}</span>
            </div>
            ${alert.description ? `<p style="font-size:11px;color:#555;margin:0 0 6px;">${alert.description}</p>` : ""}
            <div style="display:flex;align-items:center;gap:6px;">
              <span style="width:8px;height:8px;border-radius:50%;background:${cfg.color};display:inline-block;flex-shrink:0;"></span>
              <span style="font-size:10px;color:#aaa;">${alert.lat.toFixed(4)}, ${alert.lng.toFixed(4)}</span>
            </div>
          </div>`,
          { maxWidth: 260, className: "monitoring-leaflet-popup" }
        );
      });
    },
    [envAlertsRef]
  );

  // ── BOUNDARY: fetch + draw 1 lần khi map sẵn sàng ─────────────────────────
  // Không phụ thuộc vào reports — popup sẽ đọc từ reportsRef tại thời điểm click
  const drawWardBoundaries = useCallback(async () => {
    if (!boundaryLayerRef.current) return;
    boundaryLayerRef.current.clearLayers();
    setBoundaryLoading(true);
    let loaded = 0;
    const BATCH = 8;

    for (let i = 0; i < WARD_NAMES.length; i += BATCH) {
      if (!boundaryLayerRef.current) break;

      const batch = WARD_NAMES.slice(i, i + BATCH);
      const results = await Promise.all(batch.map(fetchWardBoundary));

      results.forEach(({ ward, geo }) => {
        if (!geo || !boundaryLayerRef.current) return;

        // Style mặc định xanh — sẽ cập nhật màu khi reports có dữ liệu
        const defaultStyle: L.PathOptions = {
          color: "#3b82f6", weight: 1.8,
          fillColor: "#3b82f6", fillOpacity: 0.08, opacity: 0.8,
        };
        const hoverBaseStyle: L.PathOptions = {
          color: "#1d4ed8", weight: 2.8,
          fillColor: "#1d4ed8", fillOpacity: 0.22, opacity: 1,
        };

        const geoLayer = L.geoJSON(geo, {
          style: () => ({ ...defaultStyle }),
          onEachFeature: (_feat, featureLayer) => {
            if (!(featureLayer instanceof L.Path)) return;

            featureLayer.on({
              mouseover: () => {
                // Tính màu dynamic từ reports hiện tại
                const snap = reportsRef.current;
                const wPending = snap.filter(
                  (r) => isSameWardName(r.wardName, ward)
                ).filter(r => r.status === "pending").length;
                const wTotal = snap.filter(
                  (r) => isSameWardName(r.wardName, ward)
                ).length;
                const hColor = wPending > 3 ? "#ef4444"
                  : wPending > 0 ? "#f59e0b"
                    : wTotal > 0 ? "#10b981"
                      : "#1d4ed8";
                featureLayer.setStyle({ ...hoverBaseStyle, color: hColor, fillColor: hColor });
              },
              mouseout: () => {
                featureLayer.setStyle({ ...defaultStyle });
              },
              click: (e: L.LeafletMouseEvent) => {
                // Zoom vào polygon
                const bounds = (featureLayer as unknown as L.Polygon).getBounds?.();
                if (bounds) {
                  mapRef.current?.flyToBounds(bounds.pad(0.08), {
                    duration: 0.6, maxZoom: 15,
                  });
                }

                // Lọc danh sách reports thuộc phường hiện tại
                const snap = reportsRef.current;
                const wardReports = snap.filter(
                  (r) => isSameWardName(r.wardName, ward)
                );

                // Hiện Popup bự ở giữa màn hình (React state)
                setPopupWardData({ wardName: ward, reports: wardReports, allReports: snap });
              },
            });

            featureLayer.bindTooltip(ward, {
              sticky: true, direction: "top", offset: [0, -4],
              className: "monitoring-leaflet-tooltip",
            });
          },
        });

        boundaryLayerRef.current!.addLayer(geoLayer);
        loaded++;
        setBoundaryLoaded(loaded);
      });

      if (i + BATCH < WARD_NAMES.length) {
        await new Promise((r) => setTimeout(r, 400));
      }
    }

    setBoundaryLoading(false);
  }, []); // deps rỗng — chỉ chạy 1 lần khi mount

  // ── Draw report pins ─────────────────────────────────────────────────────
  const drawReportPins = useCallback(
    (wardName: string | null) => {
      if (!reportLayerRef.current) return;
      reportLayerRef.current.clearLayers();

      // Use reportsRef to avoid re-render when reports refresh
      const filtered = reportsRef.current.filter((report) => isTargetReportWard(report.wardName));

      filtered.forEach((report) => {
        // Bỏ qua report có tọa độ (0,0) — dữ liệu không hợp lệ
        if (report.lat === 0 && report.lng === 0) return;

        const icon = buildReportIcon(report.status);
        const marker = L.marker([report.lat, report.lng], {
          icon,
          zIndexOffset: 500,
        }).addTo(reportLayerRef.current!);

        marker.on("click", () => {
          if (onReportSelect) onReportSelect(report);
        });
      });
    },
    [reportsRef, onReportSelect]
  );

  // ── Draw focused report marker (single point with view detail button) ────
  const drawFocusedReport = useCallback(
    (report: WasteReport | null) => {
      if (!focusedReportLayerRef.current) return;
      focusedReportLayerRef.current.clearLayers();

      if (!report) return;

      const icon = buildReportIcon(report.status);
      const marker = L.marker([report.lat, report.lng], {
        icon,
        zIndexOffset: 600,
      }).addTo(focusedReportLayerRef.current!);

      // Format date
      const formatDate = (dateStr: string) => {
        try {
          const date = new Date(dateStr);
          return date.toLocaleDateString("vi-VN", { day: "2-digit", month: "2-digit", year: "numeric" });
        } catch { return dateStr; }
      };

      // Reporter name
      const reporterName = report.reportedBy || "Anonymous";

      // Build popup content with dynamic address - use cached immediately if available
      const cachedKey = `addr_${report.lat.toFixed(6)}_${report.lng.toFixed(6)}`;
      const cached = sessionStorage.getItem(cachedKey);
      // Priority: cached > focusedAddress > "Đang tải..."
      const displayAddress = cached || (focusedAddress && focusedAddress !== "Không xác định được địa chỉ" ? focusedAddress : null);

      const buildPopupContent = (address: string) => `
        <div style="font-family:system-ui;width:205px;max-width:205px;padding:0;box-sizing:border-box;">
          <div style="padding:6px 8px 4px;display:flex;align-items:center;gap:6px;border-bottom:1px solid #e5e7eb;">
            <button
              onclick="window.dispatchEvent(new CustomEvent('_clearFocusedReport'))"
              style="
                background:#10b981;color:white;font-weight:600;font-size:11px;
                padding:4px 9px;border-radius:6px;border:none;cursor:pointer;
                transition:background 0.15s;white-space:nowrap;
              "
              onmouseover="this.style.background='#059669'"
              onmouseout="this.style.background='#10b981'"
            >
              ← Back
            </button>
          </div>
          <div style="padding:7px 9px 2px;">
            <p style="font-size:13px;font-weight:700;color:#1f2937;margin:0;line-height:1.25;">${report.wardName}</p>
          </div>
          <div style="padding:3px 9px;font-size:11px;color:#374151;line-height:1.35;max-height:112px;overflow-y:auto;overflow-wrap:anywhere;">
            <div style="margin-bottom:3px;"><span style="font-weight:600;">Người báo cáo:</span> ${reporterName}</div>
            <div style="margin-bottom:3px;"><span style="font-weight:600;">Địa chỉ:</span> ${displayAddress || "Đang tải..."}</div>
            <div style="margin-bottom:4px;"><span style="font-weight:600;">Ngày tạo:</span> ${formatDate(report.createdAt)}</div>
          </div>
          <div style="padding:5px 9px 8px;">
            <button
              onclick="window.dispatchEvent(new CustomEvent('viewReportDetail', {detail: '${report.id}'}))"
              style="
                display:block;width:100%;background:#4f46e5;color:white;font-weight:600;font-size:12px;
                padding:6px 10px;border-radius:7px;border:none;cursor:pointer;
                transition:background 0.15s;
              "
              onmouseover="this.style.background='#4338ca'"
              onmouseout="this.style.background='#4f46e5'"
            >
              View Detail
            </button>
          </div>
        </div>
      `;

      const popupContent = buildPopupContent(displayAddress ?? "");

      marker.bindPopup(popupContent, {
        minWidth: 205,
        maxWidth: 205,
        className: "focused-report-popup",
        offset: [0, -10],
        closeButton: false,
        autoPan: false,
      });

      // Open popup immediately with cached address if available
      marker.openPopup();
    },
    [onViewReportDetail, focusedAddress]
  );

  // Refs to hold callback functions - prevents dependency changes
  const drawAlertDetailRef = useRef<((wardName: string) => void) | null>(null);
  const drawReportPinsRef = useRef<((wardName: string | null) => void) | null>(null);
  const drawFocusedReportRef = useRef<((report: WasteReport | null) => void) | null>(null);

  // Keep refs in sync with callbacks
  drawAlertDetailRef.current = drawAlertDetail;
  drawReportPinsRef.current = drawReportPins;
  drawFocusedReportRef.current = drawFocusedReport;

  const fitToInitialReportViewport = useCallback((duration = 0.5) => {
    const validReports = reportsRef.current.filter((r) => isTargetReportWard(r.wardName) && r.lat !== 0 && r.lng !== 0);

    if (validReports.length > 0) {
      const bounds = L.latLngBounds(validReports.map((r) => L.latLng(r.lat, r.lng)));
      mapRef.current?.flyToBounds(bounds.pad(INITIAL_REPORT_BOUNDS_PADDING), {
        duration,
        maxZoom: INITIAL_REPORT_MAX_ZOOM,
      });
    } else {
      mapRef.current?.flyTo([16.065, 108.220], 13, { duration });
    }
  }, [reportsRef]);

  // ── Main render logic: switch between level 1 and level 2 ───────────────
  useEffect(() => {
    if (!mapLoaded || !mapRef.current) return;
    const previousSelectedWardName = previousSelectedWardNameRef.current;
    const previousFocusedReport = previousFocusedReportRef.current;
    const targetFocusedReport = focusedReport && isTargetReportWard(focusedReport.wardName)
      ? focusedReport
      : null;

    // Clear all layers first
    wardLayerRef.current?.clearLayers();
    alertLayerRef.current?.clearLayers();

    if (!selectedWardName) {
      // ── Level 1: boundary polygons + report pins ──

      // Clear focused report layer when back
      if (!targetFocusedReport) {
        focusedReportLayerRef.current?.clearLayers();
      }

      // Nếu có focused report → chỉ hiện 1 marker đó, zoom vào tọa độ
      if (targetFocusedReport) {
        reportLayerRef.current?.clearLayers();

        // Ẩn boundary layer để tránh click nhầm
        if (boundaryLayerRef.current && mapRef.current.hasLayer(boundaryLayerRef.current)) {
          mapRef.current.removeLayer(boundaryLayerRef.current);
        }

        // Keep the focused marker lower in the viewport so the popup does not
        // collide with the top controls.
        const map = mapRef.current;
        const targetZoom = 17;
        const reportPoint = map.project([targetFocusedReport.lat, targetFocusedReport.lng], targetZoom);
        const offsetCenter = map.unproject(reportPoint.subtract([0, 110]), targetZoom);
        map.flyTo(offsetCenter, targetZoom, { duration: 0.3 });

        // Vẽ marker ngay lập tức sau khi set view
        drawFocusedReportRef.current?.(targetFocusedReport);
      } else {
        // Boundary layer luôn được hiện ở level 1
        if (boundaryLayerRef.current && !mapRef.current.hasLayer(boundaryLayerRef.current)) {
          mapRef.current.addLayer(boundaryLayerRef.current);
        }

        // Clear focused report layer
        focusedReportLayerRef.current?.clearLayers();

        drawReportPinsRef.current?.(null);

        if ((previousFocusedReport && !targetFocusedReport) || previousSelectedWardName) {
          fitToInitialReportViewport(0.5);
        }
      }
    } else {
      // ── Level 2: ward detail — ẩn boundary để gọn bản đồ ──
      wardLayerRef.current?.clearLayers();
      if (boundaryLayerRef.current) {
        mapRef.current?.removeLayer(boundaryLayerRef.current);
      }
      drawAlertDetailRef.current?.(selectedWardName);
      drawReportPinsRef.current?.(selectedWardName);

      const ward = areas.find((a) => a.name === selectedWardName);
      if (ward) {
        if (ward.bounds && ward.bounds.length >= 3) {
          const bounds = L.latLngBounds(ward.bounds.map(([lat, lng]) => L.latLng(lat, lng)));
          mapRef.current?.flyToBounds(bounds.pad(0.35), { duration: 0.8, maxZoom: 16 });
        } else {
          mapRef.current?.flyTo([ward.lat, ward.lng], 15, { duration: 0.8 });
        }
      }
    }

    previousFocusedReportRef.current = targetFocusedReport;
    previousSelectedWardNameRef.current = selectedWardName;
  }, [
    mapLoaded,
    selectedWardName,
    areas,
    focusedReport,
    reportsRef,
    fitToInitialReportViewport,
  ]);

  // ── Data refresh: redraw report markers without moving the map ───────────
  useEffect(() => {
    if (!mapLoaded || !mapInitialRenderRef.current) return;
    if (focusedReport && isTargetReportWard(focusedReport.wardName)) return;

    drawReportPinsRef.current?.(selectedWardName);

    // Fit to data once after the first fetch, then preserve user pan/zoom.
    if (!initialViewportFitRef.current && !selectedWardName) {
      const validReports = reportsRef.current.filter((r) => isTargetReportWard(r.wardName) && r.lat !== 0 && r.lng !== 0);
      if (validReports.length > 0) {
        fitToInitialReportViewport(0.5);
        initialViewportFitRef.current = true;
      }
    }
  }, [reportsVersion, mapLoaded, selectedWardName, focusedReport, reportsRef, fitToInitialReportViewport]);

  // ── Fetch boundaries 1 lần ngay khi map sẵn sàng ────────────────────────────
  useEffect(() => {
    if (!mapLoaded) return;
    drawWardBoundaries();
  }, [mapLoaded, drawWardBoundaries]);

  // ── Highlight from ReportList click ────────────────────────────────────
  useEffect(() => {
    if (!highlightAreaName || !mapLoaded) return;
    const ward = areas.find((a) => a.name === highlightAreaName);
    if (!ward) return;
    if (ward.bounds && ward.bounds.length >= 3) {
      const bounds = L.latLngBounds(ward.bounds.map(([lat, lng]) => L.latLng(lat, lng)));
      mapRef.current?.flyToBounds(bounds.pad(0.35), { duration: 0.8, maxZoom: 16 });
    } else {
      mapRef.current?.flyTo([ward.lat, ward.lng], 15, { duration: 0.8 });
    }
  }, [highlightAreaName, areas, mapLoaded]);

  // ── Background refresh: only update pin icons, don't re-render map ────────
  useEffect(() => {
    if (!mapLoaded) return;
    if (!mapInitialRenderRef.current) return; // Wait for initial render

    // Update pin icons without clearing/redrawing the map
    // This uses the latest reports from reportsRef (external or internal)
    const updatePins = () => {
      if (!reportLayerRef.current) return;
      reportLayerRef.current.eachLayer((layer) => {
        if (layer instanceof L.Marker) {
          const latlng = layer.getLatLng();
          const report = reportsRef.current.find(
            (r) => r.lat === latlng.lat && r.lng === latlng.lng
          );
          if (report) {
            const icon = buildReportIcon(report.status);
            layer.setIcon(icon);
          }
        }
      });
    };
    updatePins();
  }, [mapLoaded]); // Only depend on mapLoaded, not reports

  // ── Legend counts ───────────────────────────────────────────────────────
  const visibleAlerts = selectedWardName
    ? envAlerts.filter((a) => a.wardName === selectedWardName)
    : envAlerts;
  const normalCount = visibleAlerts.filter((a) => a.level === "normal").length;
  const warningCount = visibleAlerts.filter((a) => a.level === "warning").length;
  const criticalCount = visibleAlerts.filter((a) => a.level === "critical").length;
  const reportCountSource = reportsRef.current.length > 0 ? reportsRef.current : reports;
  const visibleReportCounts = reportCountSource.filter(
    (r) => isTargetReportWard(r.wardName) && (!selectedWardName || isSameWardName(r.wardName, selectedWardName))
  );
  const pendingCount = visibleReportCounts.filter(r => r.status === "pending").length;
  const approvedCount = visibleReportCounts.filter(r => r.status === "approved").length;
  const doneCount = visibleReportCounts.filter(r => r.status === "done").length;

  return (
    <div className="waste-report-map relative w-full h-full rounded-2xl overflow-hidden border border-gray-100 shadow-sm">
      <div ref={mapContainerRef} className="w-full h-full z-0" />

      {/* Loading overlay */}
      {(loading || !mapLoaded) && (
        <div className="absolute inset-0 bg-gray-50 flex items-center justify-center z-50">
          <div className="text-center">
            <div className="w-8 h-8 border-2 border-indigo-500 border-t-transparent rounded-full animate-spin mx-auto mb-3" />
            <p className="text-sm text-gray-500">Đang tải bản đồ…</p>
          </div>
        </div>
      )}

      {/* Boundary loading badge — non-blocking, góc dưới trái */}
      {mapLoaded && !selectedWardName && boundaryLoading && (
        <div className="absolute bottom-4 left-4 z-[1000] bg-white/90 backdrop-blur-sm border border-blue-100 shadow-sm rounded-xl px-3 py-2 text-xs flex items-center gap-2">
          <div className="w-3.5 h-3.5 border-2 border-blue-400 border-t-transparent rounded-full animate-spin flex-shrink-0" />
          <div>
            <p className="font-semibold text-blue-700">Đang tải ranh giới phường…</p>
            <div className="mt-1 w-28 h-1 bg-gray-100 rounded-full overflow-hidden">
              <div
                className="h-full bg-blue-400 rounded-full transition-all duration-300"
                style={{ width: `${(boundaryLoaded / WARD_NAMES.length) * 100}%` }}
              />
            </div>
            <p className="text-gray-400 mt-0.5">{boundaryLoaded} / {WARD_NAMES.length}</p>
          </div>
        </div>
      )}

      {/* Level indicator + Back button (Level 2) */}
      {mapLoaded && selectedWardName && (
        <div className="absolute top-4 left-1/2 -translate-x-1/2 z-[1000] flex items-center gap-2">
          <button
            onClick={() => onClearSelection?.()}
            className="bg-white/95 border border-gray-200 shadow-md rounded-full px-3 py-1.5 text-xs font-semibold text-gray-600 hover:bg-gray-50 transition flex items-center gap-1.5"
            style={{ pointerEvents: "auto" }}
          >
            <svg width="12" height="12" viewBox="0 0 12 12" fill="none" stroke="currentColor" strokeWidth="2.2">
              <path d="M8 2L3 6l5 4" />
            </svg>
            Tổng quan
          </button>
          <span className="bg-white/95 backdrop-blur-sm border border-gray-200 shadow-md rounded-full px-4 py-1.5 text-xs font-semibold text-gray-700 flex items-center gap-2">
            <span className="w-2 h-2 rounded-full bg-indigo-500 animate-pulse" />
            {selectedWardName}
            <span className="text-gray-400">— {visibleAlerts.length} điểm</span>
          </span>
        </div>
      )}

      {/* Hint (Level 1) */}
      {mapLoaded && !selectedWardName && !focusedReport && (
        <div className="absolute top-4 left-1/2 -translate-x-1/2 z-[1000]">
          <span className="bg-white/90 backdrop-blur-sm border border-gray-100 shadow rounded-full px-4 py-1.5 text-xs text-gray-500 flex items-center gap-2">
            <span className="w-1.5 h-1.5 rounded-full bg-indigo-400 animate-pulse" />
            Click a ward to view the report statistics chart
          </span>
        </div>
      )}

      {/* Legend */}
      {mapLoaded && (
        <div className="absolute bottom-4 right-4 bg-white/92 backdrop-blur-sm rounded-xl border border-gray-100 shadow-sm p-3 text-xs space-y-2 z-[1000] min-w-[175px]">
          {selectedWardName && (
            <>
              <p className="font-semibold text-gray-600 text-xs uppercase tracking-wide">
                Cảnh báo môi trường
              </p>
              {[
                { color: ALERT_CFG.normal.color, label: `Bình thường (${normalCount})` },
                { color: ALERT_CFG.warning.color, label: `Cảnh báo (${warningCount})` },
                { color: ALERT_CFG.critical.color, label: `Nguy hiểm (${criticalCount})` },
              ].map((item) => (
                <div key={item.label} className="flex items-center gap-2">
                  <span className="w-3 h-3 rounded-full border-2 border-white shadow-sm flex-shrink-0" style={{ background: item.color }} />
                  <span className="text-gray-500 whitespace-nowrap">{item.label}</span>
                </div>
              ))}
            </>
          )}

          {/* Report pins */}
          <div className={`${selectedWardName ? "border-t border-gray-100 pt-2 mt-1" : ""} space-y-1.5`}>
            <p className="font-semibold text-gray-500 text-[10px] uppercase tracking-wide">
              Waste Reports
            </p>
            {[
              { color: "#ef4444", label: `Pending (${pendingCount})` },
              { color: "#3b82f6", label: `Approved (${approvedCount})` },
              { color: "#10b981", label: `Completed (${doneCount})` },
            ].map((item) => (
              <div key={item.label} className="flex items-center gap-2">
                <span className="w-3 h-3 rounded-full flex-shrink-0" style={{ background: item.color }} />
                <span className="text-gray-500 whitespace-nowrap">{item.label}</span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* React Modal (thay vì Leaflet Popup) cho thống kê Phường */}
      {popupWardData && (
        <div className="absolute inset-0 z-[2000] bg-white/95 backdrop-blur-md animate-in fade-in duration-200">
          <div className="w-full h-full p-6 flex flex-col">
            <WardChartPopup
              wardName={popupWardData.wardName}
              reports={popupWardData.reports}
              allReports={popupWardData.allReports}
              onClose={() => setPopupWardData(null)}
            />
          </div>
        </div>
      )}

      <style>{`
        .monitoring-leaflet-tooltip {
          background: white !important;
          border: 1px solid #e5e7eb !important;
          border-radius: 10px !important;
          box-shadow: 0 4px 16px rgba(0,0,0,0.1) !important;
          padding: 6px 10px !important;
          font-weight: 500 !important;
        }
        .monitoring-leaflet-tooltip::before { display: none !important; }
        .monitoring-leaflet-popup .leaflet-popup-content-wrapper {
          border-radius: 12px !important;
          box-shadow: 0 8px 24px rgba(0,0,0,0.12) !important;
          padding: 4px !important;
        }
        .monitoring-leaflet-popup .leaflet-popup-tip { opacity: 0.4; }
        .waste-report-map .leaflet-marker-icon[src*="/marker-icon"],
        .waste-report-map .leaflet-marker-shadow[src*="/marker-shadow"] {
          display: none !important;
        }
      `}</style>
    </div>
  );
}
