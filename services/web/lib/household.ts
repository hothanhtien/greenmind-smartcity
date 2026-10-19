import { apiGet } from "@/lib/auth";
import type { AreaStatus, Household, HouseholdMember, HouseholdProfile } from "@/types/monitoring";
import type { AxiosRequestConfig } from "axios";

const HOUSEHOLDS_API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "https://vodang-api.gauas.com";

// API payload types from /households/get-all-households
export interface ApiHouseholdMember {
    id: string;
    username: string;
    email: string;
    phoneNumber: string | null;
    fullName: string;
    gender: string;
    location: string;
    region: string;
    role: string;
    roleId: string | null;
    householdId: string;
    dateOfBirth: string;
    segmentId: string | null;
    createdAt: string;
    updatedAt: string;
}

export interface ApiHousehold {
    id: string;
    address: string;
    urbanAreaId: string | number | null;
    lat?: string | number | null;
    lng?: string | number | null;
    latitude?: string | number | null;
    longitude?: string | number | null;
    createdAt: string;
    updatedAt: string;
    members?: ApiHouseholdMember[];
    greenScore?: number;
}

export interface ApiHouseholdDetectionItem {
    area: number;
    name: string;
    quantity: number;
}

export interface ApiHouseholdDetectionRecord {
    id: string;
    imageUrl: string;
    items: ApiHouseholdDetectionItem[];
    pollution: Record<string, number> | null;
    impact: Record<string, number> | null;
    totalObjects: number;
    aiAnalysis: string;
    householdId: string;
    detectType: string;
    createdAt: string;
    updatedAt: string;
    detectedBy: Record<string, any>;
    household: Record<string, any>;
}
export interface ApiHouseholdGreenScoreItem {
    id: string;
    previousScore: number;
    delta: number;
    finalScore: number;
    householdId: string;
    items: ApiHouseholdDetectionItem[] | null;
    reasons: string[] | null;
    createdAt: string;
}

export interface ApiHouseholdGreenScoreResponse {
    message?: string;
    data?: {
        id: string;
        address: string;
        urbanAreaId: string | number | null;
        lat: string;
        lng: string;
        createdAt: string;
        updatedAt: string;
        greenScores: ApiHouseholdGreenScoreItem[];
    };
}
export type ApiGetAllHouseholdsResponse = ApiHousehold[] | { data: ApiHousehold[] };

type ApiHouseholdListEnvelope = {
    data?: ApiHousehold[] | { data?: ApiHousehold[] };
    total?: number;
    page?: number;
    limit?: number;
};

function toFiniteNumber(value: unknown): number | null {
    if (typeof value === "number") {
        return Number.isFinite(value) ? value : null;
    }

    if (typeof value === "string") {
        const normalized = value.trim();
        if (!normalized) return null;
        const parsed = Number(normalized);
        return Number.isFinite(parsed) ? parsed : null;
    }

    return null;
}

function extractLatLng(apiHousehold: ApiHousehold): { lat: number; lng: number } {
    const lat = toFiniteNumber(apiHousehold.lat ?? apiHousehold.latitude) ?? 0;
    const lng = toFiniteNumber(apiHousehold.lng ?? apiHousehold.longitude) ?? 0;
    return { lat, lng };
}

function hashStringToNumber(value: string): number {
    let hash = 0;
    for (let i = 0; i < value.length; i += 1) {
        hash = (hash * 31 + value.charCodeAt(i)) & 0x7fffffff;
    }
    return Math.abs(hash) || 1;
}

function mapApiHouseholdToProfile(apiHousehold: ApiHousehold): HouseholdProfile {
    const numericId = toFiniteNumber(apiHousehold.id) ?? hashStringToNumber(apiHousehold.id);
    const members: HouseholdMember[] = (apiHousehold.members ?? []).map((m) => ({
        name: m.fullName || m.username || "Thành viên",
        wasteKg: 0,
        role: m.role || "user",
    }));

    const { lat, lng } = extractLatLng(apiHousehold);

    const familySize = Math.max(1, members.length);
    const waste = 0;
    const reportCount = members.length;

    const greenScore = apiHousehold.greenScore;
    const status: AreaStatus = greenScore == null
        ? "yellow"
        : greenScore >= 70 ? "green"
            : greenScore >= 40 ? "yellow"
                : "red";

    const mappedHousehold: Household = {
        id: numericId,
        externalId: apiHousehold.id,
        wardId: typeof apiHousehold.urbanAreaId === "number" ? apiHousehold.urbanAreaId : 0,
        name: apiHousehold.address || "Hộ gia đình",
        address: apiHousehold.address || "Chưa rõ",
        lat,
        lng,
        waste,
        status,
        reportCount,
        greenScore: apiHousehold.greenScore,
    };

    const profile: HouseholdProfile = {
        ...mappedHousehold,
        familySize,
        members,
        wasteHistory: [],
        imageHistory: [],
    };

    // Store raw API members for leaderboard access (includes createdAt)
    (profile as any)._members = apiHousehold.members ?? [];

    return profile;
}

