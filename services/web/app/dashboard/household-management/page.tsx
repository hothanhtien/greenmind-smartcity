"use client";

import dynamic from "next/dynamic";
import { useMemo, useEffect, useState } from "react";
import { getAllHouseholdProfiles, getHouseholdDetectionHistoryByHousehold, getHouseholdGreenScoreHistory, mapHouseholdDetectionRecordsToImageHistory, ApiHouseholdMember } from "@/lib/household";
import { HouseholdDetailsPanel } from "@/components/household/HouseholdDetailsPanel";
import { Dialog, DialogContent, DialogTitle, DialogDescription, DialogClose } from "@/components/ui/dialog";
import { Leaderboard } from "@/components/blog/Leaderboard";
import type { HouseholdProfile } from "@/types/monitoring";
import { Users, TrendingUp, Award, MapPin, RefreshCw, Search, X } from "lucide-react";
import type { LeaderboardUser } from "@/services/blog.service";

const HouseholdManagementMap = dynamic(
    () => import("@/components/household/HouseholdManagementMap").then((m) => m.HouseholdManagementMap),
    { ssr: false }
);

function StatCard({ icon: Icon, label, value, gradient, subLabel }: {
    icon: React.ElementType;
    label: string;
    value: string | number;
    gradient: string;
    subLabel?: string;
}) {
    return (
        <div
            className="relative overflow-hidden rounded-xl bg-white/80 dark:bg-slate-900/80 backdrop-blur-xl border border-white/20 dark:border-slate-700/50 p-3"
        >
            <div className="relative flex items-center justify-between gap-2">
                <div className="flex-1 min-w-0">
                    <p className="text-xs font-medium text-slate-500 dark:text-slate-400 truncate">{label}</p>
                    <p className="text-2xl font-bold text-slate-900 dark:text-white tracking-tight">{value}</p>
                    {subLabel && <p className="text-[11px] text-slate-400 dark:text-slate-500 truncate">{subLabel}</p>}
                </div>

                <div className={`shrink-0 p-2 rounded-lg bg-linear-to-br ${gradient}`}>
                    <Icon className="w-4 h-4 text-white" />
                </div>
            </div>
        </div>
    );
}

