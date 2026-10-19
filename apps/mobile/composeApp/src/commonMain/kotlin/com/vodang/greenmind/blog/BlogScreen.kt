package com.vodang.greenmind.blog

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.blog.BlogDto
import com.vodang.greenmind.api.blog.getBlog
import com.vodang.greenmind.api.blog.getBlogs
import com.vodang.greenmind.api.blog.getLeaderboard
import com.vodang.greenmind.api.blog.toggleBlogLike
import com.vodang.greenmind.api.campaign.CampaignDto
import com.vodang.greenmind.api.campaign.getAllCampaigns
import com.vodang.greenmind.blog.components.BlogCard
import com.vodang.greenmind.blog.components.LeaderboardTab
import com.vodang.greenmind.home.components.CampaignDetailScreen
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.platform.BackHandler
import com.vodang.greenmind.store.BlogStore
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.theme.Green100
import com.vodang.greenmind.theme.Green800
import com.vodang.greenmind.theme.Gray100
import com.vodang.greenmind.theme.Gray400
import com.vodang.greenmind.theme.Gray500
import com.vodang.greenmind.theme.Gray600
import com.vodang.greenmind.theme.Gray700
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

private val green800 = Green800
private val green100 = Green100
private val gray100 = Gray100
private val gray400 = Gray400
private val gray500 = Gray500
private val gray600 = Gray600
private val gray700 = Gray700

private fun authorInitials(name: String, username: String): String =
    name.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")
        .ifBlank { username.take(2).uppercase() }

private fun formatDateShort(iso: String): String = try {
    val parts = iso.substringBefore('T').split('-')
    if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else iso
} catch (_: Throwable) { iso }

private fun stripHtmlBasic(html: String): String =
    html.replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), " ")
        .replace(Regex("<[^>]+>"), "")
        .replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">")
        .replace("&nbsp;", " ").replace("&quot;", "\"").replace("&#39;", "'")
        .replace(Regex("\\s+"), " ").trim()

@Composable
fun BlogScreen(
    selectedTab: Int = 0,
    onTabChange: (Int) -> Unit = {},
    onSelectCampaign: (CampaignDto) -> Unit = {},
) {
    val s = LocalAppStrings.current
    val accessToken = SettingsStore.getAccessToken() ?: return

    var selectedBlog by remember { mutableStateOf<BlogDto?>(null) }
    var showCreate by remember { mutableStateOf(false) }
    var listRefreshKey by remember { mutableIntStateOf(0) }
    var reloadTrigger by remember { mutableIntStateOf(0) }

    BackHandler(enabled = showCreate) { showCreate = false }
    BackHandler(enabled = selectedBlog != null) { selectedBlog = null }

    var allCampaigns by remember { mutableStateOf<List<CampaignDto>>(emptyList()) }
    var selectedCampaignId by remember { mutableStateOf<String?>(null) }

    val selectedCampaign = selectedCampaignId?.let { id -> allCampaigns.find { it.id == id } }
    if (selectedCampaign != null) {
        onSelectCampaign(selectedCampaign)
        selectedCampaignId = null
    }

    if (showCreate) {
        CreateBlogScreen(
            accessToken = accessToken,
            onBack = { showCreate = false },
            onCreated = { showCreate = false; listRefreshKey++; BlogStore.clearAll() },
        )
        return
    }

    fun onBlogLiked(id: String, liked: Boolean, count: Int) {
        BlogStore.onBlogLiked(id, liked, count)
    }

    val currentBlog = selectedBlog
    if (currentBlog != null) {
        BlogDetailScreen(
            blogId = currentBlog.id,
            accessToken = accessToken,
            initialPost = currentBlog,
            onBack = { selectedBlog = null; reloadTrigger++ },
            onBlogLiked = ::onBlogLiked,
            onBlogDeleted = {
                selectedBlog = null
                BlogStore.onBlogDeleted(currentBlog.id)
                BlogStore.updatePosts(BlogStore.posts.filter { it.id != currentBlog.id })
                reloadTrigger++
            },
            onSelectCampaign = onSelectCampaign,
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFEEF0F3))) {
        when (selectedTab) {
            0 -> BlogListTab(
                    accessToken = accessToken,
                    refreshKey = listRefreshKey,
                    reloadTrigger = reloadTrigger,
                    onSelectBlog = { selectedBlog = it },
                    onCreatePost = { showCreate = true },
                    onBlogLiked = ::onBlogLiked,
                    allCampaigns = allCampaigns,
                    onSelectCampaign = { selectedCampaignId = it },
                    onCampaignsLoaded = { allCampaigns = it },
                )
                1 -> LeaderboardTab(
                    accessToken = accessToken,
                    onLoad = { getLeaderboard(accessToken).data },
                    currentUserId = SettingsStore.getUser()?.id,
                )
            }
        }
}

