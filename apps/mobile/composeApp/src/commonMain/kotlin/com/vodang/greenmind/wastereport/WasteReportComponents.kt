package com.vodang.greenmind.wastereport

import com.vodang.greenmind.fmt
import com.vodang.greenmind.time.formatDateTimeLocal
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.vodang.greenmind.api.wastereport.WasteReportDto

private val green600 = Color(0xFF388E3C)
private val green50  = Color(0xFFE8F5E9)
private val orange50 = Color(0xFFFFF3E0)
private val orange   = Color(0xFFE65100)
private val orange600 = Color(0xFFEF6C00)
private val red50    = Color(0xFFFFEBEE)
private val red700   = Color(0xFFC62828)
private val blue50   = Color(0xFFE3F2FD)
private val blue700  = Color(0xFF1565C0)

@Composable
internal fun WasteReportCard(report: WasteReportDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(orange50),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(24.dp), tint = orange600)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        formatDateTimeLocal(report.createdAt),
                        fontSize = 11.sp,
                        color = Color.Gray,
                    )
                }
                if (report.description.isNotBlank()) {
                    Text(
                        report.description,
                        fontSize = 12.sp,
                        color = Color(0xFF616161),
                        lineHeight = 16.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        "📍 ${report.wardName}",
                        fontSize = 11.sp,
                        color = Color(0xFF9E9E9E),
                    )
                    StatusChip(report.status)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WasteReportDetailSheet(report: WasteReportDto, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var previewImageUrl by remember { mutableStateOf<String?>(null) }

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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header: code + status chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    report.code,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B1B1B),
                )
                StatusChip(report.status)
            }

            // Report image
            if (!report.imageUrl.isNullOrBlank()) {
                NetworkImage(
                    url = report.imageUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { previewImageUrl = report.imageUrl },
                )
            }

            // Details
            DetailRow("Ward", report.wardName, Icons.Filled.LocationOn)
            DetailRow("Reported", formatDateTimeLocal(report.createdAt), Icons.Filled.CalendarMonth)
            if (!report.resolvedAt.isNullOrBlank()) {
                DetailRow("Resolved", formatDateTimeLocal(report.resolvedAt), Icons.Filled.CheckCircle)
            }
            if (report.pollutionScore != null) {
                DetailRow("Pollution score", "%.2f".fmt(report.pollutionScore), Icons.Filled.Warning)
            }
            if (!report.pollutionLevel.isNullOrBlank()) {
                DetailRow("Pollution level", report.pollutionLevel.replaceFirstChar { it.uppercase() }, Icons.Filled.Warning)
            }
            if (report.description.isNotBlank()) {
                DetailRow("Note", report.description, Icons.AutoMirrored.Filled.Notes)
            }

            // Segmented / heatmap images
            if (!report.segmentedImageUrl.isNullOrBlank()) {
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Text("Segmented Image", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF616161))
                NetworkImage(
                    url = report.segmentedImageUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { previewImageUrl = report.segmentedImageUrl },
                )
            }
            if (!report.heatmapUrl.isNullOrBlank()) {
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Text("Heatmap", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF616161))
                NetworkImage(
                    url = report.heatmapUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { previewImageUrl = report.heatmapUrl },
                )
            }

            // Evidence image
            if (!report.imageEvidenceUrl.isNullOrBlank()) {
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Text("Collection Evidence", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF616161))
                NetworkImage(
                    url = report.imageEvidenceUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { previewImageUrl = report.imageEvidenceUrl },
                )
            }
        }
    }

    previewImageUrl?.let { url ->
        Dialog(
            onDismissRequest = { previewImageUrl = null },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center,
            ) {
                ZoomableImagePreview(
                    url = url,
                    modifier = Modifier.fillMaxSize(),
                    onTap = { previewImageUrl = null }
                )
            }
        }
    }
}

@Composable
internal fun StatusChip(status: String) {
    val (bg, fg) = when (status.lowercase()) {
        "pending"             -> orange50 to orange
        "assigned"            -> blue50   to blue700
        "resolved",
        "completed", "done"   -> green50  to green600
        "rejected"            -> red50    to red700
        else                  -> Color(0xFFF5F5F5) to Color(0xFF616161)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            status.replaceFirstChar { it.uppercase() },
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = fg,
        )
    }
}

@Composable
internal fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, fontSize = 13.sp, color = Color(0xFF9E9E9E), modifier = Modifier.width(130.dp))
        Text(value, fontSize = 13.sp, color = Color(0xFF1B1B1B), fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
    }
}

@Composable
internal fun DetailRow(label: String, value: String, icon: ImageVector) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.width(130.dp)) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF9E9E9E))
            Text(label, fontSize = 13.sp, color = Color(0xFF9E9E9E))
        }
        Text(value, fontSize = 13.sp, color = Color(0xFF1B1B1B), fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
    }
}
