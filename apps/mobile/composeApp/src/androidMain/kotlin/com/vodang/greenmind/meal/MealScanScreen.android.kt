package com.vodang.greenmind.meal

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
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
import androidx.compose.foundation.Canvas
import androidx.core.content.FileProvider
import com.vodang.greenmind.api.meal.MealAnalysisResult
import com.vodang.greenmind.permission.CameraPermissionGate
import com.vodang.greenmind.api.meal.analyzeMealByUrl
import com.vodang.greenmind.i18n.LocalAppStrings
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

private enum class MealScanPhase { IDLE, CAPTURED, ANALYZING, RESULT }

private data class RatingTone(val main: Color, val soft: Color, val accent: Color)

private fun ratingTone(ratio: Int): RatingTone = when {
    ratio >= 70 -> RatingTone(green800, green50, green400)
    ratio >= 40 -> RatingTone(orange700, orange50, Color(0xFFFFB74D))
    else        -> RatingTone(red600, red50, Color(0xFFEF5350))
}

private fun createMealPhotoUri(context: Context): Uri {
    val dir = File(context.cacheDir, "camera_photos").also { it.mkdirs() }
    val file = File(dir, "meal_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

@Composable
actual fun MealScanScreen(
    onScanComplete: (plantRatio: Int, description: String, imageUrl: String?, plantImageBase64: String?, dishImageBase64: String?) -> Unit,
    onBack: () -> Unit,
) {
    CameraPermissionGate(onDenied = onBack) {
        MealScanContent(onScanComplete = onScanComplete, onBack = onBack)
    }
}

@Composable
private fun MealScanContent(
    onScanComplete: (plantRatio: Int, description: String, imageUrl: String?, plantImageBase64: String?, dishImageBase64: String?) -> Unit,
    onBack: () -> Unit,
) {
    val s = LocalAppStrings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var phase by remember { mutableStateOf(MealScanPhase.IDLE) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var capturedBytes by remember { mutableStateOf<ByteArray?>(null) }
    var result by remember { mutableStateOf<MealAnalysisResult?>(null) }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        if (saved) {
            val uri = photoUri
            if (uri != null) {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null) {
                    capturedBytes = bytes
                    phase = MealScanPhase.CAPTURED
                }
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes != null) {
                capturedBytes = bytes
                phase = MealScanPhase.CAPTURED
            }
        }
    }

    val launchCamera: () -> Unit = {
        error = null
        val uri = createMealPhotoUri(context)
        photoUri = uri
        cameraLauncher.launch(uri)
    }
    val launchGallery: () -> Unit = {
        error = null
        galleryLauncher.launch("image/*")
    }
    val startAnalysis: () -> Unit = analyze@{
        val bytes = capturedBytes ?: return@analyze
        phase = MealScanPhase.ANALYZING
        scope.launch {
            try {
                val token = com.vodang.greenmind.store.SettingsStore.getAccessToken()
                if (token == null) {
                    error = s.mealError
                    phase = MealScanPhase.IDLE
                    return@launch
                }
                val uploadedUrl = com.vodang.greenmind.api.upload.requestAndUpload(
                    accessToken = token,
                    filename = "meal_${System.currentTimeMillis()}.jpg",
                    fileBytes = bytes,
                    contentType = "image/jpeg",
                ).imageUrl
                val analysisResult = analyzeMealByUrl(uploadedUrl, token)
                result = analysisResult
                imageUrl = uploadedUrl
                phase = MealScanPhase.RESULT
            } catch (_: Throwable) {
                error = s.mealError
                phase = MealScanPhase.IDLE
            }
        }
    }

    when (phase) {
        MealScanPhase.IDLE -> MealScanIdle(
            onBack = onBack,
            onCamera = launchCamera,
            onGallery = launchGallery,
            error = error,
        )

        MealScanPhase.CAPTURED -> MealScanCaptured(
            bytes = capturedBytes,
            onBack = { capturedBytes = null; phase = MealScanPhase.IDLE },
            onRetake = launchCamera,
            onAnalyze = startAnalysis,
        )

        MealScanPhase.ANALYZING -> MealScanAnalyzing()

        MealScanPhase.RESULT -> {
            val res = result
            if (res != null) {
                MealScanResult(
                    res = res,
                    onRescan = {
                        capturedBytes = null
                        result = null
                        imageUrl = null
                        phase = MealScanPhase.IDLE
                    },
                    onSave = { name ->
                        onScanComplete(res.plantRatio, name, imageUrl, res.plantImageBase64, res.dishImageBase64)
                    },
                )
            }
        }
    }
}

// ── IDLE ──────────────────────────────────────────────────────────────────────

@Composable
private fun MealScanIdle(
    onBack: () -> Unit,
    onCamera: () -> Unit,
    onGallery: () -> Unit,
    error: String?,
) {
    val s = LocalAppStrings.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(green50, mint, Color.White))
            )
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
                Text(
                    "AI",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = green800,
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Hero illustration
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center,
        ) {
            // Decorative outer rings
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
                    .background(
                        Brush.linearGradient(listOf(green600, green800))
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text("🥗", fontSize = 48.sp)
            }
        }

        Spacer(Modifier.height(20.dp))

        // Title block
        Text(
            text = s.mealScanTitle,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ink900,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = s.mealCapture,
            fontSize = 14.sp,
            color = ink600,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(28.dp))

        // Primary CTA — Camera
        ActionCardButton(
            icon = Icons.Filled.PhotoCamera,
            title = s.mealCapture,
            subtitle = "Scan now",
            primary = true,
            onClick = onCamera,
        )

        Spacer(Modifier.height(12.dp))

        // Secondary CTA — Gallery
        ActionCardButton(
            icon = Icons.Filled.Image,
            title = s.mealUpload,
            subtitle = "From gallery",
            primary = false,
            onClick = onGallery,
        )

        Spacer(Modifier.height(24.dp))

        // Tips card
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
                TipRow("📐", "Đặt món ăn gọn trong khung hình")
                TipRow("☀️", "Chụp với ánh sáng tự nhiên, không bóng")
                TipRow("🍽️", "Chụp từ trên xuống, gần toàn bộ đĩa")
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
        baseModifier
            .background(Color.White)
            .padding(0.dp)
    }

    Box(
        modifier = withBg
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(iconBg),
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
private fun MealScanCaptured(
    bytes: ByteArray?,
    onBack: () -> Unit,
    onRetake: () -> Unit,
    onAnalyze: () -> Unit,
) {
    val s = LocalAppStrings.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ink900),
    ) {
        // Top bar
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

        // Image card
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

        // Bottom action bar
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
                    Text(s.mealRetake, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                Button(
                    modifier = Modifier.weight(1.4f).height(52.dp),
                    onClick = onAnalyze,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = green800),
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(s.mealAnalyze, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// ── ANALYZING ─────────────────────────────────────────────────────────────────

@Composable
private fun MealScanAnalyzing() {
    val s = LocalAppStrings.current
    val infinite = rememberInfiniteTransition(label = "analyzing")
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
                        .background(
                            Brush.linearGradient(listOf(green600, green800))
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.Restaurant,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp),
                    )
                }
            }

            Text(s.mealAnalyzing, color = ink900, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(
                "AI đang ước tính tỉ lệ rau củ trong bữa ăn…",
                color = ink600,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp),
            )

            // Step dots
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
private fun MealScanResult(
    res: MealAnalysisResult,
    onRescan: () -> Unit,
    onSave: (String) -> Unit,
) {
    val s = LocalAppStrings.current
    val tone = ratingTone(res.plantRatio)
    val feedback = when {
        res.plantRatio >= 70 -> s.mealRatioGood
        res.plantRatio >= 40 -> s.mealRatioOk
        else                 -> s.mealRatioLow
    }
    val badge = when {
        res.plantRatio >= 70 -> "Tuyệt vời"
        res.plantRatio >= 40 -> "Ổn"
        else                 -> "Cần thêm rau"
    }
    var mealName by remember(res) { mutableStateOf(res.description) }

    fun decodeBase64Bitmap(b64: String?) = b64?.let {
        runCatching {
            val clean = if (it.contains(',')) it.substringAfter(',') else it
            val bytes = Base64.decode(clean, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        }.getOrNull()
    }

    val plantBitmap = remember(res.plantImageBase64) { decodeBase64Bitmap(res.plantImageBase64) }

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

        // Hero card — circular gauge
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
                // Gauge
                Box(
                    modifier = Modifier.size(180.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val stroke = Stroke(width = 18f, cap = StrokeCap.Round)
                        // Track
                        drawArc(
                            color = tone.soft,
                            startAngle = 135f,
                            sweepAngle = 270f,
                            useCenter = false,
                            topLeft = Offset(stroke.width / 2, stroke.width / 2),
                            size = Size(size.width - stroke.width, size.height - stroke.width),
                            style = stroke,
                        )
                        // Progress arc
                        val sweep = (res.plantRatio.coerceIn(0, 100) / 100f) * 270f
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
                        Text("Rau củ", fontSize = 12.sp, color = ink400, fontWeight = FontWeight.Medium)
                        Text(
                            "${res.plantRatio}",
                            fontSize = 56.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = tone.main,
                        )
                        Text("%", fontSize = 14.sp, color = tone.main, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Status pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(tone.soft)
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                ) {
                    Text(badge, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = tone.main)
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    feedback,
                    fontSize = 13.sp,
                    color = ink600,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        // Plant overlay preview
        if (plantBitmap != null) {
            Spacer(Modifier.height(14.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(green50),
                            contentAlignment = Alignment.Center,
                        ) { Text("🌿", fontSize = 14.sp) }
                        Text(
                            "Vùng rau củ được nhận diện",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ink900,
                        )
                    }
                    Image(
                        bitmap = plantBitmap,
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFF7F7F7)),
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Meal name input
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(s.mealNameLabel, fontSize = 12.sp, color = ink400, fontWeight = FontWeight.Medium)
                OutlinedTextField(
                    value = mealName,
                    onValueChange = { mealName = it },
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
                Text(s.mealScanAgain, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
            Button(
                modifier = Modifier.weight(1.4f).height(52.dp),
                onClick = { onSave(mealName) },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = green800),
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(s.mealSave, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
