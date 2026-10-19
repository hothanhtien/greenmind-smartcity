package com.vodang.greenmind.bill

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.vodang.greenmind.api.bill.BillAnalysisResult
import com.vodang.greenmind.api.bill.toBillAnalysisResult
import com.vodang.greenmind.api.bill.toWasteDetectResponse
import com.vodang.greenmind.api.envimpact.logEnvironmentalImpact
import com.vodang.greenmind.api.ocr.OcrResponse
import com.vodang.greenmind.api.ocr.ecoScore
import com.vodang.greenmind.api.ocr.ocrBill
import com.vodang.greenmind.api.upload.requestAndUpload
import com.vodang.greenmind.home.components.EnvImpactCard
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.permission.CameraPermissionGate
import com.vodang.greenmind.util.AppLogger
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.File

// ── Palette ──────────────────────────────────────────────────────────────────
private val green800   = Color(0xFF2E7D32)
private val green600   = Color(0xFF388E3C)
private val green400   = Color(0xFF66BB6A)
private val green50    = Color(0xFFE8F5E9)
private val mint       = Color(0xFFF0FAF1)
private val orange700  = Color(0xFFE65100)
private val orange50   = Color(0xFFFFF3E0)
private val red600     = Color(0xFFC62828)
private val red50      = Color(0xFFFFEBEE)
private val ink900     = Color(0xFF1B1B1B)
private val ink600     = Color(0xFF424242)
private val ink400     = Color(0xFF9E9E9E)

private enum class BillScanPhase { IDLE, CAPTURED, ANALYZING, RESULT }

private data class RatingTone(val main: Color, val soft: Color, val accent: Color)

private fun ratingTone(ratio: Int): RatingTone = when {
    ratio >= 70 -> RatingTone(green800, green50, green400)
    ratio >= 40 -> RatingTone(orange700, orange50, Color(0xFFFFB74D))
    else        -> RatingTone(red600, red50, Color(0xFFEF5350))
}

