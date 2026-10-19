package com.vodang.greenmind.bill

import com.vodang.greenmind.fmt
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.ocr.InvoiceDto
import com.vodang.greenmind.api.ocr.OcrItem
import com.vodang.greenmind.api.ocr.OcrVendor
import com.vodang.greenmind.api.ocr.getInvoices
import com.vodang.greenmind.api.wastedetect.WasteDetectResponse
import com.vodang.greenmind.api.wastedetect.WasteDetectImpact
import com.vodang.greenmind.home.components.EnvImpactCard
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.util.AppLogger

private val green800 = Color(0xFF2E7D32)
private val green600 = Color(0xFF388E3C)
private val green50  = Color(0xFFE8F5E9)
private val orange   = Color(0xFFE65100)
private val red      = Color(0xFFC62828)
private val gray50   = Color(0xFFF5F5F5)

private fun ratioColor(ratio: Int): Color = when {
    ratio >= 70 -> green800
    ratio >= 40 -> orange
    else        -> red
}

private fun currencySymbol(currency: String?): String = when (currency?.uppercase()) {
    "USD" -> "$"
    "EUR" -> "€"
    "GBP" -> "£"
    "VND" -> "₫"
    else  -> currency?.let { "$it " } ?: "$"
}

private fun formatAmount(amount: Double, symbol: String = "$"): String =
    "$symbol${"%.2f".fmt(amount)}"

@Composable
fun BillListScreen(onScanClick: () -> Unit) {
    val s = LocalAppStrings.current

    var invoices by remember { mutableStateOf<List<InvoiceDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var selectedInvoice by remember { mutableStateOf<InvoiceDto?>(null) }

    LaunchedEffect(Unit) {
        val token = SettingsStore.getAccessToken()
        if (token == null) { isLoading = false; return@LaunchedEffect }
        try {
            val serverInvoices = getInvoices(token)
            val mockInvoice = InvoiceDto(
                id = "mock-123",
                vendor = OcrVendor(name = "Scan Demo"),
                createdAt = "2026-05-11T12:00:00Z",
                items = listOf(
                    OcrItem(rawName = "coca",   quantity = 2.0),
                    OcrItem(rawName = "sprite", quantity = 1.0),
                    OcrItem(rawName = "tonic",  quantity = 1.0),
                    OcrItem(rawName = "soda",   quantity = 1.0),
                ),
                pollution = mapOf(
                    "CO2" to 0.22944161271869984,
                    "dioxin" to 0.1624365399778406,
                    "microplastic" to 0.20304567497230075,
                    "toxic_chemicals" to 0.1624365399778406,
                    "non_biodegradable" to 0.20304567497230075,
                    "NOx" to 0.019796953309799324,
                    "SO2" to 0.019796953309799324,
                    "CH4" to 0.0, "PM2.5" to 0.0, "Pb" to 0.0, "Hg" to 0.0, "Cd" to 0.0, "nitrate" to 0.0, "chemical_residue" to 0.0, "styrene" to 0.0
                ),
                impact = mapOf(
                    "air" to 0.0896785064460995,
                    "water" to 0.20304567497230075,
                    "soil" to 0.18274110747507066
                )
            )
            invoices = listOf(mockInvoice) + serverInvoices
        } catch (e: Throwable) {
            AppLogger.e("BillList", "Failed to load invoices: ${e.message}")
            errorMsg = "Could not load history. Pull down to retry."
        }
        isLoading = false
    }

    Box(modifier = Modifier.fillMaxSize().background(gray50)) {
        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = green800)
            }

            invoices.isEmpty() && errorMsg == null -> Box(
                Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(Icons.Filled.Receipt, contentDescription = null, modifier = Modifier.size(56.dp), tint = green800)
                    Text(s.billListEmpty, color = Color.Gray, fontSize = 15.sp)
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(green800)
                            .clickable { onScanClick() }
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                        Text(s.billScanTitle, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (errorMsg != null) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFFEBEE))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("⚠️", fontSize = 14.sp)
                            Text(errorMsg!!, fontSize = 12.sp, color = red, modifier = Modifier.weight(1f))
                        }
                    }
                }
                items(invoices, key = { it.id }) { invoice ->
                    InvoiceCard(invoice, onClick = { selectedInvoice = invoice })
                }
            }
        }

        // ── Scan FAB ──────────────────────────────────────────────────────────
        FloatingActionButton(
            onClick = onScanClick,
            containerColor = green800,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .navigationBarsPadding(),
        ) {
            Text("+", fontSize = 28.sp, fontWeight = FontWeight.Light)
        }
    }

    // ── Detail bottom sheet ───────────────────────────────────────────────────
    selectedInvoice?.let { invoice ->
        InvoiceDetailSheet(invoice = invoice, onDismiss = { selectedInvoice = null })
    }
}

