"use client"

import type { TimeRange, WardBounds } from "@/types/environmental"
import { getDateRange } from "@/services/environmental.service"
import { WARDS } from "@/data/wardData"

const TIME_OPTIONS: { label: string; value: TimeRange }[] = [
  { label: "Day", value: "day" },
  { label: "Week", value: "week" },
  { label: "Month", value: "month" },
]

/** Format YYYY-MM-DD → "D/M" */
function fmtShort(iso: string): string {
  const [, m, d] = iso.split("-")
  return `${parseInt(d)}/${parseInt(m)}`
}

/** Extract bounding box from a ward's bounds polygon */
function wardBounds(bounds: [number, number][]): WardBounds {
  const lats = bounds.map((b) => b[0])
  const lngs = bounds.map((b) => b[1])
  return {
    latMin: Math.min(...lats),
    latMax: Math.max(...lats),
    lngMin: Math.min(...lngs),
    lngMax: Math.max(...lngs),
  }
}

// Group WARDS by district
const WARDS_BY_DISTRICT = WARDS.reduce<Record<string, typeof WARDS>>((acc, w) => {
  const d = w.district
  if (!acc[d]) acc[d] = []
  acc[d].push(w)
  return acc
}, {})

interface Props {
  timeRange: TimeRange
  onTimeRangeChange: (range: TimeRange) => void
  selectedWardId: number | null
  onWardChange: (wardId: number | null, bounds: WardBounds | null) => void
}

export function DashboardFilters({
  timeRange,
  onTimeRangeChange,
  selectedWardId,
  onWardChange,
}: Props) {
  const { startDate, endDate } = getDateRange(timeRange)
  const rangeLabel =
    timeRange === "day"
      ? fmtShort(endDate)
      : `${fmtShort(startDate)} – ${fmtShort(endDate)}`

  const handleWardChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const val = e.target.value
    if (!val) {
      onWardChange(null, null)
      return
    }
    const ward = WARDS.find((w) => w.id === Number(val))
    if (ward) {
      onWardChange(ward.id, wardBounds(ward.bounds as [number, number][]))
    }
  }

  return (
    <div className="flex flex-wrap items-center gap-4">
      {/* Period label */}
      <span className="text-xs font-medium tracking-widest text-gray-500 uppercase">Period</span>

      {/* Time range buttons */}
      <div className="flex gap-1 rounded-lg bg-muted p-1">
        {TIME_OPTIONS.map((opt) => (
          <button
            key={opt.value}
            id={`time-filter-${opt.value}`}
            onClick={() => onTimeRangeChange(opt.value)}
            className={[
              "rounded-md px-4 py-1.5 text-sm font-medium transition-colors",
              timeRange === opt.value
                ? "bg-primary text-primary-foreground shadow"
                : "text-muted-foreground hover:text-foreground",
            ].join(" ")}
          >
            {opt.label}
          </button>
        ))}
      </div>

      {/* Date range badge */}
      <span className="text-xs font-semibold text-emerald-600 bg-emerald-50 border border-emerald-200 px-3 py-1 rounded-full">
        {rangeLabel}
      </span>

      {/* Ward/Area select — grouped by district, same style as waste-report */}
      <div className="flex items-center gap-2">
        <span className="text-xs font-medium tracking-widest text-gray-500 uppercase">Area</span>
        <select
          id="filter-ward-area"
          value={selectedWardId ?? ""}
          onChange={handleWardChange}
          className="rounded-lg border border-border bg-card px-3 py-1.5 text-sm text-foreground shadow-sm focus:outline-none focus:ring-2 focus:ring-primary/50 transition-colors"
        >
          <option value="">All areas</option>
          {Object.entries(WARDS_BY_DISTRICT).map(([district, wards]) => (
            <optgroup key={district} label={district}>
              {wards.map((w) => (
                <option key={w.id} value={w.id}>
                  {w.name}
                </option>
              ))}
            </optgroup>
          ))}
        </select>
      </div>
    </div>
  )
}
