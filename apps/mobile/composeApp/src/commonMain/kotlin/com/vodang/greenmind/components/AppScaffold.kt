package com.vodang.greenmind.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.navigation.AppNavItems
import com.vodang.greenmind.navigation.AppScreen
import com.vodang.greenmind.theme.Green800
import com.vodang.greenmind.theme.Green50
import com.vodang.greenmind.theme.SurfaceWhite
import com.vodang.greenmind.theme.SurfaceGray
import com.vodang.greenmind.theme.TextSecondary
import com.vodang.greenmind.theme.DividerColor
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun AppScaffold(
    title: String,
    subtitle: String? = null,
    showBackButton: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    onSearchClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    selectedNavItem: AppScreen? = null,
    onNavItemSelected: ((AppScreen) -> Unit)? = null,
    scrollState: ScrollState? = null,
    lazyListState: LazyListState? = null,
    content: @Composable () -> Unit,
) {
    val navItems = AppNavItems.items

    // Scroll detection for hide/show header
    var headerVisible by remember { mutableStateOf(true) }
    var lastScrollValue by remember { mutableIntStateOf(0) }

    // Observe scroll changes from either ScrollState or LazyListState
    LaunchedEffect(scrollState, lazyListState) {
        val scrollFlow = when {
            scrollState != null -> snapshotFlow { scrollState.value }
            lazyListState != null -> snapshotFlow { lazyListState.firstVisibleItemScrollOffset }
            else -> return@LaunchedEffect
        }

        scrollFlow.distinctUntilChanged().collect { current ->
            if (current > lastScrollValue && current > 50) {
                headerVisible = false
            } else if (current < lastScrollValue) {
                headerVisible = true
            }
            lastScrollValue = current
        }
    }

    Scaffold(
        containerColor = SurfaceGray,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceWhite)
            ) {
                // App Bar Row - compact (hides on scroll)
                if (headerVisible) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Leading icon (menu or back) - smaller
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Green50)
                                .clickable {
                                    if (showBackButton && onBackClick != null) {
                                        onBackClick()
                                    } else if (onMenuClick != null) {
                                        onMenuClick()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (showBackButton) Icons.AutoMirrored.Filled.ArrowBack else Icons.Filled.Menu,
                                contentDescription = if (showBackButton) "Back" else "Menu",
                                tint = Green800,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(Modifier.width(8.dp))

                        // Title and subtitle column - smaller text
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Green800,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (subtitle != null) {
                                Text(
                                    text = subtitle,
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        if (onSearchClick != null) {
                            IconButton(onClick = onSearchClick, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Filled.Search, contentDescription = "Search", tint = Green800, modifier = Modifier.size(20.dp))
                            }
                        }

                        // Actions - smaller
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            content = actions
                        )
                    }

                    HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                }

                // Top Navigation Bar - always visible
                if (selectedNavItem != null && onNavItemSelected != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceWhite)
                            .then(if (!headerVisible) Modifier.statusBarsPadding() else Modifier)
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        navItems.forEach { item ->
                            val isSelected = item.screen == selectedNavItem
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onNavItemSelected(item.screen) }
                                    .padding(vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label,
                                    tint = if (isSelected) Green800 else TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.height(1.dp))
                                Text(
                                    text = item.label,
                                    fontSize = 9.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) Green800 else TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            content()
        }
    }
}

// BackHandler is provided by platform.BackHandler