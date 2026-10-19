"use client";

import { useMemo, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend } from "recharts";
import { User, Users, TrendingUp, BarChart3, Eye } from "lucide-react";
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog";
import type { HouseholdProfile, WasteReport } from "@/types/monitoring";

interface HouseholdDetailsPanelProps {
    household: HouseholdProfile | null;
    reports?: WasteReport[];
    imageHistory?: HouseholdProfile["imageHistory"];
    imageHistoryLoading?: boolean;
    historyError?: string | null;
    greenScoreHistory?: HouseholdProfile["greenScores"];
    greenScoreLoading?: boolean;
    greenScoreError?: string | null;
}

type CaptureTrendPoint = {
    month: string;
    captureCount: number;
    pollutionCO2: number;
    pollutionDioxin: number;
    pollutionMicroplastic: number;
    pollutionNonBiodegradable: number;
};

type ScoreTrendPeriod = "day" | "month" | "year";

type GreenScoreTrendPoint = {
    month: string;
    label: string;
    finalScore: number | null;
    delta?: number;
    previousScore?: number;
    reasons?: string[] | null;
    items?: {
        area: number;
        name: string;
        quantity: number;
    }[] | null;
};

function buildGreenScoreTrendFromHistory(greenScoreHistory: NonNullable<HouseholdProfile["greenScores"]>): GreenScoreTrendPoint[] {
    const now = new Date();
    const months: string[] = [];
    for (let i = 11; i >= 0; i -= 1) {
        const dt = new Date(now.getFullYear(), now.getMonth() - i, 1);
        months.push(formatMonth(dt));
    }

    const latestEntryByMonth = new Map<string, NonNullable<HouseholdProfile["greenScores"]>[number]>();
    greenScoreHistory
        .slice()
        .filter((entry) => !Number.isNaN(new Date(entry.createdAt).getTime()))
        .sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime())
        .forEach((entry) => {
            const monthKey = formatMonth(new Date(entry.createdAt));
            latestEntryByMonth.set(monthKey, entry);
        });

    let lastScore = 0;
    return months.map((month) => {
        const entry = latestEntryByMonth.get(month);
        if (entry?.finalScore != null) {
            lastScore = entry.finalScore;
        }
        return {
            month,
            label: month.slice(5),
            finalScore: entry?.finalScore ?? lastScore,
            delta: entry?.delta,
            previousScore: entry?.previousScore,
            reasons: entry?.reasons ?? null,
            items: entry?.items ?? null,
        };
    });
}

function formatMonth(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    return `${year}-${month}`;
}

function formatDay(date: Date): string {
    const day = String(date.getDate()).padStart(2, "0");
    const month = String(date.getMonth() + 1).padStart(2, "0");
    return `${day}/${month}`;
}

function formatYear(date: Date): string {
    return String(date.getFullYear());
}

function buildCaptureTrendFromHistory(
    imageHistory: HouseholdProfile["imageHistory"],
    reports?: WasteReport[],
): CaptureTrendPoint[] {
    const now = new Date();
    const months: string[] = [];

    for (let i = 11; i >= 0; i -= 1) {
        const dt = new Date(now.getFullYear(), now.getMonth() - i, 1);
        months.push(formatMonth(dt));
    }

    const captureCounts = new Map<string, number>();
    const pollutionByMonth = new Map<string, { CO2: number; dioxin: number; microplastic: number; non_biodegradable: number }>();

    const addPollution = (monthKey: string, pollution?: HouseholdProfile["imageHistory"][number]["pollution"] | WasteReport["pollution"]) => {
        if (!pollution || typeof pollution !== "object") return;
        const summary = pollutionByMonth.get(monthKey) ?? { CO2: 0, dioxin: 0, microplastic: 0, non_biodegradable: 0 };
        summary.CO2 += Number(pollution.CO2 ?? 0);
        summary.dioxin += Number(pollution.dioxin ?? 0);
        summary.microplastic += Number(pollution.microplastic ?? 0);
        summary.non_biodegradable += Number(pollution.non_biodegradable ?? 0);
        pollutionByMonth.set(monthKey, summary);
    };

    imageHistory?.forEach((image) => {
        const date = new Date(image.uploadedAt);
        if (Number.isNaN(date.getTime())) return;
        const monthKey = formatMonth(new Date(date.getFullYear(), date.getMonth(), 1));
        captureCounts.set(monthKey, (captureCounts.get(monthKey) ?? 0) + 1);
        addPollution(monthKey, image.pollution);
    });

    if (!imageHistory?.length && reports?.length) {
        reports.forEach((report) => {
            const date = new Date(report.reportedAt);
            if (Number.isNaN(date.getTime())) return;
            const monthKey = formatMonth(new Date(date.getFullYear(), date.getMonth(), 1));
            captureCounts.set(monthKey, (captureCounts.get(monthKey) ?? 0) + 1);
            addPollution(monthKey, report.pollution);
        });
    }

    return months.map((month) => {
        const summary = pollutionByMonth.get(month) ?? { CO2: 0, dioxin: 0, microplastic: 0, non_biodegradable: 0 };
        return {
            month,
            captureCount: captureCounts.get(month) ?? 0,
            pollutionCO2: Number(summary.CO2.toFixed(1)),
            pollutionDioxin: Number(summary.dioxin.toFixed(3)),
            pollutionMicroplastic: Number(summary.microplastic.toFixed(3)),
            pollutionNonBiodegradable: Number(summary.non_biodegradable.toFixed(1)),
        };
    });
}

