package com.vodang.greenmind.store

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.vodang.greenmind.time.currentTimeMillis

data class MealRecord(
    val id: String,
    val plantRatio: Int,
    val description: String,
    val timestampMillis: Long,
    val imageUrl: String? = null,
    val plantImageBase64: String? = null,
    val dishImageBase64: String? = null,
)

object MealStore {
    private val _meals = MutableStateFlow<List<MealRecord>>(emptyList())
    val meals: StateFlow<List<MealRecord>> = _meals.asStateFlow()

    fun add(
        plantRatio: Int,
        description: String,
        imageUrl: String? = null,
        plantImageBase64: String? = null,
        dishImageBase64: String? = null,
    ) {
        val record = MealRecord(
            id = kotlin.random.Random.nextLong().toString(),
            plantRatio = plantRatio,
            description = description,
            timestampMillis = currentTimeMillis(),
            imageUrl = imageUrl,
            plantImageBase64 = plantImageBase64,
            dishImageBase64 = dishImageBase64,
        )
        _meals.value = listOf(record) + _meals.value
    }
}