export function getHouseholdProfilesFromApi(apiHouseholds: ApiHousehold[]): HouseholdProfile[] {
    return apiHouseholds.map(mapApiHouseholdToProfile);
}

export async function getAllHouseholds(config?: AxiosRequestConfig): Promise<ApiGetAllHouseholdsResponse | ApiHouseholdListEnvelope> {
    return apiGet("/households/get-all-households", {
        baseURL: HOUSEHOLDS_API_BASE_URL,
        ...config,
    });
}

export async function getHouseholdDetectionHistoryByHousehold(householdId: string): Promise<ApiHouseholdDetectionRecord[]> {
    const response = await apiGet(`/households/get-detect-by-household/${encodeURIComponent(householdId)}`, {
        baseURL: HOUSEHOLDS_API_BASE_URL,
    });

    if (Array.isArray(response)) {
        return response as ApiHouseholdDetectionRecord[];
    }

    if (response && typeof response === "object") {
        const data = (response as any).data;
        if (Array.isArray(data)) {
            return data as ApiHouseholdDetectionRecord[];
        }
        if (Array.isArray(data?.data)) {
            return data.data as ApiHouseholdDetectionRecord[];
        }
    }

    return [];
}

export async function getHouseholdGreenScoreHistory(householdId: string): Promise<ApiHouseholdGreenScoreItem[]> {
    const response = await apiGet(`/households/green-score/${encodeURIComponent(householdId)}`, {
        baseURL: HOUSEHOLDS_API_BASE_URL,
    });

    if (Array.isArray(response)) {
        return response as ApiHouseholdGreenScoreItem[];
    }

    if (response && typeof response === "object") {
        const data = (response as any).data;
        if (Array.isArray(data?.greenScores)) {
            return data.greenScores as ApiHouseholdGreenScoreItem[];
        }
        if (Array.isArray(response?.greenScores)) {
            return response.greenScores as ApiHouseholdGreenScoreItem[];
        }
    }

    return [];
}

function extractImageUrlFromAiAnalysis(aiAnalysis: string, fallbackUrl: string): string {
    try {
        // Try to parse as JSON and extract url
        const parsed = JSON.parse(aiAnalysis);
        if (parsed?.url) return parsed.url;
        if (parsed?.imageUrl) return parsed.imageUrl;
        if (parsed?.resultUrl) return parsed.resultUrl;
        if (parsed?.resultImageUrl) return parsed.resultImageUrl;
        // If it's a string URL
        if (typeof parsed === "string" && parsed.startsWith("http")) return parsed;
    } catch {
        // Not JSON, maybe it's a direct URL
        if (aiAnalysis.startsWith("http")) return aiAnalysis;
    }
    return fallbackUrl;
}

