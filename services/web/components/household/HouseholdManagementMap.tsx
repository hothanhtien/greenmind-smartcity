"use client";

import { useEffect, useRef, useCallback } from "react";
import type { HouseholdProfile } from "@/types/monitoring";

interface HouseholdManagementMapProps {
    households: Array<HouseholdProfile & { greenScore?: number; lat: number | string; lng: number | string; id: string | number }>;
    selectedHouseholdId: string | number | null;
    onHouseholdSelect: (household: HouseholdProfile & { greenScore?: number }) => void;
    loading: boolean;
}

const DEFAULT_MAP_CENTER: [number, number] = [16.065, 108.225];

function parseNumber(value: number | string): number {
    return typeof value === "string" ? Number(value) : value;
}

function getGreenScoreColor(score?: number | string | null): string {
    if (score != null && !Number.isNaN(Number(score))) {
        const numericScore = Number(score);
        if (numericScore < 40) return "#ef4444";
        if (numericScore < 70) return "#f59e0b";
        return "#10b981";
    }
    return "#6b7280";
}

function getScoreLabel(score?: number | string | null): string {
    if (score != null && !Number.isNaN(Number(score))) {
        return String(Math.round(Number(score)));
    }
    return "N/A";
}

function isValidLatLng(lat: number, lng: number): boolean {
    return (
        Number.isFinite(lat) &&
        Number.isFinite(lng) &&
        Math.abs(lat) <= 90 &&
        Math.abs(lng) <= 180 &&
        !(lat === 0 && lng === 0)
    );
}

function jitterAroundCenter(seed: number): { lat: number; lng: number } {
    const baseLat = DEFAULT_MAP_CENTER[0];
    const baseLng = DEFAULT_MAP_CENTER[1];

    const a = (seed % 360) * (Math.PI / 180);
    const r = 0.002 + ((seed % 97) / 97) * 0.01;

    return {
        lat: baseLat + Math.sin(a) * r,
        lng: baseLng + Math.cos(a) * r,
    };
}

function spreadOffset(index: number): { dLat: number; dLng: number } {
    if (index <= 0) return { dLat: 0, dLng: 0 };

    const goldenAngle = 137.5 * (Math.PI / 180);
    const angle = index * goldenAngle;
    const radius = 0.0005 * Math.sqrt(index);

    return {
        dLat: Math.sin(angle) * radius,
        dLng: Math.cos(angle) * radius,
    };
}

