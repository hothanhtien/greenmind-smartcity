package com.vodang.greenmind.catalogue

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.components.AppScaffold
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.theme.Green800
import com.vodang.greenmind.theme.Green50
import com.vodang.greenmind.theme.SurfaceGray

private data class FeatureEntry(
    val icon: ImageVector,
    val title: String,
    val desc: String,
    val onClick: (() -> Unit)? = null,
)

private val green800 = Green800
private val green50 = Green50

@Composable
fun CatalogueScreen(
    onWasteReport: () -> Unit = {},
    onPreAppSurvey: () -> Unit = {},
    onWasteSort: () -> Unit = {},
    onWasteTotalMass: () -> Unit = {},
    onEnvironmentalImpact: () -> Unit = {},
    onWasteImpact: () -> Unit = {},
    onWasteStat: () -> Unit = {},
    onHouseholdWaste: () -> Unit = {},
    onElectricityUsage: () -> Unit = {},
    onWalkDistance: () -> Unit = {},
    onScanMeal: () -> Unit = {},
    onScanBill: () -> Unit = {},
    onBlog: () -> Unit = {},
    onCampaigns: () -> Unit = {},
    onHeatmap: () -> Unit = {},
    onRoute: () -> Unit = {},
    onCheckIn: () -> Unit = {},
    onVolunteerEvents: () -> Unit = {},
    onOceanScore: () -> Unit = {},
    onSurveys: () -> Unit = {},
    onTodos: () -> Unit = {},
) {
    val s = LocalAppStrings.current

    val allFeatures = listOf(
        FeatureEntry(Icons.Filled.Description, s.todos, s.todosDesc, onClick = onTodos),
        FeatureEntry(Icons.Filled.Edit, s.surveys, s.surveysDesc, onClick = onSurveys),
        FeatureEntry(Icons.Filled.Newspaper, s.blog, s.blogDesc, onClick = onBlog),
        FeatureEntry(Icons.Filled.Public, s.oceanTitle, s.oceanSubtitle, onClick = onOceanScore),
        FeatureEntry(Icons.Filled.Edit, s.preAppSurveyTitle, s.preAppSurveySubtitle, onClick = onPreAppSurvey),
    )
    val householdFeatures = listOf(
        FeatureEntry(Icons.Filled.Cached, s.wasteSort, s.wasteSortDesc, onClick = onWasteSort),
        FeatureEntry(Icons.Filled.Delete, s.wasteReport, s.wasteReportDesc, onClick = onWasteReport),
        FeatureEntry(Icons.Filled.Scale, s.wasteTotalMassTitle, s.wasteTotalMassDesc, onClick = onWasteTotalMass),
        FeatureEntry(Icons.Filled.Warning, s.environmentalImpact, s.environmentalImpactDesc, onClick = onEnvironmentalImpact),
        FeatureEntry(Icons.Filled.Analytics, s.wasteImpactTitle, s.wasteImpactDesc, onClick = onWasteImpact),
        FeatureEntry(Icons.AutoMirrored.Filled.TrendingUp, s.wasteStatTitle, s.wasteStatDesc, onClick = onWasteStat),
        FeatureEntry(Icons.Filled.Description, s.householdWasteStatusTitle, s.householdWasteStatusDesc, onClick = onHouseholdWaste),
        FeatureEntry(Icons.Filled.Lightbulb, s.electricityUsage, s.electricityChartDesc, onClick = onElectricityUsage),
        FeatureEntry(Icons.AutoMirrored.Filled.DirectionsWalk, s.walkDistance, s.walkValue, onClick = onWalkDistance),
        FeatureEntry(Icons.Filled.Restaurant, s.scanMeal, s.scanMealDesc, onClick = onScanMeal),
        FeatureEntry(Icons.Filled.Receipt, s.scanBill, s.scanBillDesc, onClick = onScanBill),
        FeatureEntry(Icons.Filled.Newspaper, s.blog, s.blogDesc, onClick = onBlog),
        FeatureEntry(Icons.Filled.Handshake, s.campaignsTitle, s.campaignsDesc, onClick = onCampaigns),
    )
    val collectorFeatures = listOf(
        FeatureEntry(Icons.Filled.Map, s.heatmapFeatureLabel, s.heatmapFeatureDesc, onClick = onHeatmap),
        FeatureEntry(Icons.Filled.LocationOn, s.routeLabel, s.scheduleDesc, onClick = onRoute),
        FeatureEntry(Icons.Filled.CheckCircle, s.checkInCardTitle, s.checkInButton, onClick = onCheckIn),
    )
    val volunteerFeatures = listOf(
        FeatureEntry(Icons.Filled.Handshake, s.volunteerTitle, s.volunteerSubtitle),
        FeatureEntry(Icons.Filled.Event, s.volunteerEventsCardTitle, s.volunteerUpcomingTitle, onClick = onVolunteerEvents),
    )

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceGray)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        CatalogueSection(
            roleIcon = Icons.Filled.Group,
            roleLabel = s.allUsersLabel,
            roleColor = Color(0xFF1565C0),
            roleBg = Color(0xFFE3F2FD),
            features = allFeatures
        )
        CatalogueSection(
            roleIcon = Icons.Filled.House,
            roleLabel = s.householdRole,
            roleColor = green800,
            roleBg = green50,
            features = householdFeatures
        )
        CatalogueSection(
            roleIcon = Icons.Filled.LocalShipping,
            roleLabel = s.collectorRole,
            roleColor = Color(0xFFE65100),
            roleBg = Color(0xFFFFF3E0),
            features = collectorFeatures
        )
        CatalogueSection(
            roleIcon = Icons.Filled.Handshake,
            roleLabel = s.volunteerRole,
            roleColor = Color(0xFF6A1B9A),
            roleBg = Color(0xFFF3E5F5),
            features = volunteerFeatures
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun CatalogueSection(
    roleIcon: ImageVector,
    roleLabel: String,
    roleColor: Color,
    roleBg: Color,
    features: List<FeatureEntry>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(roleBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = roleIcon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = roleColor,
                )
            }
            Text(
                text = roleLabel,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = roleColor
            )
        }

        features.forEach { feature ->
            CatalogueCard(feature = feature, accentColor = roleColor, bgColor = roleBg)
        }
    }
}

@Composable
private fun CatalogueCard(feature: FeatureEntry, accentColor: Color, bgColor: Color) {
    val onClick = feature.onClick
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = bgColor,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(feature.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1B1B1B))
                Spacer(Modifier.height(2.dp))
                Text(feature.desc, fontSize = 12.sp, color = Color.Gray, lineHeight = 16.sp)
            }
        }
    }
}