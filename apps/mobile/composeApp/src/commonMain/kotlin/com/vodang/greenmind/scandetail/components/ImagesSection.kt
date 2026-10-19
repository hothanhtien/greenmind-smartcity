package com.vodang.greenmind.scandetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.vodang.greenmind.wastereport.NetworkImage
import com.vodang.greenmind.wastereport.ZoomableImagePreview

// ── Images Section ────────────────────────────────────────────────────────────

@Composable
fun ImagesSection(
    imageUrl: String,
    annotatedImageUrl: String?,
    aiAnalysisUrl: String? = null,
    depthMapUrl: String? = null,
    modifier: Modifier = Modifier,
) {
    var previewUrl by remember { mutableStateOf<String?>(null) }

    val primaryUrl = aiAnalysisUrl ?: imageUrl

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { previewUrl = primaryUrl },
        ) {
            NetworkImage(
                url = primaryUrl,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(0.dp)),
            )
        }

        val additional = listOfNotNull(
            imageUrl.takeIf { it != primaryUrl }?.let { "Original" to it },
            depthMapUrl?.let { "Depth Map" to it },
        )
        if (additional.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                additional.forEach { (label, url) ->
                    Column(
                        modifier = Modifier
                            .width(100.dp)
                            .clickable { previewUrl = url },
                    ) {
                        NetworkImage(
                            url = url,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp)),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            label,
                            fontSize = 10.sp,
                            color = Color(0xFF757575),
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }
    }

    // Image preview dialog
    previewUrl?.let { url ->
        Dialog(
            onDismissRequest = { previewUrl = null },
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
                    onTap = { previewUrl = null },
                )
            }
        }
    }
}