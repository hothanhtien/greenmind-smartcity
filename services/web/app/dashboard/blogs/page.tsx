"use client"

import { useState, useEffect, useCallback, useMemo, useRef } from "react"
import {
  fetchBlogs,
  createBlog,
  updateBlog,
  deleteBlog,
  Blog,
  LeaderboardUser,
} from "@/services/blog.service"
import { getAllHouseholdProfiles } from "@/lib/household"
import { getAccessToken } from "@/lib/auth"
import { Leaderboard } from "@/components/blog/Leaderboard"
import { BlogFeedPost } from "@/components/blog/BlogFeedPost"
import { BlogEditor } from "@/components/blog/BlogEditor"

const PAGE_SIZE = 10

type View = "feed" | "compose"

export default function BlogsPage() {
  const [view, setView] = useState<View>("feed")
  const [blogs, setBlogs] = useState<Blog[]>([])
  const [allHouseholds, setAllHouseholds] = useState<Awaited<ReturnType<typeof getAllHouseholdProfiles>>>([])
  const [loading, setLoading] = useState(true)
  const [loadingMore, setLoadingMore] = useState(false)
  const [search, setSearch] = useState("")
  const [searchInput, setSearchInput] = useState("")
  const [page, setPage] = useState(1)
  const [hasMore, setHasMore] = useState(true)

  // Compose state
  const [editBlog, setEditBlog] = useState<Blog | null>(null)
  const [composeTitle, setComposeTitle] = useState("")
  const [composeContent, setComposeContent] = useState("")
  const [composeTags, setComposeTags] = useState("")
  const [saving, setSaving] = useState(false)
  const [saveMsg, setSaveMsg] = useState<string | null>(null)

  const isLoggedIn = !!getAccessToken()
  const feedRef = useRef<HTMLDivElement>(null)

  // ── Load initial page ──────────────────────────────────────
  const loadPage = useCallback(async (pageNum: number, query: string, replace: boolean) => {
    if (replace) setLoading(true)
    else setLoadingMore(true)

    try {
      const res = await fetchBlogs(pageNum, PAGE_SIZE, query || undefined)
      const newBlogs = res.data
      setBlogs(prev => replace ? newBlogs : [...prev, ...newBlogs])
      setHasMore(pageNum < res.pagination.totalPages)
    } catch {
      // silent
    } finally {
      setLoading(false)
      setLoadingMore(false)
    }
  }, [])

  useEffect(() => {
    setPage(1)
    loadPage(1, search, true)
  }, [search, loadPage])

  const handleLoadMore = () => {
    const nextPage = page + 1
    setPage(nextPage)
    loadPage(nextPage, search, false)
  }

  useEffect(() => {
    getAllHouseholdProfiles()
      .then((profiles) => setAllHouseholds(profiles))
      .catch(() => {})
  }, [])

  // Build leaderboard from households exactly like Household Management page
  const leaderboard = useMemo((): LeaderboardUser[] => {
    return allHouseholds
      .slice()
      .sort((a, b) => (b.greenScore ?? 0) - (a.greenScore ?? 0))
      .map((household, idx) => ({
        rank: idx + 1,
        userId: `household-${household.id}`,
        fullName: household.name.split(",")[0].trim(),
        username: household.name.split(",")[0].trim(),
        reportCount: household.greenScore ?? 0,
      }))
  }, [allHouseholds])

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    setSearch(searchInput)
  }

  const openCompose = (blog?: Blog) => {
    if (blog) {
      setEditBlog(blog)
      setComposeTitle(blog.title)
      setComposeContent(blog.content)
      setComposeTags(blog.tags?.join(", ") ?? "")
    } else {
      setEditBlog(null)
      setComposeTitle("")
      setComposeContent("")
      setComposeTags("")
    }
    setSaveMsg(null)
    setView("compose")
  }

  const handleSave = async () => {
    if (!composeTitle.trim() || !composeContent.trim()) {
      setSaveMsg("Title and content are required.")
      return
    }
    setSaving(true)
    setSaveMsg(null)
    try {
      const tags = composeTags.split(",").map((t) => t.trim()).filter(Boolean)
      const payload = { title: composeTitle.trim(), content: composeContent, tags }

      if (editBlog) {
        await updateBlog(editBlog.id, payload)
        setSaveMsg("Post updated successfully.")
      } else {
        await createBlog(payload)
        setSaveMsg("Post published successfully.")
      }
      setTimeout(() => {
        setView("feed")
        setPage(1)
        loadPage(1, search, true)
      }, 1000)
    } catch {
      setSaveMsg("Failed to save post.")
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async (blogId: string) => {
    if (!confirm("Are you sure you want to delete this post?")) return
    try {
      await deleteBlog(blogId)
      setBlogs(prev => prev.filter(b => b.id !== blogId))
    } catch {
      alert("Failed to delete post.")
    }
  }

  // ── RENDER ────────────────────────────────────────────────
  return (
    <div className="p-6 lg:p-8 max-w-screen-xl mx-auto">

      {/* ═══ FEED VIEW ═══ */}
      {view === "feed" && (
        <>
          {/* Page header */}
          <div className="flex items-start justify-between gap-4 flex-wrap mb-6">
            <div>
              <h1 className="text-3xl font-bold text-foreground mb-1">Community</h1>
              <p className="text-muted-foreground text-sm">Share knowledge about environmental protection</p>
            </div>
            {isLoggedIn && (
              <button
                id="btn-new-blog"
                onClick={() => openCompose()}
                className="rounded-xl bg-emerald-500 hover:bg-emerald-600 px-5 py-2.5 text-sm font-semibold text-white shadow-sm transition-colors"
              >
                New Post
              </button>
            )}
          </div>

          {/* Two-column layout */}
          <div className="flex gap-6 items-start">

            {/* ── LEFT: Leaderboard (sticky) ── */}
            <div className="w-72 shrink-0 sticky top-6">
              <div className="bg-white/70 dark:bg-slate-900/70 backdrop-blur-xl rounded-2xl border border-white/20 dark:border-slate-700/50 shadow-xl overflow-hidden">
                <Leaderboard leaderboard={leaderboard} />
              </div>
            </div>

            {/* ── RIGHT: Search + Feed ── */}
            <div className="flex-1 min-w-0 space-y-5">
              {/* Search */}
              <form onSubmit={handleSearch} className="flex gap-2">
                <input
                  id="blog-search"
                  value={searchInput}
                  onChange={(e) => setSearchInput(e.target.value)}
                  placeholder="Search posts..."
                  className="flex-1 rounded-xl border border-border bg-background px-4 py-2.5 text-sm outline-none focus:ring-2 focus:ring-primary/30 transition"
                />
                <button
                  type="submit"
                  className="rounded-xl border border-emerald-300 bg-emerald-50 px-4 py-2.5 text-sm font-medium text-emerald-700 hover:bg-emerald-100 transition-colors"
                >
                  Search
                </button>
                {search && (
                  <button
                    type="button"
                    onClick={() => { setSearch(""); setSearchInput("") }}
                    className="rounded-xl border px-4 py-2.5 text-sm text-muted-foreground hover:text-foreground transition-colors"
                  >
                    Clear
                  </button>
                )}
              </form>

              {/* Feed skeleton */}
              {loading ? (
                <div className="space-y-4">
                  {Array.from({ length: 4 }).map((_, i) => (
                    <div key={i} className="h-48 rounded-2xl bg-muted animate-pulse" />
                  ))}
                </div>
              ) : blogs.length === 0 ? (
                <div className="flex flex-col items-center justify-center py-24 text-muted-foreground">
                  <p className="text-lg font-semibold">No posts yet</p>
                  <p className="text-sm mt-1">Be the first to share something!</p>
                </div>
              ) : (
                <div ref={feedRef} className="space-y-5">
                  {blogs.map((blog) => (
                    <BlogFeedPost
                      key={blog.id}
                      blog={blog}
                      onEdit={isLoggedIn ? openCompose : undefined}
                      onDelete={isLoggedIn ? handleDelete : undefined}
                    />
                  ))}

                  {/* Load More */}
                  {hasMore && (
                    <div className="flex justify-center pt-2">
                      <button
                        onClick={handleLoadMore}
                        disabled={loadingMore}
                        className="rounded-xl border border-emerald-300 bg-emerald-50 hover:bg-emerald-100 px-6 py-2.5 text-sm font-semibold text-emerald-700 transition-colors disabled:opacity-50"
                      >
                        {loadingMore ? "Loading..." : "Load more posts"}
                      </button>
                    </div>
                  )}

                  {!hasMore && blogs.length > 0 && (
                    <p className="text-center text-xs text-muted-foreground py-4">
                      You&apos;ve reached the end ✓
                    </p>
                  )}
                </div>
              )}
            </div>
          </div>
        </>
      )}

      {/* ═══ COMPOSE VIEW ═══ */}
      {view === "compose" && (
        <div className="max-w-3xl mx-auto space-y-5">
          <div className="flex items-center justify-between gap-4">
            <button
              onClick={() => setView("feed")}
              className="text-sm text-muted-foreground hover:text-foreground transition-colors"
            >
              ← Cancel
            </button>
            <h2 className="text-xl font-bold text-foreground">
              {editBlog ? "Edit Post" : "New Post"}
            </h2>
            <button
              id="btn-save-blog"
              onClick={handleSave}
              disabled={saving}
              className="rounded-xl bg-emerald-500 hover:bg-emerald-600 disabled:opacity-50 px-5 py-2.5 text-sm font-semibold text-white shadow-sm transition-colors"
            >
              {saving ? "Saving..." : editBlog ? "Update" : "Publish"}
            </button>
          </div>

          {saveMsg && (
            <div
              className={`rounded-xl border px-4 py-2.5 text-sm font-medium ${saveMsg.toLowerCase().includes("fail") || saveMsg.toLowerCase().includes("required")
                ? "border-red-200 bg-red-50 text-red-700"
                : "border-emerald-200 bg-emerald-50 text-emerald-700"
                }`}
            >
              {saveMsg}
            </div>
          )}

          <div className="space-y-4 rounded-2xl border bg-card p-6 shadow-sm">
            {/* Title */}
            <div className="space-y-1.5">
              <label className="text-sm font-semibold text-foreground" htmlFor="compose-title">
                Title <span className="text-red-500">*</span>
              </label>
              <input
                id="compose-title"
                value={composeTitle}
                onChange={(e) => setComposeTitle(e.target.value)}
                placeholder="Post title..."
                className="w-full rounded-xl border border-border bg-background px-4 py-2.5 text-sm font-medium outline-none focus:ring-2 focus:ring-primary/30 transition"
              />
            </div>

            {/* Tags */}
            <div className="space-y-1.5">
              <label className="text-sm font-semibold text-foreground" htmlFor="compose-tags">
                Tags <span className="text-muted-foreground font-normal">(comma-separated)</span>
              </label>
              <input
                id="compose-tags"
                value={composeTags}
                onChange={(e) => setComposeTags(e.target.value)}
                placeholder="environment, recycling, sustainability..."
                className="w-full rounded-xl border border-border bg-background px-4 py-2.5 text-sm outline-none focus:ring-2 focus:ring-primary/30 transition"
              />
            </div>

            {/* Content */}
            <div className="space-y-1.5">
              <label className="text-sm font-semibold text-foreground">
                Content <span className="text-red-500">*</span>
              </label>
              <p className="text-xs text-muted-foreground">
                Paste or drag an image directly into the editor to upload it automatically.
              </p>
              <BlogEditor value={composeContent} onChange={setComposeContent} />
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
