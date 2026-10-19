package com.vodang.greenmind.home.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.theme.*
import com.vodang.greenmind.home.components.StatsSummaryCard

@Composable
private fun PreAppSurveyBadge(onClick: () -> Unit) {
    val s = LocalAppStrings.current
    Card(
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(listOf(Green800, Green600))
                )
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(s.surveyBadgeIcon, fontSize = 32.sp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = s.preAppSurveyBadgeTitle,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = s.preAppSurveyBadgeDesc,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                }
                Text("›", fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun HouseholdDashboard(
    scrollState: ScrollState = rememberScrollState(),
    // Core waste features
    onWasteSortClick: () -> Unit = {},
    onWasteImpactClick: () -> Unit = {},
    onHouseholdWasteClick: () -> Unit = {},
    onWasteStatClick: () -> Unit = {},
    onWasteReportClick: () -> Unit = {},
    onWasteTotalMassClick: () -> Unit = {},
    // Scanning features
    onScanMealClick: () -> Unit = {},
    onScanBillClick: () -> Unit = {},
    // Lifestyle features
    onWasteStatusClick: () -> Unit = {},
    onEnvironmentalImpactClick: () -> Unit = {},
    onPaymentClick: () -> Unit = {},
    // Community features
    onBlogClick: () -> Unit = {},
    onCampaignsClick: () -> Unit = {},
    // Onboarding
    showPreAppSurveyBadge: Boolean = false,
    onPreAppSurveyClick: () -> Unit = {},
) {
    val s = LocalAppStrings.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (showPreAppSurveyBadge) {
            PreAppSurveyBadge(onClick = onPreAppSurveyClick)
        }

        // Ocean Score Card at the top
        OceanScoreCard()
        Spacer(modifier = Modifier.height(12.dp))

        // Stats Summary Card with real-time data
        StatsSummaryCard()
        Spacer(modifier = Modifier.height(8.dp))

        // Section Header for Features
        Text(s.features, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Row 1: Waste core features
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.height(IntrinsicSize.Max)) {
                FeatureGridButton(Icons.Filled.Recycling, s.wasteSort, GreenBg50, GreenIcon, Modifier.weight(1f)) { onWasteSortClick() }
                FeatureGridButton(Icons.Filled.Analytics, s.wasteImpactTitle, OrangeBg50, OrangeIcon, Modifier.weight(1f)) { onWasteImpactClick() }
                FeatureGridButton(Icons.Filled.Scale, s.wasteTotalMassTitle, BlueBg50, BlueIcon, Modifier.weight(1f)) { onWasteTotalMassClick() }
            }

            // Row 2: Impact & analytics
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.height(IntrinsicSize.Max)) {
                PaymentGridButton(label = s.paymentTitle, modifier = Modifier.weight(1f)) { onPaymentClick() }
                FeatureGridButton(Icons.Filled.Delete, s.wasteReport, RedBg50, RedIcon, Modifier.weight(1f)) { onWasteReportClick() }
                FeatureGridButton(Icons.AutoMirrored.Filled.TrendingUp, s.wasteStatTitle, BlueBg50, BlueIcon, Modifier.weight(1f)) { onWasteStatClick() }
            }

            // Row 3: Household & lifestyle
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.height(IntrinsicSize.Max)) {
                FeatureGridButton(Icons.Filled.AssignmentInd, s.householdDashboardTitle, PurpleBg50, PurpleIcon, Modifier.weight(1f)) { onHouseholdWasteClick() }
                FeatureGridButton(Icons.Filled.ListAlt, s.householdWasteStatusTitle, PurpleBg50, PurpleIcon, Modifier.weight(1f)) { onWasteStatusClick() }
                FeatureGridButton(Icons.Filled.Restaurant, s.scanMeal, GreenBg50, GreenIcon, Modifier.weight(1f)) { onScanMealClick() }
            }

            // Row 4: Community & scanning
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.height(IntrinsicSize.Max)) {
                FeatureGridButton(Icons.Filled.Receipt, s.scanBill, YellowBg50, YellowIcon, Modifier.weight(1f)) { onScanBillClick() }
                FeatureGridButton(Icons.Filled.Newspaper, s.blog, YellowBg50, YellowIcon, Modifier.weight(1f)) { onBlogClick() }
                FeatureGridButton(Icons.Filled.Handshake, s.campaignsTitle, TealBg50, TealIcon, Modifier.weight(1f)) { onCampaignsClick() }
            }
        }
    }
}