@Composable
private fun CreatePostPrompt(onTap: () -> Unit) {
    val user = SettingsStore.getUser()
    val initials = authorInitials(user?.fullName ?: "", user?.username ?: "?")
    val s = LocalAppStrings.current

    Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onTap)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(green100),
                contentAlignment = Alignment.Center,
            ) {
                Text(initials, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = green800)
            }
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(gray100)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(s.blogCreateContentHint, fontSize = 14.sp, color = gray400)
            }
        }
    }
}

@Composable
private fun BlogListTab(
    accessToken: String,
    refreshKey: Int,
    reloadTrigger: Int,
    onSelectBlog: (BlogDto) -> Unit,
    onCreatePost: () -> Unit,
    onBlogLiked: (String, Boolean, Int) -> Unit,
    allCampaigns: List<CampaignDto>,
    onSelectCampaign: (String) -> Unit,
    onCampaignsLoaded: (List<CampaignDto>) -> Unit,
) {
    val s = LocalAppStrings.current
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(true) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(accessToken) {
        try {
            val campaigns = getAllCampaigns(accessToken)
            onCampaignsLoaded(campaigns)
        } catch (_: Throwable) { }
    }

    suspend fun fetchPostDetails() {
        val needDetail = BlogStore.posts.filter { it.content.isNullOrBlank() }.map { it.id }.toSet()
        if (needDetail.isEmpty()) return
        BlogStore.loadingContentIds = needDetail
        coroutineScope {
            val results = needDetail.map { id ->
                async<Pair<String, BlogDto?>> {
                    try { id to getBlog(id, accessToken).data } catch (_: Throwable) { id to null }
                }
            }
            results.forEach { deferred ->
                val (id, detail) = deferred.await()
                if (detail != null) {
                    val plainText = detail.content?.let { stripHtmlBasic(it) }
                    val preview = plainText?.take(150)?.let { "$it..." }
                    val detailLiked = detail.isLiked || detail.liked
                    BlogStore.updatePosts(BlogStore.posts.map {
                        if (it.id == id) it.copy(
                            content = preview,
                            comments = detail.comments,
                            isLiked = detailLiked,
                            liked = detailLiked,
                            likeCount = detail.likeCount,
                            commentCount = detail.commentCount,
                        ) else it
                    })
                }
                BlogStore.loadingContentIds = BlogStore.loadingContentIds - id
            }
        }
    }

    fun load(page: Int, query: String, append: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!append && page == 1 && now - BlogStore.lastFetchTime < 300_000 && BlogStore.posts.isNotEmpty()) {
            isLoading = false
            BlogStore.isRefreshing = false
            return
        }
        if (!append) BlogStore.touch()
        scope.launch {
            if (append) isLoadingMore = true else isLoading = true
            error = null
            try {
                val result = getBlogs(page = page, limit = 10, search = query.ifBlank { null }, accessToken = accessToken)
                BlogStore.updatePosts(if (append) BlogStore.posts + result.data else result.data)
                BlogStore.currentPage = result.pagination.page
                BlogStore.totalPages = result.pagination.totalPages
                if (!append) fetchPostDetails()
            } catch (e: Throwable) {
                error = e.message ?: s.blogErrorLoad
            }
            isLoading = false
            isLoadingMore = false
            BlogStore.isRefreshing = false
        }
    }

    LaunchedEffect(refreshKey) { load(1, "") }
    LaunchedEffect(reloadTrigger) { load(1, "") }

    fun toggleLike(postId: String) {
        val post = BlogStore.posts.find { it.id == postId } ?: return
        val wasLiked = post.isLiked || post.liked
        val newLiked = !wasLiked
        val newCount = (post.likeCount + if (newLiked) 1 else -1).coerceAtLeast(0)
        BlogStore.updatePosts(BlogStore.posts.map {
            if (it.id == postId) it.copy(isLiked = newLiked, liked = newLiked, likeCount = newCount)
            else it
        })
        onBlogLiked(postId, newLiked, newCount)
        scope.launch {
            try {
                toggleBlogLike(postId, accessToken)
            } catch (e: Throwable) {
                BlogStore.updatePosts(BlogStore.posts.map {
                    if (it.id == postId) it.copy(isLiked = wasLiked, liked = wasLiked, likeCount = post.likeCount)
                    else it
                })
                onBlogLiked(postId, wasLiked, post.likeCount)
            }
        }
    }

    when {
        isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = green800)
        }
        error != null -> Box(
            Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("😕", fontSize = 40.sp)
                Text(error!!, fontSize = 14.sp, color = gray500, textAlign = TextAlign.Center)
                Button(
                    onClick = { load(1, "") },
                    colors = ButtonDefaults.buttonColors(containerColor = green800),
                    shape = RoundedCornerShape(10.dp),
                ) { Text(s.blogRetry) }
            }
        }
        BlogStore.posts.isEmpty() -> Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("📰", fontSize = 48.sp)
                Text(s.blogEmpty, fontSize = 14.sp, color = gray500)
            }
        }
        else -> {
            // Build feed: every 2 posts, insert 1 campaign
            val feedItems = buildList {
                val campaigns = allCampaigns
                    .sortedByDescending { it.createdAt }
                    .ifEmpty { emptyList() }
                var campaignIdx = 0
                BlogStore.posts.forEachIndexed { index, post ->
                    add(FeedItem.Blog(post))
                    if ((index + 1) % 2 == 0 && campaignIdx < campaigns.size) {
                        add(FeedItem.Campaign(campaigns[campaignIdx]))
                        campaignIdx++
                    }
                }
            }

            @OptIn(ExperimentalMaterial3Api::class)
            PullToRefreshBox(
                isRefreshing = BlogStore.isRefreshing,
                onRefresh = {
                    BlogStore.invalidate()
                    BlogStore.isRefreshing = true
                    load(1, "")
                },
            ) {
                LazyColumn(Modifier.fillMaxSize()) {
                item {
                    CreatePostPrompt(onTap = onCreatePost)
                    Spacer(Modifier.height(8.dp))
                }
                items(feedItems, key = {
                    when (it) {
                        is FeedItem.Blog -> "blog-${it.post.id}"
                        is FeedItem.Campaign -> "campaign-${it.campaign.id}"
                    }
                }) { item ->
                    when (item) {
                        is FeedItem.Blog -> {
                            val effectiveLiked = BlogStore.likedPosts[item.post.id] ?: (item.post.isLiked || item.post.liked)
                            val effectiveCount = BlogStore.likeCounts[item.post.id] ?: item.post.likeCount
                            BlogCard(
                                post = item.post,
                                effectiveLiked = effectiveLiked,
                                effectiveLikeCount = effectiveCount,
                                onClick = { onSelectBlog(item.post) },
                                onLike = { toggleLike(item.post.id) },
                                isContentLoading = item.post.id in BlogStore.loadingContentIds,
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                        is FeedItem.Campaign -> {
                            SponsoredCampaignCard(
                                campaign = item.campaign,
                                onClick = { onSelectCampaign(item.campaign.id) },
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
                if (BlogStore.currentPage < BlogStore.totalPages) {
                    item {
                        Box(
                            Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (isLoadingMore) {
                                CircularProgressIndicator(color = green800, modifier = Modifier.size(24.dp))
                            } else {
                                OutlinedButton(
                                    onClick = { load(BlogStore.currentPage + 1, "", append = true) },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = green800),
                                ) { Text(s.blogLoadMore) }
                            }
                        }
                    }
                }
            }
            }
        }
    }
}

private sealed class FeedItem {
    data class Blog(val post: BlogDto) : FeedItem()
    data class Campaign(val campaign: CampaignDto) : FeedItem()
}

@Composable
private fun SponsoredCampaignCard(campaign: CampaignDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🎯", fontSize = 16.sp)
                Text("Sponsored", fontSize = 12.sp, color = Gray500, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(8.dp))
            Text(campaign.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1B1B1B))
            Spacer(Modifier.height(4.dp))
                Text(campaign.description, fontSize = 13.sp, color = Gray600, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}