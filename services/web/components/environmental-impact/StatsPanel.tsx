import type { ImpactData, PollutionData } from "@/types/environmental"

interface StatCardProps {
  id: string
  label: string
  value: number
  accent: string
}

function StatCard({ id, label, value, accent }: StatCardProps) {
  return (
    <div
      id={id}
      className="flex flex-col gap-1 rounded-xl border bg-card p-5 shadow-sm"
    >
      <span className="text-xs font-medium tracking-widest text-muted-foreground uppercase">{label}</span>
      <span className={`text-3xl font-bold tabular-nums ${accent}`}>{value.toLocaleString()}</span>
    </div>
  )
}

interface Props {
  pollution: PollutionData
  impact: ImpactData
}

function computeTotalPollution(pollution: PollutionData): number {
  return parseFloat(Object.values(pollution).reduce((acc, v) => acc + v, 0).toFixed(4))
}

export function StatsPanel({ pollution, impact }: Props) {
  const totalPollution = computeTotalPollution(pollution)
  const totalCo2 = pollution.CO2

  const stats: StatCardProps[] = [
    { id: "stat-co2",    label: "Total CO₂",      value: totalCo2,       accent: "text-emerald-400" },
    { id: "stat-total",  label: "Total Pollution", value: totalPollution, accent: "text-indigo-400"  },
    { id: "stat-air",    label: "Air Impact",      value: impact.air,     accent: "text-gray-300"    },
    { id: "stat-water",  label: "Water Impact",    value: impact.water,   accent: "text-orange-400"  },
    { id: "stat-soil",   label: "Soil Impact",     value: impact.soil,    accent: "text-red-400"     },
  ]

  return (
    <div className="grid grid-cols-2 gap-3 sm:grid-cols-5">
      {stats.map((s) => (
        <StatCard key={s.id} {...s} />
      ))}
    </div>
  )
}
