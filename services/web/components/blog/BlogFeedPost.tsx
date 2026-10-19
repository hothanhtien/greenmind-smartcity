"use client"

import { useState, useCallback } from "react"
import { Blog, toggleLike, fetchBlogById } from "@/services/blog.service"
import { getAccessToken } from "@/lib/auth"
import { formatDistanceToNow } from "date-fns"
import { CommentSection } from "@/components/blog/CommentSection"

interface Props {
    blog: Blog
    onEdit?: (blog: Blog) => void
    onDelete?: (blogId: string) => void
}

export function BlogFeedPost({ blog: initialBlog, onEdit, onDelete }: Props) {
    const [blog, setBlog] = useState<Blog>(initialBlog)
    const [liked, setLiked] = useState(initialBlog.isLiked ?? false)
    const [likeCount, setLikeCount] = useState(initialBlog.like_count)
    const [liking, setLiking] = useState(false)
    const [showComments, setShowComments] = useState(false)
    const [loadingComments, setLoadingComments] = useState(false)
    const [commentsLoaded, setCommentsLoaded] = useState(false)
    const [expanded, setExpanded] = useState(false)

    const isLoggedIn = !!getAccessToken()

    const handleLike = useCallback(async () => {
        if (!isLoggedIn || liking) return
        setLiking(true)
        try {
            const res = await toggleLike(blog.id)
            setLiked(res.liked)
            setLikeCount(res.like_count)
            setBlog(prev => ({ ...prev, like_count: res.like_count, isLiked: res.liked }))
        } catch {
            // silent
        } finally {
            setLiking(false)
        }
    }, [blog.id, liking, isLoggedIn])

    const handleToggleComments = async () => {
        if (!showComments && !commentsLoaded) {
            setLoadingComments(true)
            try {
                const full = await fetchBlogById(blog.id)
                setBlog(full)
                setCommentsLoaded(true)
            } catch {
                // continue with existing blog data
            } finally {
                setLoadingComments(false)
            }
        }
        setShowComments(v => !v)
    }

    // Strip HTML for preview
    const plainText = blog.content ? blog.content.replace(/<[^>]*>/g, "") : ""
    const isLong = plainText.length > 300
    const preview = isLong && !expanded ? plainText.slice(0, 300) + "…" : plainText

    // Detect if content is HTML (has tags)
    const isHtml = /<[a-z][\s\S]*>/i.test(blog.content ?? "")

    return (
        <article className="bg-card border border-border rounded-2xl shadow-sm overflow-hidden transition-shadow hover:shadow-md">
            {/* Top accent */}
            <div className="h-0.5 w-full bg-gradient-to-r from-emerald-400 to-teal-500" />

            <div className="p-5 space-y-4">
                {/* Header */}
                <div className="flex items-start justify-between gap-3">
                    <div className="flex items-center gap-3">
                        {/* Avatar */}
                        <div className="w-10 h-10 shrink-0 rounded-full bg-gradient-to-br from-emerald-400 to-teal-500 flex items-center justify-center text-white text-sm font-bold shadow-sm select-none">
                            {(blog.author?.fullName || blog.author?.username || "?").charAt(0).toUpperCase()}
                        </div>
                        <div>
                            <p className="text-sm font-semibold text-foreground leading-tight">
                                {blog.author?.fullName || blog.author?.username || "Anonymous"}
                            </p>
                            <p className="text-xs text-muted-foreground">
                                {formatDistanceToNow(new Date(blog.createdAt), { addSuffix: true })}
                            </p>
                        </div>
                    </div>

                    {/* Tags */}
                    {blog.tags && blog.tags.length > 0 && (
                        <div className="flex flex-wrap gap-1.5 justify-end">
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
                </div>

                {/* Title */}
                <h2 className="text-base font-bold text-foreground leading-snug">{blog.title}</h2>

                {/* Content */}
                <div className="text-sm text-foreground/80 leading-relaxed">
                    {isHtml && !expanded ? (
                        <>
                            <p className="whitespace-pre-wrap break-words">{preview}</p>
                        </>
                    ) : isHtml ? (
                        <div
                            className="prose prose-sm max-w-none
                [&_img]:max-w-full [&_img]:rounded-xl [&_img]:my-3 [&_img]:shadow-sm
                [&_h2]:text-base [&_h2]:font-bold [&_h2]:mt-4 [&_h2]:mb-2
                [&_h3]:text-sm [&_h3]:font-semibold [&_h3]:mt-3 [&_h3]:mb-1
                [&_p]:mb-2 [&_ul]:list-disc [&_ul]:pl-5 [&_ol]:list-decimal [&_ol]:pl-5
                [&_a]:text-emerald-600 [&_a]:underline"
                            dangerouslySetInnerHTML={{ __html: blog.content }}
                        />
                    ) : (
                        <p className="whitespace-pre-wrap break-words">{preview}</p>
                    )}

                    {isLong && (
                        <button
                            onClick={() => setExpanded(v => !v)}
                            className="mt-1 text-xs font-semibold text-emerald-600 hover:text-emerald-700 transition-colors"
                        >
                            {expanded ? "Show less" : "See more"}
                        </button>
                    )}
                </div>
            </div>

            {/* Action bar */}
            <div className="flex items-center gap-1 px-5 py-3 border-t border-border/50 bg-muted/20">
                {/* Like */}
                <button
                    onClick={handleLike}
                    disabled={!isLoggedIn || liking}
                    aria-label="Like"
                    className={`flex items-center gap-1.5 rounded-xl px-3 py-2 text-sm font-medium transition-all ${liked
                            ? "bg-rose-50 text-rose-500 border border-rose-200"
                            : "text-muted-foreground hover:text-rose-500 hover:bg-rose-50 border border-transparent"
                        } disabled:opacity-50`}
                >
                    <span>{liked ? "♥" : "♡"}</span>
                    <span>{likeCount}</span>
                </button>

                {/* Comments toggle */}
                <button
                    onClick={handleToggleComments}
                    className="flex items-center gap-1.5 rounded-xl px-3 py-2 text-sm font-medium text-muted-foreground hover:text-foreground hover:bg-muted border border-transparent transition-all"
                >
                    {loadingComments ? (
                        <span className="animate-spin text-xs">↻</span>
                    ) : (
                        <span>💬</span>
                    )}
                    <span className="text-xs">{showComments ? "Ẩn bình luận" : "Bình luận"}</span>
                </button>

                {/* Edit / Delete */}
                {isLoggedIn && onEdit && (
                    <div className="ml-auto flex items-center gap-1">
                        <button
                            onClick={() => onEdit(blog)}
                            className="rounded-lg border px-3 py-1.5 text-xs text-muted-foreground hover:text-foreground hover:bg-muted transition-colors"
                        >
                            Edit
                        </button>
                        {onDelete && (
                            <button
                                onClick={() => onDelete(blog.id)}
                                className="rounded-lg border border-red-200 px-3 py-1.5 text-xs text-red-500 hover:bg-red-50 transition-colors"
                            >
                                Delete
                            </button>
                        )}
                    </div>
                )}
            </div>

            {/* Inline comment section */}
            {showComments && (
                <div className="border-t border-border/50 bg-muted/10 px-5 py-4">
                    <CommentSection blog={blog} setBlog={setBlog} />
                </div>
            )}
        </article>
    )
}
