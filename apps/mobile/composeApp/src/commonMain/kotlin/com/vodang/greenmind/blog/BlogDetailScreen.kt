package com.vodang.greenmind.blog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import com.vodang.greenmind.api.blog.CommentDto
import com.vodang.greenmind.api.blog.BlogDto
import com.vodang.greenmind.api.blog.addComment
import com.vodang.greenmind.api.blog.deleteComment
import com.vodang.greenmind.api.blog.deleteBlog
import com.vodang.greenmind.api.blog.getBlog
import com.vodang.greenmind.api.blog.toggleBlogLike
import com.vodang.greenmind.api.blog.updateComment
import com.vodang.greenmind.api.campaign.CampaignDto
import com.vodang.greenmind.api.campaign.getAllCampaigns
import com.vodang.greenmind.home.components.CampaignDetailScreen
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.platform.BackHandler
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.time.formatDateLocal
import com.vodang.greenmind.time.formatDateTimeLocal
import kotlinx.coroutines.launch

// -- Material 3 Color Scheme (Green Theme) -------------------------------------

private val GreenPrimary = Color(0xFF2E7D32)
private val GreenOnPrimary = Color(0xFFFFFFFF)
private val GreenPrimaryContainer = Color(0xFFA5D6A7)
private val GreenOnPrimaryContainer = Color(0xFF1B5E20)

private val SurfaceLight = Color(0xFFFFFFFF)
private val SurfaceVariantLight = Color(0xFFF5F5F5)
private val BackgroundLight = Color(0xFFEEF0F3)
private val OnSurfaceLight = Color(0xFF1C1B1F)
private val OnSurfaceVariantLight = Color(0xFF49454F)

private val OutlineLight = Color(0xFFE4E6EB)
private val ErrorColor = Color(0xFFE53935)

// -- Helpers -------------------------------------------------------------------

internal fun stripHtml(html: String): String =
    html
        .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
        .replace(Regex("<p[^>]*>", RegexOption.IGNORE_CASE), "\n")
        .replace(Regex("</p>", RegexOption.IGNORE_CASE), "\n")
        .replace(Regex("<h[1-6][^>]*>", RegexOption.IGNORE_CASE), "\n")
        .replace(Regex("</h[1-6]>", RegexOption.IGNORE_CASE), "\n")
        .replace(Regex("<li[^>]*>", RegexOption.IGNORE_CASE), "\n- ")
        .replace(Regex("<img[^>]*/?>", RegexOption.IGNORE_CASE), "\n[image]\n")
        .replace(Regex("<[^>]+>"), "")
        .replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">")
        .replace("&nbsp;", " ").replace("&quot;", "\"").replace("&#39;", "'")
        .replace(Regex("\n{3,}"), "\n\n")
        .trim()


private fun initials(fullName: String, username: String) =
    fullName.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("").ifBlank { username.take(2).uppercase() }

private fun readMinutes(content: String?) = ((content?.split(" ")?.size ?: 0) / 200).coerceAtLeast(1)

// Extract report ID from post title or content (RPT* pattern)
private fun extractReportId(post: BlogDto): String? {
    val text = "${post.title} ${post.content ?: ""}"
    return Regex("RPT\\d+").find(text)?.value
}

private fun isoNow(): String {
    val now = java.time.Instant.now()
    return now.toString()
}

// -- Screen --------------------------------------------------------------------

