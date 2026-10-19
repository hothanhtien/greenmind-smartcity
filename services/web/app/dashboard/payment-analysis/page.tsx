"use client"

import { useState, useEffect, useCallback } from "react"
import {
  fetchPaymentData,
  formatCents,
  type PaymentPayload,
  type PaymentStatus,
  type RevenuePoint,
} from "@/services/payment.service"
import {
  AreaChart,
  Area,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Cell,
  PieChart,
  Pie,
  Legend,
} from "recharts"

// ──────────────────────────────────────────────
//  Constants / helpers
// ──────────────────────────────────────────────
const DAY_OPTIONS = [
  { label: "7 days", value: 7 },
  { label: "30 days", value: 30 },
  { label: "90 days", value: 90 },
]

const STATUS_META: Record<PaymentStatus, { label: string; color: string; bg: string; text: string }> = {
  succeeded: { label: "Succeeded", color: "#10b981", bg: "bg-emerald-50", text: "text-emerald-700" },
  pending: { label: "Pending", color: "#f59e0b", bg: "bg-amber-50", text: "text-amber-700" },
  failed: { label: "Failed", color: "#ef4444", bg: "bg-red-50", text: "text-red-700" },
  refunded: { label: "Refunded", color: "#6366f1", bg: "bg-indigo-50", text: "text-indigo-700" },
}

function StatusBadge({ status }: { status: PaymentStatus }) {
  if (status === "refunded") return null  // waste collection has no refunds
  const m = STATUS_META[status]
  return (
    <span className={`inline-flex items-center gap-1 rounded-full px-2.5 py-0.5 text-xs font-semibold ${m.bg} ${m.text}`}>
      <span className="h-1.5 w-1.5 rounded-full" style={{ background: m.color }} />
      {m.label}
    </span>
  )
}

// ──────────────────────────────────────────────
//  Metric card
// ──────────────────────────────────────────────
interface MetricCardProps {
  id: string
  label: string
  value: string
  sub?: string
  icon: string
  accent: string
  accentBg: string
}

function MetricCard({ id, label, value, sub, icon, accent, accentBg }: MetricCardProps) {
  return (
    <div id={id} className="relative overflow-hidden rounded-2xl border bg-card p-5 shadow-sm">
      <div className={`absolute top-0 right-0 w-24 h-24 rounded-full opacity-10 ${accentBg} -translate-y-6 translate-x-6`} />
      <div className={`mb-3 inline-flex items-center justify-center w-10 h-10 rounded-xl text-lg font-bold ${accentBg} border ${accent}`}>
        {icon}
      </div>
      <p className="text-xs font-medium tracking-widest text-muted-foreground uppercase mb-1">{label}</p>
      <p className={`text-2xl font-bold tabular-nums ${accent}`}>{value}</p>
      {sub && <p className="text-xs text-muted-foreground mt-1">{sub}</p>}
    </div>
  )
}

// ──────────────────────────────────────────────
//  Revenue area chart
// ──────────────────────────────────────────────
function RevenueChart({ data }: { data: RevenuePoint[] }) {
  const chartData = data.map(d => ({ ...d, revenueK: +(d.revenue / 100).toFixed(2) }))
  return (
    <ResponsiveContainer width="100%" height={280}>
      <AreaChart data={chartData} margin={{ top: 8, right: 16, left: 0, bottom: 4 }}>
        <defs>
          <linearGradient id="rev-grad" x1="0" y1="0" x2="0" y2="1">
            <stop offset="5%" stopColor="#10b981" stopOpacity={0.35} />
            <stop offset="95%" stopColor="#10b981" stopOpacity={0.02} />
          </linearGradient>
        </defs>
        <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" vertical={false} />
        <XAxis
          dataKey="date"
          tick={{ fontSize: 10, fill: "#9ca3af" }}
          interval={chartData.length > 14 ? Math.floor(chartData.length / 10) : 0}
        />
        <YAxis tick={{ fontSize: 11, fill: "#9ca3af" }} tickFormatter={v => `$${v}`} />
        <Tooltip
          contentStyle={{ background: "var(--card)", border: "1px solid var(--border)", borderRadius: 8 }}
          formatter={(v: number) => [`$${v}`, "Revenue"]}
        />
        <Area
          type="monotone"
          dataKey="revenueK"
          stroke="#10b981"
          strokeWidth={2}
          fill="url(#rev-grad)"
          dot={false}
          activeDot={{ r: 4, fill: "#10b981" }}
        />
      </AreaChart>
    </ResponsiveContainer>
  )
}