export default function HouseholdManagementPage() {
    const [allHouseholds, setAllHouseholds] = useState<HouseholdProfile[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [apiError, setApiError] = useState<string | null>(null);
    const [selectedHousehold, setSelectedHousehold] = useState<HouseholdProfile | null>(null);
    const [selectedHouseholdHistory, setSelectedHouseholdHistory] = useState<HouseholdProfile["imageHistory"] | undefined>(undefined);
    const [historyLoading, setHistoryLoading] = useState(false);
    const [historyError, setHistoryError] = useState<string | null>(null);
    const [selectedHouseholdGreenScores, setSelectedHouseholdGreenScores] = useState<HouseholdProfile["greenScores"] | undefined>(undefined);
    const [greenScoreLoading, setGreenScoreLoading] = useState(false);
    const [greenScoreError, setGreenScoreError] = useState<string | null>(null);
    const [isDialogOpen, setIsDialogOpen] = useState(false);
    const [refreshing, setRefreshing] = useState(false);
    const [searchQuery, setSearchQuery] = useState("");

    const fetchHouseholds = async () => {
        setIsLoading(true);
        setApiError(null);

        try {
            const profiles = await getAllHouseholdProfiles();
            setAllHouseholds(profiles);
        } catch (error: any) {
            setApiError(error?.message ?? 'Unknown error');
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        // Fetch immediately on mount
        fetchHouseholds();
    }, []);

    useEffect(() => {
        if (!selectedHousehold?.externalId) {
            setSelectedHouseholdHistory(undefined);
            setHistoryLoading(false);
            setHistoryError(null);
            return;
        }

        let cancelled = false;

        const loadHouseholdHistory = async () => {
            setHistoryLoading(true);
            setHistoryError(null);
            try {
                const records = await getHouseholdDetectionHistoryByHousehold(selectedHousehold.externalId!);
                if (!cancelled) {
                    setSelectedHouseholdHistory(mapHouseholdDetectionRecordsToImageHistory(records));
                }
            } catch (error: any) {
                if (!cancelled) {
                    setHistoryError(error?.message ?? "Failed to load detection history");
                    setSelectedHouseholdHistory([]);
                }
            } finally {
                if (!cancelled) {
                    setHistoryLoading(false);
                }
            }
        };

        loadHouseholdHistory();

        return () => {
            cancelled = true;
        };
    }, [selectedHousehold?.externalId]);

    useEffect(() => {
        const selectedExternalId = selectedHousehold?.externalId;
        if (!selectedExternalId) {
            setSelectedHouseholdGreenScores(undefined);
            setGreenScoreLoading(false);
            setGreenScoreError(null);
            return;
        }

        let cancelled = false;
        setSelectedHouseholdGreenScores(undefined);
        setGreenScoreLoading(true);
        setGreenScoreError(null);

        const loadGreenScoreHistory = async () => {
            try {
                const scores = await getHouseholdGreenScoreHistory(selectedExternalId);
                if (!cancelled) {
                    setSelectedHouseholdGreenScores(scores);

                    const sortedScores = scores
                        .slice()
                        .filter((item) => !Number.isNaN(new Date(item.createdAt).getTime()))
                        .sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime());
                    const latestScore = sortedScores.length ? sortedScores[sortedScores.length - 1].finalScore : undefined;

                    if (latestScore != null) {
                        setSelectedHousehold((prev) =>
                            prev && prev.externalId === selectedExternalId
                                ? { ...prev, greenScore: latestScore }
                                : prev
                        );

                        setAllHouseholds((prev) =>
                            prev.map((household) =>
                                household.externalId === selectedExternalId
                                    ? { ...household, greenScore: latestScore }
                                    : household
                            )
                        );
                    }
                }
            } catch (error: any) {
                if (!cancelled) {
                    setGreenScoreError(error?.message ?? "Failed to load green score history");
                    setSelectedHouseholdGreenScores([]);
                }
            } finally {
                if (!cancelled) {
                    setGreenScoreLoading(false);
                }
            }
        };

        loadGreenScoreHistory();

        return () => {
            cancelled = true;
        };
    }, [selectedHousehold?.externalId]);

    const handleRefresh = async () => {
        setRefreshing(true);
        await fetchHouseholds();
        setRefreshing(false);
    };

    const summary = useMemo(() => {
        const totalReports = allHouseholds.reduce((acc, hh) => acc + hh.reportCount, 0);
        const totalImageUploads = allHouseholds.reduce((acc, hh) => acc + (hh.imageHistory?.length ?? 0), 0);
        const red = allHouseholds.filter((hh) => hh.status === "red").length;
        const yellow = allHouseholds.filter((hh) => hh.status === "yellow").length;
        const green = allHouseholds.filter((hh) => hh.status === "green").length;
        const validScores = allHouseholds.filter(hh => hh.greenScore != null);
        const avgScore = validScores.length > 0
            ? Math.round(validScores.reduce((acc, hh) => acc + (hh.greenScore ?? 0), 0) / validScores.length)
            : 0;

        return { totalReports, totalImageUploads, red, yellow, green, avgScore };
    }, [allHouseholds]);

    const statusTotal = summary.red + summary.yellow + summary.green;

    const leaderboard = useMemo((): LeaderboardUser[] => {
        const sorted = allHouseholds
            .slice()
            .sort((a, b) => (b.greenScore ?? 0) - (a.greenScore ?? 0));

        return sorted.map((household, idx) => {
            // First try raw API members with createdAt for sorting
            const rawMembers = (household as any)._members as ApiHouseholdMember[] | undefined;
            let headName = "";

            if (rawMembers && rawMembers.length > 0) {
                const sortedRaw = [...rawMembers].sort((a, b) =>
                    new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
                );
                const head = sortedRaw[0];
                headName = head?.fullName || head?.username || "";
            }

            // Fallback: use mapped members name
            if (!headName && household.members && household.members.length > 0) {
                headName = household.members[0]?.name || "";
            }

            // Final fallback: use household name/address
            if (!headName) {
                headName = household.name?.split(",")[0]?.trim() || "Hộ gia đình";
            }

            const address = household.address || household.name || "";

            return {
                rank: idx + 1,
                userId: `household-${household.id}`,
                fullName: headName,
                username: headName,
                location: address,
                reportCount: household.greenScore ?? 0,
            };
        });
    }, [allHouseholds]);

    // Filter households by search query
    const filteredHouseholds = useMemo(() => {
        if (!searchQuery.trim()) return allHouseholds;
        const query = searchQuery.toLowerCase();
        return allHouseholds.filter(h =>
            h.address?.toLowerCase().includes(query) ||
            h.name?.toLowerCase().includes(query)
        );
    }, [allHouseholds, searchQuery]);

    // Search result for dropdown
    const searchResults = useMemo(() => {
        if (!searchQuery.trim()) return [];
        return filteredHouseholds.slice(0, 5);
    }, [filteredHouseholds, searchQuery]);

    return (
        <div className="household-page-container flex flex-col h-full overflow-hidden bg-linear-to-br from-slate-100 via-slate-50 to-emerald-50/30 dark:from-slate-950 dark:via-slate-900 dark:to-emerald-950/20">
            <div className="fixed inset-0 overflow-hidden pointer-events-none">
                <div className="absolute -top-40 -right-40 w-96 h-96 rounded-full bg-linear-to-br from-emerald-200/30 to-teal-300/20 blur-3xl animate-pulse-soft" />
                <div className="absolute -bottom-20 -left-20 w-72 h-72 rounded-full bg-linear-to-br from-cyan-200/20 to-blue-300/10 blur-3xl" />
            </div>

            <header className="relative shrink-0 bg-white/60 dark:bg-slate-900/60 backdrop-blur-2xl border-b border-slate-200/50 dark:border-slate-700/50">
                <div className="px-4">
                    <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-2">
                        <div className="flex items-center gap-3">
                            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-linear-to-br from-emerald-500 via-teal-500 to-cyan-500 shadow-lg shadow-emerald-500/20">
                                <MapPin className="h-5 w-5 text-white" />
                            </div>
                            <div>
                                <h1 className="text-xl font-bold text-slate-900 dark:text-white tracking-tight">
                                    Household Management
                                </h1>
                                <div className="flex items-center gap-2 text-xs text-slate-500 dark:text-slate-400">
                                    <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
                                    Da Nang City • {allHouseholds.length} households
                                </div>
                            </div>
                        </div>

                        {/* Buttons Group - Right side */}
                        <div className="flex items-center gap-2">
                            {/* Always visible Search Input */}
                            <div className="relative">
                                <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                                <input
                                    type="text"
                                    placeholder="Search address..."
                                    value={searchQuery}
                                    onChange={(e) => setSearchQuery(e.target.value)}
                                    className="w-48 pl-9 pr-8 py-1.5 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-sm text-slate-900 dark:text-white placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-500"
                                />
                                {searchQuery && (
                                    <button
                                        onClick={() => setSearchQuery("")}
                                        className="absolute right-3 top-1/2 -translate-y-1/2"
                                    >
                                        <X className="w-4 h-4 text-slate-400 hover:text-slate-600" />
                                    </button>
                                )}
                                {/* Search Results Dropdown */}
                                {searchResults.length > 0 && (
                                    <div className="absolute top-full mt-1 left-0 w-64 bg-white dark:bg-slate-800 rounded-lg shadow-xl border border-slate-200 dark:border-slate-700 max-h-60 overflow-y-auto z-50">
                                        {searchResults.map((hh) => (
                                            <button
                                                key={hh.id}
                                                onClick={() => {
                                                    setSelectedHousehold(hh);
                                                    setIsDialogOpen(true);
                                                }}
                                                className="w-full text-left px-4 py-2 hover:bg-slate-100 dark:hover:bg-slate-700 border-b border-slate-100 dark:border-slate-700 last:border-0"
                                            >
                                                <p className="text-sm font-medium text-slate-900 dark:text-white truncate">{hh.address}</p>
                                                <p className="text-xs text-slate-500">Score: {hh.greenScore ?? "N/A"}</p>
                                            </button>
                                        ))}
                                    </div>
                                )}
                            </div>

                            {/* Refresh Button */}
                            <button
                                onClick={handleRefresh}
                                disabled={refreshing || isLoading}
                                className="inline-flex items-center gap-2 px-3 py-1.5 bg-emerald-500 hover:bg-emerald-600 text-white rounded-lg font-medium text-xs transition-all disabled:opacity-50 shadow-lg shadow-emerald-500/20"
                            >
                                <RefreshCw className={`w-3.5 h-3.5 ${refreshing ? "animate-spin" : ""}`} />
                                Refresh
                            </button>
                        </div>
                    </div>

                    {/* Stats Cards */}
                    <div className="grid grid-cols-2 lg:grid-cols-4 gap-1 mt-1">
                        <StatCard
                            icon={Users}
                            label="Total Households"
                            value={allHouseholds.length}
                            gradient="from-emerald-500 to-teal-600"
                            subLabel="Registered"
                        />
                        <StatCard
                            icon={TrendingUp}
                            label="Avg. Score"
                            value={summary.avgScore || "—"}
                            gradient="from-cyan-500 to-blue-600"
                            subLabel="Community avg"
                        />
                        <StatCard
                            icon={Award}
                            label="Top Score"
                            value={leaderboard[0]?.reportCount || "—"}
                            gradient="from-amber-500 to-orange-600"
                            subLabel={leaderboard[0]?.fullName || "No data"}
                        />
                        <div className="rounded-2xl bg-white/90 dark:bg-slate-900/90 backdrop-blur-xl border border-white/20 dark:border-slate-700/50 p-2">
                            <div className="flex items-center justify-between gap-2 mb-1.5">
                                <p className="text-[10px] uppercase tracking-[0.2em] text-slate-500 dark:text-slate-500 font-semibold">Status</p>
                                <span className="inline-flex items-center rounded-full bg-slate-100 dark:bg-slate-800 px-1.5 py-0.5 text-[10px] font-medium text-slate-600 dark:text-slate-300">{statusTotal}</span>
                            </div>

                            <div className="space-y-1.5">
                                <div>
                                    <div className="flex items-center justify-between text-[10px] text-slate-600 dark:text-slate-400 mb-0.5">
                                        <div className="flex items-center gap-1">
                                            <span className="w-1.5 h-1.5 rounded-full bg-emerald-500" />
                                            <span>Healthy</span>
                                        </div>
                                        <span>{summary.green} · {statusTotal ? Math.round((summary.green / statusTotal) * 100) : 0}%</span>
                                    </div>
                                    <div className="h-1 rounded-full bg-slate-100 dark:bg-slate-800 overflow-hidden">
                                        <div className="h-full rounded-full bg-emerald-500" style={{ width: `${statusTotal ? Math.round((summary.green / statusTotal) * 100) : 0}%` }} />
                                    </div>
                                </div>
                                <div>
                                    <div className="flex items-center justify-between text-[10px] text-slate-600 dark:text-slate-400 mb-0.5">
                                        <div className="flex items-center gap-1">
                                            <span className="w-1.5 h-1.5 rounded-full bg-amber-500" />
                                            <span>Warning</span>
                                        </div>
                                        <span>{summary.yellow} · {statusTotal ? Math.round((summary.yellow / statusTotal) * 100) : 0}%</span>
                                    </div>
                                    <div className="h-1 rounded-full bg-slate-100 dark:bg-slate-800 overflow-hidden">
                                        <div className="h-full rounded-full bg-amber-500" style={{ width: `${statusTotal ? Math.round((summary.yellow / statusTotal) * 100) : 0}%` }} />
                                    </div>
                                </div>
                                <div>
                                    <div className="flex items-center justify-between text-[10px] text-slate-600 dark:text-slate-400 mb-0.5">
                                        <div className="flex items-center gap-1">
                                            <span className="w-1.5 h-1.5 rounded-full bg-red-500" />
                                            <span>Critical</span>
                                        </div>
                                        <span>{summary.red} · {statusTotal ? Math.round((summary.red / statusTotal) * 100) : 0}%</span>
                                    </div>
                                    <div className="h-1 rounded-full bg-slate-100 dark:bg-slate-800 overflow-hidden">
                                        <div className="h-full rounded-full bg-red-500" style={{ width: `${statusTotal ? Math.round((summary.red / statusTotal) * 100) : 0}%` }} />
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </header>

            {/* Main Content */}
            <div className="relative flex-1 min-h-0 p-1 lg:p-2 overflow-hidden">
                <div className="grid grid-cols-1 lg:grid-cols-4 gap-2 h-full w-full">
                    {/* Map Section */}
                    <div className="lg:col-span-3 h-full w-full">
                        <div className="relative h-full w-full rounded-2xl overflow-hidden shadow-xl border border-white/20 dark:border-slate-700/50">

                            {apiError && (
                                <div className="absolute top-4 left-1/2 -translate-x-1/2 z-50 bg-red-50 dark:bg-red-950/80 border border-red-200 dark:border-red-800 rounded-2xl px-5 py-2.5 shadow-xl">
                                    <span className="text-sm font-medium text-red-600 dark:text-red-400">
                                        Error: {apiError}
                                    </span>
                                </div>
                            )}

                            <HouseholdManagementMap
                                households={allHouseholds}
                                selectedHouseholdId={selectedHousehold?.id ?? null}
                                onHouseholdSelect={(household) => {
                                    setSelectedHousehold(household);
                                    setIsDialogOpen(true);
                                }}
                                loading={isLoading}
                            />
                        </div>
                    </div>

                    {/* Leaderboard Panel */}
                    <div className="lg:col-span-1 h-full overflow-hidden w-full">
                        <div className="h-full w-full bg-white/70 dark:bg-slate-900/70 backdrop-blur-xl rounded-2xl border border-white/20 dark:border-slate-700/50 shadow-xl overflow-y-auto scrollbar-thin scrollbar-thumb-slate-300 dark:scrollbar-thumb-slate-600 scrollbar-track-transparent">
                            <Leaderboard
                                leaderboard={leaderboard}
                                title="Top Households"
                                subtitle="Ranked by green score"
                                emptyTitle="No households yet"
                                emptySubtitle="Start tracking to see rankings"
                                hideAvatar
                                previewCount={100}
                                expandedCount={100}
                                showViewAll={false}
                                autoFlow
                            />
                        </div>
                    </div>
                </div>
            </div>

            {/* Detail Dialog */}
            <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
                <DialogContent showCloseButton={false} className="w-[95vw] max-w-[95vw] lg:w-[85vw] lg:max-w-[85vw] h-[90vh] max-h-[90vh] overflow-hidden p-0">
                    <DialogTitle className="sr-only">Household Details</DialogTitle>
                    <DialogDescription className="sr-only">
                        View full household information, members, waste, images and reports.
                    </DialogDescription>
                    <div className="p-6 h-full overflow-y-auto">
                        {selectedHousehold && (
                            <HouseholdDetailsPanel
                                household={selectedHousehold}
                                imageHistory={selectedHouseholdHistory}
                                imageHistoryLoading={historyLoading}
                                historyError={historyError}
                                greenScoreHistory={selectedHouseholdGreenScores}
                                greenScoreLoading={greenScoreLoading}
                                greenScoreError={greenScoreError}
                            />
                        )}
                    </div>
                    <DialogClose asChild>
                        <button className="absolute top-4 right-4 w-10 h-10 rounded-full bg-slate-100 dark:bg-slate-800 hover:bg-slate-200 dark:hover:bg-slate-700 flex items-center justify-center transition-colors shadow-lg">
                            <svg className="w-5 h-5 text-slate-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                            </svg>
                        </button>
                    </DialogClose>
                </DialogContent>
            </Dialog>

            {/* Global Styles for Animations */}
            <style>{`
                @keyframes fade-up {
                    from {
                        opacity: 0;
                        transform: translateY(16px);
                    }
                    to {
                        opacity: 1;
                        transform: translateY(0);
                    }
                }
                .animate-fade-up {
                    animation: fade-up 0.6s cubic-bezier(0.16, 1, 0.3, 1) forwards;
                    opacity: 0;
                }
                .animate-fade-up-delay-1 { animation-delay: 100ms; }
                .animate-fade-up-delay-2 { animation-delay: 200ms; }
                .animate-fade-up-delay-3 { animation-delay: 300ms; }
                .animate-pulse-soft {
                    animation: pulse-soft 4s ease-in-out infinite;
                }
                @keyframes pulse-soft {
                    0%, 100% { opacity: 0.3; transform: scale(1); }
                    50% { opacity: 0.5; transform: scale(1.05); }
                }
                /* Prevent page scroll when on this page */
                .household-page-container {
                    height: 100%;
                    width: 100%;
                    max-width: 100vw;
                }
            `}</style>
        </div>
    );
}