private fun createBillPhotoUri(context: Context): Uri {
    val dir = File(context.cacheDir, "camera_photos").also { it.mkdirs() }
    val file = File(dir, "bill_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

@Composable
actual fun BillScanScreen(
    onScanComplete: (result: BillAnalysisResult, storeName: String, imageUrl: String?) -> Unit,
    onBack: () -> Unit,
) {
    CameraPermissionGate(onDenied = onBack) {
        BillScanContent(onScanComplete = onScanComplete, onBack = onBack)
    }
}

@Composable
private fun BillScanContent(
    onScanComplete: (result: BillAnalysisResult, storeName: String, imageUrl: String?) -> Unit,
    onBack: () -> Unit,
) {
    val s = LocalAppStrings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var phase by remember { mutableStateOf(BillScanPhase.IDLE) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var capturedBytes by remember { mutableStateOf<ByteArray?>(null) }
    var detectResult by remember { mutableStateOf<OcrResponse?>(null) }
    var uploadedImageUrl by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        if (saved) {
            val uri = photoUri ?: return@rememberLauncherForActivityResult
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes != null) { capturedBytes = bytes; phase = BillScanPhase.CAPTURED }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes != null) { capturedBytes = bytes; phase = BillScanPhase.CAPTURED }
        }
    }

    val launchCamera: () -> Unit = {
        error = null
        val uri = createBillPhotoUri(context)
        photoUri = uri
        cameraLauncher.launch(uri)
    }
    val launchGallery: () -> Unit = {
        error = null
        galleryLauncher.launch("image/*")
    }
    val startAnalysis: () -> Unit = analyze@{
        val b = capturedBytes ?: return@analyze
        phase = BillScanPhase.ANALYZING
        scope.launch {
            try {
                val filename = "bill_${System.currentTimeMillis()}.jpg"
                val token = SettingsStore.getAccessToken()
                if (token == null) {
                    error = s.billError
                    phase = BillScanPhase.IDLE
                    return@launch
                }
                coroutineScope {
                    val detectDeferred = async {
                        runCatching { ocrBill(accessToken = token, imageBytes = b, filename = filename) }
                            .onFailure { AppLogger.e("BillScan", "ocrBill failed: ${it.message}") }
                            .getOrNull()
                    }
                    val uploadDeferred = async {
                        runCatching {
                            requestAndUpload(
                                accessToken = token,
                                filename    = filename,
                                fileBytes   = b,
                                contentType = "image/jpeg",
                            ).imageUrl
                        }.getOrNull()
                    }
                    detectResult = detectDeferred.await()
                    uploadedImageUrl = uploadDeferred.await()
                }
                val res = detectResult
                val imp = res?.impact
                if (res != null && imp != null) {
                    launch {
                        runCatching {
                            logEnvironmentalImpact(
                                accessToken = token,
                                pollution   = res.pollution?.toMap().orEmpty(),
                                airImpact   = imp.air,
                                waterImpact = imp.water,
                                soilImpact  = imp.soil,
                            )
                        }.onFailure { e ->
                            AppLogger.e("EnvImpact", "Bill impact log failed: ${e::class.simpleName}: ${e.message}")
                        }
                    }
                }
                if (detectResult == null) {
                    error = s.billError
                    phase = BillScanPhase.IDLE
                } else {
                    phase = BillScanPhase.RESULT
                }
            } catch (_: Throwable) {
                error = s.billError
                phase = BillScanPhase.IDLE
            }
        }
    }

    when (phase) {
        BillScanPhase.IDLE -> BillScanIdle(
            onBack = onBack,
            onCamera = launchCamera,
            onGallery = launchGallery,
            error = error,
        )

        BillScanPhase.CAPTURED -> BillScanCaptured(
            bytes = capturedBytes,
            onBack = { capturedBytes = null; phase = BillScanPhase.IDLE },
            onRetake = launchCamera,
            onAnalyze = startAnalysis,
        )

        BillScanPhase.ANALYZING -> BillScanAnalyzing()

        BillScanPhase.RESULT -> {
            val det = detectResult
            if (det == null) {
                error = s.billError
                phase = BillScanPhase.IDLE
                return
            }
            BillScanResult(
                det = det,
                onRescan = {
                    capturedBytes = null
                    detectResult = null
                    uploadedImageUrl = null
                    phase = BillScanPhase.IDLE
                },
                onSave = { storeName ->
                    onScanComplete(
                        det.toBillAnalysisResult(),
                        storeName.ifBlank { det.items?.firstOrNull()?.rawName ?: "Bill scan" },
                        uploadedImageUrl,
                    )
                },
            )
        }
    }
}

// ── IDLE ──────────────────────────────────────────────────────────────────────

