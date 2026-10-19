package com.vodang.greenmind.blog.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.blog.LeaderboardEntryDto
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.theme.Green800
import com.vodang.greenmind.theme.Green50
import com.vodang.greenmind.theme.Green100
import com.vodang.greenmind.theme.Gray400
import com.vodang.greenmind.theme.Gray500
import com.vodang.greenmind.theme.Gray600
import com.vodang.greenmind.theme.TextSecondary

private val green800 = Green800
private val green50 = Green50
private val green100 = Green100
private val gray400 = Gray400
private val gray500 = Gray500
private val gray600 = Gray600

private val goldColor = Color(0xFFFFD700)
private val silverColor = Color(0xFFB0BEC5)
private val bronzeColor = Color(0xFFCD7F32)
private val goldLight = Color(0xFFFFF8E1)
private val silverLight = Color(0xFFF5F5F5)
private val bronzeLight = Color(0xFFF5E6D3)

private val rankColors = mapOf(1 to goldColor, 2 to silverColor, 3 to bronzeColor)
private val rankBgColors = mapOf(1 to goldLight, 2 to silverLight, 3 to bronzeLight)

private fun authorInitials(name: String, username: String): String =
    name.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")
        .ifBlank { username.take(2).uppercase() }

@Composable
fun LeaderboardTab(
    accessToken: String,
    onLoad: suspend () -> List<LeaderboardEntryDto>,
    currentUserId: String? = null,
) {
    val s = LocalAppStrings.current
    var entries by remember { mutableStateOf<List<LeaderboardEntryDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var reloadKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(accessToken, reloadKey) {
        isLoading = true
        error = null
        try {
            entries = onLoad()
        } catch (e: Throwable) {
            error = e.message ?: s.blogErrorLoad
        }
        isLoading = false
    }

    Column(Modifier.fillMaxSize()) {
        Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = goldColor,
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(s.leaderboardTitle, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Gray600)
                    Text(s.leaderboardSubtitle, fontSize = 12.sp, color = gray400)
                }
            }
        }

        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = green800)
            }
            error != null -> Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(error!!, fontSize = 14.sp, color = gray500, textAlign = TextAlign.Center)
                    Button(onClick = { reloadKey++ }, colors = ButtonDefaults.buttonColors(containerColor = green800), shape = RoundedCornerShape(10.dp)) {
                        Text(s.blogRetry)
                    }
                }
            }
            entries.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(s.leaderboardTrophy, fontSize = 40.sp)
                    Text(s.leaderboardEmpty, fontSize = 14.sp, color = gray500)
                }
            }
            else -> LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (entries.size >= 3) {
                    item { PodiumRow(entries.take(3)) }
                    item { Spacer(Modifier.height(4.dp)) }
                    val showTop5 = if (entries.size > 3) entries.subList(3, minOf(entries.size, 5)) else emptyList()
                    items(showTop5, key = { it.userId }) { entry ->
                        LeaderboardRow(entry = entry)
                    }
                } else {
                    items(entries, key = { it.userId }) { entry ->
                        LeaderboardRow(entry = entry)
                    }
                }

                if (entries.size > 5) {
                    item { LeaderboardDivider() }

                    val userEntry = currentUserId?.let { uid -> entries.find { it.userId == uid && it.rank > 5 } }
                    if (userEntry != null) {
                        item(key = "user-${userEntry.userId}") {
                            CurrentUserRow(entry = userEntry)
                        }
                    } else {
                        // Show a short summary
                        item {
                            Card(
                                Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                            ) {
                                Box(Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                                    Text(s.leaderboardMoreContributors(entries.size - 5), fontSize = 13.sp, color = gray500)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Podium Row ────────────────────────────────────────────────────────────────

@Composable
fun PodiumRow(top3: List<LeaderboardEntryDto>) {
    if (top3.size != 3) return
    val order = listOf(top3[1], top3[0], top3[2])
    val podiums = listOf(
        PodiumStyle(silverColor, silverLight, 48.dp, 1, 2),
        PodiumStyle(goldColor, goldLight, 56.dp, 0, 1),
        PodiumStyle(bronzeColor, bronzeLight, 44.dp, 2, 3),
    )

    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(top = 20.dp, bottom = 16.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom,
            ) {
                order.forEachIndexed { i, entry ->
                    val style = podiums[i]
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = if (i == 1) Modifier.offset(y = (-8).dp) else Modifier,
                    ) {
                        // Medal icon for 1st
                        if (i == 1) {
                            Icon(
                                Icons.Filled.EmojiEvents,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = goldColor,
                            )
                        }

                        // Rank circle
                        Box(
                            modifier = Modifier
                                .size(style.avatarSize)
                                .clip(CircleShape)
                                .background(style.color)
                                .then(if (i == 1) Modifier.border(3.dp, goldColor, CircleShape) else Modifier),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "${style.rankNumber}",
                                fontSize = (style.avatarSize.value / 2.5).sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                        }

                        // Name
                        Text(
                            entry.fullName.split(" ").firstOrNull() ?: entry.username,
                            fontSize = if (i == 1) 13.sp else 11.sp,
                            fontWeight = if (i == 1) FontWeight.SemiBold else FontWeight.Medium,
                            color = Color(0xFF1C1B1F),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        )

                        // Report count badge
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = style.color.copy(alpha = 0.2f),
                        ) {
                            Text(
                                "${entry.reportCount} reports",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = style.color,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class PodiumStyle(
    val color: Color,
    val bgColor: Color,
    val avatarSize: androidx.compose.ui.unit.Dp,
    val rankNumber: Int,
    val actualRank: Int,
)

// ── Leaderboard Divider ───────────────────────────────────────────────────────

@Composable
fun LeaderboardDivider() {
    val s = LocalAppStrings.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = gray400.copy(alpha = 0.4f))
        Text(s.leaderboardSeparator, fontSize = 14.sp, color = gray400, fontWeight = FontWeight.Bold)
        HorizontalDivider(modifier = Modifier.weight(1f), color = gray400.copy(alpha = 0.4f))
    }
}

// ── Current User Row ──────────────────────────────────────────────────────────

@Composable
fun CurrentUserRow(entry: LeaderboardEntryDto) {
    val s = LocalAppStrings.current
    val initials = authorInitials(entry.fullName, entry.username)

    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = green50),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                Modifier.size(36.dp).clip(CircleShape).background(green800),
                contentAlignment = Alignment.Center,
            ) {
                Text(s.leaderboardRank + entry.rank.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Box(
                Modifier.size(36.dp).clip(CircleShape).background(Color.White).border(2.dp, green800, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(initials, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = green800)
            }
            Column(Modifier.weight(1f)) {
                Text(entry.fullName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1C1B1F))
                Text(s.leaderboardUsername + entry.username, fontSize = 11.sp, color = gray400)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Surface(shape = RoundedCornerShape(20.dp), color = green800) {
                    Text(
                        s.leaderboardReports(entry.reportCount),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
                Text(s.leaderboardYou, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = green800)
            }
        }
    }
}

// ── Leaderboard Row ───────────────────────────────────────────────────────────

@Composable
fun LeaderboardRow(entry: LeaderboardEntryDto) {
    val s = LocalAppStrings.current
    val initials = authorInitials(entry.fullName, entry.username)

    // Determine rank accent color
    val rankColor = rankColors[entry.rank]
    val rankBg = rankBgColors[entry.rank]

    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                Modifier.size(36.dp).clip(CircleShape).background(rankBg ?: Color(0xFFF3F4F6)),
                contentAlignment = Alignment.Center,
            ) {
                Text(s.leaderboardRank + entry.rank.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = rankColor ?: gray500)
            }
            Box(
                Modifier.size(36.dp).clip(CircleShape).background(green100),
                contentAlignment = Alignment.Center,
            ) {
                Text(initials, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = green800)
            }
            Column(Modifier.weight(1f)) {
                Text(entry.fullName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1C1B1F))
                Text(s.leaderboardUsername + entry.username, fontSize = 11.sp, color = gray400)
            }
            Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFA5D6A7)) {
                Text(
                    s.leaderboardReports(entry.reportCount),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = green800,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
    }
}
