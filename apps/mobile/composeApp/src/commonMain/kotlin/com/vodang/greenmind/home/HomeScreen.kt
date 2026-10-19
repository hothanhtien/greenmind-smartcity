package com.vodang.greenmind.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.components.AppScaffold
import com.vodang.greenmind.platform.BackHandler
import com.vodang.greenmind.home.components.*
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.location.Geo
import com.vodang.greenmind.navigation.AppScreen
import com.vodang.greenmind.navigation.Navigation
import com.vodang.greenmind.permission.PermissionGroup
import com.vodang.greenmind.permission.PermissionRequester
import com.vodang.greenmind.store.HouseholdStore
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.store.TodoStore
import com.vodang.greenmind.store.WalkDistanceStore
import com.vodang.greenmind.wastesort.WasteSortScreen
import com.vodang.greenmind.wastesort.WasteTotalMassScreen
import com.vodang.greenmind.wastereport.WasteReportScreen
import com.vodang.greenmind.wasteimpact.WasteImpactScreen
import com.vodang.greenmind.householdwaste.HouseholdSettingsScreen
import com.vodang.greenmind.householdwaste.HouseholdWasteScreen
import com.vodang.greenmind.householdwaste.WasteStatusScreen
import com.vodang.greenmind.wasteanalytics.WasteAnalyticsScreen
import com.vodang.greenmind.meal.MealScreen
import com.vodang.greenmind.bill.BillScreen
import com.vodang.greenmind.energy.EnergyScreen
import com.vodang.greenmind.SettingsScreen
import com.vodang.greenmind.catalogue.CatalogueScreen
import com.vodang.greenmind.preappsurvey.PreAppSurveyScreen
import com.vodang.greenmind.survey.SurveyScreen
import com.vodang.greenmind.ProfileScreen
import com.vodang.greenmind.blog.BlogScreen
import com.vodang.greenmind.chat.ChatScreen
import com.vodang.greenmind.chat.ChatDetailScreen
import com.vodang.greenmind.chat.ChatThread
import com.vodang.greenmind.payment.PaymentScreen
import com.vodang.greenmind.store.ChatStore
import com.vodang.greenmind.api.campaign.CampaignDto
import com.vodang.greenmind.todos.TodoScreen
import com.vodang.greenmind.platform.exitApp

private val green800 = Color(0xFF2E7D32)
private val green50 = Color(0xFFE8F5E9)
private val textSecondary = Color(0xFF757575)
private val SurfaceWhite = Color(0xFFFFFFFF)
private val SurfaceGray = Color(0xFFF5F5F5)

