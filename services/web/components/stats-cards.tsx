import { Card, CardContent } from "@/components/ui/card"
import { Users, DollarSign, Activity, TrendingUp } from "lucide-react"
import { cn } from "@/lib/utils"

const stats = [
  {
    title: "Total Users",
    value: "12,543",
    change: "+12.5%",
    trend: "up",
    icon: Users,
    accent: "from-emerald-400/20 to-teal-400/20",
  },
  {
    title: "Revenue",
    value: "$45,231",
    change: "+8.2%",
    trend: "up",
    icon: DollarSign,
    accent: "from-amber-400/20 to-orange-400/20",
  },
  {
    title: "Active Sessions",
    value: "2,345",
    change: "-3.1%",
    trend: "down",
    icon: Activity,
    accent: "from-rose-400/20 to-pink-400/20",
  },
  {
    title: "Conversion Rate",
    value: "3.24%",
    change: "+0.5%",
    trend: "up",
    icon: TrendingUp,
    accent: "from-violet-400/20 to-purple-400/20",
  },
]

export function StatsCards() {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4 md:gap-6">
      {stats.map((stat, index) => {
        const Icon = stat.icon
        return (
          <Card
            key={stat.title}
            className={cn(
              "stat-card card-float group animate-fade-up",
              `animate-fade-up-delay-${index + 1}`
            )}
          >
            <CardContent className="p-5 md:p-6">
              {/* Gradient accent background */}
              <div
                className={cn(
                  "absolute inset-0 bg-gradient-to-br opacity-0 group-hover:opacity-100 transition-opacity duration-500 rounded-2xl",
                  stat.accent
                )}
              />

              {/* Content */}
              <div className="relative flex flex-col gap-4">
                {/* Header row */}
                <div className="flex items-start justify-between">
                  <div className="flex h-11 w-11 md:h-12 md:w-12 items-center justify-center rounded-xl bg-gradient-to-br from-primary/10 to-primary/5 border border-primary/10 group-hover:border-primary/20 transition-colors">
                    <Icon className="h-5 w-5 md:h-5.5 md:w-5.5 text-primary" />
                  </div>

                  <span
                    className={cn(
                      "trend-badge shrink-0",
                      stat.trend === "up" ? "trend-up" : "trend-down"
                    )}
                  >
                    {stat.change}
                  </span>
                </div>

                {/* Stats content */}
                <div className="space-y-1">
                  <p className="text-sm font-medium text-muted-foreground">
                    {stat.title}
                  </p>
                  <p className="text-2xl md:text-3xl font-bold tracking-tight">
                    <span className="stat-number">{stat.value}</span>
                  </p>
                </div>

                {/* Decorative element */}
                <div className="absolute -bottom-4 -right-4 h-16 w-16 rounded-full bg-gradient-to-br from-primary/5 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-700" />
              </div>
            </CardContent>
          </Card>
        )
      })}
    </div>
  )
}
