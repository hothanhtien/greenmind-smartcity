package com.vodang.greenmind.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.vodang.greenmind.api.payment.WasteBillDto
import com.vodang.greenmind.platform.PaymentDeeplinkBus
import com.vodang.greenmind.platform.PaymentDeeplinkResult
import com.vodang.greenmind.platform.UrlOpener
import com.vodang.greenmind.store.PaymentStore
import com.vodang.greenmind.theme.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

private val blue50 = Color(0xFFE3F2FD)
private val blueIcon = Color(0xFF1565C0)
private val green50 = Color(0xFFE8F5E9)
private val greenIcon = Color(0xFF2E7D32)
private val orange50 = Color(0xFFFFF3E0)
private val orangeIcon = Color(0xFFE65100)
private val red50 = Color(0xFFFFEBEE)
private val redIcon = Color(0xFFC62828)
private val SurfaceWhite = Color(0xFFFFFFFF)
private val SurfaceGray = Color(0xFFF5F5F5)
private val TextSecondary = Color(0xFF757575)
private val Gray900 = Color(0xFF212121)
private val Gray400 = Color(0xFFBDBDBD)
private val gray100 = Color(0xFFF5F5F5)
private val Green800 = Color(0xFF2E7D32)

enum class PaymentTab { PENDING, PAID, ALL }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen() {
    val s = com.vodang.greenmind.i18n.LocalAppStrings.current
    var selectedTab by remember { mutableStateOf(PaymentTab.PENDING) }
    var pendingTapCount by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val paymentState by PaymentStore.state.collectAsState()
    val bills = paymentState.bills
    val isLoading = paymentState.isLoading
    val isCheckoutLoading = paymentState.isCheckoutLoading

    val filteredBills = when (selectedTab) {
        PaymentTab.PENDING -> bills.filter { !it.isPaid }
        PaymentTab.PAID -> bills.filter { it.isPaid }
        PaymentTab.ALL -> bills
    }

    LaunchedEffect(Unit) {
        PaymentStore.fetchBills()
    }

    // Listen for deeplink results coming back from Stripe via the OS browser.
    LaunchedEffect(Unit) {
        PaymentDeeplinkBus.events.collect { result ->
            when (result) {
                PaymentDeeplinkResult.SUCCESS -> {
                    PaymentStore.markPendingFakeBillPaid()
                    PaymentStore.fetchBills()
                    snackbarHostState.showSnackbar(s.paymentSuccessTitle)
                }
                PaymentDeeplinkResult.CANCEL -> {
                    snackbarHostState.showSnackbar(s.cancel)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(SurfaceGray)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PaymentSummaryCard(
                        label = s.paymentPending,
                        value = bills.filter { !it.isPaid }.sumOf { it.total },
                        icon = Icons.Filled.Schedule,
                        backgroundColor = orange50,
                        iconColor = orangeIcon,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            pendingTapCount += 1
                            if (pendingTapCount >= 2) {
                                PaymentStore.injectFakeBill()
                                pendingTapCount = 0
                            }
                        }
                    )
                    PaymentSummaryCard(
                        label = s.paymentPaid,
                        value = bills.filter { it.isPaid }.sumOf { it.total },
                        icon = Icons.Filled.CheckCircle,
                        backgroundColor = green50,
                        iconColor = greenIcon,
                        modifier = Modifier.weight(1f)
                    )
                }

                PaymentTabSelector(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    pendingCount = bills.count { !it.isPaid },
                    paidCount = bills.count { it.isPaid },
                    allCount = bills.size
                )

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = Green800) }
                } else if (filteredBills.isEmpty()) {
                    PaymentEmptyState(
                        message = when (selectedTab) {
                            PaymentTab.PENDING -> s.paymentNoPending
                            PaymentTab.PAID -> s.paymentNoPaid
                            PaymentTab.ALL -> s.paymentNoInvoices
                        }
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(filteredBills, key = { "${it.householdId}-${it.year}-${it.month}" }) { bill ->
                            WasteBillCard(
                                bill = bill,
                                isCheckoutLoading = isCheckoutLoading,
                                onPayClick = {
                                    coroutineScope.launch {
                                        if (PaymentStore.isFakeBill(bill)) {
                                            PaymentStore.createCheckoutForFakeBill(bill)
                                                .onSuccess { UrlOpener.openInBrowser(it.url) }
                                                .onFailure {
                                                    snackbarHostState.showSnackbar(it.message ?: "Checkout failed")
                                                }
                                        } else {
                                            PaymentStore.createCheckoutForBill(bill)
                                                .onSuccess { UrlOpener.openInBrowser(it.url) }
                                                .onFailure {
                                                    snackbarHostState.showSnackbar(it.message ?: "Checkout failed")
                                                }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
        )
    }
}

@Composable
private fun PaymentSummaryCard(
    label: String,
    value: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Card(
        modifier = modifier.height(90.dp).then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(backgroundColor),
                contentAlignment = Alignment.Center
            ) { Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp)) }
            Column {
                Text(formatCurrency(value), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Gray900)
                Text(label, fontSize = 11.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun PaymentTabSelector(
    selectedTab: PaymentTab,
    onTabSelected: (PaymentTab) -> Unit,
    pendingCount: Int,
    paidCount: Int,
    allCount: Int,
) {
    val s = com.vodang.greenmind.i18n.LocalAppStrings.current
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(SurfaceWhite),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        PaymentTabButton(s.paymentPending, pendingCount, selectedTab == PaymentTab.PENDING, { onTabSelected(PaymentTab.PENDING) }, Modifier.weight(1f))
        PaymentTabButton(s.paymentPaid, paidCount, selectedTab == PaymentTab.PAID, { onTabSelected(PaymentTab.PAID) }, Modifier.weight(1f))
        PaymentTabButton(s.paymentAll, allCount, selectedTab == PaymentTab.ALL, { onTabSelected(PaymentTab.ALL) }, Modifier.weight(1f))
    }
}

@Composable
private fun PaymentTabButton(
    label: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) Green800.copy(alpha = 0.1f) else Color.Transparent
    val textColor = if (isSelected) Green800 else TextSecondary
    Column(
        modifier = modifier.clip(RoundedCornerShape(12.dp)).background(backgroundColor).padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal, color = textColor)
                Spacer(Modifier.height(2.dp))
                Text(count.toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor)
            }
        }
    }
}