@Composable
fun HomeScreen(
    onLogout: () -> Unit = {},
    hasPreAppSurvey: Boolean = true,
    onPreAppSurveyCompleted: () -> Unit = {},
) {
    val s = LocalAppStrings.current
    val navState by Navigation.state.collectAsState()
    val user by SettingsStore.user.collectAsState()
    val language by SettingsStore.language.collectAsState()
    val roleSwitchEnabled by SettingsStore.roleSwitcherEnabled.collectAsState()
    val household by HouseholdStore.household.collectAsState()
    val todos by TodoStore.todos.collectAsState()

    var userType by remember { mutableStateOf(UserType.HOUSEHOLD) }
    var showPicker by remember { mutableStateOf(false) }
    var showLangPicker by remember { mutableStateOf(false) }
    var showMenuDrawer by remember { mutableStateOf(false) }
    var showRightDrawer by remember { mutableStateOf(false) }
    var showSearchBar by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var exitToastShown by remember { mutableStateOf(false) }
    var blogTab by remember { mutableIntStateOf(0) }
    var selectedChatThread by remember { mutableStateOf<ChatThread?>(null) }
    var selectedCampaign by remember { mutableStateOf<CampaignDto?>(null) }

    // Scroll states for header hide/show
    val dashboardScrollState = rememberScrollState()
    val collectorScrollState = rememberScrollState()
    val volunteerScrollState = rememberScrollState()
    val todosScrollState = rememberScrollState()
    val blogScrollState = rememberScrollState()
    val blogLazyListState = rememberLazyListState()
    val profileScrollState = rememberScrollState()
    val householdScrollState = rememberScrollState()
    val wasteReportLazyListState = rememberLazyListState()

    val currentTab = Navigation.getCurrentTab()
    val showBackButton = navState.backStack.isNotEmpty()

    // Get active scroll state based on current screen
    val activeScrollState = when (navState.currentScreen) {
        AppScreen.HOME -> when (userType) {
            UserType.COLLECTOR -> collectorScrollState
            UserType.VOLUNTEER -> volunteerScrollState
            else -> dashboardScrollState
        }
        AppScreen.TODOS -> todosScrollState
        AppScreen.BLOG -> blogScrollState
        AppScreen.PROFILE -> profileScrollState
        AppScreen.HOUSEHOLD -> householdScrollState
        else -> null
    }

    val activeLazyListState: androidx.compose.foundation.lazy.LazyListState? = when (navState.currentScreen) {
        AppScreen.BLOG -> blogLazyListState
        else -> null
    }

    // Initialize services
    val locationGranted by PermissionRequester.grantedFlow(PermissionGroup.LOCATION).collectAsState()
    LaunchedEffect(Unit) {
        if (!PermissionRequester.grantedFlow(PermissionGroup.LOCATION).value) {
            PermissionRequester.request(PermissionGroup.LOCATION)
        }
        HouseholdStore.fetchHousehold()
        // Foreground daily-metrics auto-runner. Fires once per ~24 h while the
        // user has metricsAutoEnabled = true (the 21:00 "tối" cadence).
        com.vodang.greenmind.store.OceanStore.maybeRunDailyMetrics()
    }
    LaunchedEffect(locationGranted) {
        if (locationGranted) Geo.service.start()
    }
    LaunchedEffect(Unit) {
        WalkDistanceStore.startPolling()
    }

    // Show exit toast on first back press when at root
    LaunchedEffect(exitToastShown) {
        if (exitToastShown) {
            kotlinx.coroutines.delay(2000)
            exitToastShown = false
        }
    }

    // Handle back button with double-back-to-exit
    BackHandler(enabled = true) {
        // Chat overlay takes priority - close it first
        if (selectedChatThread != null) {
            selectedChatThread = null
            return@BackHandler
        }
        if (selectedCampaign != null) {
            selectedCampaign = null
            return@BackHandler
        }
        if (blogTab == 1 && navState.currentScreen == AppScreen.BLOG) {
            blogTab = 0
        } else if (showBackButton) {
            Navigation.goBack()
        } else {
            if (Navigation.shouldExit()) {
                exitApp()
            } else {
                exitToastShown = true
            }
        }
    }

    val title: String
    val subtitle: String?
    when (navState.currentScreen) {
        AppScreen.HOME -> {
            title = "GreenMind"
            subtitle = s.welcomeBack
        }
        AppScreen.WASTE_SCAN -> {
            title = s.wasteScan
            subtitle = null
        }
        AppScreen.TODOS -> {
            title = s.todos
            subtitle = null
        }
        AppScreen.HOUSEHOLD -> {
            title = household?.address ?: s.household
            subtitle = household?.members?.size?.let { s.memberCount(it) }
        }
        AppScreen.BLOG -> {
            title = s.blog
            subtitle = null
        }
        AppScreen.PROFILE -> {
            title = s.profileTitle
            subtitle = null
        }
        AppScreen.WASTE_SORT -> {
            title = s.wasteSort
            subtitle = null
        }
        AppScreen.WASTE_REPORT -> {
            title = s.wasteReport
            subtitle = null
        }
        AppScreen.WASTE_IMPACT, AppScreen.ENVIRONMENTAL_IMPACT -> {
            title = s.environmentalImpact
            subtitle = null
        }
        AppScreen.WASTE_ANALYTICS -> {
            title = s.wasteStatTitle
            subtitle = null
        }
        AppScreen.WASTE_TOTAL_MASS -> {
            title = s.wasteTotalMassTitle
            subtitle = null
        }
        AppScreen.HOUSEHOLD_WASTE -> {
            title = s.household
            subtitle = null
        }
        AppScreen.MEAL_SCAN -> {
            title = s.scanMeal
            subtitle = null
        }
        AppScreen.BILL_SCAN -> {
            title = s.billScreenTitle
            subtitle = null
        }
        AppScreen.ENERGY -> {
            title = s.electricityUsage
            subtitle = null
        }
        AppScreen.WALK_DISTANCE -> {
            title = s.walkDistance
            subtitle = null
        }
        AppScreen.CAMPAIGNS -> {
            title = s.campaignsTitle
            subtitle = null
        }
        AppScreen.PROFILE_DETAIL -> {
            title = s.profileTitle
            subtitle = null
        }
        AppScreen.SETTINGS -> {
            title = s.settings
            subtitle = null
        }
        AppScreen.HOUSEHOLD_SETTINGS -> {
            title = s.householdSettings
            subtitle = null
        }
        AppScreen.WASTE_STATUS -> {
            title = s.wasteStatus
            subtitle = null
        }
        AppScreen.CATALOGUE -> {
            title = s.catalogue
            subtitle = null
        }
        AppScreen.PRE_APP_SURVEY -> {
            title = s.preAppSurveyTitle
            subtitle = null
        }
        AppScreen.SURVEY -> {
            title = s.surveys
            subtitle = null
        }
        AppScreen.CHAT -> {
            title = s.chatTitle
            subtitle = null
        }
        AppScreen.CHAT_DETAIL -> {
            title = ""
            subtitle = null
        }
        AppScreen.PAYMENT -> {
            title = s.paymentTitle
            subtitle = s.paymentSubtitle
        }
    }

    val trailingActions: @Composable RowScope.() -> Unit = {
        when (currentTab) {
            AppScreen.HOME -> {
                IconButton(onClick = { Navigation.navigate(AppScreen.CHAT) }, modifier = Modifier.size(36.dp)) {
                    BadgedBox(
                        badge = { if (false) Badge(modifier = Modifier.offset(x = 4.dp, y = (-4).dp)) { Text("0") } }
                    ) {
                        Icon(Icons.Filled.Chat, contentDescription = "Chat", tint = green800, modifier = Modifier.size(20.dp))
                    }
                }
                IconButton(onClick = { /* TODO */ }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.Search, contentDescription = "Search", tint = green800, modifier = Modifier.size(20.dp))
                }
            }
            AppScreen.WASTE_SCAN -> {
                IconButton(onClick = { /* TODO */ }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.QrCodeScanner, contentDescription = "Scan", tint = green800, modifier = Modifier.size(22.dp))
                }
                IconButton(onClick = { /* TODO */ }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.CloudUpload, contentDescription = "Upload", tint = green800, modifier = Modifier.size(20.dp))
                }
            }
            AppScreen.TODOS -> {
                IconButton(onClick = { /* TODO */ }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.Add, contentDescription = "Add", tint = green800, modifier = Modifier.size(22.dp))
                }
            }
            AppScreen.BLOG -> {
                IconButton(onClick = { blogTab = 0 }, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Article,
                        contentDescription = s.blogTabPosts,
                        tint = if (blogTab == 0) green800 else textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = { blogTab = 1 }, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = s.blogTabLeaderboard,
                        tint = if (blogTab == 1) green800 else textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            AppScreen.PROFILE -> {
                IconButton(onClick = { Navigation.navigate(AppScreen.SETTINGS) }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = green800, modifier = Modifier.size(20.dp))
                }
                if (roleSwitchEnabled) {
                    IconButton(onClick = { showPicker = true }, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = when (userType) {
                                UserType.HOUSEHOLD -> Icons.Filled.House
                                UserType.COLLECTOR -> Icons.Filled.LocalShipping
                                UserType.VOLUNTEER -> Icons.Filled.VolunteerActivism
                            },
                            contentDescription = "Switch Role",
                            tint = green800,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AppScaffold(
            title = title,
            subtitle = subtitle,
            showBackButton = showBackButton,
            onBackClick = { Navigation.goBack() },
            onMenuClick = { showMenuDrawer = true },
            onSearchClick = { showRightDrawer = true },
            actions = trailingActions,
            selectedNavItem = currentTab,
            onNavItemSelected = { Navigation.navigate(it) },
            scrollState = activeScrollState,
            lazyListState = activeLazyListState,
        ) {
            when (navState.currentScreen) {
                AppScreen.HOME -> {
                    when (userType) {
                        UserType.HOUSEHOLD -> HouseholdDashboard(
                            scrollState = dashboardScrollState,
                            onWasteSortClick = { Navigation.navigate(AppScreen.WASTE_SORT) },
                            onWasteImpactClick = { Navigation.navigate(AppScreen.WASTE_IMPACT) },
                            onHouseholdWasteClick = { Navigation.navigate(AppScreen.HOUSEHOLD_WASTE) },
                            onWasteStatClick = { Navigation.navigate(AppScreen.WASTE_ANALYTICS) },
                            onWasteReportClick = { Navigation.navigate(AppScreen.WASTE_REPORT) },
                            onWasteTotalMassClick = { Navigation.navigate(AppScreen.WASTE_TOTAL_MASS) },
                            onScanMealClick = { Navigation.navigate(AppScreen.MEAL_SCAN) },
                            onScanBillClick = { Navigation.navigate(AppScreen.BILL_SCAN) },
                            onWasteStatusClick = { Navigation.navigate(AppScreen.WASTE_STATUS) },
                            onEnvironmentalImpactClick = { Navigation.navigate(AppScreen.ENVIRONMENTAL_IMPACT) },
                            onPaymentClick = { Navigation.navigate(AppScreen.PAYMENT) },
                            onBlogClick = { Navigation.navigate(AppScreen.BLOG) },
                            onCampaignsClick = { Navigation.navigate(AppScreen.CAMPAIGNS) },
                            showPreAppSurveyBadge = !hasPreAppSurvey,
                            onPreAppSurveyClick = { Navigation.navigate(AppScreen.PRE_APP_SURVEY) },
                        )
                        UserType.COLLECTOR -> CollectorDashboard(user = user, scrollState = collectorScrollState)
                        UserType.VOLUNTEER -> VolunteerDashboard(
                            user = user,
                            scrollState = volunteerScrollState,
                            onSelectCampaign = { campaign ->
                                selectedCampaign = campaign
                            },
                        )
                    }
                }
                AppScreen.WASTE_SCAN -> ScanHubCard(
                    onWasteSortClick = { Navigation.navigate(AppScreen.WASTE_SORT) },
                    onMealScanClick = { Navigation.navigate(AppScreen.MEAL_SCAN) },
                    onBillScanClick = { Navigation.navigate(AppScreen.BILL_SCAN) },
                    onWasteDetectClick = { Navigation.navigate(AppScreen.ENVIRONMENTAL_IMPACT) }
                )
                AppScreen.TODOS -> TodoScreen(scrollState = todosScrollState)
                AppScreen.HOUSEHOLD -> HouseholdWasteScreen(
                    onBack = { Navigation.navigate(AppScreen.HOME) },
                    onNavigateToWasteImpact = { Navigation.navigate(AppScreen.WASTE_IMPACT) },
                    onNavigateToWasteStat = { Navigation.navigate(AppScreen.WASTE_ANALYTICS) },
                    scrollState = householdScrollState,
                    onSettingsClick = { Navigation.navigate(AppScreen.HOUSEHOLD_SETTINGS) },
                )
                AppScreen.BLOG -> BlogScreen(
                    selectedTab = blogTab,
                    onTabChange = { blogTab = it },
                    onSelectCampaign = { campaign ->
                        selectedCampaign = campaign
                    },
                )
                AppScreen.PROFILE -> ProfileScreen(scrollState = profileScrollState)
                AppScreen.WASTE_SORT -> WasteSortScreen(onScanClick = { Navigation.goBack() })
                AppScreen.WASTE_REPORT -> WasteReportScreen(lazyListState = wasteReportLazyListState)
                AppScreen.WASTE_IMPACT, AppScreen.ENVIRONMENTAL_IMPACT -> WasteImpactScreen()
                AppScreen.WASTE_ANALYTICS -> WasteAnalyticsScreen(onBack = { Navigation.goBack() })
                AppScreen.WASTE_TOTAL_MASS -> WasteTotalMassScreen(onBack = { Navigation.goBack() })
                AppScreen.HOUSEHOLD_WASTE -> HouseholdWasteScreen(
                    onBack = { Navigation.goBack() },
                    onNavigateToWasteImpact = { Navigation.navigate(AppScreen.WASTE_IMPACT) },
                    onNavigateToWasteStat = { Navigation.navigate(AppScreen.WASTE_ANALYTICS) },
                    onSettingsClick = { Navigation.navigate(AppScreen.HOUSEHOLD_SETTINGS) },
                )
                AppScreen.HOUSEHOLD_SETTINGS -> HouseholdSettingsScreen(
                    onBack = { Navigation.goBack() },
                )
                AppScreen.WASTE_STATUS -> WasteStatusScreen(
                    onBack = { Navigation.goBack() },
                )
                AppScreen.MEAL_SCAN -> MealScreen()
                AppScreen.BILL_SCAN -> BillScreen()
                AppScreen.ENERGY -> EnergyScreen()
                AppScreen.WALK_DISTANCE -> WalkDistanceCard(onBack = { Navigation.goBack() })
                AppScreen.CAMPAIGNS -> CampaignsList(
                    onBack = { Navigation.goBack() },
                    onSelectCampaign = { campaign ->
                        selectedCampaign = campaign
                    },
                )
                AppScreen.CHAT -> ChatScreen(
                    onBack = { Navigation.goBack() },
                    onThreadClick = {
                        selectedChatThread = it
                    },
                )

                AppScreen.SETTINGS -> SettingsScreen()
                AppScreen.CATALOGUE -> CatalogueScreen(
                    onWasteReport = { Navigation.navigate(AppScreen.WASTE_REPORT) },
                    onPreAppSurvey = { Navigation.navigate(AppScreen.PRE_APP_SURVEY) },
                    onWasteSort = { Navigation.navigate(AppScreen.WASTE_SORT) },
                    onWasteTotalMass = { Navigation.navigate(AppScreen.WASTE_TOTAL_MASS) },
                    onEnvironmentalImpact = { Navigation.navigate(AppScreen.ENVIRONMENTAL_IMPACT) },
                    onWasteImpact = { Navigation.navigate(AppScreen.WASTE_IMPACT) },
                    onWasteStat = { Navigation.navigate(AppScreen.WASTE_ANALYTICS) },
                    onHouseholdWaste = { Navigation.navigate(AppScreen.HOUSEHOLD_WASTE) },
                    onElectricityUsage = { Navigation.navigate(AppScreen.ENERGY) },
                    onWalkDistance = { Navigation.navigate(AppScreen.WALK_DISTANCE) },
                    onScanMeal = { Navigation.navigate(AppScreen.MEAL_SCAN) },
                    onScanBill = { Navigation.navigate(AppScreen.BILL_SCAN) },
                    onBlog = { Navigation.navigate(AppScreen.BLOG) },
                    onCampaigns = { Navigation.navigate(AppScreen.CAMPAIGNS) },
                    onHeatmap = { /* TODO: Collector heatmap feature */ },
                    onRoute = { /* TODO: Collector route feature */ },
                    onCheckIn = { /* TODO: Collector check-in feature */ },
                    onVolunteerEvents = { /* TODO: Volunteer events feature */ },
                    onOceanScore = { /* TODO: Ocean score screen */ },
                    onSurveys = { Navigation.navigate(AppScreen.SURVEY) },
                    onTodos = { Navigation.navigate(AppScreen.TODOS) },
                )
                AppScreen.PRE_APP_SURVEY -> PreAppSurveyScreen(
                    onCompleted = {
                        onPreAppSurveyCompleted()
                        Navigation.goBack()
                    }
                )
                AppScreen.SURVEY -> SurveyScreen()
                AppScreen.PROFILE_DETAIL -> ProfileScreen()
                AppScreen.CHAT_DETAIL -> { }
                AppScreen.PAYMENT -> PaymentScreen()
            }
        }

        // Campaign detail overlay (full screen, outside AppScaffold)
        selectedCampaign?.let { campaign ->
            CampaignDetailScreen(
                campaign = campaign,
                accessToken = SettingsStore.getAccessToken() ?: "",
                onBack = { selectedCampaign = null },
                onRegistered = { /* handle registration if needed */ },
                onOpenChat = { campaignId ->
                    // Find or create chat thread for this campaign
                    val thread = ChatThread(
                        id = campaignId,
                        name = campaign.name,
                        initials = campaign.name.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("").ifBlank { campaign.name.take(2).uppercase() },
                        lastMessage = null,
                        lastMessageAt = null,
                        isGroup = true,
                        campaignStatus = campaign.status,
                        participantCount = campaign.participantsCount,
                    )
                    selectedChatThread = thread
                },
            )
        }

        // Chat detail overlay (outside AppScaffold — hides appbar + navbar)
        selectedChatThread?.let { thread ->
            ChatDetailScreen(
                thread = thread,
                onBack = {
                    ChatStore.closeCampaign()
                    selectedChatThread = null
                },
            )
        }

        // Exit toast overlay
        if (exitToastShown) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF323232))
                ) {
                    Text(
                        text = "Press back again to exit",
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Left drawer overlay
        if (showMenuDrawer) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showMenuDrawer = false }
            ) {
                Surface(
                    modifier = Modifier
                        .width(280.dp)
                        .fillMaxHeight()
                        .clickable(enabled = false) { },
                    color = SurfaceWhite,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(onClick = { showMenuDrawer = false }) {
                                Icon(Icons.Filled.Close, contentDescription = "Close", tint = textSecondary)
                            }
                        }
                        ProfilePlaceholder(user = user, onEditClick = { }, compact = false)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                        DrawerItem(label = s.home, icon = Icons.Filled.Home, onClick = {
                            Navigation.clearAndNavigate(AppScreen.HOME)
                            showMenuDrawer = false
                        })
                        DrawerItem(label = s.todos, icon = Icons.Filled.CheckCircle, onClick = {
                            Navigation.navigate(AppScreen.TODOS)
                            showMenuDrawer = false
                        })
                        DrawerItem(label = s.surveys, icon = Icons.Filled.AssignmentInd, onClick = {
                            Navigation.navigate(AppScreen.SURVEY)
                            showMenuDrawer = false
                        })
                        DrawerItem(label = s.blog, icon = Icons.Filled.Newspaper, onClick = {
                            Navigation.navigate(AppScreen.BLOG)
                            showMenuDrawer = false
                        })
                        DrawerItem(label = s.catalogue, icon = Icons.Filled.Apps, onClick = {
                            Navigation.navigate(AppScreen.CATALOGUE)
                            showMenuDrawer = false
                        })
                        DrawerItem(label = s.settings, icon = Icons.Filled.Settings, onClick = {
                            Navigation.navigate(AppScreen.SETTINGS)
                            showMenuDrawer = false
                        })
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                        LanguageSwitcher(currentLang = language, onClick = { showLangPicker = true })
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    SettingsStore.clearAll()
                                    onLogout()
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color(0xFFC62828), modifier = Modifier.size(20.dp))
                            Text(text = s.logout, color = Color(0xFFC62828), fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        // Right drawer overlay
        AnimatedVisibility(
            visible = showRightDrawer,
            enter = slideInHorizontally(tween(300)) { it },
            exit = slideOutHorizontally(tween(300)) { it }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showRightDrawer = false }
            ) {
                Surface(
                    modifier = Modifier
                        .width(300.dp)
                        .fillMaxHeight()
                        .align(Alignment.CenterEnd)
                        .clickable(enabled = false) { },
                    color = SurfaceWhite,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(s.catalogue, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = green800)
                            IconButton(onClick = { showRightDrawer = false }) {
                                Icon(Icons.Filled.Close, contentDescription = "Close", tint = textSecondary)
                            }
                        }

                        // Search bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(s.blogSearchHint, fontSize = 14.sp) },
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = textSecondary) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Filled.Clear, contentDescription = "Clear", tint = textSecondary)
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = green800,
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                            ),
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        // Function items
                        RightDrawerItem(label = s.wasteSort, icon = Icons.Filled.Cached, onClick = {
                            Navigation.navigate(AppScreen.WASTE_SORT)
                            showRightDrawer = false
                        })
                        RightDrawerItem(label = s.wasteReport, icon = Icons.Filled.Delete, onClick = {
                            Navigation.navigate(AppScreen.WASTE_REPORT)
                            showRightDrawer = false
                        })
                        RightDrawerItem(label = s.wasteTotalMassTitle, icon = Icons.Filled.Scale, onClick = {
                            Navigation.navigate(AppScreen.WASTE_TOTAL_MASS)
                            showRightDrawer = false
                        })
                        RightDrawerItem(label = s.environmentalImpact, icon = Icons.Filled.Warning, onClick = {
                            Navigation.navigate(AppScreen.WASTE_IMPACT)
                            showRightDrawer = false
                        })
                        RightDrawerItem(label = s.wasteImpactTitle, icon = Icons.Filled.Analytics, onClick = {
                            Navigation.navigate(AppScreen.WASTE_ANALYTICS)
                            showRightDrawer = false
                        })
                        RightDrawerItem(label = s.electricityUsage, icon = Icons.Filled.ListAlt, onClick = {
                            Navigation.navigate(AppScreen.WASTE_STATUS)
                            showRightDrawer = false
                        })
                        RightDrawerItem(label = s.walkDistance, icon = Icons.AutoMirrored.Filled.DirectionsWalk, onClick = {
                            Navigation.navigate(AppScreen.WALK_DISTANCE)
                            showRightDrawer = false
                        })
                        RightDrawerItem(label = s.scanMeal, icon = Icons.Filled.Restaurant, onClick = {
                            Navigation.navigate(AppScreen.MEAL_SCAN)
                            showRightDrawer = false
                        })
                        RightDrawerItem(label = s.scanBill, icon = Icons.Filled.Receipt, onClick = {
                            Navigation.navigate(AppScreen.BILL_SCAN)
                            showRightDrawer = false
                        })
                        RightDrawerItem(label = s.campaignsTitle, icon = Icons.Filled.Handshake, onClick = {
                            Navigation.navigate(AppScreen.CAMPAIGNS)
                            showRightDrawer = false
                        })
                        RightDrawerItem(label = s.preAppSurveyTitle, icon = Icons.Filled.Edit, onClick = {
                            Navigation.navigate(AppScreen.PRE_APP_SURVEY)
                            showRightDrawer = false
                        })
                        RightDrawerItem(label = s.settings, icon = Icons.Filled.Settings, onClick = {
                            Navigation.navigate(AppScreen.SETTINGS)
                            showRightDrawer = false
                        })
                    }
                }
            }
        }
    }

    // Role picker
    if (showPicker) {
        UserTypePickerModal(
            current = userType,
            onSelect = { 
                userType = it
                showPicker = false
                Navigation.navigate(AppScreen.HOME)
            },
            onDismiss = { showPicker = false }
        )
    }

    // Language picker
    if (showLangPicker) {
        LanguagePickerModal(
            currentLang = language,
            onSelect = { SettingsStore.setLanguage(it) },
            onDismiss = { showLangPicker = false }
        )
    }
}

@Composable
private fun DrawerItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = green800, modifier = Modifier.size(24.dp))
        Text(text = label, fontSize = 15.sp, color = Color(0xFF424242))
    }
}

@Composable
private fun RightDrawerItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = null, tint = green800, modifier = Modifier.size(22.dp))
            Text(text = label, fontSize = 14.sp, color = Color(0xFF424242))
        }
    }
}