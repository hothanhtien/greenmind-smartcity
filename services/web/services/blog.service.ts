import axios from "axios"
import { getAccessToken } from "@/lib/auth"

const API_URL = process.env.NEXT_PUBLIC_API_URL || "https://vodang-api.gauas.com"

export interface BlogComment {
  id: string
  content: string
  createdAt: string
  updatedAt?: string
  user: {
    id: string
    username: string
    fullName: string
  } | null
}

export interface Blog {
  id: string
  title: string
  content: string
  tags?: string[]
  like_count: number
  comment_count: number
  author_id: string
  author?: {
    id: string
    username: string
    fullName: string
  }
  isLiked?: boolean
  comments?: BlogComment[]
  createdAt: string
  updatedAt: string
}

export interface BlogPagination {
  page: number
  limit: number
  total: number
  totalPages: number
}

export interface BlogListResponse {
  data: Blog[]
  pagination: BlogPagination
}

export interface LeaderboardUser {
  rank: number
  userId: string
  fullName: string
  username: string
  reportCount: number
  location?: string
}

function authHeaders() {
  const token = getAccessToken()
  return token ? { Authorization: `Bearer ${token}` } : {}
}

export async function fetchBlogs(page = 1, limit = 10, search?: string): Promise<BlogListResponse> {
  const params: Record<string, string | number> = { page, limit }
  if (search) params.search = search
  const res = await axios.get(`${API_URL}/blogs`, { params })
  return { data: res.data.data, pagination: res.data.pagination }
}

export async function fetchBlogById(id: string): Promise<Blog> {
  const token = getAccessToken()
  const headers = token ? { Authorization: `Bearer ${token}` } : {}
  const res = await axios.get(`${API_URL}/blogs/${id}`, { headers })
  return res.data.data
}

export async function fetchMyBlogs(page = 1, limit = 10): Promise<BlogListResponse> {
  const res = await axios.get(`${API_URL}/blogs/user/my-blogs`, {
    params: { page, limit },
    headers: authHeaders(),
  })
  return { data: res.data.data, pagination: res.data.pagination }
}

export async function createBlog(data: { title: string; content: string; tags?: string[] }): Promise<Blog> {
  const res = await axios.post(`${API_URL}/blogs`, data, { headers: authHeaders() })
  return res.data.data
}

export async function updateBlog(
  id: string,
  data: { title?: string; content?: string; tags?: string[] }
): Promise<Blog> {
  const res = await axios.put(`${API_URL}/blogs/${id}`, data, { headers: authHeaders() })
  return res.data.data
}

export async function deleteBlog(id: string): Promise<void> {
  await axios.delete(`${API_URL}/blogs/${id}`, { headers: authHeaders() })
}

export async function toggleLike(blogId: string): Promise<{ liked: boolean; like_count: number }> {
  const res = await axios.post(`${API_URL}/blogs/${blogId}/like`, {}, { headers: authHeaders() })
  return { liked: res.data.liked, like_count: res.data.like_count }
}

export async function addComment(blogId: string, content: string): Promise<BlogComment> {
  const res = await axios.post(`${API_URL}/blogs/${blogId}/comments`, { content }, { headers: authHeaders() })
  return res.data.data
}

export async function updateComment(blogId: string, commentId: string, content: string): Promise<BlogComment> {
  const res = await axios.put(`${API_URL}/blogs/${blogId}/comments/${commentId}`, { content }, { headers: authHeaders() })
  return res.data.data
}

export async function deleteComment(blogId: string, commentId: string): Promise<void> {
  await axios.delete(`${API_URL}/blogs/${blogId}/comments/${commentId}`, { headers: authHeaders() })
}

export async function fetchLeaderboard(): Promise<LeaderboardUser[]> {
  const res = await axios.get(`${API_URL}/waste-reports/leaderboard`)
  return res.data.data
}

/**
 * Upload image to Cloudflare R2 via the backend /media/upload route.
 * Returns the public URL of the uploaded image.
 */
export async function uploadImageToR2(file: File): Promise<string> {
  const formData = new FormData()
  formData.append("file", file)
  const token = getAccessToken()
  const res = await axios.post(`${API_URL}/media/upload`, formData, {
    headers: {
      "Content-Type": "multipart/form-data",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
  })
  return res.data.url
}
