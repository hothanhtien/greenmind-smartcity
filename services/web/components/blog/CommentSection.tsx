"use client"

import { useState } from "react"
import {
  addComment,
  updateComment as updateCommentApi,
  deleteComment as deleteCommentApi,
  Blog,
  BlogComment,
} from "@/services/blog.service"
import { getAccessToken } from "@/lib/auth"
import { formatDistanceToNow } from "date-fns"

interface Props {
  blog: Blog
  setBlog: (blog: Blog) => void
}

/** Decode JWT payload to get current userId (no library needed). */
function getCurrentUserId(): string | null {
  const token = getAccessToken()
  if (!token) return null
  try {
    const payload = JSON.parse(atob(token.split(".")[1]))
    return payload.userId ?? payload.sub ?? null
  } catch {
    return null
  }
}

export function CommentSection({ blog, setBlog }: Props) {
  const [commentText, setCommentText] = useState("")
  const [submitting, setSubmitting] = useState(false)
  const [editingId, setEditingId] = useState<string | null>(null)
  const [editText, setEditText] = useState("")
  const [editSaving, setEditSaving] = useState(false)
  const isLoggedIn = !!getAccessToken()
  const currentUserId = getCurrentUserId()

  const comments = blog.comments ?? []

  // ── Add comment ───────────────────────────────────────────
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!commentText.trim() || submitting) return

    setSubmitting(true)
    try {
      const newComment = await addComment(blog.id, commentText.trim())
      setBlog({
        ...blog,
        comment_count: (blog.comment_count ?? 0) + 1,
        comments: [...comments, newComment],
      })
      setCommentText("")
    } catch {
      // silent
    } finally {
      setSubmitting(false)
    }
  }

  // ── Edit comment ──────────────────────────────────────────
  const startEdit = (comment: BlogComment) => {
    setEditingId(comment.id)
    setEditText(comment.content)
  }

  const cancelEdit = () => {
    setEditingId(null)
    setEditText("")
  }

  const handleUpdate = async (commentId: string) => {
    if (!editText.trim() || editSaving) return
    setEditSaving(true)
    try {
      const updated = await updateCommentApi(blog.id, commentId, editText.trim())
      setBlog({
        ...blog,
        comments: comments.map((c) => (c.id === commentId ? updated : c)),
      })
      setEditingId(null)
      setEditText("")
    } catch {
      // silent
    } finally {
      setEditSaving(false)
    }
  }

  // ── Delete comment ────────────────────────────────────────
  const handleDelete = async (commentId: string) => {
    if (!confirm("Delete this comment?")) return
    try {
      await deleteCommentApi(blog.id, commentId)
      setBlog({
        ...blog,
        comment_count: Math.max(0, (blog.comment_count ?? 0) - 1),
        comments: comments.filter((c) => c.id !== commentId),
      })
    } catch {
      // silent
    }
  }

  return (
    <section className="rounded-2xl border bg-card shadow-sm overflow-hidden">
      <div className="p-6 space-y-5">
        <h2 className="text-lg font-bold text-foreground flex items-center gap-2">
          💬 Comments
          <span className="rounded-full bg-muted px-2.5 py-0.5 text-xs font-medium text-muted-foreground">
            {comments.length}
          </span>
        </h2>

        {/* Comment list */}
        {comments.length === 0 ? (
          <p className="text-sm text-muted-foreground py-4 text-center">
            No comments yet. Be the first to share your thoughts!
          </p>
        ) : (
          <div className="space-y-4">
            {comments.map((comment) => {
              const isOwner = currentUserId && comment.user?.id === currentUserId
              const isEditing = editingId === comment.id
              const wasEdited =
                comment.updatedAt &&
                comment.createdAt !== comment.updatedAt &&
                new Date(comment.updatedAt).getTime() - new Date(comment.createdAt).getTime() > 1000

              return (
                <div key={comment.id} className="flex gap-3 group">
                  {/* Avatar */}
                  <div className="w-9 h-9 shrink-0 rounded-full bg-gradient-to-br from-emerald-400 to-teal-500 flex items-center justify-center text-white text-xs font-bold shadow-sm">
                    {(comment.user?.fullName || comment.user?.username || "?")
                      .charAt(0)
                      .toUpperCase()}
                  </div>

                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 mb-1 flex-wrap">
                      <span className="text-sm font-semibold text-foreground">
                        {comment.user?.fullName || comment.user?.username || "Anonymous"}
                      </span>
                      <span className="text-xs text-muted-foreground">
                        {formatDistanceToNow(new Date(comment.createdAt), { addSuffix: true })}
                      </span>
                      {wasEdited && (
                        <span className="text-xs text-muted-foreground italic">(edited)</span>
                      )}

                      {/* Edit / Delete buttons — visible on hover or always on mobile */}
                      {isOwner && !isEditing && (
                        <span className="ml-auto flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                          <button
                            onClick={() => startEdit(comment)}
                            className="rounded-md px-2 py-0.5 text-xs text-muted-foreground hover:text-foreground hover:bg-muted transition-colors"
                          >
                            Edit
                          </button>
                          <button
                            onClick={() => handleDelete(comment.id)}
                            className="rounded-md px-2 py-0.5 text-xs text-red-500 hover:text-red-600 hover:bg-red-50 transition-colors"
                          >
                            Delete
                          </button>
                        </span>
                      )}
                    </div>

                    {isEditing ? (
                      <div className="space-y-2">
                        <textarea
                          value={editText}
                          onChange={(e) => setEditText(e.target.value)}
                          rows={2}
                          className="w-full rounded-xl border border-border bg-background px-4 py-2.5 text-sm outline-none focus:ring-2 focus:ring-primary/30 transition resize-none"
                        />
                        <div className="flex gap-2 justify-end">
                          <button
                            onClick={cancelEdit}
                            className="rounded-lg border px-3 py-1.5 text-xs text-muted-foreground hover:text-foreground transition-colors"
                          >
                            Cancel
                          </button>
                          <button
                            onClick={() => handleUpdate(comment.id)}
                            disabled={editSaving || !editText.trim()}
                            className="rounded-lg bg-emerald-500 hover:bg-emerald-600 disabled:opacity-50 px-3 py-1.5 text-xs font-semibold text-white transition-colors"
                          >
                            {editSaving ? "Saving..." : "Save"}
                          </button>
                        </div>
                      </div>
                    ) : (
                      <p className="text-sm text-foreground/90 leading-relaxed whitespace-pre-wrap break-words">
                        {comment.content}
                      </p>
                    )}
                  </div>
                </div>
              )
            })}
          </div>
        )}

        {/* Comment form */}
        {isLoggedIn ? (
          <form onSubmit={handleSubmit} className="flex gap-3 pt-2 border-t border-border/50">
            <div className="w-9 h-9 shrink-0 rounded-full bg-gradient-to-br from-emerald-400 to-teal-500 flex items-center justify-center text-white text-xs font-bold shadow-sm">
              You
            </div>
            <div className="flex-1 space-y-2">
              <textarea
                id="comment-input"
                value={commentText}
                onChange={(e) => setCommentText(e.target.value)}
                placeholder="Write a comment..."
                rows={2}
                className="w-full rounded-xl border border-border bg-background px-4 py-2.5 text-sm outline-none focus:ring-2 focus:ring-primary/30 transition resize-none"
              />
              <div className="flex justify-end">
                <button
                  id="btn-submit-comment"
                  type="submit"
                  disabled={submitting || !commentText.trim()}
                  className="rounded-xl bg-emerald-500 hover:bg-emerald-600 disabled:opacity-50 px-4 py-2 text-xs font-semibold text-white shadow-sm transition-colors"
                >
                  {submitting ? "Posting..." : "Post Comment"}
                </button>
              </div>
            </div>
          </form>
        ) : (
          <p className="text-sm text-muted-foreground text-center pt-2 border-t border-border/50">
            Please log in to leave a comment.
          </p>
        )}
      </div>
    </section>
  )
}
