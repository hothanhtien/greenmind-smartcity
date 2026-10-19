package com.vodang.greenmind.store

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ErrorLevel { W, E }

data class ErrorLogEntry(
    val timestampMs: Long,
    val level: ErrorLevel,
    val tag: String,
    val message: String,
    val stackTrace: String? = null,
)

object ErrorLogStore {
    private const val MAX_ENTRIES = 200

    private val _entries = MutableStateFlow<List<ErrorLogEntry>>(emptyList())
    val entries: StateFlow<List<ErrorLogEntry>> = _entries.asStateFlow()

    fun add(entry: ErrorLogEntry) {
        _entries.value = (_entries.value + entry).takeLast(MAX_ENTRIES)
    }

    fun clear() {
        _entries.value = emptyList()
    }
}
