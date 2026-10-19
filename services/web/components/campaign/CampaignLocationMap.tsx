"use client";

import { useEffect, useRef, useState, useCallback } from "react";
import L from "leaflet";
import { MapPin, Navigation, Globe, Loader2 } from "lucide-react";
import { VIETNAM_ISLANDS_GEOJSON, HOANG_SA_ISLANDS, TRUONG_SA_ISLANDS } from "@/data/vietnam-islands";
import { reverseGeocode } from "@/lib/geocode";

// Fix Leaflet icon path
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
});

// Island styling
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

// Label cover style - ocean blue, solid opaque to cover labels from OSM
const LABEL_COVER_STYLE: L.PathOptions = {
  color: "#AAD3DF",
  fillColor: "#AAD3DF",
  fillOpacity: 1,
  opacity: 1,
  interactive: false,
};

// ---------------------------------------------------------------------------
// Create island group labels
// ---------------------------------------------------------------------------
function createIslandGroupLabels(): L.LayerGroup {
  const group = L.layerGroup();

  // Trường Sa label
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

  // Hoàng Sa label
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

// ---------------------------------------------------------------------------
// Create label cover overlays to hide labels from OSM tiles
// ---------------------------------------------------------------------------
function createVietnamLabelCovers(): L.LayerGroup {
  const group = L.layerGroup();

  // Hoàng Sa cover polygon
  const hoangSaCover = L.polygon(
    [
      [18.5, 112.5],   // North
      [17.3, 114.9],   // Northeast
      [16.0, 115.0],   // East
      [14.7, 114.9],   // Southeast
      [13.5, 112.5],   // South
      [14.7, 110.1],   // Southwest
      [16.0, 110.0],   // West
      [17.3, 110.1],   // Northwest
      [18.5, 112.5],
    ],
    LABEL_COVER_STYLE
  );
  group.addLayer(hoangSaCover);

  // Trường Sa cover polygon
  const truongSaCover = L.polygon(
    [
      [13.85, 113.5],  // North
      [12.82, 115.97], // Northeast
      [10.35, 117],    // East
      [7.88, 115.97],  // Southeast
      [6.85, 113.5],  // South
      [7.88, 111.03],  // Southwest
      [10.35, 110],    // West
      [12.82, 111.03], // Northwest
      [13.85, 113.5],
    ],
    LABEL_COVER_STYLE
  );
  group.addLayer(truongSaCover);

  return group;
}

interface CampaignLocationMapProps {
  lat: number;
  lng: number;
  radius: number;
  campaignName: string;
}

export function CampaignLocationMap({ lat, lng, radius, campaignName }: CampaignLocationMapProps) {
  const mapContainerRef = useRef<HTMLDivElement>(null);
  const mapRef = useRef<L.Map | null>(null);
  const islandsLayerRef = useRef<L.GeoJSON | null>(null);
  const campaignLayerRef = useRef<L.LayerGroup | null>(null);
  const [mapLoaded, setMapLoaded] = useState(false);
  const [zoom, setZoom] = useState(15);
  const [islandCounts, setIslandCounts] = useState({ hoangSa: 0, truongSa: 0 });
  const [address, setAddress] = useState<string | null>(null);
  const [loadingAddress, setLoadingAddress] = useState(false);
  const markerRef = useRef<L.Marker | null>(null);

  // Reverse geocode to get address
  useEffect(() => {
    if (!lat || !lng) return;

    setLoadingAddress(true);
    reverseGeocode(lat, lng).then((addr) => {
      setAddress(addr);
      setLoadingAddress(false);
    });
  }, [lat, lng]);

  // Build campaign marker icon
  const buildCampaignIcon = useCallback(() => {
    return L.divIcon({
      className: "",
      iconSize: [36, 44],
      iconAnchor: [18, 44],
      popupAnchor: [0, -48],
      html: `
        <div style="
          position: relative;
          width: 36px;
          height: 44px;
          display: flex;
          flex-direction: column;
          align-items: center;
        ">
          <div style="
            width: 36px;
            height: 36px;
            background: linear-gradient(135deg, #10b981, #059669);
            border: 3px solid white;
            border-radius: 50% 50% 50% 0;
            transform: rotate(-45deg);
            box-shadow: 0 4px 12px rgba(16, 185, 129, 0.4);
            display: flex;
            align-items: center;
            justify-content: center;
          ">
            <div style="
              transform: rotate(45deg);
              width: 18px;
              height: 18px;
              background: white;
              border-radius: 50%;
              display: flex;
              align-items: center;
              justify-content: center;
            ">
              <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="#059669" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="10" r="3"/>
                <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
              </svg>
            </div>
          </div>
          <div style="
            width: 8px;
            height: 8px;
            background: #059669;
            border-radius: 50%;
            margin-top: -4px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.2);
          "></div>
        </div>
      `,
    });
  }, []);

  // Initialize map
  useEffect(() => {
    if (!mapContainerRef.current || mapRef.current) return;

    const map = L.map(mapContainerRef.current, {
      center: [lat, lng],
      zoom: 13,
      zoomControl: false,
      attributionControl: false,
    });

    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
      attribution: "&copy; OpenStreetMap contributors",
    }).addTo(map);

    L.control.zoom({ position: "topleft" }).addTo(map);
    L.control.attribution({ position: "bottomleft", prefix: false }).addTo(map);

    // Add label cover overlays (bottom layer)
    const labelCoverLayer = createVietnamLabelCovers();
    labelCoverLayer.addTo(map);

    // Add Vietnam islands layer
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
          (layer.options as any)._islandName = name;
          (layer.options as any)._islandNameEn = nameEn;
        }

        // Show tooltip on hover
        layer.bindTooltip(`${name} (${nameEn})`, {
          className: "campaign-map-tooltip",
          direction: "top",
          offset: [0, -5],
        });
      },
    }).addTo(map);

    // Update island labels based on zoom
    const updateIslandLabels = () => {
      if (!islandsLayerRef.current) return;
      const currentZoom = map.getZoom();

      islandsLayerRef.current.eachLayer((layer) => {
        if (!(layer instanceof L.CircleMarker)) return;

        const name = (layer.options as any)._islandName;
        const nameEn = (layer.options as any)._islandNameEn;
        if (!name) return;

        if (currentZoom >= 10) {
          layer.bindTooltip(`${name} (${nameEn})`, {
            permanent: true,
            direction: "top",
            className: "campaign-map-tooltip",
            offset: [0, -10],
          });
        } else {
          layer.bindTooltip("", { permanent: false });
        }
      });
    };

    map.on("zoomend", updateIslandLabels);
    updateIslandLabels();

    // Add island group labels (on top of islands)
    const islandGroupLabelLayer = createIslandGroupLabels();
    islandGroupLabelLayer.addTo(map);

    // Create campaign layer group
    campaignLayerRef.current = L.layerGroup().addTo(map);

    mapRef.current = map;
    setMapLoaded(true);

    map.on("zoomend", () => {
      setZoom(map.getZoom());
    });

    // Count islands
    setIslandCounts({
      hoangSa: HOANG_SA_ISLANDS.length,
      truongSa: TRUONG_SA_ISLANDS.length,
    });

    return () => {
      map.remove();
      mapRef.current = null;
    };
  }, [lat, lng]);

  // Draw campaign marker and radius circle
  useEffect(() => {
    if (!mapLoaded || !mapRef.current || !campaignLayerRef.current) return;

    const map = mapRef.current;

    // Clear campaign layers
    campaignLayerRef.current.clearLayers();

    // Add campaign marker
    const marker = L.marker([lat, lng], {
      icon: buildCampaignIcon(),
      zIndexOffset: 1000,
    }).addTo(campaignLayerRef.current);
    markerRef.current = marker;

    // Build popup content with address
    const buildPopupContent = () => {
      const addressDisplay = loadingAddress
        ? '<span style="color: #94a3b8;">Đang tải...</span>'
        : (address || '<span style="color: #94a3b8;">Không xác định</span>');

      return `
      <div style="font-family: system-ui, sans-serif; min-width: 240px; padding: 0; border-radius: 12px; overflow: hidden; box-shadow: 0 10px 25px rgba(0,0,0,0.15);">
        <div style="padding: 10px 12px; background: linear-gradient(135deg, #10b981, #059669); display: flex; align-items: center; gap: 8px;">
          <div style="width: 28px; height: 28px; background: rgba(255,255,255,0.2); border-radius: 8px; display: flex; align-items: center; justify-content: center;">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
              <circle cx="12" cy="10" r="3"/>
            </svg>
          </div>
          <span style="font-size: 13px; font-weight: 700; color: white;">${campaignName}</span>
        </div>
        <div style="padding: 12px;">
          <div style="display: flex; align-items: flex-start; gap: 10px; margin-bottom: 0;">
            <div style="width: 32px; height: 32px; background: #ecfdf5; border-radius: 8px; display: flex; align-items: center; justify-content: center; flex-shrink: 0;">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#10b981" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                <circle cx="12" cy="10" r="3"/>
              </svg>
            </div>
            <div style="flex: 1; min-width: 0;">
              <p style="margin: 0 0 4px 0; font-size: 10px; color: #64748b; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px;">Địa chỉ</p>
              <p style="margin: 0; font-size: 12px; font-weight: 500; color: #1f2937; line-height: 1.4; word-break: break-word;">${addressDisplay}</p>
            </div>
          </div>
        </div>
      </div>
    `;
    };

    marker.bindPopup(buildPopupContent(), {
      maxWidth: 300,
      className: "campaign-location-popup",
    });

    // Open popup
    marker.openPopup();

    // Fit bounds to show marker with padding
    setTimeout(() => {
      const bounds = L.latLngBounds([[lat, lng] as L.LatLngExpression]);
      map.fitBounds(bounds, { duration: 0.5 });
    }, 100);

  }, [mapLoaded, lat, lng, campaignName, buildCampaignIcon, address, loadingAddress]);

  // Handle zoom controls
  const handleZoomIn = () => {
    mapRef.current?.zoomIn();
  };

  const handleZoomOut = () => {
    mapRef.current?.zoomOut();
  };

  return (
    <div className="relative w-full h-full bg-slate-50 rounded-xl overflow-hidden">
      <div ref={mapContainerRef} className="w-full h-full" />

      {/* Map Controls */}
      <div className="absolute top-3 right-3 z-[1000] flex flex-col gap-1.5">
        <button
          onClick={handleZoomIn}
          className="w-9 h-9 bg-white rounded-lg shadow-md flex items-center justify-center hover:bg-slate-50 transition-colors border border-slate-200"
          title="Phóng to"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#374151" strokeWidth="2.5" strokeLinecap="round">
            <line x1="12" y1="5" x2="12" y2="19" />
            <line x1="5" y1="12" x2="19" y2="12" />
          </svg>
        </button>
        <button
          onClick={handleZoomOut}
          className="w-9 h-9 bg-white rounded-lg shadow-md flex items-center justify-center hover:bg-slate-50 transition-colors border border-slate-200"
          title="Thu nhỏ"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#374151" strokeWidth="2.5" strokeLinecap="round">
            <line x1="5" y1="12" x2="19" y2="12" />
          </svg>
        </button>
      </div>

      {/* Address Display */}
      <div className="absolute bottom-3 left-3 z-[1000] bg-white/95 backdrop-blur-sm rounded-lg border border-slate-200 shadow-sm px-3 py-2 flex items-center gap-2 max-w-[250px]">
        {loadingAddress ? (
          <>
            <Loader2 className="w-3.5 h-3.5 text-slate-400 animate-spin shrink-0" />
            <span className="text-[11px] text-slate-400 truncate">Đang tải địa chỉ...</span>
          </>
        ) : address ? (
          <>
            <Navigation className="w-3.5 h-3.5 text-emerald-500 shrink-0" />
            <span className="text-[11px] text-slate-600 truncate">{address}</span>
          </>
        ) : (
          <>
            <MapPin className="w-3.5 h-3.5 text-slate-400 shrink-0" />
            <span className="text-[11px] text-slate-400 truncate">Không xác định được địa chỉ</span>
          </>
        )}
      </div>

      {/* Loading overlay */}
      {!mapLoaded && (
        <div className="absolute inset-0 bg-slate-50 flex items-center justify-center">
          <div className="text-center">
            <div className="w-7 h-7 border-2 border-emerald-500 border-t-transparent rounded-full animate-spin mx-auto mb-2" />
            <p className="text-xs text-slate-500">Đang tải bản đồ...</p>
          </div>
        </div>
      )}

      <style>{`
        .campaign-location-popup .leaflet-popup-content-wrapper {
          border-radius: 12px !important;
          padding: 0 !important;
          overflow: hidden;
        }
        .campaign-location-popup .leaflet-popup-content {
          margin: 0 !important;
          width: auto !important;
        }
        .campaign-location-popup .leaflet-popup-tip-container {
          display: none;
        }
        .campaign-map-tooltip {
          background: white !important;
          border: 1px solid #e5e7eb !important;
          border-radius: 8px !important;
          box-shadow: 0 2px 8px rgba(0,0,0,0.1) !important;
          padding: 4px 8px !important;
          font-weight: 500 !important;
          font-size: 11px !important;
        }
        .campaign-map-tooltip::before { display: none !important; }
      `}</style>
    </div>
  );
}