@Composable
fun BlogDetailScreen(
    blogId: String,
    accessToken: String,
    initialPost: BlogDto? = null,
    onBack: () -> Unit,
    onBlogLiked: (String, Boolean, Int) -> Unit = { _, _, _ -> },
    onBlogDeleted: () -> Unit = {},
    onSelectCampaign: (CampaignDto) -> Unit = {},
    lazyListState: LazyListState = rememberLazyListState(),
) {
    BackHandler(onBack = onBack)

    val s     = LocalAppStrings.current
    val scope = rememberCoroutineScope()
    val currentUser = SettingsStore.getUser()

    var blog      by remember { mutableStateOf(initialPost) }
    var isLoading by remember { mutableStateOf(initialPost == null) }
    var error     by remember { mutableStateOf<String?>(null) }
    var likeCount by remember { mutableStateOf(initialPost?.likeCount ?: 0) }
    var isLiked   by remember { mutableStateOf(initialPost?.isLiked ?: initialPost?.liked ?: false) }
    var isLiking  by remember { mutableStateOf(false) }
    var showMenu  by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    // Campaign state
    var allCampaigns by remember { mutableStateOf<List<CampaignDto>>(emptyList()) }
    var selectedCampaignId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(accessToken) {
        try {
            allCampaigns = getAllCampaigns(accessToken)
        } catch (_: Throwable) { }
    }

    val selectedCampaign = selectedCampaignId?.let { id -> allCampaigns.find { it.id == id } }
    if (selectedCampaign != null) {
        onSelectCampaign(selectedCampaign)
        selectedCampaignId = null
    }

    fun loadBlog() {
        scope.launch {
            isLoading = true; error = null
            try {
                val result = getBlog(blogId, accessToken)
                blog      = result.data
                likeCount = result.data.likeCount
                isLiked   = result.data.isLiked || result.data.liked
            } catch (e: Throwable) {
                error = e.message ?: s.blogErrorLoad
            }
            isLoading = false
        }
    }

    LaunchedEffect(blogId) {
        if (initialPost == null) {
            loadBlog()
        } else {
            // Vẫn fetch background để lấy comments + like state mới nhất
            try {
                val result = getBlog(blogId, accessToken)
                blog = result.data
                likeCount = result.data.likeCount
                isLiked = result.data.isLiked || result.data.liked
            } catch (_: Throwable) { }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(s.blogDeleteTitle) },
            text = { Text(s.blogDeleteConfirm) },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            isDeleting = true
                            try {
                                deleteBlog(blogId, accessToken)
                                showDeleteDialog = false
                                onBlogDeleted()
                            } catch (e: Throwable) {
                                error = e.message
                                isDeleting = false
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = ErrorColor),
                ) {
                    if (isDeleting) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    else Text(s.delete)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text(s.cancel) }
            },
        )
    }

    Box(Modifier.fillMaxSize().background(BackgroundLight)) {
        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GreenPrimary)
            }

            error != null -> ErrorState(
                message = error!!,
                retryLabel = s.blogRetry,
                onBack = onBack,
                onRetry = {
                    scope.launch {
                        isLoading = true; error = null
                        try {
                            val result = getBlog(blogId, accessToken)
                            blog      = result.data
                            likeCount = result.data.likeCount
                            isLiked   = result.data.isLiked || result.data.liked
                        } catch (e: Throwable) { error = e.message ?: s.blogErrorLoad }
                        isLoading = false
                    }
                },
            )

            blog != null -> {
                val post = blog!!
                val reportId = extractReportId(post)
                val linkedCampaign = if (reportId != null) {
                    allCampaigns.find { campaign -> campaign.reports.any { it.id == reportId } }
                } else null

                PostDetail(
                    post = post,
                    linkedCampaign = linkedCampaign,
                    likeCount = likeCount,
                    isLiked = isLiked,
                    isLiking = isLiking,
                    onBack = onBack,
                    onLike = {
                        if (isLiking) return@PostDetail
                        scope.launch {
                            isLiking = true
                            val wasLiked = isLiked
                            isLiked   = !wasLiked
                            likeCount = (likeCount + if (!wasLiked) 1 else -1).coerceAtLeast(0)
                            try {
                                val result = toggleBlogLike(post.id, accessToken)
                                if (result.likeCount > 0) likeCount = result.likeCount
                                onBlogLiked(post.id, isLiked, likeCount)
                            } catch (_: Throwable) {
                                isLiked   = wasLiked
                                likeCount = (likeCount + if (wasLiked) 1 else -1).coerceAtLeast(0)
                            }
                            isLiking = false
                        }
                    },
                    currentUserId = currentUser?.id,
                    onDeleteClick = { showDeleteDialog = true },
                    onCommentAdded = { newComment ->
                        blog = blog?.copy(
                            comments = blog!!.comments + newComment,
                            commentCount = blog!!.commentCount + 1,
                        )
                    },
                    onCommentUpdated = { commentId, content ->
                        blog = blog?.copy(
                            comments = blog!!.comments.map {
                                if (it.id == commentId) it.copy(content = content, updatedAt = isoNow())
                                else it
                            },
                        )
                    },
                    onCommentDeleted = { commentId ->
                        blog = blog?.copy(
                            comments = blog!!.comments.filter { it.id != commentId },
                            commentCount = blog!!.commentCount - 1,
                        )
                    },
                    accessToken = accessToken,
                    showMenu = showMenu,
                    onShowMenu = { showMenu = it },
                    onViewCampaign = { campaignId -> selectedCampaignId = campaignId },
                    lazyListState = lazyListState,
                )
            }
        }
    }
}

// -- Post detail (Material 3 style) --------------------------------------------

