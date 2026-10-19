package com.vodang.greenmind.api.todo

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
data class TodoDto(
    val id: String,
    val title: String,
    val completed: Boolean,
    @SerialName("parent_id") val parentId: String? = null,
    @SerialName("user_id") val userId: String,
    val order: Int,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class SubtaskDto(
    val id: String,
    val title: String,
    val completed: Boolean,
    val completedItems: Int,
    val totalItems: Int,
    val subtasks: List<SubtaskDto> = emptyList(),
    @SerialName("parent_id") val parentId: String,
    val order: Int,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class TodoWithSubtasksDto(
    val id: String,
    val title: String,
    val completed: Boolean,
    val completedItems: Int,
    val totalItems: Int,
    val subtasks: List<SubtaskDto> = emptyList(),
    @SerialName("parent_id") val parentId: String? = null,
    val order: Int,
    val createdAt: String,
    val updatedAt: String,
)

// ── Requests ─────────────────────────────────────────────────────────────────

@Serializable
data class CreateTodoRequest(
    val title: String,
    @SerialName("parent_id") val parentId: String? = null,
    val completed: Boolean = false,
)

@Serializable
data class BatchTodoItem(
    val title: String,
    val completed: Boolean = false,
    @SerialName("parent_id") val parentId: String? = null,
)

@Serializable
data class BatchTodoRequest(
    @SerialName("parent_id") val parentId: String? = null,
    val todos: List<BatchTodoItem>,
)

@Serializable
data class UpdateTodoRequest(
    val title: String? = null,
    val completed: Boolean? = null,
    @SerialName("parent_id") val parentId: String? = null,
)

// ── Responses ─────────────────────────────────────────────────────────────────

@Serializable
data class CreateTodoResponse(
    val message: String,
    val data: TodoDto,
)

@Serializable
data class GetTodosResponse(
    val message: String,
    val data: List<TodoWithSubtasksDto>,
)

@Serializable
data class BatchTodoResponse(
    val message: String,
    val data: List<TodoDto>,
)

// ── API calls ────────────────────────────────────────────────────────────────

/** POST /todos */
suspend fun createTodo(accessToken: String, request: CreateTodoRequest): CreateTodoResponse {
    AppLogger.i("Todo", "createTodo title=${request.title} parentId=${request.parentId}")
    try {
        val resp = httpClient.post("$BASE_URL/todos") {
            header("Authorization", "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("Todo", "createTodo failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("Todo", "createTodo error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** GET /todos */
suspend fun getTodos(accessToken: String): GetTodosResponse {
    AppLogger.i("Todo", "getTodos")
    try {
        val resp = httpClient.get("$BASE_URL/todos") {
            header("Authorization", "Bearer $accessToken")
        }
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("Todo", "getTodos failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("Todo", "getTodos error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** POST /todos/batch */
suspend fun batchCreateTodos(accessToken: String, request: BatchTodoRequest): BatchTodoResponse {
    AppLogger.i("Todo", "batchCreateTodos count=${request.todos.size}")
    try {
        val resp = httpClient.post("$BASE_URL/todos/batch") {
            header("Authorization", "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("Todo", "batchCreateTodos failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("Todo", "batchCreateTodos error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** PUT /todos/{id} */
suspend fun updateTodo(accessToken: String, id: String, request: UpdateTodoRequest) {
    AppLogger.i("Todo", "updateTodo id=$id")
    try {
        val resp = httpClient.put("$BASE_URL/todos/$id") {
            header("Authorization", "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (!resp.status.isSuccess()) {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("Todo", "updateTodo failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("Todo", "updateTodo error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** DELETE /todos/{id} */
suspend fun deleteTodo(accessToken: String, id: String) {
    AppLogger.i("Todo", "deleteTodo id=$id")
    try {
        val resp = httpClient.delete("$BASE_URL/todos/$id") {
            header("Authorization", "Bearer $accessToken")
        }
        if (!resp.status.isSuccess()) {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("Todo", "deleteTodo failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("Todo", "deleteTodo error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** PATCH /todos/{id}/toggle */
suspend fun toggleTodo(accessToken: String, id: String) {
    AppLogger.i("Todo", "toggleTodo id=$id")
    try {
        val resp = httpClient.patch("$BASE_URL/todos/$id/toggle") {
            header("Authorization", "Bearer $accessToken")
        }
        if (!resp.status.isSuccess()) {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("Todo", "toggleTodo failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("Todo", "toggleTodo error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}
