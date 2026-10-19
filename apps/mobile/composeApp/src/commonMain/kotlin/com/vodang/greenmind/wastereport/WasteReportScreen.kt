package com.vodang.greenmind.wastereport

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.wastereport.CreateWasteReportRequest
import com.vodang.greenmind.api.wastereport.WasteReportDto
import com.vodang.greenmind.api.wastereport.createWasteReport
import com.vodang.greenmind.api.wastereport.getAllWasteReports
import com.vodang.greenmind.api.wastereport.getMyWasteReports
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.util.AppLogger
import kotlinx.coroutines.launch

private val green800 = Color(0xFF2E7D32)
private val green600 = Color(0xFF388E3C)

private enum class SubmitStep {
    SENDING_DATA,
    COMPLETING,
}

@Composable
fun WasteReportScreen(lazyListState: androidx.compose.foundation.lazy.LazyListState? = null) {
    val s = LocalAppStrings.current
    val scope = rememberCoroutineScope()

    var showScan by remember { mutableStateOf(false) }
    var scanFromGallery by remember { mutableStateOf(false) }
    var fabExpanded by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedReport by remember { mutableStateOf<WasteReportDto?>(null) }

    var myReports by remember { mutableStateOf<List<WasteReportDto>>(emptyList()) }
    var allReports by remember { mutableStateOf<List<WasteReportDto>>(emptyList()) }
    var isLoadingMy by remember { mutableStateOf(true) }
    var isLoadingAll by remember { mutableStateOf(false) }
    var allLoaded by remember { mutableStateOf(false) }

    var isSubmitting by remember { mutableStateOf(false) }
    var submitStep by remember { mutableStateOf(SubmitStep.SENDING_DATA) }
    var submitError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val token = SettingsStore.getAccessToken() ?: run { isLoadingMy = false; return@LaunchedEffect }
        try {
            myReports = getMyWasteReports(token).data
        } catch (e: Throwable) {
            AppLogger.e("WasteReport", "Load my reports failed: ${e.message}")
        }
        isLoadingMy = false
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab == 1 && !allLoaded) {
            isLoadingAll = true
            val token = SettingsStore.getAccessToken() ?: run { isLoadingAll = false; return@LaunchedEffect }
            try {
                allReports = getAllWasteReports(token).data
                allLoaded = true
            } catch (e: Throwable) {
                AppLogger.e("WasteReport", "Load all reports failed: ${e.message}")
            }
            isLoadingAll = false
        }
    }

    fun reloadMyReports() {
        scope.launch {
            isLoadingMy = true
            val token = SettingsStore.getAccessToken() ?: run { isLoadingMy = false; return@launch }
            try {
                myReports = getMyWasteReports(token).data
            } catch (e: Throwable) {
                AppLogger.e("WasteReport", "Reload my reports failed: ${e.message}")
            }
            isLoadingMy = false
        }
    }

    val myListState  = lazyListState ?: rememberLazyListState()
    val allListState = rememberLazyListState()

    Box(modifier = Modifier.fillMaxSize()) {
        if (showScan) {
            WasteReportScanScreen(
                onStartSubmit = { form ->
                    isSubmitting = true
                    submitStep = SubmitStep.SENDING_DATA
                    submitError = null
                    scope.launch {
                        val token = SettingsStore.getAccessToken() ?: run {
                            isSubmitting = false
                            submitError = "No access token"
                            return@launch
                        }
                        try {
                            submitStep = SubmitStep.SENDING_DATA
                            val created = createWasteReport(
                                token,
                                CreateWasteReportRequest(
                                    wardName    = form.wardName,
                                    lat         = form.lat,
                                    lng         = form.lng,
                                    description = form.description,
                                    imageUrl    = form.imageUrl,
                                )
                            )
                            submitStep = SubmitStep.COMPLETING
                            myReports = listOf(created) + myReports
                            if (allLoaded) allReports = listOf(created) + allReports
                            kotlinx.coroutines.delay(500)
                            showScan = false
                        } catch (e: Throwable) {
                            AppLogger.e("WasteReport", "Create failed: ${e.message}")
                            submitError = e.message ?: "Failed to submit report"
                        }
                        isSubmitting = false
                    }
                },
                onBack = { showScan = false },
                launchCamera = !scanFromGallery,
                isSubmitting = isSubmitting,
            )
        } else {
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
                Column(modifier = Modifier.fillMaxSize()) {
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = green800,
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            s.wasteReportMyTab,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp,
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            s.wasteReportAllTab,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp,
                        )
                    }
                )
            }

            val items = if (selectedTab == 0) myReports else allReports
            val isLoading = if (selectedTab == 0) isLoadingMy else isLoadingAll
            val listState = if (selectedTab == 0) myListState else allListState

            when {
                isLoading && items.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = green800)
                }

                items.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            s.wasteReportEmpty,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                else -> Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(items, key = { it.id }) { report ->
                            WasteReportCard(report, onClick = { selectedReport = report })
                        }
                        item { Spacer(Modifier.height(72.dp)) } // FAB clearance
                    }
                    
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = green800,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(16.dp)
                                .size(24.dp)
                        )
                    }
                }
            }
        }
        }
    }

        // Submit loading overlay
        if (isSubmitting) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    if (submitStep == SubmitStep.COMPLETING) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(s.wasteReportComplete, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(s.wasteReportSendingReport, color = Color.White, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(s.wasteReportPhotoUploaded, color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                )
                                Text(s.wasteReportSendingInfo, color = Color.White, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // Submit error snackbar
        submitError?.let { errorMsg ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .navigationBarsPadding(),
                action = {
                    TextButton(onClick = { submitError = null }) {
                        Text(s.close, color = Color.White)
                    }
                },
                containerColor = Color(0xFFC62828),
                contentColor = Color.White,
            ) {
                Text(errorMsg)
            }
        }

        // FAB with expandable menu
        if (!showScan) {
            Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AnimatedVisibility(
                visible = fabExpanded,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    MiniFabRow(
                        icon = Icons.Filled.Image,
                        label = s.uploadImage,
                        onClick = { fabExpanded = false; scanFromGallery = true; showScan = true }
                    )
                    MiniFabRow(
                        icon = Icons.Filled.Add,
                        label = s.takePhoto,
                        onClick = { fabExpanded = false; scanFromGallery = false; showScan = true }
                    )
                }
            }

            FloatingActionButton(
                onClick = { fabExpanded = !fabExpanded },
                containerColor = green800,
                contentColor = Color.White,
                shape = CircleShape,
            ) {
                Icon(
                    imageVector = if (fabExpanded) Icons.Filled.Close else Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(if (fabExpanded) 20.dp else 28.dp)
                )
            }
        }
    }

    selectedReport?.let { report ->
        WasteReportDetailSheet(report = report, onDismiss = { selectedReport = null })
    }
    }
}

@Composable
private fun MiniFabRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Text(label, fontSize = 13.sp, color = Color(0xFF1B1B1B), fontWeight = FontWeight.Medium)
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(green800),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}