@Composable
private fun BillScanIdle(
    onBack: () -> Unit,
    onCamera: () -> Unit,
    onGallery: () -> Unit,
    error: String?,
) {
    val s = LocalAppStrings.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(green50, mint, Color.White)))
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp)
            .navigationBarsPadding(),
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s.back, tint = green800)
            }
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(green50)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            ) {
                Text("AI", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = green800)
            }
        }

        Spacer(Modifier.height(8.dp))

        // Hero illustration — receipt
        Box(
            modifier = Modifier.fillMaxWidth().height(180.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape)
                    .background(green50.copy(alpha = 0.7f))
            )
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(green400.copy(alpha = 0.2f))
            )
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(green600, green800))),
                contentAlignment = Alignment.Center,
            ) {
                Text("🧾", fontSize = 48.sp)
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = s.billScanTitle,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ink900,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = s.billScanHint,
            fontSize = 14.sp,
            color = ink600,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(28.dp))

        ActionCardButton(
            icon = Icons.Filled.PhotoCamera,
            title = s.billCapture,
            subtitle = "Scan now",
            primary = true,
            onClick = onCamera,
        )

        Spacer(Modifier.height(12.dp))

        ActionCardButton(
            icon = Icons.Filled.Image,
            title = s.billUpload,
            subtitle = "From gallery",
            primary = false,
            onClick = onGallery,
        )

        Spacer(Modifier.height(24.dp))

        // Tips
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = green800, modifier = Modifier.size(18.dp))
                    Text("Tips", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ink900)
                }
                TipRow("📐", "Đặt hoá đơn phẳng, không gấp nếp")
                TipRow("☀️", "Chụp đủ ánh sáng, không bóng đổ")
                TipRow("📝", "Toàn bộ chữ rõ ràng, không bị mờ")
            }
        }

        if (error != null) {
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(red50)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Text(error, color = red600, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun ActionCardButton(
    icon: ImageVector,
    title: String,
    subtitle: String,
    primary: Boolean,
    onClick: () -> Unit,
) {
    val titleColor = if (primary) Color.White else green800
    val subtitleColor = if (primary) Color.White.copy(alpha = 0.85f) else ink400
    val iconBg = if (primary) Color.White.copy(alpha = 0.18f) else green50
    val iconTint = if (primary) Color.White else green800

    val baseModifier = Modifier
        .fillMaxWidth()
        .shadow(if (primary) 6.dp else 0.dp, RoundedCornerShape(18.dp), spotColor = green800.copy(alpha = 0.25f))
        .clip(RoundedCornerShape(18.dp))
    val withBg = if (primary) {
        baseModifier.background(Brush.horizontalGradient(listOf(green600, green800)))
    } else {
        baseModifier.background(Color.White)
    }

    Box(
        modifier = withBg
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                modifier = Modifier.size(46.dp).clip(CircleShape).background(iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = titleColor)
                Text(subtitle, fontSize = 12.sp, color = subtitleColor)
            }
            Text("›", fontSize = 24.sp, color = titleColor, fontWeight = FontWeight.Light)
        }
    }
}

@Composable
private fun TipRow(emoji: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(emoji, fontSize = 16.sp)
        Text(label, fontSize = 12.sp, color = ink600)
    }
}

// ── CAPTURED ──────────────────────────────────────────────────────────────────

@Composable
private fun BillScanCaptured(
    bytes: ByteArray?,
    onBack: () -> Unit,
    onRetake: () -> Unit,
    onAnalyze: () -> Unit,
) {
    val s = LocalAppStrings.current
    Column(modifier = Modifier.fillMaxSize().background(ink900)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s.back, tint = Color.White)
            }
            Spacer(Modifier.weight(1f))
            Text("Xem trước", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.width(48.dp))
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center,
            ) {
                if (bytes != null) {
                    val bitmap = remember(bytes) { BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap() }
                    bitmap?.let { bmp ->
                        Image(
                            bitmap = bmp,
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = Color.White,
            shadowElevation = 8.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f).height(52.dp),
                    onClick = onRetake,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ink600),
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(s.billRetake, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                Button(
                    modifier = Modifier.weight(1.4f).height(52.dp),
                    onClick = onAnalyze,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = green800),
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(s.billAnalyze, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// ── ANALYZING ─────────────────────────────────────────────────────────────────

@Composable
private fun BillScanAnalyzing() {
    val s = LocalAppStrings.current
    val infinite = rememberInfiniteTransition(label = "billAnalyzing")
    val angle by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ring",
    )
    val pulse by infinite.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(green50, mint, Color.White))),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.matchParentSize().rotate(angle)) {
                    val stroke = Stroke(width = 8f, cap = StrokeCap.Round)
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(green800, green400, green800.copy(alpha = 0f)),
                        ),
                        startAngle = 0f,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = Offset(stroke.width / 2, stroke.width / 2),
                        size = Size(size.width - stroke.width, size.height - stroke.width),
                        style = stroke,
                    )
                }
                Box(
                    modifier = Modifier
                        .size((110 * pulse).dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(green600, green800))),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.ReceiptLong,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp),
                    )
                }
            }

            Text(s.billAnalyzing, color = ink900, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(
                "AI đang đọc hoá đơn và ước tính ảnh hưởng môi trường…",
                color = ink600,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { idx ->
                    val active = ((angle / 120f).toInt()) % 3 == idx
                    Box(
                        modifier = Modifier
                            .size(if (active) 10.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (active) green800 else green800.copy(alpha = 0.25f))
                    )
                }
            }
        }
    }
}