function buildGreenScoreTrendFromHistoryByPeriod(
    greenScoreHistory: NonNullable<HouseholdProfile["greenScores"]>,
    period: ScoreTrendPeriod,
) {
    const validEntries = greenScoreHistory
        .slice()
        .filter((entry) => !Number.isNaN(new Date(entry.createdAt).getTime()))
        .sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime());

    const groupKey = (date: Date) => {
        if (period === "day") {
            return date.toISOString().slice(0, 10);
        }
        if (period === "year") {
            return String(date.getFullYear());
        }
        return formatMonth(date);
    };

    const labelFor = (date: Date) => {
        if (period === "day") return formatDay(date);
        if (period === "year") return formatYear(date);
        return String(date.getMonth() + 1).padStart(2, "0");
    };

    const entriesByPeriod = new Map<string, NonNullable<HouseholdProfile["greenScores"]>[number]>();
    validEntries.forEach((entry) => {
        const date = new Date(entry.createdAt);
        const key = groupKey(date);
        const existing = entriesByPeriod.get(key);
        if (!existing || date.getTime() > new Date(existing.createdAt).getTime()) {
            entriesByPeriod.set(key, entry);
        }
    });

    const range: Date[] = [];
    const now = new Date();
    if (period === "day") {
        for (let i = 29; i >= 0; i -= 1) {
            range.push(new Date(now.getFullYear(), now.getMonth(), now.getDate() - i));
        }
    } else if (period === "year") {
        for (let i = 4; i >= 0; i -= 1) {
            range.push(new Date(now.getFullYear() - i, 0, 1));
        }
    } else {
        for (let i = 11; i >= 0; i -= 1) {
            range.push(new Date(now.getFullYear(), now.getMonth() - i, 1));
        }
    }

    let lastScore = 0;
    return range.map((date) => {
        const key = groupKey(date);
        const entry = entriesByPeriod.get(key);
        if (entry?.finalScore != null) {
            lastScore = entry.finalScore;
        }

        return {
            month: key,
            label: labelFor(date),
            finalScore: entry?.finalScore ?? lastScore,
            delta: entry?.delta,
            previousScore: entry?.previousScore,
            reasons: entry?.reasons ?? null,
            items: entry?.items ?? null,
        };
    });
}

function clamp(value: number, min: number, max: number) {
    return Math.min(max, Math.max(min, value));
}

function normalizePersonName(name?: string) {
    return (name ?? "").trim().toLowerCase();
}

function buildMemberShotCounts(
    imageHistory: HouseholdProfile["imageHistory"],
    reports?: WasteReport[]
) {
    const counts = new Map<string, number>();

    const addShot = (name?: string) => {
        const normalized = normalizePersonName(name);
        if (!normalized) return;
        counts.set(normalized, (counts.get(normalized) ?? 0) + 1);
    };

    imageHistory?.forEach((image) => {
        addShot(image.sender);
    });

    reports?.forEach((report) => {
        if (typeof report.reportedBy === "string") {
            addShot(report.reportedBy);
        } else {
            addShot(report.householdName);
        }
    });

    return counts;
}

