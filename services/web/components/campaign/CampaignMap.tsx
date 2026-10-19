"use client";

import { useEffect, useRef, useState, useCallback } from "react";
import L from "leaflet";
import type { CampaignRegion } from "@/types/waste-report";

// Fix Leaflet's default icon path resolving issue in Next.js
if (typeof window !== "undefined") {
  delete (L.Icon.Default.prototype as any)._getIconUrl;
  L.Icon.Default.mergeOptions({
    iconRetinaUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png",
    iconUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png",
    shadowUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png",
  });
}

// Build report pin icon
function buildReportIcon(isSelectedRegion: boolean): L.DivIcon {
  const bg = isSelectedRegion ? "#ef4444" : "#94a3b8"; // Red if selected, slate if not
  const border = isSelectedRegion ? "#b91c1c" : "#64748b";
  const pulseStyle = isSelectedRegion ? "animation:alert-pin-pulse 1.4s ease-in-out infinite;" : "";
  
  return L.divIcon({
    className: "",
    iconSize: [20, 26],
    iconAnchor: [10, 26],
    popupAnchor: [0, -30],
    html: `
      <div style="position:relative;width:20px;height:26px;${pulseStyle}">
        <div style="position:absolute;bottom:0;left:50%;transform:translateX(-50%);width:6px;height:3px;background:rgba(0,0,0,0.15);border-radius:50%;"></div>
        <div style="position:absolute;top:0;left:0;width:20px;height:20px;background:${bg};border:2px solid ${border};border-radius:50% 50% 50% 0;transform:rotate(-45deg);box-shadow:0 2px 6px rgba(0,0,0,0.22);"></div>
      </div>
    `,
  });
}

interface CampaignMapProps {
  regions: CampaignRegion[];
  selectedRegionId: string | null;
  onRegionSelect: (region: CampaignRegion | null) => void;
  radiusInMeters: number;
}

export function CampaignMap({ regions, selectedRegionId, onRegionSelect, radiusInMeters }: CampaignMapProps) {
  const mapContainerRef = useRef<HTMLDivElement>(null);
  const mapRef = useRef<L.Map | null>(null);
  const reportLayerRef = useRef<L.LayerGroup | null>(null);
  const circleLayerRef = useRef<L.LayerGroup | null>(null);
  const [mapLoaded, setMapLoaded] = useState(false);

  // Inject keyframe once
  useEffect(() => {
    const id = "campaign-pin-style";
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

  // Init map once
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

    reportLayerRef.current = L.layerGroup().addTo(map);
    circleLayerRef.current = L.layerGroup().addTo(map);

    mapRef.current = map;
    setMapLoaded(true);

    return () => {
      map.remove();
      mapRef.current = null;
    };
  }, []);

  const drawReportsAndCircles = useCallback(() => {
    if (!reportLayerRef.current || !circleLayerRef.current) return;
    
    reportLayerRef.current.clearLayers();
    circleLayerRef.current.clearLayers();

    regions.forEach((region) => {
      const isSelected = region.id === selectedRegionId;
      
      // Draw reports in this region
      region.reports.forEach((report) => {
        if (report.lat === 0 && report.lng === 0) return;

        const icon = buildReportIcon(isSelected);
        const marker = L.marker([report.lat, report.lng], {
          icon,
          zIndexOffset: isSelected ? 1000 : 500,
        }).addTo(reportLayerRef.current!);

        marker.on("click", () => {
          onRegionSelect(region);
        });
      });

      // Draw circle if selected
      if (isSelected) {
        L.circle([region.center.lat, region.center.lng], {
          radius: radiusInMeters,
          color: "#ef4444",
          fillColor: "#ef4444",
          fillOpacity: 0.15,
          weight: 2,
          dashArray: "5, 5"
        }).addTo(circleLayerRef.current!);

        // Fly to region
        mapRef.current?.flyTo([region.center.lat, region.center.lng], 15, { duration: 0.8 });
      }
    });

    // If nothing selected, just reset bounds or leave as is
    if (!selectedRegionId && regions.length > 0) {
      const allPoints = regions.flatMap(r => r.reports).filter(r => r.lat !== 0 && r.lng !== 0);
      if (allPoints.length > 0) {
        const bounds = L.latLngBounds(allPoints.map(p => L.latLng(p.lat, p.lng)));
        mapRef.current?.flyToBounds(bounds.pad(0.1), { duration: 0.8, maxZoom: 14 });
      }
    }

  }, [regions, selectedRegionId, onRegionSelect, radiusInMeters]);

  useEffect(() => {
    if (mapLoaded) {
      drawReportsAndCircles();
    }
  }, [mapLoaded, drawReportsAndCircles]);

  return (
    <div className="relative w-full h-full rounded-2xl overflow-hidden border border-slate-200 shadow-xl ring-1 ring-slate-900/5">
      <div ref={mapContainerRef} className="w-full h-full z-0" />
      
      {/* Map Tooltip */}
      <div className="absolute top-4 left-1/2 -translate-x-1/2 z-[1000] pointer-events-none">
        <span className="bg-white/90 backdrop-blur-md border border-slate-200 shadow-lg rounded-full px-5 py-2 text-xs text-slate-700 font-medium flex items-center gap-2">
          <span className="relative flex h-2.5 w-2.5 mr-1">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-primary/60 opacity-75"></span>
            <span className="relative inline-flex rounded-full h-2.5 w-2.5 bg-primary"></span>
          </span>
          Click on any point to view its region 
        </span>
      </div>

    </div>
  );
}