export function mapHouseholdDetectionRecordsToImageHistory(records: ApiHouseholdDetectionRecord[]) {
    // Filter only analyze_all records
    const filteredRecords = records.filter((record) => record.detectType === "analyze_all");

    const groupedByUrl = new Map<string, ApiHouseholdDetectionRecord[]>();

    filteredRecords
        .slice()
        .forEach((record) => {
            const url = String(record.imageUrl ?? "").trim();
            if (!url) return;

            const group = groupedByUrl.get(url);
            if (group) {
                group.push(record);
            } else {
                groupedByUrl.set(url, [record]);
            }
        });

    return Array.from(groupedByUrl.values())
        .map((group) => {
            const sortedGroup = group.slice().sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
            const primary = sortedGroup[0];

            // Extract image URL from aiAnalysis, fallback to resultImageUrl, then imageUrl
            const resultImageUrl = extractImageUrlFromAiAnalysis(primary.aiAnalysis, undefined);
            const finalImageUrl = resultImageUrl || (primary as any).resultImageUrl || (primary as any).resultUrl || primary.imageUrl;

            const mergedItems = group
                .flatMap((record) => record.items ?? [])
                .reduce((acc, item) => {
                    const existing = acc.find((entry) => entry.name === item.name);
                    if (existing) {
                        existing.quantity += item.quantity;
                    } else {
                        acc.push({ ...item });
                    }
                    return acc;
                }, [] as ApiHouseholdDetectionItem[]);

            const mergedPollution = group.reduce<Record<string, number>>((acc, record) => {
                if (record.pollution && typeof record.pollution === "object") {
                    Object.entries(record.pollution).forEach(([key, value]) => {
                        if (typeof value === "number" && !Number.isNaN(value)) {
                            acc[key] = value;
                        }
                    });
                }
                return acc;
            }, {});

            return {
                id: primary.id,
                uploadedAt: primary.createdAt,
                imageUrl: finalImageUrl,
                resultImageUrl: resultImageUrl || undefined,
                label: "Lịch sử phát hiện",
                sender: primary.detectedBy?.fullName || primary.detectedBy?.username || primary.detectedBy?.email || undefined,
                items: mergedItems.length ? mergedItems : undefined,
                total_objects: Math.max(...group.map((record) => Number(record.totalObjects) || 0)),
                pollution: Object.keys(mergedPollution).length ? mergedPollution : undefined,
            } as import("@/types/monitoring").HouseholdImageHistory;
        })
        .sort((a, b) => new Date(b.uploadedAt).getTime() - new Date(a.uploadedAt).getTime());
}

function resolveHouseholdPayload(response: ApiGetAllHouseholdsResponse | ApiHouseholdListEnvelope): {
    items: ApiHousehold[];
    total?: number;
    page?: number;
    limit?: number;
} {
    if (Array.isArray(response)) {
        return { items: response };
    }

    const envelope = response as ApiHouseholdListEnvelope;
    const direct = (response as { data?: unknown })?.data;
    if (Array.isArray(direct)) {
        return { items: direct, total: envelope.total, page: envelope.page, limit: envelope.limit };
    }

    if (direct && typeof direct === "object" && Array.isArray((direct as any).data)) {
        return { items: (direct as any).data, total: envelope.total, page: envelope.page, limit: envelope.limit };
    }

    return { items: [], total: envelope.total, page: envelope.page, limit: envelope.limit };
}

export async function getAllHouseholdProfiles(): Promise<HouseholdProfile[]> {
    const PREFERRED_LIMIT = 100;
    const MAX_PAGES = 200;

    const fetchAllPaged = async (limit: number): Promise<ApiHousehold[]> => {
        const seen = new Set<string>();
        const merged: ApiHousehold[] = [];

        const addUnique = (items: ApiHousehold[]) => {
            for (const item of items) {
                if (!item?.id) continue;
                if (seen.has(item.id)) continue;
                seen.add(item.id);
                merged.push(item);
            }
        };

        let total: number | undefined;
        let effectiveLimit = limit;

        for (let page = 1; page <= MAX_PAGES; page += 1) {
            const resp = await getAllHouseholds({ params: { page, limit: effectiveLimit } });
            const payload = resolveHouseholdPayload(resp);
            if (typeof payload.limit === "number" && payload.limit > 0) {
                effectiveLimit = payload.limit;
            }
            if (typeof payload.total === "number") {
                total = payload.total;
            }

            const before = merged.length;
            addUnique(payload.items);

            if (!payload.items.length) break;
            if (merged.length === before) break; // page ignored / no new data

            if (typeof total === "number" && merged.length >= total) break;
            if (payload.items.length < effectiveLimit) break; // last page
        }

        return merged;
    };

    // Best effort: try paged fetch with large limit first.
    try {
        const merged = await fetchAllPaged(PREFERRED_LIMIT);
        if (merged.length) return getHouseholdProfilesFromApi(merged);
    } catch {
        // ignore and fallback
    }

    // Fallback: call without params (some endpoints truly return all without pagination).
    const fallback = resolveHouseholdPayload(await getAllHouseholds());
    if (fallback.items.length) return getHouseholdProfilesFromApi(fallback.items);

    return [];
}
