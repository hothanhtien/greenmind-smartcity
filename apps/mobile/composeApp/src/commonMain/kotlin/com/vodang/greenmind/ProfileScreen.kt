package com.vodang.greenmind

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.auth.ApiException
import com.vodang.greenmind.api.preappsurvey.PreAppSurveyDto
import com.vodang.greenmind.api.preappsurvey.getPreAppSurveyByUser
import com.vodang.greenmind.home.components.OceanScoreCard
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.store.BillStore
import com.vodang.greenmind.store.MealStore
import com.vodang.greenmind.store.OceanStore
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.store.WalkDistanceStore
import com.vodang.greenmind.theme.Green50
import com.vodang.greenmind.theme.Green600
import com.vodang.greenmind.theme.Green700
import com.vodang.greenmind.theme.Green800
import com.vodang.greenmind.theme.SurfaceGray
import com.vodang.greenmind.theme.TextPrimary
import com.vodang.greenmind.theme.TextSecondary
import com.vodang.greenmind.fmt
import com.vodang.greenmind.time.currentTimeMillis
import com.vodang.greenmind.wastereport.NetworkImage

private fun isToday(millis: Long) = (currentTimeMillis() - millis) < 86_400_000L

private fun formatDistance(meters: Int): String = when {
    meters < 1000 -> "${meters} m"
    else -> "${(meters / 1000.0).fmt(2)} km"
}

@Composable
fun ProfileScreen(scrollState: ScrollState? = null) {
    val s = LocalAppStrings.current
    val user by SettingsStore.user.collectAsState()
    val distMeters by WalkDistanceStore.distanceMeters.collectAsState()
    val meals by MealStore.meals.collectAsState()
    val bills by BillStore.bills.collectAsState()

    LaunchedEffect(Unit) {
        WalkDistanceStore.startPolling()
        OceanStore.load()
    }

    val walkTimeMin = (distMeters / 84).coerceAtLeast(0)
    val calories = (distMeters / 1000.0 * 60).toInt()

    val todayMeals = meals.filter { isToday(it.timestampMillis) }
    val todayBills = bills.filter { isToday(it.timestampMillis) }
    val avgPlant = if (todayMeals.isEmpty()) 0 else todayMeals.map { it.plantRatio }.average().toInt()
    val avgGreen = if (todayBills.isEmpty()) 0 else todayBills.map { it.greenRatio }.average().toInt()

    var survey by remember { mutableStateOf<PreAppSurveyDto?>(null) }

    LaunchedEffect(user?.id) {
        val token = SettingsStore.getAccessToken() ?: return@LaunchedEffect
        val userId = user?.id ?: return@LaunchedEffect
        try {
            survey = getPreAppSurveyByUser(token, userId)
        } catch (_: ApiException) { } catch (_: Throwable) { }
    }

    val localScrollState = scrollState ?: rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceGray)
            .verticalScroll(localScrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Hero Card ─────────────────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                // Header section
                Row(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Avatar
                    val initials = (user?.fullName?.split(" ")?.mapNotNull { it.firstOrNull()?.uppercaseChar() }?.take(2)?.joinToString("")
                        ?: user?.username?.take(2)?.uppercase() ?: "?")
                    Box(
                        modifier = Modifier.size(64.dp).clip(CircleShape).background(Green50),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(initials, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Green800)
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(user?.fullName?.ifBlank { s.profileName } ?: s.profileName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(user?.email ?: s.profileEmail, fontSize = 13.sp, color = TextSecondary)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(shape = RoundedCornerShape(20.dp), color = Green50) {
                                Text(
                                    user?.role?.replaceFirstChar { it.uppercase() } ?: s.notSet,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Green700,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                            if (!user?.location.isNullOrBlank()) {
                                Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = TextSecondary)
                                Text(user?.location ?: "", fontSize = 12.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFF0F0F0))

                // Journey stats
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatItem(icon = Icons.AutoMirrored.Filled.DirectionsWalk, value = formatDistance(distMeters), label = s.walkDistance, modifier = Modifier.weight(1f).fillMaxHeight())
                    StatItem(icon = Icons.Filled.Timer, value = s.walkTimeMin(walkTimeMin), label = s.walkTimeLabel, modifier = Modifier.weight(1f).fillMaxHeight())
                    StatItem(icon = Icons.Filled.LocalFireDepartment, value = s.caloriesKcal(calories), label = s.caloriesLabel, modifier = Modifier.weight(1f).fillMaxHeight())
                }
            }
        }

        // ── Personal Info ────────────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                InfoRow(icon = Icons.Filled.Person, label = s.fullName, value = user?.fullName?.ifBlank { s.notSet } ?: s.notSet)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF0F0F0))
                InfoRow(icon = Icons.Filled.Email, label = s.emailAddress, value = user?.email ?: s.notSet)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF0F0F0))
                InfoRow(icon = Icons.Filled.Wc, label = s.genderLabel, value = user?.gender?.replaceFirstChar { it.uppercase() } ?: s.notSet)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF0F0F0))
                InfoRow(icon = Icons.Filled.Cake, label = s.ageLabel, value = user?.age?.let { "$it" } ?: s.notSet)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF0F0F0))
                InfoRow(icon = Icons.Filled.LocationOn, label = s.locationLabel, value = user?.location?.ifBlank { s.notSet } ?: s.notSet)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF0F0F0))
                InfoRow(icon = Icons.Filled.Badge, label = s.roleLabel, value = user?.role?.replaceFirstChar { it.uppercase() } ?: s.notSet)
            }
        }

        // ── My QR Code ──────────────────────────────────────────────────────
        user?.id?.let { uid ->
            QrCodeCard(userId = uid, s = s)
        }

        // ── Eco Activity Today ───────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(s.ecoToday, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    EcoCard(icon = Icons.Filled.Restaurant, label = s.mealsToday(todayMeals.size), value = "$avgPlant%", progress = avgPlant / 100f, color = Green700, modifier = Modifier.weight(1f))
                    EcoCard(icon = Icons.Filled.Receipt, label = s.billsToday(todayBills.size), value = "$avgGreen%", progress = avgGreen / 100f, color = Color(0xFFF57F17), modifier = Modifier.weight(1f))
                }
            }
        }

        // ── OCEAN Score ─────────────────────────────────────────────────────
        OceanScoreCard()

        // ── Habit Profile ─────────────────────────────────────────────────────
        survey?.let { sv ->
            HabitProfileCard(sv = sv, s = s)
        }

        Spacer(Modifier.height(16.dp))
    }
}

