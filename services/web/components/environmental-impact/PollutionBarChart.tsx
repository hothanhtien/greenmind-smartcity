"use client"

import type { ImpactPoint } from "@/types/environmental"
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts"

interface Props {
  timeSeries: ImpactPoint[]
}

const BARS = [
  { key: "air",   label: "Air",   color: "#6366f1" },
  { key: "water", label: "Water", color: "#f97316" },
  { key: "soil",  label: "Soil",  color: "#ef4444" },
] as const

export function PollutionBarChart({ timeSeries }: Props) {
  const hasDate = timeSeries.length > 0 && !!timeSeries[0].date
  const data = timeSeries.map((p) => ({
    label: hasDate ? p.date! : `Day ${p.day}`,
    air:   p.air,
    water: p.water,
    soil:  p.soil,
  }))

  if (data.length === 0) {
    return (
      <div className="flex h-72 items-center justify-center text-sm text-muted-foreground">
        No data available for this period
      </div>
    )
  }

  return (
    <div className="w-full h-72">
      <ResponsiveContainer width="100%" height="100%">
        <BarChart
          data={data}
          margin={{ top: 8, right: 16, left: 0, bottom: timeSeries.length > 14 ? 30 : 12 }}
          barCategoryGap="20%"
        >
          <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" vertical={false} />
          <XAxis
            dataKey="label"
            tick={{ fill: "#9ca3af", fontSize: 10 }}
            angle={timeSeries.length > 10 ? -40 : 0}
            textAnchor={timeSeries.length > 10 ? "end" : "middle"}
            interval={timeSeries.length > 14 ? Math.floor(timeSeries.length / 10) : 0}
          />
          <YAxis
            tick={{ fill: "#9ca3af", fontSize: 11 }}
            label={{ value: "Value", angle: -90, position: "insideLeft", fill: "#6b7280", fontSize: 12 }}
          />
          <Tooltip
            contentStyle={{ background: "var(--card)", border: "1px solid var(--border)", borderRadius: 8 }}
            labelStyle={{ color: "var(--foreground)" }}
            itemStyle={{ color: "var(--muted-foreground)" }}
          />
          <Legend
            wrapperStyle={{ paddingTop: 8, fontSize: 12, color: "#9ca3af" }}
            formatter={(value) => <span style={{ color: "#9ca3af" }}>{value}</span>}
          />
          {BARS.map((b) => (
            <Bar
              key={b.key}
              dataKey={b.key}
              name={b.label}
              fill={b.color}
              radius={[3, 3, 0, 0]}
              fillOpacity={0.85}
            />
          ))}
        </BarChart>
      </ResponsiveContainer>
    </div>
  )
}