// ──────────────────────────────────────────────
//  Transaction count bar chart
// ──────────────────────────────────────────────
function TransactionCountChart({ data }: { data: RevenuePoint[] }) {
  return (
    <ResponsiveContainer width="100%" height={280}>
      <BarChart data={data} margin={{ top: 8, right: 16, left: 0, bottom: 4 }} barCategoryGap="30%">
        <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" vertical={false} />
        <XAxis
          dataKey="date"
          tick={{ fontSize: 10, fill: "#9ca3af" }}
          interval={data.length > 14 ? Math.floor(data.length / 10) : 0}
        />
        <YAxis tick={{ fontSize: 11, fill: "#9ca3af" }} allowDecimals={false} />
        <Tooltip
          contentStyle={{ background: "var(--card)", border: "1px solid var(--border)", borderRadius: 8 }}
          formatter={(v: number) => [v, "Transactions"]}
        />
        <Bar dataKey="count" fill="#6366f1" radius={[3, 3, 0, 0]} fillOpacity={0.85} />
      </BarChart>
    </ResponsiveContainer>
  )
}

// ──────────────────────────────────────────────
//  Status donut chart
// ──────────────────────────────────────────────
function StatusDonut({ data }: { data: { status: PaymentStatus; count: number }[] }) {
  // Waste collection has no refunds — filter it out
  const chartData = data
    .filter(d => d.count > 0 && d.status !== "refunded")
    .map(d => ({
      name: STATUS_META[d.status].label,
      value: d.count,
      color: STATUS_META[d.status].color,
    }))

  return (
    <ResponsiveContainer width="100%" height={220}>
      <PieChart>
        <Pie
          data={chartData}
          cx="50%"
          cy="50%"
          innerRadius={55}
          outerRadius={85}
          paddingAngle={3}
          dataKey="value"
        >
          {chartData.map((entry, index) => (
            <Cell key={`cell-${index}`} fill={entry.color} />
          ))}
        </Pie>
        <Tooltip
          contentStyle={{ background: "var(--card)", border: "1px solid var(--border)", borderRadius: 8 }}
          formatter={(v: number, name: string) => [v, name]}
        />
        <Legend
          wrapperStyle={{ fontSize: 12 }}
          formatter={(value) => <span style={{ color: "#9ca3af" }}>{value}</span>}
        />
      </PieChart>
    </ResponsiveContainer>
  )
}