// ── Hero Stats ────────────────────────────────────────────────────────────────

@Composable
private fun StatItem(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(12.dp)).background(Green50).padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp), tint = Green700)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Green800)
        Text(label, fontSize = 10.sp, color = TextSecondary)
    }
}

// ── QR Code Card ──────────────────────────────────────────────────────────────

@Composable
private fun QrCodeCard(userId: String, s: com.vodang.greenmind.i18n.AppStrings) {
    val qrUrl = remember(userId) {
        "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=$userId"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(s.myQrCode, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            NetworkImage(
                url = qrUrl,
                modifier = Modifier.size(200.dp),
            )
            Text(s.qrCodeHint, fontSize = 11.sp, color = Color.Gray)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(s.userIdLabel, fontSize = 11.sp, color = Color.Gray)
                Text(userId, fontSize = 11.sp, color = Green700, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ── Personal Info Row ─────────────────────────────────────────────────────────

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = Green700)
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(label, fontSize = 11.sp, color = TextSecondary)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
        }
    }
}

// ── Eco Card ──────────────────────────────────────────────────────────────────

@Composable
private fun EcoCard(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, progress: Float, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(label, fontSize = 11.sp, color = TextSecondary)
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
                color = color,
                trackColor = color.copy(alpha = 0.2f)
            )
        }
    }
}

// ── Habit Profile ─────────────────────────────────────────────────────────────

@Composable
private fun HabitProfileCard(sv: PreAppSurveyDto, s: com.vodang.greenmind.i18n.AppStrings) {
    var expanded by remember { mutableStateOf(false) }
    val isVi = s.langCode == "vi"

    val rows = listOf(
        HabitItem("💸", "Daily Spending", "Chi tiêu hàng ngày", "${sv.dailySpending} ${if (isVi) "đ/ngày" else "VND/day"}"),
        HabitItem("🚶", "Daily Distance", "Quãng đường hàng ngày", "${sv.dailyDistance} km"),
        HabitItem("📊", "Spending Variation", "Biến động chi tiêu", scaleDisplay(sv.spendingVariation)),
        HabitItem("🛍️", "Brand Trial", "Thử thương hiệu mới", scaleDisplay(sv.brandTrial)),
        HabitItem("📋", "Shopping List", "Mua sắm theo danh sách", scaleDisplay(sv.shoppingList)),
        HabitItem("🗺️", "Explores New Places", "Khám phá nơi mới", scaleDisplay(sv.newPlaces)),
        HabitItem("🚌", "Public Transport", "Giao thông công cộng", scaleDisplay(sv.publicTransport)),
        HabitItem("🗓️", "Stable Schedule", "Lịch trình ổn định", scaleDisplay(sv.stableSchedule)),
        HabitItem("🌙", "Night Outings", "Ra ngoài buổi tối", "${sv.nightOutings} ${if (isVi) "đêm/tuần" else "nights/week"}"),
        HabitItem("🥗", "Healthy Eating", "Ăn uống lành mạnh", scaleDisplay(sv.healthyEating)),
        HabitItem("📱", "Social Media", "Mạng xã hội", scaleDisplay(sv.socialMedia)),
        HabitItem("🎯", "Goal Setting", "Đặt mục tiêu", scaleDisplay(sv.goalSetting)),
        HabitItem("🎭", "Mood Swings", "Thay đổi tâm trạng", scaleDisplay(sv.moodSwings)),
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(20.dp), tint = Green700)
                Text(s.habitProfile, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, modifier = Modifier.weight(1f))
                Text(if (expanded) "▲" else "▼", fontSize = 12.sp, color = TextSecondary)
            }
            if (expanded) {
                HorizontalDivider(color = Color(0xFFF0F0F0))
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    rows.forEachIndexed { i, row ->
                        HabitRow(emoji = row.emoji, label = if (isVi) row.labelVi else row.labelEn, value = row.display)
                        if (i < rows.lastIndex) HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF0F0F0))
                    }
                }
            }
        }
    }
}

private data class HabitItem(val emoji: String, val labelEn: String, val labelVi: String, val display: String)
private fun scaleDisplay(value: Int, max: Int = 5): String = "●".repeat(value) + "○".repeat(max - value)

@Composable
private fun HabitRow(emoji: String, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(emoji, fontSize = 16.sp, modifier = Modifier.width(24.dp))
        Text(label, fontSize = 13.sp, color = TextSecondary, modifier = Modifier.weight(1f))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Green700)
    }
}
