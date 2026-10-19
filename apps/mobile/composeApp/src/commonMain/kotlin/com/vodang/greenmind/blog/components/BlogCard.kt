package com.vodang.greenmind.blog.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.blog.BlogDto
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.theme.Green100
import com.vodang.greenmind.theme.Green800
import com.vodang.greenmind.theme.Gray600
import com.vodang.greenmind.theme.Gray400

private val green800 = Green800
private val green100 = Green100
private val gray600 = Gray600
private val gray400 = Gray400
private val heartRed = Color(0xFFE53935)

private fun formatDateShort(iso: String): String = try {
    val parts = iso.substringBefore('T').split('-')
    if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else iso
} catch (_: Throwable) { iso }

private fun authorInitials(name: String, username: String): String =
    name.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")
        .ifBlank { username.take(2).uppercase() }

private fun stripHtmlBasic(html: String): String =
    html.replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), " ")
        .replace(Regex("<[^>]+>"), "")
        .replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">")
        .replace("&nbsp;", " ").replace("&quot;", "\"").replace("&#39;", "'")
        .replace(Regex("\\s+"), " ").trim()

@Composable
fun BlogCard(
    post: BlogDto,
    effectiveLiked: Boolean,
    effectiveLikeCount: Int,
    onClick: () -> Unit,
    onLike: () -> Unit,
    isContentLoading: Boolean = false,
) {
    val s = LocalAppStrings.current
    val initials = authorInitials(post.author?.fullName ?: "", post.author?.username ?: "?")
    val plainContent = post.content?.let(::stripHtmlBasic)
    var showFullContent by remember { mutableStateOf(false) }
    val isLongContent = (plainContent?.length ?: 0) > 250

    // Shimmer animation
    val shimmerTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerAlpha by shimmerTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Reverse),
        label = "alpha",
    )
    val shimmerColors = listOf(
        Color(0xFFE0E0E0).copy(alpha = shimmerAlpha),
        Color(0xFFF5F5F5).copy(alpha = shimmerAlpha),
        Color(0xFFE0E0E0).copy(alpha = shimmerAlpha),
    )
    val shimmerBrush = Brush.linearGradient(shimmerColors, start = Offset.Zero, end = Offset(1000f, 0f))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 1.dp,
    ) {
        Column {
            Spacer(Modifier.height(12.dp))

            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(start = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    Modifier.size(40.dp).clip(CircleShape).background(green100),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(initials, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = green800)
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        post.author?.fullName ?: post.author?.username ?: "",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF050505),
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(formatDateShort(post.createdAt), fontSize = 12.sp, color = gray600)
                        Text("·", fontSize = 12.sp, color = gray600)
                        Text("Public", fontSize = 12.sp, color = gray600)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                post.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(horizontal = 16.dp),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF050505),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 24.sp,
            )

            if (isContentLoading) {
                Spacer(Modifier.height(6.dp))
                Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(4.dp)).background(shimmerBrush))
                    Box(Modifier.fillMaxWidth(0.85f).height(14.dp).clip(RoundedCornerShape(4.dp)).background(shimmerBrush))
                    Box(Modifier.fillMaxWidth(0.6f).height(14.dp).clip(RoundedCornerShape(4.dp)).background(shimmerBrush))
                }
            } else if (!plainContent.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                val content = if (isLongContent && !showFullContent) plainContent.take(250) + "..." else plainContent
                Text(
                    content,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onClick)
                        .padding(horizontal = 16.dp),
                    fontSize = 15.sp,
                    color = Color(0xFF1A1A1A),
                    maxLines = if (showFullContent) Int.MAX_VALUE else 5,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 21.sp,
                )
                if (isLongContent) {
                    TextButton(
                        onClick = { showFullContent = !showFullContent },
                        modifier = Modifier.padding(start = 8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    ) {
                        Text(
                            if (showFullContent) "Show less" else "See more",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = gray600,
                        )
                    }
                }
            }

            if (post.tags.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Row(
                    Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    post.tags.take(4).forEach { tag ->
                        Text("#$tag", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = green800)
                    }
                }
            }

            if (effectiveLikeCount > 0 || post.commentCount > 0) {
                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (effectiveLikeCount > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(if (effectiveLiked) "♥" else "♡", fontSize = 14.sp, color = if (effectiveLiked) heartRed else gray600)
                            Text("$effectiveLikeCount", fontSize = 13.sp, color = gray600)
                        }
                    }
                    if (post.commentCount > 0) {
                        Text("${post.commentCount} ${s.blogComments}", fontSize = 13.sp, color = gray600)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFE4E6EB))
            Spacer(Modifier.height(2.dp))

            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                TextButton(
                    onClick = onLike,
                    modifier = Modifier.weight(1f).height(40.dp),
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            if (effectiveLiked) "♥" else "♡",
                            fontSize = 18.sp,
                            color = if (effectiveLiked) heartRed else gray600,
                        )
                        Text(
                            if (effectiveLiked) "Liked" else "Like",
                            fontSize = 13.sp,
                            fontWeight = if (effectiveLiked) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (effectiveLiked) heartRed else gray600,
                        )
                    }
                }
                TextButton(
                    onClick = onClick,
                    modifier = Modifier.weight(1f).height(40.dp),
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(Icons.Filled.ChatBubble, contentDescription = null, modifier = Modifier.size(18.dp), tint = gray600)
                        Text(s.blogCommentLabel, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = gray600)
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}
