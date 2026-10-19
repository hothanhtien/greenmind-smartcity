package com.vodang.greenmind.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.verticalScroll
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.theme.*

@Composable
fun ScanHubCard(
    onWasteSortClick: () -> Unit = {},
    onMealScanClick: () -> Unit = {},
    onBillScanClick: () -> Unit = {},
    onWasteDetectClick: () -> Unit = {},
) {
    val s = LocalAppStrings.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceGray)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Scan & Detect",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Choose a scan feature to get started",
            fontSize = 14.sp,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Row 1: Waste Sort + Meal Scan
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ScanCard(
                icon = Icons.Filled.Recycling,
                title = s.wasteSort,
                description = "Classify waste items",
                bgColor = GreenBg50,
                iconColor = GreenIcon,
                gradientColors = listOf(Green800.copy(alpha = 0.08f), Green800.copy(alpha = 0.03f)),
                onClick = onWasteSortClick,
                modifier = Modifier.weight(1f)
            )
            ScanCard(
                icon = Icons.Filled.Restaurant,
                title = s.scanMeal,
                description = "Analyze meal photos",
                bgColor = OrangeBg50,
                iconColor = OrangeIcon,
                gradientColors = listOf(OrangeIcon.copy(alpha = 0.08f), OrangeIcon.copy(alpha = 0.03f)),
                onClick = onMealScanClick,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: Bill Scan + Detect Waste
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ScanCard(
                icon = Icons.Filled.Receipt,
                title = s.scanBill,
                description = "Scan receipts",
                bgColor = BlueBg50,
                iconColor = BlueIcon,
                gradientColors = listOf(BlueIcon.copy(alpha = 0.08f), BlueIcon.copy(alpha = 0.03f)),
                onClick = onBillScanClick,
                modifier = Modifier.weight(1f)
            )
            ScanCard(
                icon = Icons.Filled.CameraAlt,
                title = s.environmentalImpact,
                description = "Detect waste impacts",
                bgColor = PurpleBg50,
                iconColor = PurpleIcon,
                gradientColors = listOf(PurpleIcon.copy(alpha = 0.08f), PurpleIcon.copy(alpha = 0.03f)),
                onClick = onWasteDetectClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ScanCard(
    icon: ImageVector,
    title: String,
    description: String,
    bgColor: Color,
    iconColor: Color,
    gradientColors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(colors = gradientColors)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Icon container
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp),
                        tint = iconColor
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Text content
                Column {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Action indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Start",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = iconColor
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = iconColor
                    )
                }
            }
        }
    }
}