package com.vodang.greenmind.store

import com.vodang.greenmind.api.todo.CreateTodoRequest
import com.vodang.greenmind.api.todo.SubtaskDto
import com.vodang.greenmind.api.todo.TodoWithSubtasksDto
import com.vodang.greenmind.api.todo.createTodo
import com.vodang.greenmind.api.todo.deleteTodo
import com.vodang.greenmind.api.todo.getTodos
import com.vodang.greenmind.api.todo.splitTask
import com.vodang.greenmind.api.todo.toggleTodo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class TodoItem(
    val id: String,
    val title: String,
    val done: Boolean = false,
    val children: List<TodoItem> = emptyList()
)

fun TodoItem.countAll(): Int = 1 + children.sumOf { it.countAll() }
fun TodoItem.countDone(): Int = (if (done) 1 else 0) + children.sumOf { it.countDone() }

// ── Mapping ───────────────────────────────────────────────────────────────────

private fun SubtaskDto.toTodoItem(): TodoItem = TodoItem(
    id = id,
    title = title,
    done = completed,
    children = subtasks.map { it.toTodoItem() }
)

private fun TodoWithSubtasksDto.toTodoItem(): TodoItem = TodoItem(
    id = id,
    title = title,
    done = completed,
    children = subtasks.map { it.toTodoItem() }
)

// ── Store ─────────────────────────────────────────────────────────────────────

object TodoStore {

    private val scope = CoroutineScope(SupervisorJob())

    private val _todos = MutableStateFlow<List<TodoItem>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _generatingIds = MutableStateFlow<Set<String>>(emptySet())

    val todos: StateFlow<List<TodoItem>> = _todos.asStateFlow()
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    val error: StateFlow<String?> = _error.asStateFlow()
    /** IDs of todo items currently being AI-expanded. */
    val generatingIds: StateFlow<Set<String>> = _generatingIds.asStateFlow()

    private suspend fun reload(token: String) {
        _isLoading.value = true
        try {
            val resp = getTodos(token)
            _todos.value = resp.data.map { it.toTodoItem() }
        } finally {
            _isLoading.value = false
        }
    }

    fun load() {
        val token = SettingsStore.getAccessToken() ?: return
        scope.launch {
            _error.value = null
            try {
                reload(token)
            } catch (e: Throwable) {
                _error.value = e.message
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun addItem(parentId: String?, title: String) {
        val token = SettingsStore.getAccessToken() ?: return
        val tempId = "temp-${Uuid.random()}"
        val newItem = TodoItem(id = tempId, title = title, done = false)
        val snapshot = _todos.value

        // Optimistic: insert into UI immediately
        if (parentId == null) {
            _todos.value = snapshot + newItem
        } else {
            _todos.value = snapshot.map { it.addChildOptimistic(parentId, newItem) }
        }

        scope.launch {
            try {
                createTodo(token, CreateTodoRequest(title = title, parentId = parentId))
                // Sync with server to get real id
                reload(token)
            } catch (e: Throwable) {
                // Revert on failure
                _todos.value = snapshot
                _error.value = e.message
            }
        }
    }

    fun deleteItem(id: String) {
        val token = SettingsStore.getAccessToken() ?: return
        val snapshot = _todos.value

        // Optimistic: remove from UI immediately
        _todos.value = snapshot.removeItemRecursive(id)

        scope.launch {
            try {
                deleteTodo(token, id)
            } catch (e: Throwable) {
                // Revert on failure
                _todos.value = snapshot
                _error.value = e.message
            }
        }
    }

    fun toggleItem(id: String) {
        val token = SettingsStore.getAccessToken() ?: return
        val snapshot = _todos.value

        val (newTodos, toggledIds) = snapshot.computeToggleCascade(id)
        _todos.value = newTodos

        scope.launch {
            try {
                for (tid in toggledIds) {
                    toggleTodo(token, tid)
                }
            } catch (e: Throwable) {
                _todos.value = snapshot
                _error.value = e.message
            }
        }
    }

    /**
     * Calls the AI task-splitter for [itemId], then batch-creates all returned
     * subtasks as children of that item and reloads the list.
     */
    fun generateSubtasks(itemId: String, title: String) {
        val token = SettingsStore.getAccessToken() ?: return
        scope.launch {
            _generatingIds.value = _generatingIds.value + itemId
            _error.value = null
            try {
                val resp = splitTask(task = title, accessToken = token)
                if (resp.success && resp.result.subtasks.isNotEmpty()) {
                    for (subtask in resp.result.subtasks) {
                        createTodo(token, CreateTodoRequest(title = subtask, parentId = itemId))
                    }
                    reload(token)
                }
            } catch (e: Throwable) {
                _error.value = e.message
            } finally {
                _generatingIds.value = _generatingIds.value - itemId
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}

// ── Optimistic-update helpers ─────────────────────────────────────────────────

private fun TodoItem.addChildOptimistic(parentId: String, child: TodoItem): TodoItem =
    if (id == parentId) copy(children = children + child)
    else copy(children = children.map { it.addChildOptimistic(parentId, child) })

private fun List<TodoItem>.removeItemRecursive(targetId: String): List<TodoItem> =
    mapNotNull { item ->
        if (item.id == targetId) null
        else item.copy(children = item.children.removeItemRecursive(targetId))
    }

private fun TodoItem.setDeep(done: Boolean, collect: MutableList<String>? = null): TodoItem {
    if (collect != null && this.done != done) collect.add(id)
    return copy(done = done, children = children.map { it.setDeep(done, collect) })
}

private fun TodoItem.allChildrenDeepDone(): Boolean =
    children.isNotEmpty() && children.all { it.done && it.allChildrenDeepDone() }

private fun List<TodoItem>.computeToggleCascade(targetId: String): Pair<List<TodoItem>, List<String>> {
    val toggledIds = mutableListOf<String>()

    fun TodoItem.process(id: String): TodoItem {
        if (this.id == id) {
            toggledIds.add(id)
            val newDone = !done
            return setDeep(newDone, toggledIds)
        }
        val newChildren = children.map { it.process(id) }

        if (newChildren.isNotEmpty() && newChildren.all { it.done }) {
            if (!done) { toggledIds.add(this.id) }
            return copy(children = newChildren, done = true)
        }
        if (done && newChildren.isNotEmpty() && newChildren.any { !it.done }) {
            toggledIds.add(this.id)
            return copy(children = newChildren, done = false)
        }
        return copy(children = newChildren)
    }

    val result = map { it.process(targetId) }
    return result to toggledIds.distinct()
}