@Composable
private fun PostDetail(
    post: BlogDto,
    linkedCampaign: CampaignDto?,
    likeCount: Int,
    isLiked: Boolean,
    isLiking: Boolean,
    onBack: () -> Unit,
    onLike: () -> Unit,
    currentUserId: String?,
    onDeleteClick: () -> Unit,
    onCommentAdded: (CommentDto) -> Unit,
    onCommentUpdated: (String, String) -> Unit,
    onCommentDeleted: (String) -> Unit,
    accessToken: String,
    showMenu: Boolean,
    onShowMenu: (Boolean) -> Unit,
    onViewCampaign: (String) -> Unit,
    lazyListState: LazyListState = rememberLazyListState(),
) {
    val s = LocalAppStrings.current
    val isOwner = currentUserId == post.authorId
    val isActive = linkedCampaign?.status?.equals("ACTIVE", ignoreCase = true) == true ||
            linkedCampaign?.status?.equals("IN_PROGRESS", ignoreCase = true) == true

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = lazyListState,
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = SurfaceLight),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            ) {
                Column {
                    // Campaign badge if this is a campaign post
                    if (linkedCampaign != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(GreenPrimaryContainer.copy(alpha = 0.3f))
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            AssistChip(
                                onClick = { },
                                label = { Text(s.blogCampaignBadge, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                                leadingIcon = { Text("🎯", fontSize = 14.sp) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = GreenPrimary,
                                    labelColor = GreenOnPrimary,
                                ),
                                border = null,
                            )
                            Spacer(Modifier.weight(1f))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isActive) GreenPrimary.copy(alpha = 0.2f) else SurfaceVariantLight,
                            ) {
                                Text(
                                    if (isActive) s.campaignActive else s.campaignInactive,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isActive) GreenPrimary else OnSurfaceVariantLight,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                )
                            }
                        }
                    }

                    // Author header
                    val avInitials = initials(post.author?.fullName ?: "", post.author?.username ?: "?")
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            color = GreenPrimaryContainer,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(avInitials, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = GreenOnPrimaryContainer)
                            }
                        }
                        Column(Modifier.weight(1f)) {
                            Text(
                                post.author?.fullName ?: post.author?.username ?: post.authorId,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OnSurfaceLight,
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(formatDateLocal(post.createdAt), fontSize = 12.sp, color = OnSurfaceVariantLight)
                                Text("·", fontSize = 12.sp, color = OnSurfaceVariantLight)
                                Text("${readMinutes(post.content)} min read", fontSize = 12.sp, color = OnSurfaceVariantLight)
                            }
                        }
                        if (isOwner) {
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.TopEnd) {
                                IconButton(onClick = { onShowMenu(!showMenu) }) {
                                    Text("⋮", fontSize = 20.sp, color = OnSurfaceVariantLight)
                                }
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { onShowMenu(false) },
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(s.blogEdit, color = OnSurfaceLight) },
                                        onClick = { onShowMenu(false) },
                                    )
                                    DropdownMenuItem(
                                        text = { Text(s.blogDelete, color = ErrorColor) },
                                        onClick = {
                                            onShowMenu(false)
                                            onDeleteClick()
                                        },
                                    )
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }

                    // Title
                    Text(
                        post.title,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceLight,
                        lineHeight = 30.sp,
                    )

                    Spacer(Modifier.height(12.dp))

                    // Content body
                    val contentText = post.content?.let { stripHtml(it) }?.ifBlank { null }

                    if (contentText != null) {
                        val paragraphs = contentText.split("\n\n").filter { it.isNotBlank() }
                        Column(
                            Modifier.padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            paragraphs.forEach { para ->
                                val trimmed = para.trim()
                                when {
                                    trimmed.startsWith("- ") -> {
                                        Text(trimmed, fontSize = 15.sp, color = OnSurfaceLight, lineHeight = 24.sp)
                                    }
                                    trimmed.length < 80 && !trimmed.contains('.') -> {
                                        Text(
                                            trimmed,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = OnSurfaceLight,
                                            lineHeight = 26.sp,
                                        )
                                    }
                                    else -> {
                                        Text(trimmed, fontSize = 15.sp, color = OnSurfaceLight, lineHeight = 26.sp)
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            s.noContentAvailable,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            fontSize = 15.sp,
                            color = OnSurfaceVariantLight,
                            textAlign = TextAlign.Center,
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Tags
                    if (post.tags.isNotEmpty()) {
                        Row(
                            Modifier.padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            post.tags.forEach { tag ->
                                AssistChip(
                                    onClick = { },
                                    label = { Text("#$tag", fontSize = 13.sp, fontWeight = FontWeight.Medium) },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = GreenPrimaryContainer.copy(alpha = 0.5f),
                                        labelColor = GreenOnPrimaryContainer,
                                    ),
                                    border = null,
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    // Embedded Campaign Card
                    if (linkedCampaign != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clickable { onViewCampaign(linkedCampaign.id) },
                            shape = RoundedCornerShape(12.dp),
                            color = GreenPrimaryContainer.copy(alpha = 0.3f),
                            tonalElevation = 1.dp,
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    s.blogCampaignInfo,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GreenPrimary,
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Surface(
                                        modifier = Modifier.size(48.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isActive) GreenPrimary else OnSurfaceVariantLight.copy(alpha = 0.3f),
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(if (isActive) "🎯" else "📅", fontSize = 24.sp)
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            linkedCampaign.name,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = OnSurfaceLight,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            s.dateRange(formatDateLocal(linkedCampaign.startDate), formatDateLocal(linkedCampaign.endDate)),
                                            fontSize = 12.sp,
                                            color = OnSurfaceVariantLight,
                                        )
                                        if (linkedCampaign.participantsCount > 0) {
                                            Text(
                                                "${linkedCampaign.participantsCount} participants",
                                                fontSize = 12.sp,
                                                color = OnSurfaceVariantLight,
                                            )
                                        }
                                    }
                                }
                                if (linkedCampaign.description.isNotBlank()) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        linkedCampaign.description,
                                        fontSize = 13.sp,
                                        color = OnSurfaceVariantLight,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                Spacer(Modifier.height(12.dp))
                                Button(
                                    onClick = { onViewCampaign(linkedCampaign.id) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                ) {
                                    Text(s.blogViewCampaign, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    // Like & comment count summary
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (likeCount > 0) {
                            Text(
                                if (isLiked) "♥" else "♡",
                                fontSize = 15.sp,
                                color = if (isLiked) ErrorColor else OnSurfaceVariantLight,
                            )
                            Text("$likeCount", fontSize = 14.sp, color = OnSurfaceVariantLight)
                        }
                        if (post.commentCount > 0) {
                            if (likeCount > 0) Text("·", fontSize = 14.sp, color = OnSurfaceVariantLight)
                            Text("${post.commentCount} ${s.blogCommentsLabel}", fontSize = 14.sp, color = OnSurfaceVariantLight)
                        }
                    }

                    HorizontalDivider(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), thickness = 1.dp, color = OutlineLight)

                    // Action bar
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        FilledTonalButton(
                            onClick = onLike,
                            enabled = !isLiking,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (isLiked) ErrorColor.copy(alpha = 0.1f) else SurfaceVariantLight,
                                contentColor = if (isLiked) ErrorColor else OnSurfaceVariantLight,
                            ),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            if (isLiking) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = GreenPrimary, strokeWidth = 2.dp)
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Text(
                                        if (isLiked) "♥" else "♡",
                                        fontSize = 18.sp,
                                    )
                                    Text(
                                        if (isLiked) s.blogLiked else s.blogLike,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // Add comment input
        item {
            AddCommentSection(
                accessToken = accessToken,
                blogId = post.id,
                onCommentAdded = onCommentAdded,
            )
            Spacer(Modifier.height(8.dp))
        }

        // Comments list
        if (post.comments.isNotEmpty()) {
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = SurfaceLight),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            s.blogCommentsTitle(post.commentCount),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnSurfaceLight,
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            items(post.comments, key = { it.id }) { comment ->
                CommentItem(
                    comment = comment,
                    currentUserId = currentUserId,
                    blogId = post.id,
                    accessToken = accessToken,
                    onUpdated = onCommentUpdated,
                    onDeleted = onCommentDeleted,
                )
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

private fun Modifier.clickable(onClick: () -> Unit): Modifier = this.then(
    Modifier.clickable(onClick = onClick)
)

// -- Add comment section (Material 3 style) -------------------------------------

@Composable
private fun AddCommentSection(
    accessToken: String,
    blogId: String,
    onCommentAdded: (CommentDto) -> Unit,
) {
    val s = LocalAppStrings.current
    val user = SettingsStore.getUser()
    val scope = rememberCoroutineScope()
    var text by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = SurfaceLight),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = GreenPrimaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(initials(user?.fullName ?: "", user?.username ?: "?"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GreenOnPrimaryContainer)
                }
            }
            OutlinedTextField(
                value = text,
                onValueChange = { if (it.length <= 500) text = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(s.blogAddCommentHint, fontSize = 14.sp, color = OnSurfaceVariantLight) },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenPrimary,
                    unfocusedBorderColor = OutlineLight,
                    focusedContainerColor = SurfaceVariantLight,
                    unfocusedContainerColor = SurfaceVariantLight,
                ),
            )
            IconButton(
                onClick = {
                    val trimmed = text.trim()
                    if (trimmed.isEmpty() || isSubmitting) return@IconButton
                    scope.launch {
                        isSubmitting = true
                        try {
                            val result = addComment(blogId, trimmed, accessToken)
                            onCommentAdded(result.data)
                            text = ""
                        } catch (_: Throwable) { }
                        isSubmitting = false
                    }
                },
                enabled = text.isNotBlank() && !isSubmitting,
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = GreenPrimary, strokeWidth = 2.dp)
                } else {
                    Text("➤", fontSize = 18.sp, color = if (text.isNotBlank()) GreenPrimary else OnSurfaceVariantLight)
                }
            }
        }
    }
}

// -- Comment item (Material 3 style) -------------------------------------------

@Composable
private fun CommentItem(
    comment: CommentDto,
    currentUserId: String?,
    blogId: String,
    accessToken: String,
    onUpdated: (String, String) -> Unit,
    onDeleted: (String) -> Unit,
) {
    val s = LocalAppStrings.current
    val scope = rememberCoroutineScope()
    val isOwner = currentUserId == comment.user.id

    var showMenu by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var editText by remember { mutableStateOf(comment.content) }
    var isSubmitting by remember { mutableStateOf(false) }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(s.blogEditComment) },
            text = {
                OutlinedTextField(
                    value = editText,
                    onValueChange = { if (it.length <= 500) editText = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    shape = RoundedCornerShape(8.dp),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmed = editText.trim()
                        if (trimmed.isEmpty()) return@TextButton
                        scope.launch {
                            isSubmitting = true
                            try {
                                val result = updateComment(blogId, comment.id, trimmed, accessToken)
                                onUpdated(comment.id, result.data.content)
                                showEditDialog = false
                            } catch (_: Throwable) { }
                            isSubmitting = false
                        }
                    },
                    enabled = editText.isNotBlank() && !isSubmitting,
                ) {
                    if (isSubmitting) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    else Text(s.save)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text(s.cancel) }
            },
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(s.blogDeleteCommentTitle) },
            text = { Text(s.blogDeleteCommentConfirm) },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            isSubmitting = true
                            try {
                                deleteComment(blogId, comment.id, accessToken)
                                onDeleted(comment.id)
                                showDeleteDialog = false
                            } catch (_: Throwable) { }
                            isSubmitting = false
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = ErrorColor),
                ) {
                    if (isSubmitting) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    else Text(s.delete)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text(s.cancel) }
            },
        )
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = SurfaceLight),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = GreenPrimaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(initials(comment.user.fullName, comment.user.username), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GreenOnPrimaryContainer)
                }
            }
            Column(Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        comment.user.fullName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OnSurfaceLight,
                    )
                    Text(formatDateTimeLocal(comment.createdAt), fontSize = 11.sp, color = OnSurfaceVariantLight)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    comment.content,
                    fontSize = 14.sp,
                    color = OnSurfaceLight,
                    lineHeight = 20.sp,
                )
            }
            if (isOwner) {
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(28.dp),
                    ) {
                        Text("⋮", fontSize = 18.sp, color = OnSurfaceVariantLight)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(s.blogEdit, color = OnSurfaceLight) },
                            onClick = {
                                showMenu = false
                                editText = comment.content
                                showEditDialog = true
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(s.blogDelete, color = ErrorColor) },
                            onClick = {
                                showMenu = false
                                showDeleteDialog = true
                            },
                        )
                    }
                }
            }
        }
    }
}

// -- Error state (Material 3 style) --------------------------------------------

@Composable
private fun ErrorState(
    message: String,
    retryLabel: String,
    onBack: () -> Unit,
    onRetry: () -> Unit,
) {
    val s = LocalAppStrings.current
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("😕", fontSize = 40.sp)
            Text(message, fontSize = 14.sp, color = OnSurfaceVariantLight, textAlign = TextAlign.Center)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onBack,
                    shape = RoundedCornerShape(10.dp),
                ) { Text(s.back) }
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape = RoundedCornerShape(10.dp),
                ) { Text(retryLabel) }
            }
        }
    }
}