// ── RESULT ────────────────────────────────────────────────────────────────────

@Composable
private fun BillScanResult(
    det: OcrResponse,
    onRescan: () -> Unit,
    onSave: (String) -> Unit,
) {
    val s = LocalAppStrings.current
    val score = det.ecoScore()
    val tone = ratingTone(score)
    val badge = when {
        score >= 70 -> "Bữa mua xanh"
        score >= 40 -> "Tạm ổn"
        else        -> "Cần cải thiện"
    }
    val analysis = remember(det) { det.toBillAnalysisResult() }
    var storeName by remember(det) {
        mutableStateOf(det.vendor?.name ?: det.items?.firstOrNull()?.rawName ?: "")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(green50, mint, Color.White)))
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onRescan) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s.back, tint = green800)
            }
            Spacer(Modifier.weight(1f))
            Text("Kết quả", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = ink900)
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.width(48.dp))
        }

        Spacer(Modifier.height(8.dp))

        // Hero gauge card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(modifier = Modifier.size(180.dp), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val stroke = Stroke(width = 18f, cap = StrokeCap.Round)
                        drawArc(
                            color = tone.soft,
                            startAngle = 135f,
                            sweepAngle = 270f,
                            useCenter = false,
                            topLeft = Offset(stroke.width / 2, stroke.width / 2),
                            size = Size(size.width - stroke.width, size.height - stroke.width),
                            style = stroke,
                        )
                        val sweep = (score.coerceIn(0, 100) / 100f) * 270f
                        drawArc(
                            brush = Brush.sweepGradient(listOf(tone.accent, tone.main)),
                            startAngle = 135f,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = Offset(stroke.width / 2, stroke.width / 2),
                            size = Size(size.width - stroke.width, size.height - stroke.width),
                            style = stroke,
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Eco score", fontSize = 12.sp, color = ink400, fontWeight = FontWeight.Medium)
                        Text(
                            "$score",
                            fontSize = 56.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = tone.main,
                        )
                        Text("/ 100", fontSize = 12.sp, color = tone.main, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(tone.soft)
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                ) {
                    Text(badge, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = tone.main)
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    StatCell("Sản phẩm", "${det.items?.size ?: 0}", tone.main)
                    Divider(Modifier.height(36.dp).width(1.dp), color = Color(0xFFEEEEEE))
                    StatCell(s.billGreenSpend, s.billGreenRatio(score), green800)
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Store name input
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(s.billStoreNameLabel, fontSize = 12.sp, color = ink400, fontWeight = FontWeight.Medium)
                OutlinedTextField(
                    value = storeName,
                    onValueChange = { storeName = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = green800,
                        focusedLabelColor = green800,
                        cursorColor = green800,
                    ),
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // Items card
        if (analysis.items.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier.size(28.dp).clip(CircleShape).background(green50),
                            contentAlignment = Alignment.Center,
                        ) { Text("🛒", fontSize = 14.sp) }
                        Text(s.billItems, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ink900, modifier = Modifier.weight(1f))
                        Text("${analysis.items.size}", fontSize = 12.sp, color = ink400, fontWeight = FontWeight.SemiBold)
                    }
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    analysis.items.forEachIndexed { idx, item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                                Text("${idx + 1}.", fontSize = 12.sp, color = ink400)
                                Text(item.name, fontSize = 13.sp, color = ink900)
                            }
                            if (item.amount > 0) {
                                Text(item.amount.toString(), fontSize = 13.sp, color = ink600, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
        }

        // Environmental impact card (reuse existing component)
        EnvImpactCard(result = det.toWasteDetectResponse())

        Spacer(Modifier.height(20.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                modifier = Modifier.weight(1f).height(52.dp),
                onClick = onRescan,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ink600),
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(s.billScanAgain, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
            Button(
                modifier = Modifier.weight(1.4f).height(52.dp),
                onClick = { onSave(storeName) },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = green800),
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(s.billSave, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
private fun StatCell(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = valueColor)
        Text(label, fontSize = 11.sp, color = ink400, fontWeight = FontWeight.Medium)
    }
}
