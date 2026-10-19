"use client";

import type { Summary } from "@/types/waste-report";

interface SummaryCardsProps {
  summary: Summary;
  loading: boolean;
  reportCounts?: {
    noCampaignCount: number;
    approvedCount: number;
    totalCount: number;
  };
  filter?: "no-campaign" | "approved" | "all";
  onFilterChange?: (filter: "no-campaign" | "approved" | "all") => void;
}

export function SummaryCards({ summary, loading, reportCounts, filter, onFilterChange }: SummaryCardsProps) {
  if (loading) {
    return (
      <div className="grid grid-cols-3 gap-4">
        {[...Array(3)].map((_, i) => (
          <div
            key={i}
            className="bg-white rounded-2xl border border-gray-100 shadow-sm p-3 animate-pulse"
          >
            <div className="h-2 bg-gray-200 rounded w-1/2 mb-3" />
            <div className="h-6 bg-gray-200 rounded w-3/4 mb-2" />
            <div className="h-2 bg-gray-100 rounded w-full" />
          </div>
        ))}
      </div>
    );
  }

  const cards = [
    {
      label: "Total Urban Areas",
      value: summary.urbanAreas,
      sub: "Active monitoring zones",
      accent: "from-blue-50 to-indigo-50 border-blue-100",
      valueColor: "text-blue-700",
    },
    {
      label: "Pending Reports",
      value: summary.pendingReports,
      sub: "Awaiting action from teams",
      accent: "from-amber-50 to-orange-50 border-amber-100",
      valueColor: "text-amber-700",
    },
    {
      label: "Waste Reports",
      value: summary.totalWaste || 0,
      sub: null,
      accent: "from-violet-50 to-purple-50 border-violet-100",
      valueColor: "text-violet-700",
      isReportsCard: true,
      noCampaignCount: reportCounts?.noCampaignCount ?? 0,
      approvedCount: reportCounts?.approvedCount ?? 0,
      totalCount: reportCounts?.totalCount ?? 0,
      currentFilter: filter ?? "all",
      onFilterChange,
    },
  ];

  return (
    <div className="grid grid-cols-3 gap-4 h-full">
      {cards.map((card) => (
        <div
          key={card.label}
          className={`bg-gradient-to-br ${card.accent} border rounded-2xl shadow-sm p-3 transition-all duration-200 hover:shadow-md hover:-translate-y-0.5 ${card.isReportsCard ? "flex flex-col" : "flex flex-col justify-center"}`}
        >
          <p className="text-[10px] font-semibold uppercase tracking-widest text-gray-400">
            {card.label}
          </p>
          {card.isReportsCard ? (
            <>
              <p className={`text-2xl font-bold mt-1 ${card.valueColor}`}>
                {card.value}
              </p>
              {/* Horizontal stats */}
              <div className="grid grid-cols-3 gap-1.5 mt-2">
                <button
                  onClick={() => card.onFilterChange?.("no-campaign")}
                  className={`rounded-md px-1.5 py-1 text-center cursor-pointer transition-all ${
                    card.currentFilter === "no-campaign"
                      ? "bg-red-100 ring-1 ring-red-300"
                      : "bg-red-50/60 hover:bg-red-100"
                  }`}
                >
                  <p className="text-sm font-bold text-red-600">{card.noCampaignCount}</p>
                  <p className="text-[9px] text-red-500/70 leading-tight">Await Campaign</p>
                </button>
                <button
                  onClick={() => card.onFilterChange?.("approved")}
                  className={`rounded-md px-1.5 py-1 text-center cursor-pointer transition-all ${
                    card.currentFilter === "approved"
                      ? "bg-blue-100 ring-1 ring-blue-300"
                      : "bg-blue-50/60 hover:bg-blue-100"
                  }`}
                >
                  <p className="text-sm font-bold text-blue-600">{card.approvedCount}</p>
                  <p className="text-[9px] text-blue-500/70 leading-tight">Approved</p>
                </button>
                <button
                  onClick={() => card.onFilterChange?.("all")}
                  className={`rounded-md px-1.5 py-1 text-center cursor-pointer transition-all ${
                    card.currentFilter === "all"
                      ? "bg-gray-200 ring-1 ring-gray-400"
                      : "bg-gray-50/60 hover:bg-gray-100"
                  }`}
                >
                  <p className="text-sm font-bold text-gray-700">{card.totalCount}</p>
                  <p className="text-[9px] text-gray-500 leading-tight">All</p>
                </button>
              </div>
              {/* Alert box */}
              {card.currentFilter === "no-campaign" && (
                <div className="mt-2 bg-indigo-50/80 border border-indigo-100 p-2 rounded-md flex items-start gap-2">
                  <svg className="text-indigo-500 mt-0.5 shrink-0" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5"><circle cx="12" cy="12" r="10"/><path d="M12 16v-4M12 8h.01"/></svg>
                  <p className="text-[10px] font-medium text-indigo-800 leading-relaxed">
                    High pollution areas — <strong>priority create campaigns</strong>
                  </p>
                </div>
              )}
            </>
          ) : (
            <>
              {card.value !== null && (
                <p className={`text-2xl font-bold mt-1 ${card.valueColor}`}>
                  {card.value}
                </p>
              )}
              {card.sub && (
                <p className="text-[10px] text-gray-400 mt-0.5">{card.sub}</p>
              )}
            </>
          )}
          {card.custom}
        </div>
      ))}
    </div>
  );
}