// ──────────────────────────────────────────────
//  Main page
// ──────────────────────────────────────────────
export default function PaymentAnalysisPage() {
  const [days, setDays] = useState(30)
  const [data, setData] = useState<PaymentPayload | null>(null)
  const [loading, setLoading] = useState(true)

  const load = useCallback(async (d: number) => {
    setLoading(true)
    const result = await fetchPaymentData(d)
    setData(result)
    setLoading(false)
  }, [])

  useEffect(() => { load(days) }, [days, load])

  const m = data?.metrics

  return (
    <div className="p-6 lg:p-8 space-y-6 max-w-screen-xl mx-auto">
      {/* Header */}
      <div className="flex items-start justify-between gap-4 flex-wrap">
        <div>
          <h1 className="text-3xl font-semibold text-foreground mb-1">Payment Analysis</h1>
          <p className="text-muted-foreground text-sm">
            Payment &amp; revenue analytics
            {data?.isMock && (
              <span className="ml-2 rounded-full bg-amber-50 border border-amber-200 px-2 py-0.5 text-xs text-amber-700 font-medium">
                Demo data
              </span>
            )}
          </p>
        </div>

        {/* Day range selector */}
        <div className="flex gap-1 rounded-lg bg-muted p-1">
          {DAY_OPTIONS.map(o => (
            <button
              key={o.value}
              id={`pay-range-${o.value}`}
              onClick={() => setDays(o.value)}
              className={[
                "rounded-md px-4 py-1.5 text-sm font-medium transition-colors",
                days === o.value
                  ? "bg-primary text-primary-foreground shadow"
                  : "text-muted-foreground hover:text-foreground",
              ].join(" ")}
            >
              {o.label}
            </button>
          ))}
        </div>
      </div>

      {/* Metric cards — 5 cards, no refund */}
      {loading ? (
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-5">
          {Array.from({ length: 5 }).map((_, i) => (
            <div key={i} className="h-32 rounded-2xl bg-muted animate-pulse" />
          ))}
        </div>
      ) : m && (
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-5">
          <MetricCard
            id="pay-metric-revenue"
            label="Total Revenue"
            value={formatCents(m.totalRevenue)}
            icon="$"
            accent="text-emerald-500"
            accentBg="bg-emerald-50"
          />
          <MetricCard
            id="pay-metric-txn"
            label="Transactions"
            value={m.totalTransactions.toLocaleString()}
            icon="#"
            accent="text-indigo-500"
            accentBg="bg-indigo-50"
          />
          <MetricCard
            id="pay-metric-rate"
            label="Success Rate"
            value={`${m.successRate}%`}
            icon="%"
            accent="text-teal-500"
            accentBg="bg-teal-50"
          />
          <MetricCard
            id="pay-metric-avg"
            label="Avg / Transaction"
            value={formatCents(m.avgTransactionValue)}
            icon="~"
            accent="text-blue-500"
            accentBg="bg-blue-50"
          />
          <MetricCard
            id="pay-metric-pending"
            label="Pending"
            value={formatCents(m.pendingAmount)}
            icon="…"
            accent="text-amber-500"
            accentBg="bg-amber-50"
          />
        </div>
      )}

      {/* Charts row */}
      <div className="grid grid-cols-1 gap-6 xl:grid-cols-3">
        {/* Revenue area chart */}
        <section className="xl:col-span-2 rounded-2xl border bg-card p-5 shadow-sm">
          <h2 className="mb-4 text-sm font-semibold text-foreground tracking-wide uppercase">Revenue by Day</h2>
          {loading || !data ? (
            <div className="flex h-72 items-center justify-center">
              <div className="h-6 w-6 animate-spin rounded-full border-2 border-primary border-t-transparent" />
            </div>
          ) : (
            <RevenueChart data={data.revenueSeries} />
          )}
        </section>

        {/* Status donut */}
        <section className="rounded-2xl border bg-card p-5 shadow-sm">
          <h2 className="mb-4 text-sm font-semibold text-foreground tracking-wide uppercase">Status Breakdown</h2>
          {loading || !data ? (
            <div className="flex h-56 items-center justify-center">
              <div className="h-6 w-6 animate-spin rounded-full border-2 border-primary border-t-transparent" />
            </div>
          ) : (
            <StatusDonut data={data.statusBreakdown} />
          )}
        </section>
      </div>

      {/* Transaction count bar chart */}
      <section className="rounded-2xl border bg-card p-5 shadow-sm">
        <h2 className="mb-4 text-sm font-semibold text-foreground tracking-wide uppercase">Transactions by Day</h2>
        {loading || !data ? (
          <div className="flex h-72 items-center justify-center">
            <div className="h-6 w-6 animate-spin rounded-full border-2 border-primary border-t-transparent" />
          </div>
        ) : (
          <TransactionCountChart data={data.revenueSeries} />
        )}
      </section>

      {/* Recent transactions table */}
      <section className="rounded-2xl border bg-card shadow-sm overflow-hidden">
        <div className="px-5 py-4 border-b">
          <h2 className="text-sm font-semibold text-foreground tracking-wide uppercase">Recent Transactions</h2>
        </div>
        {loading || !data ? (
          <div className="space-y-3 p-5">
            {Array.from({ length: 5 }).map((_, i) => (
              <div key={i} className="h-10 rounded-lg bg-muted animate-pulse" />
            ))}
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b bg-muted/30">
                  {["ID", "Customer", "Description", "Amount", "Status", "Time"].map(h => (
                    <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-muted-foreground uppercase tracking-wider">
                      {h}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-border/50">
                {data.recentTransactions.map(txn => (
                  <tr key={txn.id} className="hover:bg-muted/20 transition-colors">
                    <td className="px-4 py-3 font-mono text-xs text-muted-foreground">
                      {txn.stripeId?.slice(0, 14) ?? txn.id.slice(0, 14)}…
                    </td>
                    <td className="px-4 py-3">
                      <p className="font-medium text-foreground">{txn.customer}</p>
                      <p className="text-xs text-muted-foreground">{txn.email}</p>
                    </td>
                    <td className="px-4 py-3 text-muted-foreground">{txn.description}</td>
                    <td className="px-4 py-3 font-semibold tabular-nums">
                      {formatCents(txn.amount, txn.currency.toUpperCase())}
                    </td>
                    <td className="px-4 py-3">
                      <StatusBadge status={txn.status} />
                    </td>
                    <td className="px-4 py-3 text-xs text-muted-foreground whitespace-nowrap">
                      {new Date(txn.createdAt).toLocaleDateString("en-GB", {
                        day: "2-digit", month: "2-digit", year: "numeric",
                        hour: "2-digit", minute: "2-digit",
                      })}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  )
}
