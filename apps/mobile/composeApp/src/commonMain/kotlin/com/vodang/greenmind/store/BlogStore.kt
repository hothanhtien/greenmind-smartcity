package com.vodang.greenmind.store

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.vodang.greenmind.api.blog.BlogDto

object BlogStore {
    var posts by mutableStateOf<List<BlogDto>>(emptyList())
    var lastFetchTime by mutableStateOf(0L)
    var loadingContentIds by mutableStateOf<Set<String>>(emptySet())
    var isRefreshing by mutableStateOf(false)
    var likedPosts by mutableStateOf<Map<String, Boolean>>(emptyMap())
    var likeCounts by mutableStateOf<Map<String, Int>>(emptyMap())
    var currentPage by mutableStateOf(1)
    var totalPages by mutableStateOf(1)

    fun updatePosts(newPosts: List<BlogDto>) {
        posts = newPosts
    }

    fun touch() {
        lastFetchTime = System.currentTimeMillis()
    }

    fun invalidate() {
        lastFetchTime = 0L
    }

    fun onBlogLiked(id: String, liked: Boolean, count: Int) {
        likedPosts = likedPosts + (id to liked)
        likeCounts = likeCounts + (id to count)
    }

    fun onBlogDeleted(id: String) {
        likedPosts = likedPosts - id
        likeCounts = likeCounts - id
    }

    fun clearAll() {
        posts = emptyList()
        lastFetchTime = 0L
        loadingContentIds = emptySet()
        isRefreshing = false
        likedPosts = emptyMap()
        likeCounts = emptyMap()
        currentPage = 1
        totalPages = 1
    }
}
