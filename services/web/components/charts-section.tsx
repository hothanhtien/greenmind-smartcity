"use client"

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Line, LineChart, Bar, BarChart, XAxis, YAxis, CartesianGrid, ResponsiveContainer, Area, AreaChart } from "recharts"
import { ChartContainer, ChartTooltip, ChartTooltipContent } from "@/components/ui/chart"

const revenueData = [
  { month: "Jan", revenue: 4200, forecast: 4000 },
  { month: "Feb", revenue: 3800, forecast: 4200 },
  { month: "Mar", revenue: 5100, forecast: 4800 },
  { month: "Apr", revenue: 4600, forecast: 5200 },
  { month: "May", revenue: 5400, forecast: 5600 },
  { month: "Jun", revenue: 6200, forecast: 6000 },
]

const activityData = [
  { day: "Mon", users: 420, sessions: 680 },
  { day: "Tue", users: 380, sessions: 590 },
  { day: "Wed", users: 510, sessions: 820 },
  { day: "Thu", users: 460, sessions: 740 },
  { day: "Fri", users: 540, sessions: 880 },
  { day: "Sat", users: 320, sessions: 480 },
  { day: "Sun", users: 280, sessions: 420 },
]

export function ChartsSection() {
  return (
    <div className="grid grid-cols-1 lg:grid-cols-5 gap-4 md:gap-6">
      {/* Revenue Trends - Larger card */}
      <Card className="lg:col-span-3 card-float animate-fade-up animate-fade-up-delay-2">
        <CardHeader className="pb-2 md:pb-4">
          <div className="flex items-start justify-between">
            <div>
              <CardTitle className="text-lg md:text-xl font-semibold tracking-tight">
                Revenue Trends
              </CardTitle>
              <p className="text-xs md:text-sm text-muted-foreground mt-1">
                Monthly performance with forecast
              </p>
            </div>
            <div className="flex items-center gap-2">
              <span className="flex items-center gap-1.5 text-xs">
                <span className="h-2 w-2 rounded-full bg-[hsl(var(--chart-1))]" />
                Revenue
              </span>
              <span className="flex items-center gap-1.5 text-xs text-muted-foreground">
                <span className="h-2 w-2 rounded-full bg-[hsl(var(--chart-1))]/30" />
                Forecast
              </span>
            </div>
          </div>
        </CardHeader>
        <CardContent className="pt-2">
          <ChartContainer
            config={{
              revenue: {
                label: "Revenue",
                color: "hsl(var(--chart-1))",
              },
              forecast: {
                label: "Forecast",
                color: "hsl(var(--chart-1))",
              },
            }}
            className="h-[280px] md:h-[320px]"
          >
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={revenueData} margin={{ top: 10, right: 10, left: -10, bottom: 0 }}>
                <defs>
                  <linearGradient id="revenueGradient" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="hsl(var(--chart-1))" stopOpacity={0.3} />
                    <stop offset="95%" stopColor="hsl(var(--chart-1))" stopOpacity={0} />
                  </linearGradient>
                  <linearGradient id="forecastGradient" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="hsl(var(--chart-1))" stopOpacity={0.1} />
                    <stop offset="95%" stopColor="hsl(var(--chart-1))" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" vertical={false} />
                <XAxis
                  dataKey="month"
                  stroke="hsl(var(--muted-foreground))"
                  fontSize={12}
                  tickLine={false}
                  axisLine={false}
                />
                <YAxis
                  stroke="hsl(var(--muted-foreground))"
                  fontSize={12}
                  tickLine={false}
                  axisLine={false}
                  tickFormatter={(value) => `$${value / 1000}k`}
                />
                <ChartTooltip content={<ChartTooltipContent />} />
                <Area
                  type="monotone"
                  dataKey="forecast"
                  stroke="hsl(var(--chart-1))"
                  strokeWidth={1.5}
                  strokeDasharray="5 5"
                  fill="url(#forecastGradient)"
                  dot={false}
                />
                <Area
                  type="monotone"
                  dataKey="revenue"
                  stroke="hsl(var(--chart-1))"
                  strokeWidth={2.5}
                  fill="url(#revenueGradient)"
                  dot={{ fill: "hsl(var(--chart-1))", r: 4, strokeWidth: 0 }}
                  activeDot={{ r: 6, fill: "hsl(var(--chart-1))", strokeWidth: 2, stroke: "hsl(var(--background))" }}
                />
              </AreaChart>
            </ResponsiveContainer>
          </ChartContainer>
        </CardContent>
      </Card>

      {/* User Activity */}
      <Card className="lg:col-span-2 card-float animate-fade-up animate-fade-up-delay-3">
        <CardHeader className="pb-2 md:pb-4">
          <div className="flex items-start justify-between">
            <div>
              <CardTitle className="text-lg md:text-xl font-semibold tracking-tight">
                User Activity
              </CardTitle>
              <p className="text-xs md:text-sm text-muted-foreground mt-1">
                Daily engagement this week
              </p>
            </div>
            <div className="flex items-center gap-2">
              <span className="flex items-center gap-1.5 text-xs">
                <span className="h-2 w-2 rounded-full bg-[hsl(var(--chart-2))]" />
                Users
              </span>
              <span className="flex items-center gap-1.5 text-xs text-muted-foreground">
                <span className="h-2 w-2 rounded-full bg-[hsl(var(--chart-3))]" />
                Sessions
              </span>
            </div>
          </div>
        </CardHeader>
        <CardContent className="pt-2">
          <ChartContainer
            config={{
              users: {
                label: "Users",
                color: "hsl(var(--chart-2))",
              },
              sessions: {
                label: "Sessions",
                color: "hsl(var(--chart-3))",
              },
            }}
            className="h-[280px] md:h-[320px]"
          >
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={activityData} margin={{ top: 10, right: 10, left: -10, bottom: 0 }}>
                <defs>
                  <linearGradient id="usersGradient" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stopColor="hsl(var(--chart-2))" stopOpacity={1} />
                    <stop offset="100%" stopColor="hsl(var(--chart-2))" stopOpacity={0.6} />
                  </linearGradient>
                  <linearGradient id="sessionsGradient" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stopColor="hsl(var(--chart-3))" stopOpacity={0.8} />
                    <stop offset="100%" stopColor="hsl(var(--chart-3))" stopOpacity={0.4} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" vertical={false} />
                <XAxis
                  dataKey="day"
                  stroke="hsl(var(--muted-foreground))"
                  fontSize={12}
                  tickLine={false}
                  axisLine={false}
                />
                <YAxis
                  stroke="hsl(var(--muted-foreground))"
                  fontSize={12}
                  tickLine={false}
                  axisLine={false}
                />
                <ChartTooltip content={<ChartTooltipContent />} />
                <Bar
                  dataKey="sessions"
                  fill="url(#sessionsGradient)"
                  radius={[6, 6, 0, 0]}
                  width={12}
                />
                <Bar
                  dataKey="users"
                  fill="url(#usersGradient)"
                  radius={[6, 6, 0, 0]}
                  width={12}
                />
              </BarChart>
            </ResponsiveContainer>
          </ChartContainer>
        </CardContent>
      </Card>
    </div>
  )
}
