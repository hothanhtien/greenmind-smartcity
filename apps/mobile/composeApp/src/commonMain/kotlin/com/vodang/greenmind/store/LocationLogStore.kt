package com.vodang.greenmind.store

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LocationLogEntry(
    val timestampMs: Long,
    val action: String,
    val detail: String,
)

object LocationLogStore {
    private const val MAX_ENTRIES = 100

    private val _entries = MutableStateFlow<List<LocationLogEntry>>(emptyList())
    val entries: StateFlow<List<LocationLogEntry>> = _entries.asStateFlow()

    fun log(action: String, detail: String) {
        val entry = LocationLogEntry(
            timestampMs = System.currentTimeMillis(),
            action = action,
            detail = detail,
        )
        _entries.value = (_entries.value + entry).takeLast(MAX_ENTRIES)
        // Also print to console for debugging
        println("[GreenMind][Location] $action: $detail")
    }

    fun clear() {
        _entries.value = emptyList()
    }
}