export function HouseholdManagementMap({ households, selectedHouseholdId, onHouseholdSelect, loading }: HouseholdManagementMapProps) {
    const mapContainerRef = useRef<HTMLDivElement>(null);
    const mapRef = useRef<any>(null);
    const onHouseholdSelectRef = useRef(onHouseholdSelect);
    const householdsRef = useRef(households);
    const selectedHouseholdIdRef = useRef(selectedHouseholdId);
    const hasInitializedRef = useRef(false);

    useEffect(() => {
        onHouseholdSelectRef.current = onHouseholdSelect;
    }, [onHouseholdSelect]);

    useEffect(() => {
        householdsRef.current = households;
    }, [households]);

    useEffect(() => {
        selectedHouseholdIdRef.current = selectedHouseholdId;
    }, [selectedHouseholdId]);

    const updateMarkers = useCallback((map: any) => {
        if (!map || !map.getSource("households")) return;

        const indexByKey = new Map<string, number>();
        const features: any[] = [];
        const currentHouseholds = householdsRef.current;
        const currentSelectedId = selectedHouseholdIdRef.current;

        currentHouseholds.forEach((household) => {
            const lat = parseNumber(household.lat);
            const lng = parseNumber(household.lng);
            const isReal = isValidLatLng(lat, lng);
            const basePoint = isReal ? { lat, lng } : jitterAroundCenter(household.id);
            const key = `${basePoint.lat.toFixed(5)}:${basePoint.lng.toFixed(5)}`;
            const idx = indexByKey.get(key) ?? 0;
            indexByKey.set(key, idx + 1);
            const offset = spreadOffset(idx);
            const point = { lat: basePoint.lat + offset.dLat, lng: basePoint.lng + offset.dLng };

            const isSelected = currentSelectedId != null && String(household.id) === String(currentSelectedId);
            const color = getGreenScoreColor(household.greenScore);
            const label = getScoreLabel(household.greenScore);

            features.push({
                type: "Feature",
                geometry: {
                    type: "Point",
                    coordinates: [point.lng, point.lat],
                },
                properties: {
                    id: String(household.id),
                    name: household.name,
                    address: household.address,
                    color,
                    label: label !== "N/A" ? label : "?",
                    selected: isSelected,
                    isReal,
                    greenScore: household.greenScore ?? "Không có điểm",
                    reportCount: household.reportCount,
                    imageCount: household.imageHistory?.length ?? 0,
                },
            });

            if (isSelected) {
                map.flyTo({ center: [point.lng, point.lat], zoom: 15, duration: 800 });
            }
        });

        map.getSource("households").setData({
            type: "FeatureCollection",
            features,
        });
    }, []);

    useEffect(() => {
        if (typeof window === "undefined") return;
        if (!mapContainerRef.current) return;
        if (mapRef.current) return;
        if (hasInitializedRef.current) return;

        const initMap = () => {
            if (!(window as any).trackasiagl) {
                setTimeout(initMap, 50);
                return;
            }

            hasInitializedRef.current = true;

            const map = new (window as any).trackasiagl.Map({
                container: mapContainerRef.current!,
                style: "https://maps.track-asia.com/styles/v2/simple.json?key=public_key",
                center: { lat: 16.0544, lng: 108.2022 },
                zoom: 11,
            });

            mapRef.current = map;

            map.on("load", () => {
                map.addSource("households", {
                    type: "geojson",
                    data: { type: "FeatureCollection", features: [] },
                });

                map.addLayer({
                    id: "households-circle-glow",
                    type: "circle",
                    source: "households",
                    paint: {
                        "circle-radius": ["case", ["boolean", ["get", "selected"], false], 22, 18],
                        "circle-color": ["get", "color"],
                        "circle-opacity": 0.25,
                    },
                });

                map.addLayer({
                    id: "households-circle",
                    type: "circle",
                    source: "households",
                    paint: {
                        "circle-radius": ["case", ["boolean", ["get", "selected"], false], 16, 12],
                        "circle-color": ["get", "color"],
                        "circle-stroke-width": ["case", ["boolean", ["get", "selected"], false], 4, 3],
                        "circle-stroke-color": "#ffffff",
                        "circle-opacity": 1,
                    },
                });

                map.addLayer({
                    id: "households-label",
                    type: "symbol",
                    source: "households",
                    layout: {
                        "text-field": ["get", "label"],
                        "text-size": 12,
                        "text-font": ["Noto Sans Regular"],
                    },
                    paint: {
                        "text-color": "#ffffff",
                        "text-halo-color": "rgba(0,0,0,0.5)",
                        "text-halo-width": 2,
                    },
                });

                map.on("click", "households-circle", (e: any) => {
                    if (e.features && e.features.length > 0) {
                        const props = e.features[0].properties;
                        const currentHouseholds = householdsRef.current;
                        const household = currentHouseholds.find(h => String(h.id) === props.id);
                        if (household) {
                            onHouseholdSelectRef.current(household);
                        }
                    }
                });

                map.on("mouseenter", "households-circle", () => {
                    map.getCanvas().style.cursor = "pointer";
                });

                map.on("mouseenter", "households-circle", () => {
                    map.setPaintProperty("households-circle", "circle-radius", [
                        "case",
                        ["boolean", ["feature-state", "selected"], false],
                        18,
                        14,
                    ]);
                });

                map.on("mouseleave", "households-circle", () => {
                    map.getCanvas().style.cursor = "";
                    map.setPaintProperty("households-circle", "circle-radius", [
                        "case",
                        ["boolean", ["feature-state", "selected"], false],
                        16,
                        12,
                    ]);
                });

                updateMarkers(map);
            });
        };

        if (document.getElementById("trackasia-gl-script")) {
            initMap();
        } else {
            const script = document.createElement("script");
            script.id = "trackasia-gl-script";
            script.src = "https://unpkg.com/trackasia-gl@2.0.1/dist/trackasia-gl.js";
            script.onload = initMap;
            document.head.appendChild(script);

            if (!document.getElementById("trackasia-gl-css")) {
                const link = document.createElement("link");
                link.id = "trackasia-gl-css";
                link.rel = "stylesheet";
                link.href = "https://unpkg.com/trackasia-gl@2.0.1/dist/trackasia-gl.css";
                document.head.appendChild(link);
            }
        }

        return () => {
            if (mapRef.current) {
                try {
                    mapRef.current.remove();
                } catch (e) {
                    // Ignore cleanup errors
                }
                mapRef.current = null;
                hasInitializedRef.current = false;
            }
        };
    }, [updateMarkers]);

    useEffect(() => {
        if (mapRef.current) {
            updateMarkers(mapRef.current);
        }
    }, [households, selectedHouseholdId, updateMarkers]);

    return (
        <div className="relative w-full h-full rounded-2xl overflow-hidden border border-gray-100 shadow-lg">
            <div ref={mapContainerRef} className="w-full h-full" />

            {loading && (
                <div className="absolute inset-0 bg-white/70 flex items-center justify-center z-10">
                    <div className="text-center">
                        <div className="w-8 h-8 border-2 border-indigo-500 border-t-transparent rounded-full animate-spin mx-auto mb-2" />
                        <p className="text-sm text-gray-500">Đang tải bản đồ...</p>
                    </div>
                </div>
            )}

            <div className="absolute top-4 left-1/2 -translate-x-1/2 z-10 bg-white/90 backdrop-blur-sm rounded-full px-4 py-2 shadow-md text-xs font-medium text-gray-600">
                Click on any marker to view household details
            </div>
        </div>
    );
}

declare global {
    interface Window {
        trackasiagl: any;
    }
}