// ── Invoice row card ──────────────────────────────────────────────────────────

@Composable
private fun InvoiceCard(invoice: InvoiceDto, onClick: () -> Unit) {
    val ratio  = invoice.ecoScore()
    val color  = ratioColor(ratio)
    val symbol = currencySymbol(invoice.doc?.currency)
    val grand  = invoice.totals?.grandTotalDouble() ?: 0.0

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Green ratio badge
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Text("$ratio%", color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            // Middle — vendor + date
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    invoice.vendor?.name ?: "Unknown vendor",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1B5E20),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val dateLine = listOfNotNull(invoice.datetime?.date, invoice.datetime?.time)
                    .joinToString("  ·  ")
                if (dateLine.isNotBlank()) {
                    Text(dateLine, fontSize = 12.sp, color = Color.Gray)
                }
                val itemCount = invoice.items?.size ?: 0
                Text("$itemCount item${if (itemCount != 1) "s" else ""}", fontSize = 11.sp, color = Color(0xFF9E9E9E))
            }

            // Right — grand total or impact
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (invoice.totals != null) {
                    Text(formatAmount(grand, symbol), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF424242))
                } else if (invoice.pollution != null) {
                    Text("${invoice.items?.sumOf { (it.quantity ?: 1.0).toInt() } ?: 0} items", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF424242))
                    val co2 = invoice.pollution["CO2"] ?: 0.0
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.Eco, contentDescription = null, modifier = Modifier.size(14.dp), tint = green600)
                        Text("${co2.fmt(2)} kg CO₂", fontSize = 11.sp, color = green600)
                    }
                }
            }
        }
    }
}

// ── Detail bottom sheet ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InvoiceDetailSheet(invoice: InvoiceDto, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val symbol     = currencySymbol(invoice.doc?.currency)
    val totals     = invoice.totals
    val s = LocalAppStrings.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Header
            Text(
                invoice.vendor?.name ?: "Invoice",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B1B1B),
            )

            // Meta
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val dateLine = listOfNotNull(invoice.datetime?.date, invoice.datetime?.time).joinToString("  ·  ")
                if (dateLine.isNotBlank()) SheetRow(Icons.Filled.CalendarMonth, dateLine)
            }

            // Items
            if (!invoice.items.isNullOrEmpty()) {
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Text(s.paymentItems, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF616161))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Header row
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(s.paymentItems, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.weight(1f))
                        Text(s.billQty, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.width(28.dp))
                        Text(s.billUnit, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.width(54.dp))
                        Text(s.billTotal, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.width(56.dp))
                    }
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    invoice.items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    item.rawName ?: "Unknown",
                                    fontSize = 13.sp,
                                    color = Color(0xFF212121),
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                            Text(item.quantity?.let { "%.0f".fmt(it) } ?: "—", fontSize = 12.sp, color = Color(0xFF616161), modifier = Modifier.width(28.dp))
                            Text(item.unitPrice?.let { formatAmount(it, symbol) } ?: "—", fontSize = 12.sp, color = Color(0xFF616161), modifier = Modifier.width(54.dp))
                            Text(
                                item.lineTotal?.let { formatAmount(it, symbol) } ?: "—",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF424242),
                                modifier = Modifier.width(56.dp),
                            )
                        }
                    }
                }
            }

            // Totals
            if (totals != null) {
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Text(s.billTotals, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF616161))
                TotalsRow(s.billGrandTotal, formatAmount(totals.grandTotalDouble(), symbol), green800, bold = true)
            } else if (invoice.pollution != null && invoice.impact != null) {
                Spacer(Modifier.height(8.dp))
                val wasteDetect = WasteDetectResponse(
                    items = emptyList(), // Items already displayed above
                    totalObjects = invoice.items?.sumOf { (it.quantity ?: 1.0).toInt() } ?: 0,
                    imageUrl = "",
                    pollution = invoice.pollution,
                    impact = WasteDetectImpact(
                        airPollution = invoice.impact["air"] ?: 0.0,
                        waterPollution = invoice.impact["water"] ?: 0.0,
                        soilPollution = invoice.impact["soil"] ?: 0.0
                    )
                )
                EnvImpactCard(result = wasteDetect)
            }
        }
    }
}

@Composable
private fun SheetRow(icon: ImageVector, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = green800)
        Text(text, fontSize = 13.sp, color = Color(0xFF616161), lineHeight = 18.sp, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun TotalsRow(label: String, value: String, valueColor: Color = Color(0xFF424242), bold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 13.sp, color = Color(0xFF757575))
        Text(
            value,
            fontSize = if (bold) 15.sp else 13.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
            color = valueColor,
        )
    }
}
