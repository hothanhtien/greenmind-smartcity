package com.vodang.greenmind.home.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import com.vodang.greenmind.i18n.AppStrings
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.theme.*

enum class BottomNavTab {
    HOME,
    WASTE_SCAN,
    TODOS,
    HOUSEHOLD,
    BLOG,
    PROFILE;

    fun label(s: AppStrings): String = when (this) {
        HOME -> s.home
        WASTE_SCAN -> s.wasteScan
        TODOS -> s.todos
        HOUSEHOLD -> s.household
        BLOG -> s.blog
        PROFILE -> s.profileTab
    }
}

@Composable
fun HomeBottomNavigation(
    selectedTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val s = LocalAppStrings.current
    NavigationBar(
        modifier = modifier.height(56.dp),
        containerColor = SurfaceWhite,
        tonalElevation = 2.dp
    ) {
        BottomNavTab.entries.forEach { tab ->
            val selected = tab == selectedTab

            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = when (tab) {
                            BottomNavTab.HOME -> if (selected) Icons.Filled.Home else Icons.Outlined.Home
                            BottomNavTab.WASTE_SCAN -> if (selected) Icons.Filled.QrCodeScanner else Icons.Outlined.QrCodeScanner
                            BottomNavTab.TODOS -> if (selected) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle
                            BottomNavTab.HOUSEHOLD -> if (selected) Icons.Filled.House else Icons.Outlined.House
                            BottomNavTab.BLOG -> if (selected) Icons.Filled.Newspaper else Icons.Outlined.Newspaper
                            BottomNavTab.PROFILE -> if (selected) Icons.Filled.Person else Icons.Outlined.Person
                        },
                        contentDescription = tab.label(s),
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = tab.label(s),
                        fontSize = 10.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Green800,
                    selectedTextColor = Green800,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary,
                    indicatorColor = Green50
                )
            )
        }
    }
}
