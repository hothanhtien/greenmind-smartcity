package com.vodang.greenmind.permission

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import androidx.activity.ComponentActivity
import android.Manifest

private val green800 = Color(0xFF2E7D32)
private val green50  = Color(0xFFE8F5E9)

/**
 * Guards camera-launching screens behind a permission check.
 *
 * - If [skip] is true (e.g. gallery-only mode) or permission is already granted,
 *   [content] is rendered immediately.
 * - Otherwise a modal dialog explains why the permission is needed and offers
 *   "Allow Camera" and, once denied at least once, "Open Settings".
 * - Pressing "Not Now" calls [onDenied] so the caller can navigate back.
 */
@Composable
fun CameraPermissionGate(
    onDenied: () -> Unit,
    skip: Boolean = false,
    content: @Composable () -> Unit,
) {
    val cameraGranted by PermissionRequester.grantedFlow(PermissionGroup.CAMERA).collectAsState()

    if (skip || cameraGranted) {
        content()
        return
    }

    val context = LocalContext.current
    // Track whether we have already fired a request this session so we can
    // surface the "Open Settings" button after a denial.
    var hasRequested by remember { mutableStateOf(false) }

    // After a request completes and permission is still denied, check whether
    // the system will show the rationale UI (= user denied once, not permanently)
    // or not (= permanently denied / never requested yet).
    val isPermanentlyDenied = hasRequested &&
        !(context as? ComponentActivity)
            ?.let { ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA) }
            .let { it ?: false }

    Dialog(
        onDismissRequest = onDenied,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(green50),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.CameraAlt,
                        contentDescription = null,
                        tint = green800,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Title
                Text(
                    text = "Camera Access Required",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B1B1B),
                    textAlign = TextAlign.Center,
                )

                // Body
                Text(
                    text = if (isPermanentlyDenied)
                        "Camera permission was denied. Please enable it in your device settings to use this feature."
                    else
                        "GreenMind needs camera access to scan waste, meals, and bills. Your photos are processed locally and never stored without your confirmation.",
                    fontSize = 14.sp,
                    color = Color(0xFF616161),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                )

                Spacer(Modifier.height(4.dp))

                // Primary action
                if (isPermanentlyDenied) {
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = green800),
                    ) {
                        Text("Open Settings", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Button(
                        onClick = {
                            hasRequested = true
                            PermissionRequester.request(PermissionGroup.CAMERA)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = green800),
                    ) {
                        Text("Allow Camera", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Secondary: always offer "Open Settings" as escape hatch after first request
                if (hasRequested && !isPermanentlyDenied) {
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = green800),
                    ) {
                        Text("Open Settings", fontSize = 14.sp)
                    }
                }

                // Dismiss
                TextButton(
                    onClick = onDenied,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Not Now", fontSize = 13.sp, color = Color.Gray)
                }
            }
        }
    }
}
