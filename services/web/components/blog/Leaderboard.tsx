"use client"

import { useMemo, useState } from "react"
import { LeaderboardUser } from "@/services/blog.service"

interface Props {
  leaderboard: LeaderboardUser[]
  title?: string
  subtitle?: string
  emptyTitle?: string
  emptySubtitle?: string
  hideAvatar?: boolean
  previewCount?: number
  expandedCount?: number
  showViewAll?: boolean
  autoFlow?: boolean
}

const PODIUM_ORDER = [1, 0, 2]
// pos: 0=left(rank2), 1=center(rank1), 2=right(rank3)
const PODIUM_HEIGHT = ["h-20", "h-28", "h-16"]
const AVATAR_SIZE = ["h-10 w-10", "h-14 w-14", "h-9 w-9"]
const RANK_BG = [
  "bg-slate-100 border-slate-300",   // pos 0: rank 2 (silver)
  "bg-amber-50 border-amber-300",    // pos 1: rank 1 (gold)
  "bg-orange-50 border-orange-300",  // pos 2: rank 3 (bronze)
]
const AVATAR_RING = [
  "ring-2 ring-slate-400",  // pos 0: rank 2
  "ring-4 ring-amber-400",  // pos 1: rank 1
  "ring-2 ring-orange-400", // pos 2: rank 3
]
const LABEL_COLOR = [
  "text-slate-500",           // pos 0: rank 2
  "text-amber-500 font-bold", // pos 1: rank 1
  "text-orange-500",          // pos 2: rank 3
]

function RankRow({
  rank,
  user,
  hideAvatar,
  getLabel,
}: {
  rank: string
  user: LeaderboardUser
  hideAvatar: boolean
  getLabel: (user: LeaderboardUser) => string
}) {
  return (
    <div className="flex items-center gap-3 rounded-xl border border-border/40 bg-muted/20 px-3 py-2">
      <span className="w-5 text-center text-xs font-bold text-slate-400 shrink-0">{rank}</span>
      {!hideAvatar && (
        <div className="h-6 w-6 rounded-full bg-primary/10 flex items-center justify-center text-primary text-[10px] font-bold shrink-0">
          {(user.fullName || user.username).charAt(0).toUpperCase()}
        </div>
      )}
      <div className="flex-1 min-w-0">
        <p className="font-medium text-foreground text-xs whitespace-nowrap truncate">
          {getLabel(user)}
        </p>
      </div>
      <span className="text-xs font-semibold text-emerald-700 shrink-0">
        {user.reportCount}
      </span>
    </div>
  )
}

function formatRank(rank: number) {
  const mod100 = rank % 100
  if (mod100 >= 11 && mod100 <= 13) return `${rank}th`
  switch (rank % 10) {
    case 1:
      return `${rank}st`
    case 2:
      return `${rank}nd`
    case 3:
      return `${rank}rd`
    default:
      return `${rank}th`
  }
}

export function Leaderboard({
  leaderboard,
  title = "Contribution Leaderboard",
  subtitle = "Top reporters this period",
  emptyTitle = "No households yet",
  emptySubtitle = "Start tracking to see rankings",
  hideAvatar = false,
  previewCount = 10,
  expandedCount = 100,
  showViewAll = true,
  autoFlow = false,
}: Props) {
  const [expanded, setExpanded] = useState(false)
  const visibleLimit = expanded ? expandedCount : previewCount
  const visibleLeaderboard = useMemo(
    () => leaderboard.slice(0, Math.min(visibleLimit, leaderboard.length)),
    [leaderboard, visibleLimit],
  )
  const top3 = visibleLeaderboard.slice(0, 3)
  const remaining = visibleLeaderboard.slice(3)
  const hasLeaderboard = leaderboard.length > 0
  // Always place rank1 at center (pos=1) so it gets the tallest podium
  const podiumOrder =
    top3.length >= 3 ? PODIUM_ORDER :
      top3.length === 2 ? [1, 0] :
        [0]

  const formatLocation = (location?: string) => location?.split(",")[0]?.trim() || ""
  const getLabel = (user: LeaderboardUser) => user.fullName || user.username

  return (
    <aside className="flex flex-col gap-4">
      {/* Header */}
      <div className="px-5 pt-3">
        <h3 className="text-base font-bold text-foreground">{title}</h3>
        <p className="text-xs text-muted-foreground mt-0.5">{subtitle}</p>
      </div>

      {hasLeaderboard ? (
        <>
          <div className="rounded-2xl border bg-card p-5 shadow-sm">
            <div className="flex items-end justify-center gap-2">
              {podiumOrder.map((idx, pos) => {
                const user = top3[idx]
                if (!user) return null
                return (
                  <div key={user.userId} className="flex flex-col items-center gap-1 flex-1">
                    {/* Name */}
                    <p className="text-xs font-semibold text-foreground text-center w-full">
                      {getLabel(user)}
                    </p>
                    {user.location && (
                      <p className="text-[10px] text-muted-foreground text-center w-full">
                        {formatLocation(user.location)}
                      </p>
                    )}
                    <p className="text-xs text-muted-foreground">{user.reportCount}</p>
                    {user.rank === 1 && (
                      <span className="text-2xl leading-none">🏆</span>
                    )}
                    {!hideAvatar && (
                      <div
                        className={`${AVATAR_SIZE[pos]} rounded-full bg-linear-to-br from-emerald-400 to-teal-500 ${AVATAR_RING[pos]} flex items-center justify-center text-white font-bold text-sm shrink-0`}
                      >
                        {(user.fullName || user.username).charAt(0).toUpperCase()}
                      </div>
                    )}
                    <div
                      className={`w-full rounded-t-lg border ${PODIUM_HEIGHT[pos]} ${RANK_BG[pos]} flex items-center justify-center`}
                    >
                      <span className={`text-xs font-semibold ${LABEL_COLOR[pos]}`}>
                        {user.rank === 1 ? "1st" : user.rank === 2 ? "2nd" : "3rd"}
                      </span>
                    </div>
                  </div>
                )
              })}
            </div>
            {remaining.length > 0 && (
              <div className={`mt-4 flex flex-col gap-2 overflow-y-auto max-h-64 scrollbar-thin scrollbar-thumb-slate-300 dark:scrollbar-thumb-slate-600 scrollbar-track-transparent`}>
                {remaining.map((user) => (
                  <RankRow
                    key={user.userId}
                    rank={formatRank(user.rank)}
                    user={user}
                    hideAvatar={hideAvatar}
                    getLabel={getLabel}
                  />
                ))}
              </div>
            )}

            {showViewAll && leaderboard.length > previewCount && (
              <button
                type="button"
                onClick={() => setExpanded((value) => !value)}
                className="mt-3 w-full rounded-xl border border-emerald-200 bg-emerald-50 px-3 py-2 text-xs font-semibold text-emerald-700 transition-colors hover:bg-emerald-100"
              >
                {expanded ? "Show less" : "View all"}
              </button>
            )}

          </div>
        </>
      ) : (
        <div className="rounded-xl border border-dashed border-border p-8 text-center">
          <div className="h-12 w-12 mx-auto mb-3 rounded-full bg-muted flex items-center justify-center">
            <svg className="h-6 w-6 text-muted-foreground" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
            </svg>
          </div>
          <p className="text-sm font-medium text-foreground">{emptyTitle}</p>
          <p className="text-xs text-muted-foreground mt-1">{emptySubtitle}</p>
        </div>
      )}
    </aside>
  )
}
