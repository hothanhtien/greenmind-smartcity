"use client";

/**
 * MapWard.tsx
 * -----------
 * Hiển thị bản đồ Leaflet với polygon boundary của từng phường/xã
 * tại Đà Nẵng bằng cách gọi Nominatim OSM Search API.
 *
 * Flow:
 *   1. Component mount → gọi fetchAllBoundaries()
 *   2. Promise.all song song cho tất cả phường
 *   3. Mỗi kết quả → L.geoJSON() → add vào map
 *   4. Hover: highlight   |   Click: zoom + popup tên phường
 *   5. Cache in-memory tránh gọi lại khi re-render
 */

import { useEffect, useRef, useState } from "react";
import L from "leaflet";

// Fix Leaflet's default icon path resolving issue in Next.js
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
});

// ─── Danh sách phường cần render ────────────────────────────────────────────
const WARDS = [
  "An Hải", "An Khê", "An Thắng", "Avương", "Bà Nà", "Bàn Thạch",
  "Bến Giằng", "Bến Hiên", "Cẩm Lệ", "Chiên Đàn", "Duy Nghĩa", "Duy Xuyên",
  "Đại Lộc", "Đắc Pring", "Điện Bàn", "Điện Bàn Bắc", "Điện Bàn Đông",
  "Điện Bàn Tây", "Đồng Dương", "Đông Giang", "Đức Phú", "Gò Nổi", "Hà Nha",
  "Hải Châu", "Hải Vân", "Hiệp Đức", "Hòa Cường", "Hòa Khánh", "Hòa Tiến",
  "Hòa Vang", "Hòa Xuân", "Hoàng Sa", "Hội An", "Hội An Đông", "Hội An Tây",
  "Hùng Sơn", "Hương Trà", "Khâm Đức", "La Dêê", "La Êê", "Lãnh Ngọc",
  "Liên Chiểu", "Nam Giang", "Nam Phước", "Nam Trà My", "Ngũ Hành Sơn",
  "Nông Sơn", "Núi Thành", "Phú Ninh", "Phú Thuận", "Phước Chánh",
  "Phước Hiệp", "Phước Năng", "Phước Thành", "Phước Trà", "Quảng Phú",
  "Quế Phước", "Quế Sơn", "Quế Sơn Trung", "Sông Kôn", "Sông Vàng",
  "Sơn Cẩm Hà", "Sơn Trà", "Tam Anh", "Tam Hải", "Tam Kỳ", "Tam Mỹ",
  "Tam Xuân", "Tân Hiệp", "Tây Giang", "Tây Hồ", "Thạnh Bình", "Thanh Khê",
  "Thạnh Mỹ", "Thăng An", "Thăng Bình", "Thăng Điền", "Thăng Phú",
  "Thăng Trường", "Thu Bồn", "Thượng Đức", "Tiên Phước", "Trà Đốc",
  "Trà Giáp", "Trà Leng", "Trà Liên", "Trà Linh", "Trà My", "Trà Tân",
  "Trà Tập", "Trà Vân", "Việt An", "Vu Gia", "Xuân Phú",
];

// ─── Style mặc định / hover ──────────────────────────────────────────────────
const DEFAULT_STYLE: L.PathOptions = {
  color: "#3b82f6",          // xanh viền
  weight: 1.8,
  fillColor: "#3b82f6",
  fillOpacity: 0.12,
  opacity: 0.8,
};

const HOVER_STYLE: L.PathOptions = {
  color: "#1d4ed8",
  weight: 2.5,
  fillColor: "#1d4ed8",
  fillOpacity: 0.3,
  opacity: 1,
};

// ─── Cache module-level (tồn tại cả session) ────────────────────────────────
const geoCache = new Map<string, GeoJSON.GeoJsonObject | null>();

// ─── Fetch boundary cho 1 phường ────────────────────────────────────────────
async function fetchWardBoundary(ward: string): Promise<{ ward: string; geo: GeoJSON.GeoJsonObject | null }> {
  // Trả từ cache nếu đã có
  if (geoCache.has(ward)) {
    return { ward, geo: geoCache.get(ward)! };
  }

  try {
    const q = encodeURIComponent(`${ward}, Đà Nẵng, Việt Nam`);
    const url = `https://nominatim.openstreetmap.org/search?q=${q}&format=json&polygon_geojson=1&limit=1`;

    const res = await fetch(url, {
      headers: { "Accept-Language": "vi" },
    });

    if (!res.ok) throw new Error(`HTTP ${res.status}`);

    const data = await res.json();
    // Lấy phần tử đầu tiên nếu có kết quả và có geojson
    const geo: GeoJSON.GeoJsonObject | null =
      data.length > 0 && data[0].geojson ? data[0].geojson : null;

    // Cache kết quả (kể cả null để tránh retry)
    geoCache.set(ward, geo);
    return { ward, geo };
  } catch {
    geoCache.set(ward, null);
    return { ward, geo: null };
  }
}

