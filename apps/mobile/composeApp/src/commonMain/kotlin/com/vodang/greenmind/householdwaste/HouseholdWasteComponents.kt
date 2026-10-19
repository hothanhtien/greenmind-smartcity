package com.vodang.greenmind.householdwaste

import com.vodang.greenmind.api.households.DetectTrashHistoryDto
import com.vodang.greenmind.wastesort.WasteSortStatus

internal fun parseWasteSortStatus(status: String?): WasteSortStatus {
    return when(status) {
        "brought_out" -> WasteSortStatus.BRINGOUTED
        "picked_up", "done", "completed" -> WasteSortStatus.COLLECTED
        else -> WasteSortStatus.SORTED
    }
}

/** Compute the most advanced status from a group of detect records */
internal fun groupStatus(records: List<DetectTrashHistoryDto>): WasteSortStatus {
    // COLLECTED takes priority
    if (records.any { it.status in listOf("picked_up", "done", "completed") }) {
        return WasteSortStatus.COLLECTED
    }
    // Then BRINGOUTED
    if (records.any { it.status == "brought_out" }) {
        return WasteSortStatus.BRINGOUTED
    }
    // Then SORTED (default)
    return WasteSortStatus.SORTED
}
