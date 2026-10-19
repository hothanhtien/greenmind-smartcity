package com.vodang.greenmind.wastesort

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.households.DetectItemMassDto
import com.vodang.greenmind.api.households.DetectTrashHistoryDto
import com.vodang.greenmind.api.households.getHistoryByHouseholdByType
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.platform.BackHandler
import com.vodang.greenmind.fmt
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.wastereport.NetworkImage
import com.vodang.greenmind.wastereport.ZoomableImagePreview
import com.vodang.greenmind.util.AppLogger
import com.vodang.greenmind.time.formatDateTimeLocal
import kotlinx.coroutines.launch
import kotlin.math.roundToLong

private val green800m = Color(0xFF2E7D32)
private val green50m  = Color(0xFFE8F5E9)
private val blue600m  = Color(0xFF1976D2)
private val blue50m   = Color(0xFFE3F2FD)
private val orange600m = Color(0xFFF57C00)
private val orange50m = Color(0xFFFFF3E0)
private val gray100m  = Color(0xFFF5F5F5)
private val gray200m  = Color(0xFFEEEEEE)

private fun formatDateShort(iso: String): String = try {
    val parts = iso.substringBefore('T').split('-')
    if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else iso
} catch (_: Throwable) { iso }

/** Vietnamese currency formatting: "10.000 ₫" (uses '.' as thousand separator). */
private fun formatVndCurrency(amount: Double): String {
    val n = amount.roundToLong()
    val abs = if (n < 0) -n else n
    val s = abs.toString()
    val sb = StringBuilder()
    var count = 0
    for (i in s.length - 1 downTo 0) {
        sb.append(s[i])
        count++
        if (count == 3 && i != 0) {
            sb.append('.')
            count = 0
        }
    }
    val grouped = sb.reverse().toString()
    val sign = if (n < 0) "-" else ""
    return "$sign$grouped ₫"
}

@Composable
fun WasteTotalMassScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val s     = LocalAppStrings.current
    val scope = rememberCoroutineScope()
    val accessToken = SettingsStore.getAccessToken() ?: return

    var history  by remember { mutableStateOf<List<DetectTrashHistoryDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error    by remember { mutableStateOf<String?>(null) }
    var selectedItem by remember { mutableStateOf<DetectTrashHistoryDto?>(null) }
    var showScanDialog by remember { mutableStateOf(false) }

    fun reload() {
        scope.launch {
            isLoading = true
            error = null
            try {
                history = getHistoryByHouseholdByType(accessToken, "total_mass").data
            } catch (e: Throwable) {
                error = e.message ?: ""
            }
            isLoading = false
        }
    }

    LaunchedEffect(accessToken) { reload() }

    val totalKg       = history.mapNotNull { it.totalMassKg }.sum()
    val estimatedMoney = totalKg * 500  // VND mock

    // ── Detail overlay ───────────────────────────────────────────────────────
    selectedItem?.let { item ->
        MassDetailScreen(
            item = item,
            onBack = { selectedItem = null },
        )
        return
    }

    // ── Scan dialog ───────────────────────────────────────────────────────────
    if (showScanDialog) {
        WasteTotalMassPicker(
            onScanned = {
                showScanDialog = false
                reload()
            },
            onDismiss = { showScanDialog = false },
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
        ) {
            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = green800m)
                }
                error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(error!!, fontSize = 14.sp, color = Color(0xFFB71C1C))
                        Button(onClick = { reload() }, colors = ButtonDefaults.buttonColors(containerColor = green800m)) {
                            Text(s.retry)
                        }
                    }
                }
                history.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.Inventory, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                        Text(s.wasteTotalMassEmpty, fontSize = 14.sp, color = Color.Gray)
                    }
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // ── Summary cards ──────────────────────────────────────────────
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = green50m),
                                shape = RoundedCornerShape(16.dp),
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Icon(Icons.Filled.Inventory, contentDescription = null, modifier = Modifier.size(28.dp), tint = green800m)
                                    Text(s.wasteTotalMassTotalKg, fontSize = 12.sp, color = Color.Gray)
                                    Text("%.2f kg".fmt(totalKg), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = green800m)
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = orange50m),
                                shape = RoundedCornerShape(16.dp),
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Icon(Icons.Filled.AccountBalance, contentDescription = null, modifier = Modifier.size(28.dp), tint = orange600m)
                                    Text(s.wasteTotalMassEstimated, fontSize = 12.sp, color = Color.Gray)
                                    Text(formatVndCurrency(estimatedMoney), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = orange600m)
                                }
                            }
                        }
                    }

                    // ── History header ───────────────────────────────────────────
                    item {
                        Text(
                            "${s.wasteTotalMassHistory} (${history.size})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray,
                        )
                    }

                    // ── History rows ─────────────────────────────────────────────
                    items(history, key = { it.id }) { item ->
                        MassHistoryRow(
                            item = item,
                            s = s,
                            onClick = { selectedItem = item },
                        )
                    }

                    // FAB clearance
                    item { Spacer(Modifier.height(72.dp)) }
                }
            }
        }

        // ── FAB ─────────────────────────────────────────────────────────────────
        FloatingActionButton(
            onClick = { showScanDialog = true },
            containerColor = green800m,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        ) {
            Text("+", fontSize = 28.sp, fontWeight = FontWeight.Light)
        }
    }
}

// ── History row ───────────────────────────────────────────────────────────────