// ─── Props ───────────────────────────────────────────────────────────────────
interface MapWardProps {
  /** Chiều cao container, mặc định "100%" */
  height?: string;
  /** Callback khi click vào 1 phường */
  onWardClick?: (wardName: string) => void;
}

// ─── Component chính ─────────────────────────────────────────────────────────
export function MapWard({ height = "100%", onWardClick }: MapWardProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const mapRef       = useRef<L.Map | null>(null);
  const layerGroupRef = useRef<L.LayerGroup | null>(null);

  const [loading, setLoading]       = useState(true);
  const [loadedCount, setLoadedCount] = useState(0);   // số phường đã load xong
  const [errorCount, setErrorCount]  = useState(0);    // số phường không tìm thấy

  /* ── Khởi tạo map một lần ─────────────────────────────────────────────── */
  useEffect(() => {
    if (!containerRef.current || mapRef.current) return;

    const map = L.map(containerRef.current, {
      center: [15.9, 108.2],   // trung tâm Đà Nẵng / Quảng Nam
      zoom: 10,
      zoomControl: true,
      attributionControl: true,
    });

    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
      attribution: "© OpenStreetMap contributors",
      maxZoom: 19,
    }).addTo(map);

    layerGroupRef.current = L.layerGroup().addTo(map);
    mapRef.current = map;

    return () => {
      map.remove();
      mapRef.current = null;
      layerGroupRef.current = null;
    };
  }, []);

  /* ── Fetch boundaries và vẽ polygon ──────────────────────────────────── */
  useEffect(() => {
    if (!mapRef.current || !layerGroupRef.current) return;

    setLoading(true);
    setLoadedCount(0);
    setErrorCount(0);

    // Gọi tất cả cùng lúc (rate-limit Nominatim: ~1 req/s nên dùng throttle nhẹ)
    // Trên thực tế Nominatim public chấp nhận burst nhỏ từ browser
    const fetchAll = async () => {
      // Chia thành batch 10 để thân thiện với Nominatim
      const BATCH = 10;
      let loaded = 0;
      let errors = 0;

      for (let i = 0; i < WARDS.length; i += BATCH) {
        const batch = WARDS.slice(i, i + BATCH);
        const results = await Promise.all(batch.map(fetchWardBoundary));

        results.forEach(({ ward, geo }) => {
          if (!geo || !layerGroupRef.current) {
            errors++;
            return;
          }
          loaded++;

          // Vẽ GeoJSON polygon lên bản đồ
          const layer = L.geoJSON(geo, {
            style: () => ({ ...DEFAULT_STYLE }),

            // Hover handlers cho từng feature
            onEachFeature: (_feature, featureLayer) => {
              if (!(featureLayer instanceof L.Path)) return;

              featureLayer.on({
                mouseover: () => featureLayer.setStyle(HOVER_STYLE),
                mouseout:  () => featureLayer.setStyle(DEFAULT_STYLE),
                click: (e: L.LeafletMouseEvent) => {
                  // Zoom vào polygon
                  const bounds = (featureLayer as L.Polygon).getBounds?.();
                  if (bounds) {
                    mapRef.current?.flyToBounds(bounds.pad(0.15), {
                      duration: 0.6,
                      maxZoom: 15,
                    });
                  }

                  // Popup tên phường
                  L.popup({ closeButton: false, className: "ward-popup" })
                    .setLatLng(e.latlng)
                    .setContent(
                      `<div style="font-family:system-ui;padding:2px 4px;">
                        <span style="font-size:13px;font-weight:700;color:#1e3a5f;">${ward}</span>
                        <br/>
                        <span style="font-size:10px;color:#6b7280;">Đà Nẵng · Việt Nam</span>
                      </div>`
                    )
                    .openOn(mapRef.current!);

                  onWardClick?.(ward);
                },
              });

              // Tooltip luôn hiển thị tên khi hover (thay vì popup)
              featureLayer.bindTooltip(ward, {
                sticky: true,
                className: "ward-tooltip",
                direction: "top",
                offset: [0, -4],
              });
            },
          });

          layerGroupRef.current!.addLayer(layer);
        });

        // Cập nhật progress UI sau mỗi batch
        setLoadedCount(loaded);
        setErrorCount(errors);

        // Delay nhỏ giữa các batch để tránh rate-limit Nominatim
        if (i + BATCH < WARDS.length) {
          await new Promise((r) => setTimeout(r, 300));
        }
      }

      setLoading(false);
    };

    fetchAll();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []); // chỉ chạy 1 lần khi mount

  /* ── Render ───────────────────────────────────────────────────────────── */
  return (
    <div className="relative w-full rounded-2xl overflow-hidden border border-gray-100 shadow-sm" style={{ height }}>
      {/* Map container */}
      <div ref={containerRef} className="w-full h-full z-0" />

      {/* Loading overlay với progress */}
      {loading && (
        <div className="absolute inset-0 bg-white/70 backdrop-blur-sm flex flex-col items-center justify-center z-[1000] gap-3">
          <div className="w-9 h-9 border-[3px] border-blue-500 border-t-transparent rounded-full animate-spin" />
          <div className="text-center">
            <p className="text-sm font-semibold text-gray-700">Đang tải ranh giới phường…</p>
            <p className="text-xs text-gray-400 mt-1">
              {loadedCount} / {WARDS.length} phường
              {errorCount > 0 && (
                <span className="text-amber-500 ml-2">({errorCount} không tìm thấy)</span>
              )}
            </p>
            {/* Progress bar */}
            <div className="w-48 h-1.5 bg-gray-200 rounded-full mt-2 mx-auto overflow-hidden">
              <div
                className="h-full bg-blue-500 rounded-full transition-all duration-300"
                style={{ width: `${((loadedCount + errorCount) / WARDS.length) * 100}%` }}
              />
            </div>
          </div>
        </div>
      )}

      {/* Badge thông tin sau khi load xong */}
      {!loading && (
        <div className="absolute bottom-4 left-4 z-[1000] bg-white/90 backdrop-blur-sm border border-gray-100 shadow-sm rounded-xl px-3 py-2 text-xs space-y-0.5">
          <p className="font-semibold text-gray-700 flex items-center gap-1.5">
            <span className="w-2.5 h-2.5 rounded-sm bg-blue-400 border border-blue-500 inline-block" />
            Ranh giới phường
          </p>
          <p className="text-gray-400">
            <span className="text-blue-600 font-medium">{loadedCount}</span> phường đã hiển thị
            {errorCount > 0 && (
              <span className="text-amber-500 ml-1">· {errorCount} không có dữ liệu</span>
            )}
          </p>
          <p className="text-gray-400 italic">Click vào phường để zoom</p>
        </div>
      )}

      {/* Legend */}
      <div className="absolute top-4 right-4 z-[1000] bg-white/90 backdrop-blur-sm border border-gray-100 shadow-sm rounded-xl px-3 py-2 text-xs">
        <p className="font-semibold text-gray-600 text-[10px] uppercase tracking-wide mb-1.5">Bản đồ phường</p>
        <div className="flex items-center gap-2">
          <span className="w-3 h-3 rounded-sm border-2 border-blue-500 bg-blue-200 inline-block flex-shrink-0" />
          <span className="text-gray-500">Ranh giới phường/xã</span>
        </div>
        <div className="flex items-center gap-2 mt-1">
          <span className="w-3 h-3 rounded-sm border-2 border-blue-700 bg-blue-400 inline-block flex-shrink-0" />
          <span className="text-gray-500">Đang chọn (hover)</span>
        </div>
      </div>

      {/* CSS */}
      <style>{`
        .ward-tooltip {
          background: white !important;
          border: 1px solid #e5e7eb !important;
          border-radius: 8px !important;
          box-shadow: 0 2px 8px rgba(0,0,0,0.1) !important;
          padding: 4px 10px !important;
          font-size: 12px !important;
          font-weight: 600 !important;
          color: #1e3a5f !important;
        }
        .ward-tooltip::before { display: none !important; }
        .ward-popup .leaflet-popup-content-wrapper {
          border-radius: 12px !important;
          box-shadow: 0 8px 24px rgba(0,0,0,0.12) !important;
          padding: 6px !important;
        }
        .ward-popup .leaflet-popup-tip { opacity: 0.4; }
      `}</style>
    </div>
  );
}
