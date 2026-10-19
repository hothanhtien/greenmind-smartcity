"use client";

import { useEffect, useRef, useState } from "react";
import {
  LineChart,
  Line,
  BarChart,
  Bar,
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from "recharts";
import type { UrbanArea, WasteReport } from "@/types/waste-report";
import { HOUSEHOLDS } from "@/data/wardData";

interface AreaDrawerProps {
  area: UrbanArea | null;
  wasteReports: WasteReport[];
  onClose: () => void;
}

// Fallback detail for dynamically added areas
const defaultDetail = (area: UrbanArea) => ({
  households: Math.round(area.totalWaste / 2.5),
  plasticRatio: Math.round(30 + area.reports * 2),
  wasteTrend: [
    { date: "03-13", waste: Math.round(area.totalWaste * 0.85) },
    { date: "03-14", waste: Math.round(area.totalWaste * 0.9) },
    { date: "03-15", waste: Math.round(area.totalWaste * 0.93) },
    { date: "03-16", waste: Math.round(area.totalWaste * 0.97) },
    { date: "03-17", waste: Math.round(area.totalWaste * 1.0) },
    { date: "03-18", waste: Math.round(area.totalWaste * 1.03) },
    { date: "03-19", waste: area.totalWaste },
  ],
  plasticByMonth: [
    { month: "Oct", plastic: Math.round(area.totalWaste * 0.22) },
    { month: "Nov", plastic: Math.round(area.totalWaste * 0.25) },
    { month: "Dec", plastic: Math.round(area.totalWaste * 0.3) },
    { month: "Jan", plastic: Math.round(area.totalWaste * 0.28) },
    { month: "Feb", plastic: Math.round(area.totalWaste * 0.32) },
    { month: "Mar", plastic: Math.round(area.totalWaste * 0.35) },
  ],
  electricityByMonth: [
    { month: "Oct", kwh: area.totalWaste * 8 },
    { month: "Nov", kwh: area.totalWaste * 8.5 },
    { month: "Dec", kwh: area.totalWaste * 9.5 },
    { month: "Jan", kwh: area.totalWaste * 9 },
    { month: "Feb", kwh: area.totalWaste * 8.2 },
    { month: "Mar", kwh: area.totalWaste * 8.8 },
  ],
  emissionTrend: [
    { date: "03-13", emission: +(area.totalWaste * 0.00042).toFixed(2) },
    { date: "03-14", emission: +(area.totalWaste * 0.00044).toFixed(2) },
    { date: "03-15", emission: +(area.totalWaste * 0.00046).toFixed(2) },
    { date: "03-16", emission: +(area.totalWaste * 0.00048).toFixed(2) },
    { date: "03-17", emission: +(area.totalWaste * 0.00047).toFixed(2) },
    { date: "03-18", emission: +(area.totalWaste * 0.0005).toFixed(2) },
    { date: "03-19", emission: +(area.totalWaste * 0.00048).toFixed(2) },
  ],
});

const STATUS_LABEL: Record<string, { label: string; bg: string; text: string }> = {
  red: { label: "Critical", bg: "bg-red-50", text: "text-red-600" },
  yellow: { label: "Moderate", bg: "bg-amber-50", text: "text-amber-600" },
  green: { label: "Normal", bg: "bg-emerald-50", text: "text-emerald-600" },
};

function ChartCard({
  title,
  children,
}: {
  title: string;
  children: React.ReactNode;
}) {
  return (
    <div className="bg-gray-50 rounded-xl p-4">
      <p className="text-xs font-semibold text-gray-500 uppercase tracking-wide mb-3">
        {title}
      </p>
      {children}
    </div>
  );
}

const REPORT_STATUS_CFG: Record<WasteReport["status"], { label: string; bg: string; text: string; dot: string }> = {
  pending:  { label: "Pending",    bg: "bg-red-50",      text: "text-red-700",     dot: "bg-red-400 animate-pulse" },
  approved: { label: "Approved",   bg: "bg-blue-50",     text: "text-blue-700",    dot: "bg-blue-400" },
  done:     { label: "Completed",  bg: "bg-emerald-50",  text: "text-emerald-700", dot: "bg-emerald-400" },
};

export function AreaDrawer({ area, wasteReports, onClose }: AreaDrawerProps) {
  const drawerRef = useRef<HTMLDivElement>(null);

  // Selected report for detail view
  const [selectedReportId, setSelectedReportId] = useState<string | null>(null);

  const selectedReport = wasteReports.find(r => r.id === selectedReportId) ?? null;

  // Close on Escape key
  useEffect(() => {
    const handleKey = (e: KeyboardEvent) => {
      if (e.key === "Escape") {
        if (selectedReportId) { setSelectedReportId(null); }
        else onClose();
      }
    };
    window.addEventListener("keydown", handleKey);
    return () => window.removeEventListener("keydown", handleKey);
  }, [onClose, selectedReportId]);

  const detail = area ? defaultDetail(area) : null;

  const statusCfg = area ? STATUS_LABEL[area.status] : null;

  return (
    <div
      ref={drawerRef}
      className="w-full h-full bg-white rounded-2xl border border-gray-100 shadow-sm flex flex-col overflow-hidden"
    >
        {/* Drawer header */}
        <div className="flex items-start justify-between px-6 pt-6 pb-4 border-b border-gray-100">
          <div>
            <div className="flex items-center gap-2 mb-1">
              <h2 className="text-lg font-bold text-gray-900">
                {area?.name ?? "—"}
              </h2>
              {statusCfg && (
                <span
                  className={`text-xs font-semibold px-2 py-0.5 rounded-full ${statusCfg.bg} ${statusCfg.text}`}
                >
                  {statusCfg.label}
                </span>
              )}
            </div>
            <p className="text-xs text-gray-400">Real-time environmental & waste data</p>
          </div>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 transition-colors p-1 rounded-lg hover:bg-gray-100"
          >
            <svg
              width="16"
              height="16"
              viewBox="0 0 16 16"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
            >
              <path d="M2 2l12 12M14 2L2 14" />
            </svg>
          </button>
        </div>

        {/* Scrollable content */}
        <div className="flex-1 overflow-y-auto px-6 py-5 space-y-5">
          {area && detail ? (
            <>
              {/* Report pipeline for this ward — TOP */}
              {(() => {
                const wardReports = wasteReports.filter(r => r.wardName === area.name);
                if (wardReports.length === 0) return null;
                const pendingCount = wardReports.filter(r => r.status === "pending").length;
                return (
                  <div>
                    <div className="flex items-center justify-between mb-3">
                      <p className="text-xs font-semibold text-gray-500 uppercase tracking-wide">
                        Waste Reports ({wardReports.length} reports)
                      </p>
                      {pendingCount > 0 && (
                        <span className="text-[10px] font-semibold text-red-500 bg-red-50 px-2 py-0.5 rounded-full">
                          {pendingCount} pending
                        </span>
                      )}
                    </div>
                    <div className="space-y-2 max-h-72 overflow-y-auto pr-1">
                      {wardReports.map(r => {
                        const sc = REPORT_STATUS_CFG[r.status] ?? REPORT_STATUS_CFG["pending"];
                        const t  = new Date(r.createdAt).toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit" });
                        return (
                          <div
                            key={r.id}
                            className={`rounded-xl border border-gray-100 p-3 ${sc.bg} cursor-pointer hover:shadow-sm hover:border-gray-200 transition-all`}
                            onClick={() => { setSelectedReportId(r.id); }}
                          >
                            <div className="flex items-center justify-between mb-1.5">
                              <span className={`inline-flex items-center gap-1.5 text-[11px] font-semibold ${sc.text}`}>
                                <span className={`w-1.5 h-1.5 rounded-full ${sc.dot}`} />
                                {sc.label}
                              </span>
                              <div className="flex items-center gap-2">
                                <span className="text-[10px] text-gray-400 tabular-nums">{t}</span>
                                <span className="text-[10px] text-gray-300">›</span>
                              </div>
                            </div>
                            <p className="text-xs font-semibold text-gray-800 mb-0.5">{r.reportedByName || "Citizen"}</p>
                            <p className="text-[10px] text-gray-500 line-clamp-1 mb-2">{r.description}</p>
                          </div>
                        );
                      })}
                    </div>
                  </div>
                );
              })()}

              {/* Household list — TOP */}
              {(() => {
                const wardHouseholds = HOUSEHOLDS.filter(h => h.wardId === area.id);
                if (wardHouseholds.length === 0) return null;
                return (
                  <div>
                    <p className="text-xs font-semibold text-gray-500 uppercase tracking-wide mb-3">
                      Household List ({wardHouseholds.length} households)
                    </p>
                    <div className="rounded-xl border border-gray-100 overflow-hidden">
                      <div className="max-h-48 overflow-y-auto">
                        <table className="w-full text-xs">
                          <thead className="sticky top-0 bg-gray-50">
                            <tr>
                              <th className="text-left px-3 py-2 text-gray-500 font-semibold">Hộ dân</th>
                              <th className="text-left px-3 py-2 text-gray-500 font-semibold">Địa chỉ</th>
                              <th className="text-right px-3 py-2 text-gray-500 font-semibold">Rác (kg)</th>
                            </tr>
                          </thead>
                          <tbody className="divide-y divide-gray-50">
                            {wardHouseholds
                              .sort((a, b) => b.waste - a.waste)
                              .map((hh) => {
                                const statusCfgHH = STATUS_LABEL[hh.status];
                                return (
                                  <tr key={hh.id} className="hover:bg-gray-50 transition-colors">
                                    <td className="px-3 py-2">
                                      <p className="text-gray-700 font-medium">{hh.name}</p>
                                      <span className={`px-1.5 py-0.5 rounded-full text-[9px] font-semibold ${statusCfgHH.bg} ${statusCfgHH.text}`}>
                                        {statusCfgHH.label === "Critical" ? "Cao" : statusCfgHH.label === "Moderate" ? "TB" : "Thấp"}
                                      </span>
                                    </td>
                                    <td className="px-3 py-2 text-[10px] text-gray-400">{hh.address}</td>
                                    <td className="px-3 py-2 text-right font-bold text-gray-900">{hh.waste.toLocaleString()}</td>
                                  </tr>
                                );
                              })
                            }
                          </tbody>
                        </table>
                      </div>
                    </div>
                  </div>
                );
              })()}

              {/* Stat grid */}
              <div className="grid grid-cols-2 gap-3">
                {[
                  {
                    label: "Số dân",
                    value: area.population.toLocaleString(),
                    accent: "text-blue-700",
                    sub: area.district,
                  },
                  {
                    label: "Tổng rác thải",
                    value: `${area.totalWaste.toLocaleString()} kg`,
                    accent: "text-red-600",
                    sub: "kg/ngày",
                  },
                  {
                    label: "Tỷ lệ nhựa",
                    value: `${detail.plasticRatio}%`,
                    accent: "text-violet-600",
                    sub: "trong tổng rác",
                  },
                  {
                    label: "Báo cáo chờ",
                    value: wasteReports.filter(r => r.wardName === area.name && r.status === "pending").length,
                    accent: "text-amber-600",
                    sub: `/ ${wasteReports.filter(r => r.wardName === area.name).length} tổng`,
                  },
                ].map((stat) => (
                  <div key={stat.label} className="bg-gray-50 rounded-xl p-4 border border-gray-100">
                    <p className="text-xs text-gray-400 mb-1">{stat.label}</p>
                    <p className={`text-xl font-bold ${stat.accent}`}>{stat.value}</p>
                    {stat.sub && <p className="text-[10px] text-gray-400 mt-0.5">{stat.sub}</p>}
                  </div>
                ))}
              </div>

              {/* Chart 1: Waste Trend (Last 7 Days) */}
              <ChartCard title="Xu hướng rác thải – 7 ngày gần nhất">
                <ResponsiveContainer width="100%" height={120}>
                  <LineChart data={detail.wasteTrend}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                    <XAxis
                      dataKey="date"
                      tick={{ fontSize: 10, fill: "#9ca3af" }}
                      axisLine={false}
                      tickLine={false}
                    />
                    <YAxis
                      tick={{ fontSize: 10, fill: "#9ca3af" }}
                      axisLine={false}
                      tickLine={false}
                      width={40}
                    />
                    <Tooltip
                      contentStyle={{
                        fontSize: 11,
                        borderRadius: 8,
                        border: "none",
                        boxShadow: "0 4px 12px rgba(0,0,0,0.1)",
                      }}
                    />
                    <Line
                      type="monotone"
                      dataKey="waste"
                      stroke="#6366f1"
                      strokeWidth={2}
                      dot={{ r: 3, fill: "#6366f1" }}
                      activeDot={{ r: 5 }}
                    />
                  </LineChart>
                </ResponsiveContainer>
              </ChartCard>

              {/* Chart 2: Plastic Usage by Month */}
              <ChartCard title="Sử dụng nhựa theo tháng (kg)">
                <ResponsiveContainer width="100%" height={120}>
                  <BarChart data={detail.plasticByMonth}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                    <XAxis
                      dataKey="month"
                      tick={{ fontSize: 10, fill: "#9ca3af" }}
                      axisLine={false}
                      tickLine={false}
                    />
                    <YAxis
                      tick={{ fontSize: 10, fill: "#9ca3af" }}
                      axisLine={false}
                      tickLine={false}
                      width={40}
                    />
                    <Tooltip
                      contentStyle={{
                        fontSize: 11,
                        borderRadius: 8,
                        border: "none",
                        boxShadow: "0 4px 12px rgba(0,0,0,0.1)",
                      }}
                    />
                    <Bar
                      dataKey="plastic"
                      fill="#818cf8"
                      radius={[4, 4, 0, 0]}
                    />
                  </BarChart>
                </ResponsiveContainer>
              </ChartCard>


              {/* Chart 4: Waste Emission Trend */}
              <ChartCard title="Xu hướng phát thải CO₂ (tấn)">
                <ResponsiveContainer width="100%" height={120}>
                  <AreaChart data={detail.emissionTrend}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                    <XAxis
                      dataKey="date"
                      tick={{ fontSize: 10, fill: "#9ca3af" }}
                      axisLine={false}
                      tickLine={false}
                    />
                    <YAxis
                      tick={{ fontSize: 10, fill: "#9ca3af" }}
                      axisLine={false}
                      tickLine={false}
                      width={40}
                    />
                    <Tooltip
                      contentStyle={{
                        fontSize: 11,
                        borderRadius: 8,
                        border: "none",
                        boxShadow: "0 4px 12px rgba(0,0,0,0.1)",
                      }}
                    />
                    <defs>
                      <linearGradient id="emissionGrad" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="#f59e0b" stopOpacity={0.35} />
                        <stop offset="95%" stopColor="#f59e0b" stopOpacity={0} />
                      </linearGradient>
                    </defs>
                    <Area
                      type="monotone"
                      dataKey="emission"
                      stroke="#f59e0b"
                      strokeWidth={2}
                      fill="url(#emissionGrad)"
                    />
                  </AreaChart>
                </ResponsiveContainer>
              </ChartCard>
            </>
          ) : (
            <div className="flex items-center justify-center h-48 text-sm text-gray-400">
              Select a ward on the map
            </div>
          )}
        </div>

        {/* ── Report Detail Panel (slides in over the drawer content) ── */}
        <div
          className={`absolute inset-0 z-10 bg-white rounded-2xl flex flex-col transition-transform duration-300 ease-out ${
            selectedReport ? "translate-x-0" : "translate-x-full"
          }`}
        >
          {selectedReport && (() => {
            const sc = REPORT_STATUS_CFG[selectedReport.status] ?? REPORT_STATUS_CFG["pending"];
            const t  = new Date(selectedReport.createdAt).toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit" });
            const resolvedT = selectedReport.resolvedAt
              ? new Date(selectedReport.resolvedAt).toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit" })
              : null;
            return (
              <>
                {/* Detail header */}
                <div className="flex items-center gap-3 px-6 pt-5 pb-4 border-b border-gray-100">
                  <button
                    onClick={() => { setSelectedReportId(null); }}
                    className="p-1.5 rounded-lg hover:bg-gray-100 text-gray-400 hover:text-gray-600 transition-colors"
                  >
                    <svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M10 3L5 8l5 5" />
                    </svg>
                  </button>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2">
                      <h3 className="text-sm font-bold text-gray-900 truncate">
                        {selectedReport.code || selectedReport.id.slice(0,8).toUpperCase()}
                      </h3>
                      <span className={`text-[10px] font-semibold px-2 py-0.5 rounded-full ${sc.bg} ${sc.text} shrink-0`}>
                        {sc.label}
                      </span>
                    </div>
                    <p className="text-[11px] text-gray-400">{selectedReport.wardName}</p>
                  </div>
                </div>

                {/* Detail content */}
                <div className="flex-1 overflow-y-auto px-6 py-5 space-y-4">
                  {/* Household info */}
                  <div className="bg-gray-50 rounded-xl p-4 space-y-2">
                    <p className="text-[10px] font-semibold text-gray-400 uppercase tracking-wide">Report Information</p>
                    <p className="text-sm font-bold text-gray-800">{selectedReport.reportedByName || "Anonymous"}</p>
                    <div className="grid grid-cols-2 gap-2 text-xs">
                      <div>
                        <p className="text-gray-400">Reported at</p>
                        <p className="font-semibold text-gray-700">{t}</p>
                      </div>
                      {resolvedT && (
                        <div>
                          <p className="text-gray-400">Completed at</p>
                          <p className="font-semibold text-emerald-600">{resolvedT}</p>
                        </div>
                      )}
                    </div>
                  </div>

                  {/* Description */}
                  <div className="bg-gray-50 rounded-xl p-4">
                    <p className="text-[10px] font-semibold text-gray-400 uppercase tracking-wide mb-1.5">Description</p>
                    <p className="text-xs text-gray-700 leading-relaxed">{selectedReport.description || "No description"}</p>
                  </div>
                </div>
              </>
            );
          })()}
        </div>
    </div>
  );
}