@Composable
private fun WasteBillCard(
    bill: WasteBillDto,
    isCheckoutLoading: Boolean,
    onPayClick: () -> Unit,
) {
    val s = com.vodang.greenmind.i18n.LocalAppStrings.current
    Card(
        modifier = Modifier.fillMaxWidth().clickable(enabled = !bill.isPaid, onClick = onPayClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(bill.billName, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Gray900,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(2.dp))
                    Text("${bill.recordCount} bản ghi · ${bill.ratePerKg}đ/kg", fontSize = 11.sp, color = TextSecondary)
                }
                BillStatusBadge(isPaid = bill.isPaid)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(formatCurrency(bill.total), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Gray900)
                Text(s.paymentDue + " " + formatDate(bill.dueDate), fontSize = 12.sp,
                    fontWeight = FontWeight.Medium, color = if (bill.isPaid) greenIcon else orangeIcon)
            }
            if (!bill.isPaid) {
                Button(
                    onClick = onPayClick,
                    enabled = !isCheckoutLoading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Green800),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isCheckoutLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Filled.Payment, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(s.paymentPayNow, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun BillStatusBadge(isPaid: Boolean) {
    val s = com.vodang.greenmind.i18n.LocalAppStrings.current
    val (bg, fg, label) = if (isPaid)
        Triple(green50, greenIcon, s.paymentStatusPaid)
    else
        Triple(orange50, orangeIcon, s.paymentStatusPending)
    Box(
        modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(bg).padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = fg)
    }
}

@Composable
private fun PaymentEmptyState(message: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier.size(80.dp).clip(CircleShape).background(gray100),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Filled.ReceiptLong, contentDescription = null, tint = Gray400, modifier = Modifier.size(40.dp)) }
        Text(message, fontSize = 14.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
    }
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    return format.format(amount)
}

private fun formatDate(dateString: String?): String {
    if (dateString.isNullOrEmpty()) return ""
    return try {
        val raw = dateString.substringBefore("T")
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = inputFormat.parse(raw)
        outputFormat.format(date!!)
    } catch (e: Exception) {
        dateString
    }
}