@Composable
private fun MassHistoryRow(
    item: DetectTrashHistoryDto,
    s: com.vodang.greenmind.i18n.AppStrings,
    onClick: () -> Unit,
) {
    val mass = item.totalMassKg ?: 0.0
    val dateStr = formatDateShort(item.createdAt ?: "")
    val scanType = item.detectType ?: ""
    val imageUrl = item.aiAnalysis ?: item.imageUrl

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = gray100m),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Thumbnail image
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(gray200m),
                contentAlignment = Alignment.Center,
            ) {
                if (!imageUrl.isNullOrBlank()) {
                    NetworkImage(
                        url = imageUrl,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Icon(Icons.Filled.Inventory, contentDescription = null, modifier = Modifier.size(28.dp), tint = green800m)
                }
            }

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    scanType.replaceFirstChar { it.uppercase() },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF212121),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(dateStr, fontSize = 12.sp, color = Color.Gray)
                val addr = item.household?.address
                if (!addr.isNullOrBlank()) {
                    Text(addr, fontSize = 11.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            // Mass
            Column(horizontalAlignment = Alignment.End) {
                Text("%.2f kg".fmt(mass), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = green800m)
                Spacer(Modifier.height(2.dp))
                val statusColor = when (item.status) {
                    "brought_out" -> orange600m
                    "picked_up"   -> blue600m
                    else          -> Color.Gray
                }
                val statusLabel = when (item.status) {
                    "brought_out" -> s.wasteSorted
                    "picked_up"   -> s.wasteCollected
                    else          -> s.wasteScanned
                }
                Text(statusLabel, fontSize = 11.sp, color = statusColor, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ── Detail screen ─────────────────────────────────────────────────────────────

@Composable
private fun MassDetailScreen(item: DetectTrashHistoryDto, onBack: () -> Unit) {
    val s = LocalAppStrings.current
    val scrollState = rememberScrollState()
    val imageUrl = item.aiAnalysis ?: item.imageUrl

    BackHandler(onBack = onBack)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ── Main image ─────────────────────────────────────────────────────
            if (!imageUrl.isNullOrBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth().height(260.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    ZoomableImagePreview(
                        url = imageUrl,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            // ── Depth Map ────────────────────────────────────────────────────
            if (!item.depthMapUrl.isNullOrBlank()) {
                Text(
                    "Bản đồ Chiều sâu",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF424242),
                )
                Card(
                    modifier = Modifier.fillMaxWidth().height(220.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    ZoomableImagePreview(
                        url = item.depthMapUrl.orEmpty(),
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            // ── Summary row ────────────────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = green50m),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(Icons.Filled.Inventory, contentDescription = null, modifier = Modifier.size(24.dp), tint = green800m)
                        Text("%.2f kg".fmt(item.totalMassKg ?: 0.0), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = green800m)
                        Text(s.wasteTotalMassTotalKg, fontSize = 11.sp, color = Color.Gray)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = orange50m),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(Icons.Filled.AccountBalance, contentDescription = null, modifier = Modifier.size(24.dp), tint = orange600m)
                        Text(formatVndCurrency((item.totalMassKg ?: 0.0) * 500), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = orange600m)
                        Text(s.wasteTotalMassEstimated, fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }

            // ── Info card ──────────────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = gray100m),
                shape = RoundedCornerShape(12.dp),
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val statusColor = when (item.status) {
                        "brought_out" -> orange600m
                        "picked_up"   -> blue600m
                        else          -> green800m
                    }
                    val statusLabel = when (item.status) {
                        "brought_out" -> s.wasteSorted
                        "picked_up"   -> s.wasteCollected
                        else          -> s.wasteScanned
                    }
                    InfoRow(s.wasteTotalMassDetailStatus, statusLabel, statusColor)
                    InfoRow(s.wasteTotalMassDetailDate, formatDateTimeLocal(item.createdAt ?: ""))
                    InfoRow(s.wasteTotalMassDetailType, item.detectType?.replaceFirstChar { it.uppercase() } ?: "")
                    item.household?.address?.let { addr ->
                        InfoRow(s.wasteTotalMassDetailLocation, addr)
                    }
                    item.totalObjects?.let { objs ->
                        InfoRow(s.wasteTotalMassDetailObjects, "$objs")
                    }
                }
            }

            // ── Items breakdown ────────────────────────────────────────────────
            val itemsMass = item.itemsMass
            if (!itemsMass.isNullOrEmpty()) {
                Text(
                    s.wasteTotalMassDetailItems,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF424242),
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = gray100m),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Column(modifier = Modifier.padding(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        itemsMass.forEachIndexed { idx, itemMass ->
                            if (idx > 0) HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                            MassItemRow(itemMass = itemMass)
                        }
                    }
                }
            }

            // ── AI Analysis ────────────────────────────────────────────────────
            if (!item.aiAnalysis.isNullOrBlank()) {
                Text(
                    s.wasteTotalMassDetailAnalysis,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF424242),
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = gray100m),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        item.aiAnalysis,
                        modifier = Modifier.padding(14.dp),
                        fontSize = 13.sp,
                        color = Color(0xFF424242),
                        lineHeight = 20.sp,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, color: Color = Color(0xFF424242)) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, fontSize = 13.sp, color = Color.Gray)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = color)
    }
}

@Composable
private fun MassItemRow(itemMass: DetectItemMassDto) {
    val itemColor = Color(0xFF616161)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(Icons.Filled.Inventory, contentDescription = null, modifier = Modifier.size(18.dp), tint = itemColor)
        Text(
            itemMass.name,
            fontSize = 13.sp,
            color = Color(0xFF212121),
            modifier = Modifier.weight(1f),
        )
        Text(
            "%.3f kg".fmt(itemMass.massKg),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = itemColor,
        )
    }
}