export function HouseholdDetailsPanel({ household, reports, imageHistory: imageHistoryProp, imageHistoryLoading = false, historyError, greenScoreHistory, greenScoreLoading = false, greenScoreError }: HouseholdDetailsPanelProps) {
    const householdReports = useMemo(() => {
        if (!household || !reports) return [];
        return reports.filter((r) => r.householdId === household.id);
    }, [household, reports]);

    const imageHistory = useMemo(() => {
        if (imageHistoryProp?.length) return imageHistoryProp;
        if (household?.imageHistory?.length) return household.imageHistory;
        return [];
    }, [household?.imageHistory, imageHistoryProp]);

    
    const [scoreTrendPeriod, setScoreTrendPeriod] = useState<ScoreTrendPeriod>("month");
    const [selectedImageForDetail, setSelectedImageForDetail] = useState<HouseholdProfile["imageHistory"][number] | null>(null);

    const memberShotCounts = useMemo(() => {
        return buildMemberShotCounts(imageHistory, householdReports);
    }, [imageHistory, householdReports]);

    if (!household) {
        return (
            <div className="p-4 rounded-2xl border border-dashed border-gray-200 bg-white h-full flex items-center justify-center text-gray-500">
                Please select a household on the map.
            </div>
        );
    }

    const captureTrend = useMemo(() => {
        return buildCaptureTrendFromHistory(imageHistory, householdReports);
    }, [imageHistory, householdReports]);

    const monthlyGreenScoreTrend = useMemo(() => {
        if (!greenScoreHistory?.length) return [];

        const data = buildGreenScoreTrendFromHistoryByPeriod(greenScoreHistory, "month");
        const hasScore = data.some((point) => point.finalScore != null);
        return hasScore ? data : [];
    }, [greenScoreHistory]);

    const selectedGreenScoreTrend = useMemo(() => {
        if (!greenScoreHistory?.length) return [];
        return buildGreenScoreTrendFromHistoryByPeriod(greenScoreHistory, scoreTrendPeriod);
    }, [greenScoreHistory, scoreTrendPeriod]);

    const latestGreenScore = useMemo(() => {
        if (!greenScoreHistory?.length) return null;
        const validEntries = greenScoreHistory
            .slice()
            .filter((entry) => !Number.isNaN(new Date(entry.createdAt).getTime()))
            .sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime());
        return validEntries.length ? validEntries[validEntries.length - 1] : null;
    }, [greenScoreHistory]);

    const latestCaptureMonth = captureTrend.length ? captureTrend[captureTrend.length - 1] : null;
    const trendChartData = monthlyGreenScoreTrend.length ? selectedGreenScoreTrend : captureTrend;
    const isGreenScoreChart = monthlyGreenScoreTrend.length > 0;

    const displayGreenScore = household.greenScore != null ? household.greenScore : latestGreenScore?.finalScore;
    const scoreColorClass = displayGreenScore != null
        ? displayGreenScore < 40
            ? "text-red-700 bg-red-100"
            : displayGreenScore < 70
                ? "text-amber-700 bg-amber-100"
                : "text-emerald-700 bg-emerald-100"
        : "text-slate-500 bg-slate-100";
    const displayGreenDelta = latestGreenScore?.delta != null ? `${latestGreenScore.delta >= 0 ? "+" : ""}${latestGreenScore.delta}` : null;

    return (
        <div className="min-h-[55vh] overflow-y-auto p-2 bg-slate-50 text-slate-800">
            <div className="grid grid-cols-1 xl:grid-cols-2 gap-2 mb-2">
                <Card className="p-2 shadow-lg border border-slate-200 bg-white rounded-xl">
                    <CardHeader className="flex items-center gap-2 mb-1">
                        <User className="w-4 h-4 text-emerald-600" />
                        <CardTitle className="text-base font-semibold flex items-center gap-2">Household Info
                            <span className={`inline-flex items-center justify-center w-5 h-5 rounded-full text-[10px] font-semibold ${scoreColorClass}`}>
                                {displayGreenScore != null ? displayGreenScore : "?"}
                            </span>
                        </CardTitle>
                    </CardHeader>
                    <CardContent>
                        <div className="space-y-2 text-sm">
                            <p><strong>Household head:</strong> {household.members?.length ? household.members[0].name : household.name}</p>
                            <p>Address: {household.address}</p>
                            <p>Total image uploads: {imageHistory.length}</p>
                            <p>Latest month captures: {latestCaptureMonth ? `${latestCaptureMonth.captureCount} captures` : "No data available"}</p>
                            <p>
                                Latest green score: {greenScoreLoading ? "Loading..." : greenScoreError ? greenScoreError : latestGreenScore ? `${latestGreenScore.finalScore} (Δ ${latestGreenScore.delta != null ? `${latestGreenScore.delta >= 0 ? "+" : ""}${latestGreenScore.delta}` : "0"})` : "No green score data"}
                            </p>
                        </div>
                    </CardContent>
                </Card>

                <Card className="shadow-lg border border-slate-200 bg-white rounded-xl">
                    <CardHeader className="mb-1 flex items-center gap-2">
                        <Users className="w-4 h-4 text-blue-600" />
                        <CardTitle className="text-sm font-semibold">Household Members</CardTitle>
                    </CardHeader>
                    <CardContent className="p-2">
                        <div className="space-y-2">
                            {!household.members?.length ? (
                                <div className="rounded-2xl border border-dashed border-slate-200 bg-slate-50 px-3 py-3 text-center text-xs text-slate-500">
                                    No members or data available
                                </div>
                            ) : (
                                household.members.map((member, idx) => (
                                    <div key={`${member.name}-${idx}`} className="flex items-center justify-between gap-2 rounded-2xl border border-slate-200 bg-slate-50 px-2 py-2 text-xs">
                                        <div>
                                            <p className="font-semibold text-slate-800 truncate">{member.name || "N/A"}</p>
                                            <p className="text-[10px] uppercase tracking-[0.2em] text-slate-500">{member.role || "N/A"}</p>
                                        </div>
                                        <div className="text-right min-w-14">
                                            <p className="text-[10px] text-slate-500">Shots</p>
                                            <p className="font-semibold text-slate-700">
                                                {memberShotCounts.get(normalizePersonName(member.name)) ?? 0}
                                            </p>
                                        </div>
                                    </div>
                                ))
                            )}
                        </div>
                    </CardContent>
                </Card>
            </div>

            <Card className="shadow-lg border border-slate-200 bg-white rounded-2xl">
                <CardHeader className="mb-2 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                    <div className="flex items-center gap-2">
                        <BarChart3 className="w-4 h-4 text-emerald-600" />
                        <CardTitle className="text-base font-semibold">{isGreenScoreChart ? "Green Score Trend" : "12-Month Capture Trend"}</CardTitle>
                    </div>
                    {isGreenScoreChart ? (
                        <div className="inline-flex rounded-full border border-slate-200 bg-slate-100 p-1">
                            {(["day", "month", "year"] as ScoreTrendPeriod[]).map((period) => (
                                <button
                                    key={period}
                                    type="button"
                                    onClick={() => setScoreTrendPeriod(period)}
                                    className={`rounded-full px-3 py-1 text-xs font-semibold transition ${scoreTrendPeriod === period ? "bg-white text-slate-900 shadow-sm" : "text-slate-500 hover:text-slate-700"}`}
                                >
                                    {period === "day" ? "Ngày" : period === "month" ? "Tháng" : "Năm"}
                                </button>
                            ))}
                        </div>
                    ) : null}
                </CardHeader>
                <CardContent>
                    <div className="w-full h-48 md:h-52 xl:h-56">
                        <ResponsiveContainer width="100%" height="100%">
                            <LineChart<GreenScoreTrendPoint | CaptureTrendPoint> data={trendChartData}>
                                <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" />
                                <XAxis dataKey="label" tickFormatter={(value) => String(value)} />
                                <YAxis allowDecimals={false} domain={isGreenScoreChart ? [0, 50] : undefined} />
                                <Tooltip formatter={(value: any) => {
                                    if (value == null) return "";
                                    const val = Number(value);
                                    if (Number.isNaN(val)) return String(value);
                                    return isGreenScoreChart ? `${val.toLocaleString()} pts` : `${val.toLocaleString()} times`;
                                }} />
                                <Legend verticalAlign="bottom" align="center" height={36} />
                                <Line
                                    type="monotone"
                                    dataKey={isGreenScoreChart ? "finalScore" : "captureCount"}
                                    stroke="#10b981"
                                    strokeWidth={3}
                                    dot={{ r: 4, fill: "#10b981" }}
                                    activeDot={{ r: 6 }}
                                    connectNulls
                                />
                            </LineChart>
                        </ResponsiveContainer>
                    </div>
                </CardContent>
            </Card>

            <Card className="shadow-lg border border-slate-200 bg-white rounded-2xl">
                <CardHeader className="mb-3 flex items-center gap-2">
                    <TrendingUp className="w-4 h-4 text-blue-600" />
                    <CardTitle className="text-base font-semibold">12-Month Capture & Pollution Trend</CardTitle>
                </CardHeader>
                <CardContent>
                    <div className="w-full h-40 md:h-44">
                        <ResponsiveContainer width="100%" height="100%">
                            <LineChart data={captureTrend}>
                                <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" />
                                <XAxis dataKey="month" tickFormatter={(value) => value.slice(5)} />
                                <YAxis yAxisId="capture" allowDecimals={false} />
                                <YAxis yAxisId="pollution" orientation="right" />
                                <Tooltip
                                    content={({ active, payload, label }: any) => {
                                        if (!active || !payload?.length) return null;
                                        const row = payload[0]?.payload as CaptureTrendPoint | undefined;

                                        return (
                                            <div className="rounded-lg border bg-background p-3 text-xs shadow-sm">
                                                <div className="font-semibold">Month {String(label).slice(5)}</div>
                                                <div className="mt-2 space-y-1">
                                                    <div>Captures: <span className="font-semibold">{Number(row?.captureCount ?? 0).toLocaleString()} times</span></div>
                                                    <div className="grid grid-cols-2 gap-x-3 gap-y-1 pt-2">
                                                        <div>CO2: {Number(row?.pollutionCO2 ?? 0).toFixed(1)}</div>
                                                        <div>Dioxin: {Number(row?.pollutionDioxin ?? 0).toFixed(3)}</div>
                                                        <div>Microplastic: {Number(row?.pollutionMicroplastic ?? 0).toFixed(3)}</div>
                                                        <div>Non-biodeg.: {Number(row?.pollutionNonBiodegradable ?? 0).toFixed(1)}</div>
                                                    </div>
                                                </div>
                                            </div>
                                        );
                                    }}
                                />
                                <Legend verticalAlign="bottom" align="center" height={36} />
                                <Line
                                    yAxisId="capture"
                                    type="monotone"
                                    dataKey="captureCount"
                                    stroke="#10b981"
                                    strokeWidth={3}
                                    dot={{ r: 4, fill: "#10b981" }}
                                    activeDot={{ r: 6 }}
                                    connectNulls
                                />
                                <Line
                                    yAxisId="pollution"
                                    type="monotone"
                                    dataKey="pollutionCO2"
                                    stroke="#f59e0b"
                                    strokeWidth={2}
                                    dot={{ r: 2 }}
                                    connectNulls
                                />
                                <Line
                                    yAxisId="pollution"
                                    type="monotone"
                                    dataKey="pollutionDioxin"
                                    stroke="#6366f1"
                                    strokeWidth={2}
                                    dot={{ r: 2 }}
                                    connectNulls
                                />
                                <Line
                                    yAxisId="pollution"
                                    type="monotone"
                                    dataKey="pollutionMicroplastic"
                                    stroke="#14b8a6"
                                    strokeWidth={2}
                                    dot={{ r: 2 }}
                                    connectNulls
                                />
                                <Line
                                    yAxisId="pollution"
                                    type="monotone"
                                    dataKey="pollutionNonBiodegradable"
                                    stroke="#ef4444"
                                    strokeWidth={2}
                                    dot={{ r: 2 }}
                                    connectNulls
                                />
                            </LineChart>
                        </ResponsiveContainer>
                    </div>
                </CardContent>
            </Card>


            <Card className="shadow-sm border border-gray-100">
                <CardHeader>
                    <CardTitle className="text-sm">Report Image History</CardTitle>
                </CardHeader>
                <CardContent>
                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3 max-h-[40vh] overflow-y-auto">
                        {imageHistoryLoading ? (
                            <div className="rounded-2xl border border-dashed border-slate-200 bg-slate-50 px-3 py-4 text-center text-sm text-slate-500 col-span-full">
                                Loading image history...
                            </div>
                        ) : historyError ? (
                            <div className="rounded-2xl border border-dashed border-rose-200 bg-rose-50 px-3 py-4 text-center text-sm text-rose-700 col-span-full">
                                {historyError}
                            </div>
                        ) : imageHistory.length === 0 ? (
                            <div className="rounded-2xl border border-dashed border-slate-200 bg-slate-50 px-3 py-4 text-center text-sm text-slate-500 col-span-full">
                                Không có bất kì detect nào.
                            </div>
                        ) : imageHistory.map((image) => {
                            const hasItems = image.items?.length;
                            const hasPollution = image.pollution && Object.keys(image.pollution).length > 0;

                            return (
                                <div key={image.id} className="border rounded-xl p-2 bg-white shadow-sm transition hover:shadow-md">
                                    <div className="relative w-full aspect-[4/3] overflow-hidden rounded-lg mb-2 bg-slate-100">
                                        <img src={image.imageUrl} alt={image.label} loading="lazy" className="absolute inset-0 w-full h-full object-cover" />
                                    </div>
                                    <div className="space-y-0.5">
                                        <div className="flex items-center justify-between">
                                            <p className="text-[11px] font-semibold text-slate-700">{new Date(image.uploadedAt).toLocaleDateString("en-US")}</p>
                                            <span className="text-[10px] text-slate-500">{new Date(image.uploadedAt).toLocaleTimeString("en-US", { hour: "2-digit", minute: "2-digit" })}</span>
                                        </div>
                                        <p className="text-[11px] text-slate-500 truncate">{image.caption || "Household waste image"}</p>
                                        <p className="text-[11px]">
                                            <span className="font-medium text-slate-600">Sender:</span> <span className="text-slate-700">{image.sender || "Unknown"}</span>
                                        </p>
                                        {image.total_objects != null && (
                                            <p className="text-[11px] text-slate-500">Objects: {image.total_objects}</p>
                                        )}
                                        {(hasItems || hasPollution) && (
                                            <button
                                                type="button"
                                                onClick={() => setSelectedImageForDetail(image)}
                                                className="flex items-center gap-1 px-2 py-1 bg-blue-100 hover:bg-blue-200 text-blue-700 rounded-lg text-[11px] font-medium transition"
                                            >
                                                <Eye className="w-3 h-3" />
                                                Details
                                            </button>
                                        )}
                                    </div>
                                </div>
                            );
                        })}
                    </div>
                </CardContent>
            </Card>

            <Dialog open={!!selectedImageForDetail} onOpenChange={(open) => !open && setSelectedImageForDetail(null)}>
                <DialogContent className="max-w-2xl max-h-max">
                    <DialogHeader>
                        <DialogTitle>Image Details</DialogTitle>
                    </DialogHeader>
                    {selectedImageForDetail && (
                        <div className="space-y-4">
                            <div className="flex gap-2">
                                {selectedImageForDetail.items?.length ? (
                                    <div className="flex-1 border rounded-lg p-3">
                                        <div className="flex items-center justify-between mb-2">
                                            <h3 className="font-semibold text-sm text-slate-700">Items Detected</h3>
                                            <span className="text-xs text-slate-500">{selectedImageForDetail.items.length} items</span>
                                        </div>
                                        <ul className="space-y-1">
                                            {selectedImageForDetail.items.map((item) => (
                                                <li key={item.name} className="flex justify-between text-sm bg-slate-50 rounded px-2 py-1">
                                                    <span className="font-medium">{item.name}</span>
                                                    <span className="text-slate-600">x{item.quantity} (area: {item.area})</span>
                                                </li>
                                            ))}
                                        </ul>
                                    </div>
                                ) : null}
                                {selectedImageForDetail.pollution && Object.keys(selectedImageForDetail.pollution).length > 0 ? (
                                    <div className="flex-1 border rounded-lg p-3">
                                        <div className="flex items-center justify-between mb-2">
                                            <h3 className="font-semibold text-sm text-slate-700">Pollution Data</h3>
                                            <span className="text-xs text-slate-500">{Object.keys(selectedImageForDetail.pollution).length} metrics</span>
                                        </div>
                                        <div className="space-y-1">
                                            {Object.entries(selectedImageForDetail.pollution).map(([key, value]) => (
                                                <div key={key} className="flex justify-between text-sm bg-slate-50 rounded px-2 py-1">
                                                    <span className="font-medium capitalize">{key}</span>
                                                    <span className="text-slate-600">{Number(value).toFixed(3)}</span>
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                ) : null}
                            </div>
                        </div>
                    )}
                </DialogContent>
            </Dialog>
        </div>
    );
}