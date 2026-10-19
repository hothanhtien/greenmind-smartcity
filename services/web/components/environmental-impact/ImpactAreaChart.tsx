"use client"

import type { ImpactPoint } from "@/types/environmental"
import {
  AreaChart,
  Area,
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

const SERIES = [
  { key: "air",   label: "Air",   color: "#6366f1", fillOpacity: 0.15 },
  { key: "soil",  label: "Soil",  color: "#ef4444", fillOpacity: 0.2  },
  { key: "water", label: "Water", color: "#f97316", fillOpacity: 0.2  },
] as const

export function ImpactAreaChart({ timeSeries }: Props) {
  const hasDate = timeSeries.length > 0 && !!timeSeries[0].date

  return (
    <div className="w-full h-72">
      <ResponsiveContainer width="100%" height="100%">
        <AreaChart data={timeSeries} margin={{ top: 8, right: 16, left: 0, bottom: 4 }}>
          <defs>
            {SERIES.map((s) => (
              <linearGradient key={s.key} id={`grad-${s.key}`} x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%"  stopColor={s.color} stopOpacity={0.35} />
                <stop offset="95%" stopColor={s.color} stopOpacity={0.02} />
              </linearGradient>
            ))}
          </defs>
          <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" vertical={false} />
          <XAxis
            dataKey={hasDate ? "date" : "day"}
            tick={{ fontSize: 11 }}
            label={
              hasDate
                ? undefined
                : { value: "Day", position: "insideBottomRight", offset: -8, fontSize: 12 }
            }
            interval={timeSeries.length > 14 ? Math.floor(timeSeries.length / 10) : 0}
          />
          <YAxis
            tick={{ fontSize: 11 }}
            label={{ value: "Value", angle: -90, position: "insideLeft", fontSize: 12 }}
          />
          <Tooltip
            contentStyle={{ background: "var(--card)", border: "1px solid var(--border)", borderRadius: 8 }}
            labelStyle={{ color: "var(--foreground)" }}
            itemStyle={{ color: "var(--muted-foreground)" }}
            labelFormatter={(v) => hasDate ? `${v}` : `Day ${v}`}
          />
          <Legend
            wrapperStyle={{ paddingTop: 12, fontSize: 12, color: "#9ca3af" }}
            formatter={(value) => <span style={{ color: "#9ca3af" }}>{value}</span>}
          />
          {SERIES.map((s) => (
            <Area
              key={s.key}
              type="monotone"
              dataKey={s.key}
              name={s.label}
              stroke={s.color}
              strokeWidth={2}
              fill={`url(#grad-${s.key})`}
              dot={false}
              activeDot={{ r: 4, fill: s.color }}
            />
          ))}
        </AreaChart>
      </ResponsiveContainer>
    </div>
  )
}
