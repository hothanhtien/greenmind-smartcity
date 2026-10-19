"use client"

import { useEffect, useState } from "react"
import Link from "next/link"
import { Users, DollarSign, Activity, ArrowUpRight, MoreHorizontal, Plus, FileText, BarChart3, UserCog, RefreshCw } from "lucide-react"
import { dashboardService, Stats } from "@/services/dashboard.service"
import { getAllHouseholdProfiles } from "@/lib/household"
import { Leaderboard } from "@/components/blog/Leaderboard"
import type { LeaderboardUser } from "@/services/blog.service"
import type { HouseholdProfile } from "@/types/monitoring"

function formatTimeAgo(dateString: string) {
  try {
    const date = new Date(dateString)
    const now = new Date()
    const diffMs = now.getTime() - date.getTime()
    const minutes = Math.floor(diffMs / 60000)
    if (minutes < 60) return `${minutes}m ago`
    const hours = Math.floor(minutes / 60)
    if (hours < 24) return `${hours}h ago`
    const days = Math.floor(hours / 24)
    return `${days}d ago`
  } catch {
    return "Just now"
  }
}


export default function DashboardPage() {
  const [stats, setStats] = useState<Stats | null>(null)
  const [householdLeaderboard, setHouseholdLeaderboard] = useState<HouseholdProfile[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [refreshing, setRefreshing] = useState(false)

  const fetchData = async () => {
    setLoading(true)
    setError(null)

    try {
      const [overview, households] = await Promise.all([
        dashboardService.getOverview(),
        getAllHouseholdProfiles(),
      ])

      const sortedHouseholds = households
        .slice()
        .sort((a, b) => {
          const scoreA = a.greenScore != null ? a.greenScore : a.reportCount
          const scoreB = b.greenScore != null ? b.greenScore : b.reportCount
          return scoreB - scoreA
        })

      setStats(overview)
      setHouseholdLeaderboard(sortedHouseholds.slice(0, 100))
    } catch (err: any) {
      console.error("Dashboard fetch error:", err)
      setError(err?.message || "Failed to load dashboard data")
    } finally {
      setLoading(false)
    }
  }

  const handleRefresh = async () => {
    setRefreshing(true)
    await fetchData()
    setRefreshing(false)
  }

  useEffect(() => {
    fetchData()
  }, [])

  const totalUsers = stats?.totalHouseholds ?? 0
  const totalWaste = stats?.totalWaste ?? 0
  const totalActivities = stats?.total_activities ?? 0
  const avgPlastic = stats?.avgPlastic ?? 0
  const totalAreas = stats?.total_areas ?? stats?.areaCount ?? 0
  const dashboardLeaderboard: LeaderboardUser[] = householdLeaderboard.map((household, idx) => {
    // Get the member who joined earliest (household head)
    let headName = household.name;
    const rawMembers = (household as any)._members;
    if (rawMembers && rawMembers.length > 0) {
      const sortedRaw = [...rawMembers].sort((a: any, b: any) =>
        new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
      );
      headName = sortedRaw[0]?.fullName || sortedRaw[0]?.username || household.name;
    } else if (household.members && household.members.length > 0) {
      headName = household.members[0]?.name || household.name;
    }

    return {
      rank: idx + 1,
      userId: `household-${household.id}`,
      fullName: headName,
      username: headName,
      location: household.name,
      reportCount: household.greenScore != null ? household.greenScore : household.reportCount,
    }
  })

  return (
    <div className="relative min-h-screen overflow-hidden">
      {error && (
        <div className="mx-6 md:mx-10 lg:mx-14 mt-6 p-4 bg-rose-500/10 border border-rose-500/20 text-rose-600 dark:text-rose-400 rounded-xl flex items-center justify-between">
          <span>Error: {error}</span>
          <button
            onClick={handleRefresh}
            className="flex items-center gap-2 text-sm font-medium hover:underline"
          >
            <RefreshCw className="h-4 w-4" />
            Retry
          </button>
        </div>
      )}

      {/* Hero Section */}
      <section className="relative px-6 md:px-10 lg:px-14 pt-8 md:pt-12 pb-16 md:pb-24">
        {/* Decorative Background Blob */}
        <div className="absolute top-0 right-0 w-150 h-100 -translate-y-1/2 translate-x-1/4">
          <svg viewBox="0 0 800 600" fill="none" className="w-full h-full opacity-[0.07]">
            <path
              d="M400 50C550 50 700 150 700 300C700 450 550 550 400 550C250 550 100 450 100 300C100 150 250 50 400 50Z"
              fill="url(#blob-gradient)"
            />
            <defs>
              <linearGradient id="blob-gradient" x1="100" y1="50" x2="700" y2="550" gradientUnits="userSpaceOnUse">
                <stop stopColor="#22c55e" />
                <stop offset="1" stopColor="#0d9488" />
              </linearGradient>
            </defs>
          </svg>
        </div>

        <div className="relative max-w-7xl mx-auto">
          <div className="grid lg:grid-cols-12 gap-12 lg:gap-8 items-end">
            {/* Left Content */}
            <div className="lg:col-span-7 space-y-6">
              <div className="inline-flex items-center gap-2">
                <span className="h-px w-12 bg-linear-to-r from-transparent to-emerald-500" />
                <span className="text-xs uppercase tracking-[0.2em] text-emerald-600 font-medium">
                  Dashboard
                </span>
              </div>

              <h1 className="text-5xl md:text-7xl lg:text-8xl font-bold leading-[0.9] tracking-tight gap-4 flex">
                <span className="block text-foreground">Green</span>
                <span className="block bg-linear-to-r from-emerald-500 via-teal-500 to-cyan-500 bg-clip-text text-transparent">
                  Mind
                </span>
              </h1>

              <p className="text-lg md:text-xl text-muted-foreground max-w-lg leading-relaxed">
                Smart management platform for green communities. Track and develop your sustainable ecosystem.
              </p>

              <div className="flex flex-wrap gap-3 pt-4">
                <Link
                  href="/dashboard/environmental-impact"
                  className="group inline-flex items-center gap-2 px-5 py-2.5 bg-foreground text-background rounded-full font-medium text-sm transition-all hover:gap-3"
                >
                  View Details
                  <ArrowUpRight className="h-4 w-4 transition-transform group-hover:translate-x-0.5 group-hover:-translate-y-0.5" />
                </Link>
                <Link
                  href="/dashboard/waste-report"
                  className="inline-flex items-center gap-2 px-5 py-2.5 border border-border rounded-full font-medium text-sm hover:bg-muted/50 transition-colors"
                >
                  New Report
                </Link>
              </div>
            </div>

            {/* Right Content - Stats */}
            <div className="lg:col-span-5 grid grid-cols-3 gap-3 md:gap-4">
              {/* Households - Main */}
              <Link
                href="/dashboard/household-management"
                className="col-span-3 md:col-span-1 relative p-4 bg-linear-to-br from-emerald-500 to-teal-600 rounded-2xl text-white shadow-lg shadow-emerald-500/15 hover:shadow-emerald-500/25 transition-shadow cursor-pointer"
              >
                <div className="flex items-center gap-2 mb-2">
                  <Users className="h-4 w-4 opacity-80" />
                  <span className="text-[10px] font-medium text-white/70">Households</span>
                </div>
                <div className="text-4xl md:text-5xl font-bold tracking-tighter leading-none">
                  {loading ? "--" : totalUsers.toLocaleString()}
                </div>
              </Link>

              {/* Waste */}
              <Link
                href="/dashboard/waste-report"
                className="relative p-4 bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 shadow-sm hover:shadow-md transition-shadow cursor-pointer"
              >
                <div className="flex items-center justify-between mb-2">
                  <DollarSign className="h-4 w-4 text-amber-500" />
                  <span className="text-[9px] font-medium px-1.5 py-0.5 bg-amber-100 dark:bg-amber-900/30 text-amber-600 dark:text-amber-400 rounded">
                    {loading ? "..." : avgPlastic > 50 ? "Needs work" : "Good"}
                  </span>
                </div>
                <div className="text-2xl md:text-3xl font-bold text-slate-900 dark:text-white tracking-tight">
                  {loading ? "--" : totalWaste}
                </div>
                <p className="text-[10px] text-slate-500 mt-1">Total waste report</p>
              </Link>

              {/* Activities */}
              <Link
                href="/dashboard/campaign-management"
                className="relative p-4 bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 shadow-sm hover:shadow-md transition-shadow cursor-pointer"
              >
                <div className="flex items-center justify-between mb-2">
                  <Activity className="h-4 w-4 text-violet-500" />
                  <span className="w-1.5 h-1.5 rounded-full bg-violet-500 animate-pulse" />
                </div>
                <div className="text-2xl md:text-3xl font-bold text-slate-900 dark:text-white tracking-tight">
                  {loading ? "--" : totalActivities.toLocaleString()}
                </div>
                <p className="text-[10px] text-slate-500 mt-1">Activities</p>
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* Wave Divider */}
      <div className="relative h-16 -mt-8 md:-mt-12">
        <svg viewBox="0 0 1440 64" fill="none" className="w-full h-full" preserveAspectRatio="none">
          <path
            d="M0 64V32C240 0 480 0 720 16C960 32 1200 48 1440 32V64H0Z"
            fill="hsl(var(--background))"
          />
        </svg>
      </div>

      {/* Content Section */}
      <section className="px-6 md:px-10 lg:px-14 pb-16">
        <div className="max-w-7xl mx-auto">
          <div className="grid lg:grid-cols-12 gap-8 lg:gap-12">
            {/* Left Column */}
            <div className="lg:col-span-8 space-y-8">
              <div className="flex items-end justify-between">
                <div>
                  <h2 className="text-2xl md:text-3xl font-bold tracking-tight">System Performance</h2>
                </div>
                <div className="flex items-center gap-3">
                  <button
                    onClick={handleRefresh}
                    disabled={refreshing}
                    className="flex items-center gap-2 px-3 py-1.5 text-sm text-muted-foreground hover:text-foreground hover:bg-muted/50 rounded-lg transition-colors disabled:opacity-50"
                  >
                    <RefreshCw className={`h-4 w-4 ${refreshing ? "animate-spin" : ""}`} />
                  </button>
                </div>
              </div>

              {/* Chart Card */}
              <div className="relative bg-card rounded-2xl overflow-hidden border border-border/50 shadow-sm">
                <div className="h-1 w-full bg-linear-to-r from-emerald-500 via-teal-500 to-cyan-500" />
                <div className="p-5">
                  {/* Mini Stats Row */}
                  <div className="grid grid-cols-2 gap-3 mb-6">
                    {/* Areas */}
                    <Link
                      href="/dashboard/environmental-impact"
                      className="p-3 bg-emerald-50/70 dark:bg-emerald-950/30 rounded-xl hover:bg-emerald-100/70 dark:hover:bg-emerald-900/40 transition-colors cursor-pointer"
                    >
                      <p className="text-[10px] text-emerald-600 dark:text-emerald-400 mb-1">Areas</p>
                      <p className="text-xl font-bold text-emerald-700 dark:text-emerald-300">{loading ? "--" : totalAreas}</p>
                    </Link>
                  </div>

                  {/* Chart */}
                  <div className="relative h-40 md:h-56">
                    <svg viewBox="0 0 800 200" className="w-full h-full" preserveAspectRatio="none">
                      <defs>
                        <linearGradient id="areaGradient" x1="0" y1="0" x2="0" y2="1">
                          <stop offset="0%" stopColor="#22c55e" stopOpacity="0.3" />
                          <stop offset="100%" stopColor="#22c55e" stopOpacity="0" />
                        </linearGradient>
                      </defs>
                      <line x1="0" y1="50" x2="800" y2="50" stroke="hsl(var(--border))" strokeDasharray="4" />
                      <line x1="0" y1="100" x2="800" y2="100" stroke="hsl(var(--border))" strokeDasharray="4" />
                      <line x1="0" y1="150" x2="800" y2="150" stroke="hsl(var(--border))" strokeDasharray="4" />
                      <path
                        d="M0 180 Q100 160 200 140 T400 100 T600 120 T800 60 L800 200 L0 200 Z"
                        fill="url(#areaGradient)"
                      />
                      <path
                        d="M0 180 Q100 160 200 140 T400 100 T600 120 T800 60"
                        fill="none"
                        stroke="#22c55e"
                        strokeWidth="3"
                        strokeLinecap="round"
                      />
                      <circle cx="200" cy="140" r="6" fill="#22c55e" />
                      <circle cx="400" cy="100" r="6" fill="#22c55e" />
                      <circle cx="600" cy="120" r="6" fill="#22c55e" />
                      <circle cx="800" cy="60" r="8" fill="#22c55e" stroke="white" strokeWidth="3" />
                      <text x="200" y="185" textAnchor="middle" fill="hsl(var(--muted-foreground))" fontSize="12">Week 1</text>
                      <text x="400" y="185" textAnchor="middle" fill="hsl(var(--muted-foreground))" fontSize="12">Week 2</text>
                      <text x="600" y="185" textAnchor="middle" fill="hsl(var(--muted-foreground))" fontSize="12">Week 3</text>
                      <text x="800" y="185" textAnchor="middle" fill="hsl(var(--muted-foreground))" fontSize="12">Week 4</text>
                    </svg>
                  </div>
                </div>
              </div>

              {/* Quick Actions */}
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                <Link
                  href="/dashboard/campaign-management"
                  className="group relative p-5 rounded-2xl text-left transition-all duration-300 hover:scale-[1.02] hover:-translate-y-1 bg-linear-to-br from-emerald-100 to-emerald-50 dark:from-emerald-950/40 dark:to-emerald-900/20 hover:shadow-lg hover:shadow-emerald-500/10"
                >
                  <Plus className="h-5 w-5 text-emerald-600 mb-3" />
                  <span className="text-sm font-semibold text-emerald-700 dark:text-emerald-300">Create Campaign</span>
                  <ArrowUpRight className="absolute top-4 right-4 h-4 w-4 text-emerald-400 opacity-0 group-hover:opacity-100 transition-opacity" />
                </Link>

                <Link
                  href="/dashboard/questions"
                  className="group relative p-5 rounded-2xl text-left transition-all duration-300 hover:scale-[1.02] hover:-translate-y-1 bg-linear-to-br from-teal-100 to-teal-50 dark:from-teal-950/40 dark:to-teal-900/20 hover:shadow-lg hover:shadow-teal-500/10"
                >
                  <FileText className="h-5 w-5 text-teal-600 mb-3" />
                  <span className="text-sm font-semibold text-teal-700 dark:text-teal-300">Add Question</span>
                  <ArrowUpRight className="absolute top-4 right-4 h-4 w-4 text-teal-400 opacity-0 group-hover:opacity-100 transition-opacity" />
                </Link>

                <Link
                  href="/dashboard/waste-report"
                  className="group relative p-5 rounded-2xl text-left transition-all duration-300 hover:scale-[1.02] hover:-translate-y-1 bg-linear-to-br from-cyan-100 to-cyan-50 dark:from-cyan-950/40 dark:to-cyan-900/20 hover:shadow-lg hover:shadow-cyan-500/10"
                >
                  <BarChart3 className="h-5 w-5 text-cyan-600 mb-3" />
                  <span className="text-sm font-semibold text-cyan-700 dark:text-cyan-300">View Reports</span>
                  <ArrowUpRight className="absolute top-4 right-4 h-4 w-4 text-cyan-400 opacity-0 group-hover:opacity-100 transition-opacity" />
                </Link>

                <Link
                  href="/dashboard/users-ocean"
                  className="group relative p-5 rounded-2xl text-left transition-all duration-300 hover:scale-[1.02] hover:-translate-y-1 bg-linear-to-br from-violet-100 to-violet-50 dark:from-violet-950/40 dark:to-violet-900/20 hover:shadow-lg hover:shadow-violet-500/10"
                >
                  <UserCog className="h-5 w-5 text-violet-600 mb-3" />
                  <span className="text-sm font-semibold text-violet-700 dark:text-violet-300">Manage Users</span>
                  <ArrowUpRight className="absolute top-4 right-4 h-4 w-4 text-violet-400 opacity-0 group-hover:opacity-100 transition-opacity" />
                </Link>
              </div>
            </div>

            <div className="lg:col-span-4">
              <div className="sticky top-24 h-full">
                <div className="h-full bg-white/70 dark:bg-slate-900/70 backdrop-blur-xl rounded-2xl border border-white/20 dark:border-slate-700/50 shadow-xl overflow-hidden">
                  <Leaderboard
                    leaderboard={dashboardLeaderboard}
                    title="Top Households"
                    subtitle="Ranked by green score"
                    emptyTitle="No households yet"
                    emptySubtitle="Start tracking scores to see rankings"
                    hideAvatar
                    previewCount={10}
                    expandedCount={100}
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  )
}
