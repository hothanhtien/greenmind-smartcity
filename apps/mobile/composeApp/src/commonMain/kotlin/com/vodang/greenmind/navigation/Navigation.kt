package com.vodang.greenmind.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class AppScreen {
    // Main tabs
    HOME, WASTE_SCAN, TODOS, HOUSEHOLD, BLOG, PROFILE,

    // Detail screens
    CHAT, CHAT_DETAIL,
    WASTE_SORT, WASTE_REPORT, WASTE_IMPACT, WASTE_ANALYTICS, WASTE_TOTAL_MASS,
    HOUSEHOLD_WASTE,
    WASTE_STATUS,
    MEAL_SCAN, BILL_SCAN,
    ENERGY, WALK_DISTANCE, ENVIRONMENTAL_IMPACT,
    CAMPAIGNS,
    PROFILE_DETAIL, SETTINGS, CATALOGUE, PRE_APP_SURVEY, SURVEY,
    HOUSEHOLD_SETTINGS,
    PAYMENT,
}

data class NavItem(
    val screen: AppScreen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

object AppNavItems {
    val items = listOf(
        NavItem(AppScreen.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home),
        NavItem(AppScreen.WASTE_SCAN, "Scan", Icons.Filled.QrCodeScanner, Icons.Outlined.QrCodeScanner),
        NavItem(AppScreen.TODOS, "Todos", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle),
        NavItem(AppScreen.HOUSEHOLD, "Household", Icons.Filled.House, Icons.Outlined.House),
        NavItem(AppScreen.BLOG, "Blog", Icons.Filled.Newspaper, Icons.Outlined.Newspaper),
        NavItem(AppScreen.PROFILE, "Profile", Icons.Filled.Person, Icons.Outlined.Person),
    )

    fun find(screen: AppScreen): NavItem = items.find { it.screen == screen } ?: items.first()
}

data class NavigationState(
    val currentScreen: AppScreen = AppScreen.HOME,
    val backStack: List<AppScreen> = emptyList(),
    val lastBackTime: Long = 0L,
)

object Navigation {
    private val _state = MutableStateFlow(NavigationState())
    val state: StateFlow<NavigationState> = _state.asStateFlow()

    fun getCurrentTab(): AppScreen {
        val current = _state.value.currentScreen
        return when (current) {
            AppScreen.WASTE_SCAN, AppScreen.WASTE_SORT, AppScreen.MEAL_SCAN, AppScreen.BILL_SCAN,
            AppScreen.ENVIRONMENTAL_IMPACT -> AppScreen.WASTE_SCAN
            AppScreen.HOUSEHOLD, AppScreen.HOUSEHOLD_WASTE, AppScreen.HOUSEHOLD_SETTINGS -> AppScreen.HOUSEHOLD
            AppScreen.PAYMENT -> AppScreen.HOME
            else -> current
        }
    }

    fun isTab(screen: AppScreen): Boolean {
        return screen in listOf(
            AppScreen.HOME, AppScreen.WASTE_SCAN, AppScreen.TODOS,
            AppScreen.HOUSEHOLD, AppScreen.BLOG, AppScreen.PROFILE
        )
    }

    fun navigate(screen: AppScreen) {
        _state.update { current ->
            val newStack = if (isTab(current.currentScreen) && screen in listOf(
                    AppScreen.HOME, AppScreen.WASTE_SCAN, AppScreen.TODOS,
                    AppScreen.HOUSEHOLD, AppScreen.BLOG, AppScreen.PROFILE
                )
            ) {
                // When navigating to a tab from a tab, just switch (don't add to stack)
                emptyList()
            } else {
                current.backStack + current.currentScreen
            }
            current.copy(currentScreen = screen, backStack = newStack)
        }
    }

    fun goBack(): Boolean {
        val current = _state.value
        return if (current.backStack.isNotEmpty()) {
            val previous = current.backStack.last()
            _state.value = current.copy(
                currentScreen = previous,
                backStack = current.backStack.dropLast(1)
            )
            true
        } else {
            false
        }
    }

    fun canGoBack(): Boolean = _state.value.backStack.isNotEmpty()

    fun clearAndNavigate(screen: AppScreen) {
        _state.update { it.copy(currentScreen = screen, backStack = emptyList()) }
    }

    fun shouldExit(): Boolean {
        val current = _state.value
        return if (current.backStack.isEmpty()) {
            val now = System.currentTimeMillis()
            if (now - current.lastBackTime < 2000) {
                true
            } else {
                _state.update { it.copy(lastBackTime = now) }
                false
            }
        } else {
            false
        }
    }

    fun resetBackTime() {
        _state.update { it.copy(lastBackTime = 0L) }
    }
}