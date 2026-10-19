package com.vodang.greenmind.api.blog

import com.vodang.greenmind.api.BASE_URL
import com.vodang.greenmind.api.httpClient
import com.vodang.greenmind.api.auth.ApiException
import com.vodang.greenmind.api.auth.ErrorResponse
import com.vodang.greenmind.util.AppLogger
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── DTOs ─────────────────────────────────────────────────────────────────────

@Serializable
data class BlogAuthorDto(
    val id: String,
    val username: String,
    val fullName: String,
)

@Serializable
data class BlogDto(
    val id: String,
    val title: String,
    val content: String? = null,
    val tags: List<String> = emptyList(),
    @SerialName("like_count") val likeCount: Int = 0,
    @SerialName("comment_count") val commentCount: Int = 0,
    @SerialName("author_id") val authorId: String,
    val author: BlogAuthorDto? = null,
    val liked: Boolean = false,
    val isLiked: Boolean = false,
    val comments: List<CommentDto> = emptyList(),
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class PaginationDto(
    val page: Int,
    val limit: Int,
    val total: Int,
    val totalPages: Int,
)

@Serializable
data class GetBlogsResponse(
    val message: String,
    val data: List<BlogDto>,
    val pagination: PaginationDto,
)

@Serializable
data class GetBlogResponse(
    val message: String,
    val data: BlogDto,
)

@Serializable
data class LikeResponse(
    val message: String,
    val liked: Boolean = false,
    @SerialName("like_count") val likeCount: Int = 0,
)

@Serializable
data class LeaderboardEntryDto(
    val rank: Int,
    val userId: String,
    val fullName: String,
    val username: String,
    val reportCount: Int,
)

@Serializable
data class GetLeaderboardResponse(
    val data: List<LeaderboardEntryDto>,
)

// ── Comments ──────────────────────────────────────────────────────────────────

@Serializable
data class CommentUserDto(
    val id: String,
    val username: String,
    val fullName: String,
)

@Serializable
data class CommentDto(
    val id: String,
    val content: String,
    val createdAt: String,
    val updatedAt: String? = null,
    val user: CommentUserDto,
)

@Serializable
data class AddCommentResponse(
    val message: String,
    val data: CommentDto,
)

@Serializable
data class UpdateCommentResponse(
    val message: String,
    val data: CommentDto,
)

@Serializable
data class GetBlogLikesResponse(
    val message: String,
    val data: List<BlogLikeDto>,
    val pagination: PaginationDto,
)

@Serializable
data class BlogLikeDto(
    val id: String,
    val createdAt: String,
    val user: CommentUserDto,
)

// ── API calls ────────────────────────────────────────────────────────────────

@Serializable
data class CreateBlogRequest(
    val title: String,
    val content: String,
    val tags: List<String> = emptyList(),
)

@Serializable
data class CreateBlogResponse(
    val message: String,
    val data: BlogDto,
)

/** POST /blogs */
suspend fun createBlog(accessToken: String, request: CreateBlogRequest): CreateBlogResponse {
    AppLogger.i("Blog", "createBlog title=${request.title}")
    try {
        val resp = httpClient.post("$BASE_URL/blogs") {
            header("Authorization", "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("Blog", "createBlog failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("Blog", "createBlog error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** GET /blogs */
suspend fun getBlogs(
    page: Int = 1,
    limit: Int = 10,
    search: String? = null,
    accessToken: String,
): GetBlogsResponse {
    AppLogger.i("Blog", "getBlogs page=$page limit=$limit search=$search")
    try {
        val resp = httpClient.get("$BASE_URL/blogs") {
            header("Authorization", "Bearer $accessToken")
            parameter("page", page)
            parameter("limit", limit)
            if (!search.isNullOrBlank()) parameter("search", search)
        }
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("Blog", "getBlogs failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("Blog", "getBlogs error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** GET /blogs/:id */
suspend fun getBlog(id: String, accessToken: String): GetBlogResponse {
    AppLogger.i("Blog", "getBlog id=$id")
    try {
        val resp = httpClient.get("$BASE_URL/blogs/$id") {
            header("Authorization", "Bearer $accessToken")
        }
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("Blog", "getBlog failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("Blog", "getBlog error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** POST /blogs/:id/like */
suspend fun toggleBlogLike(id: String, accessToken: String): LikeResponse {
    AppLogger.i("Blog", "toggleBlogLike id=$id")
    try {
        val resp = httpClient.post("$BASE_URL/blogs/$id/like") {
            header("Authorization", "Bearer $accessToken")
        }
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("Blog", "toggleBlogLike failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("Blog", "toggleBlogLike error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** GET /waste-reports/leaderboard */
suspend fun getLeaderboard(accessToken: String): GetLeaderboardResponse {
    AppLogger.i("Blog", "getLeaderboard")
    try {
        val resp = httpClient.get("$BASE_URL/waste-reports/leaderboard") {
            header("Authorization", "Bearer $accessToken")
        }
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("Blog", "getLeaderboard failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("Blog", "getLeaderboard error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

// ── My Blogs ─────────────────────────────────────────────────────────────────

/** GET /blogs/user/my-blogs */
suspend fun getMyBlogs(
    page: Int = 1,
    limit: Int = 10,
    accessToken: String,
): GetBlogsResponse {
    AppLogger.i("Blog", "getMyBlogs page=$page limit=$limit")
    try {
        val resp = httpClient.get("$BASE_URL/blogs/user/my-blogs") {
            header("Authorization", "Bearer $accessToken")
            parameter("page", page)
            parameter("limit", limit)
        }
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("Blog", "getMyBlogs failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("Blog", "getMyBlogs error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

// ── Update / Delete Blog ─────────────────────────────────────────────────────

@Serializable
data class UpdateBlogRequest(
    val title: String? = null,
    val content: String? = null,
    val tags: List<String>? = null,
)

/** PUT /blogs/:id */
suspend fun updateBlog(
    id: String,
    request: UpdateBlogRequest,
    accessToken: String,
): GetBlogResponse {
    AppLogger.i("Blog", "updateBlog id=$id")
    try {
        val resp = httpClient.put("$BASE_URL/blogs/$id") {
            header("Authorization", "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("Blog", "updateBlog failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("Blog", "updateBlog error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** DELETE /blogs/:id */
suspend fun deleteBlog(id: String, accessToken: String) {
    AppLogger.i("Blog", "deleteBlog id=$id")
    try {
        val resp = httpClient.delete("$BASE_URL/blogs/$id") {
            header("Authorization", "Bearer $accessToken")
        }
        if (!resp.status.isSuccess()) {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("Blog", "deleteBlog failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("Blog", "deleteBlog error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

// ── Blog Likes ────────────────────────────────────────────────────────────────

/** GET /blogs/:id/likes */
suspend fun getBlogLikes(
    id: String,
    page: Int = 1,
    limit: Int = 20,
): GetBlogLikesResponse {
    AppLogger.i("Blog", "getBlogLikes id=$id page=$page limit=$limit")
    try {
        val resp = httpClient.get("$BASE_URL/blogs/$id/likes") {
            parameter("page", page)
            parameter("limit", limit)
        }
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("Blog", "getBlogLikes failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("Blog", "getBlogLikes error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

// ── Comments ─────────────────────────────────────────────────────────────────

@Serializable
data class AddCommentRequest(
    val content: String,
)

@Serializable
data class UpdateCommentRequest(
    val content: String,
)

/** POST /blogs/:id/comments */
suspend fun addComment(
    blogId: String,
    content: String,
    accessToken: String,
): AddCommentResponse {
    AppLogger.i("Blog", "addComment blogId=$blogId")
    try {
        val resp = httpClient.post("$BASE_URL/blogs/$blogId/comments") {
            header("Authorization", "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(AddCommentRequest(content))
        }
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("Blog", "addComment failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("Blog", "addComment error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** PUT /blogs/:id/comments/:commentId */
suspend fun updateComment(
    blogId: String,
    commentId: String,
    content: String,
    accessToken: String,
): UpdateCommentResponse {
    AppLogger.i("Blog", "updateComment blogId=$blogId commentId=$commentId")
    try {
        val resp = httpClient.put("$BASE_URL/blogs/$blogId/comments/$commentId") {
            header("Authorization", "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(UpdateCommentRequest(content))
        }
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("Blog", "updateComment failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("Blog", "updateComment error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** DELETE /blogs/:id/comments/:commentId */
suspend fun deleteComment(blogId: String, commentId: String, accessToken: String) {
    AppLogger.i("Blog", "deleteComment blogId=$blogId commentId=$commentId")
    try {
        val resp = httpClient.delete("$BASE_URL/blogs/$blogId/comments/$commentId") {
            header("Authorization", "Bearer $accessToken")
        }
        if (!resp.status.isSuccess()) {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("Blog", "deleteComment failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("Blog", "deleteComment error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}
