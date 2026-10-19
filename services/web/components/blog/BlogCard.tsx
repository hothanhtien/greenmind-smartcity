"use client"

import { useState, useCallback } from "react"
import { Blog, toggleLike } from "@/services/blog.service"
import { getAccessToken } from "@/lib/auth"
import { formatDistanceToNow } from "date-fns"

interface Props {
  blog: Blog
  onClick: (blog: Blog) => void
}

export function BlogCard({ blog, onClick }: Props) {
  const [liked, setLiked] = useState(blog.isLiked ?? false)
  const [likeCount, setLikeCount] = useState(blog.like_count)
  const [liking, setLiking] = useState(false)

  const handleLike = useCallback(
    async (e: React.MouseEvent) => {
      e.stopPropagation()
      if (!getAccessToken()) return
      if (liking) return
      setLiking(true)
      try {
        const res = await toggleLike(blog.id)
        setLiked(res.liked)
        setLikeCount(res.like_count)
      } catch {
        // silent
      } finally {
        setLiking(false)
      }
    },
    [blog.id, liking]
  )

  // Strip HTML tags for plain-text preview (content is not in the list response)
  const preview = blog.content ? blog.content.replace(/<[^>]*>/g, "").slice(0, 180) : ""

  return (
    <article
      onClick={() => onClick(blog)}
      className="group relative flex flex-col rounded-2xl border bg-card shadow-sm hover:shadow-md transition-all duration-200 cursor-pointer overflow-hidden hover:-translate-y-0.5"
    >
      {/* Top accent bar */}
      <div className="h-1 w-full bg-gradient-to-r from-emerald-400 to-teal-500 opacity-0 group-hover:opacity-100 transition-opacity" />

      <div className="flex flex-col gap-3 p-5 flex-1">
        {/* Tags */}
        {blog.tags && blog.tags.length > 0 && (
          <div className="flex flex-wrap gap-1.5">
            {blog.tags.slice(0, 3).map((tag) => (
              <span
                key={tag}
                className="rounded-full bg-emerald-50 border border-emerald-200 px-2.5 py-0.5 text-xs font-medium text-emerald-700"
              >
                #{tag}
              </span>
            ))}
          </div>
        )}

        {/* Title */}
        <h3 className="font-bold text-base text-foreground leading-snug line-clamp-2 group-hover:text-emerald-600 transition-colors">
          {blog.title}
        </h3>

        {/* Preview text */}
        <p className="text-sm text-muted-foreground line-clamp-3 flex-1">{preview}…</p>
      </div>

      {/* Footer */}
      <div className="flex items-center justify-between border-t border-border/50 px-5 py-3 bg-muted/20">
        <div className="flex items-center gap-3 text-xs text-muted-foreground">
          <span>{blog.author?.fullName || blog.author?.username || "Anonymous"}</span>
          <span>·</span>
          <span>{formatDistanceToNow(new Date(blog.createdAt), { addSuffix: true })}</span>
        </div>

        {/* Comment count */}
        <span className="text-xs text-muted-foreground flex items-center gap-1">
          💬 {blog.comment_count ?? 0}
        </span>

        {/* Like button */}
        <button
          onClick={handleLike}
          aria-label="Like"
          className={`rounded-full px-3 py-1.5 text-xs font-medium transition-all ${liked
              ? "bg-rose-50 text-rose-500 border border-rose-200"
              : "text-muted-foreground hover:text-rose-500 hover:bg-rose-50"
            }`}
        >
          {liked ? "♥" : "♡"} {likeCount}
        </button>
      </div>
    </article>
  )
}
