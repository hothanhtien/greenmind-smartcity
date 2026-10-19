package com.vodang.greenmind

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.vodang.greenmind.api.auth.ApiException
import com.vodang.greenmind.api.auth.getProfile
import com.vodang.greenmind.api.auth.toUserDto
import com.vodang.greenmind.api.preappsurvey.getPreAppSurveyByUser
import com.vodang.greenmind.home.HomeScreen
import com.vodang.greenmind.i18n.EnStrings
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.i18n.ViStrings
import com.vodang.greenmind.location.Geo
import com.vodang.greenmind.store.SettingsStore

@Composable
expect fun CameraScreen()

enum class AppScreen { LOGIN, REGISTER, HOME }

@Composable
@Preview
fun App() {
    val language by SettingsStore.language.collectAsState()
    val strings = if (language == "vi") ViStrings else EnStrings

    CompositionLocalProvider(LocalAppStrings provides strings) {
        MaterialTheme {
            var currentScreen by remember { mutableStateOf(AppScreen.LOGIN) }
            var isCheckingAuth by remember { mutableStateOf(true) }
            var hasPreAppSurvey by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                Geo.service.start()
                val token = SettingsStore.getAccessToken()
                if (token.isNullOrBlank()) {
                    currentScreen = AppScreen.LOGIN
                } else {
                    try {
                        val profile = getProfile(token)
                        SettingsStore.setUser(profile.toUserDto())
                        currentScreen = AppScreen.HOME
                        // Check if user has completed the pre-app survey
                        hasPreAppSurvey = try {
                            getPreAppSurveyByUser(token, profile.id)
                            true
                        } catch (e: ApiException) {
                            false // 404 = survey not yet completed
                        } catch (_: Throwable) {
                            true // network error → don't nag the user
                        }
                    } catch (e: ApiException) {
                        // 401 = token expired/invalid → force re-login
                        SettingsStore.setAccessToken(null)
                        SettingsStore.setUser(null)
                        currentScreen = AppScreen.LOGIN
                    } catch (_: Throwable) {
                        // Network unavailable — honour the stored token optimistically
                        currentScreen = AppScreen.HOME
                    }
                }
                isCheckingAuth = false
            }

            if (isCheckingAuth) {
                SplashScreen()
            } else {
                when (currentScreen) {
                    AppScreen.LOGIN -> LoginScreen(
                        onLoginSuccess = { currentScreen = AppScreen.HOME },
                        onNavigateToRegister = { currentScreen = AppScreen.REGISTER }
                    )
                    AppScreen.REGISTER -> RegisterScreen(
                        onRegisterSuccess = { currentScreen = AppScreen.HOME },
                        onCancel = { currentScreen = AppScreen.LOGIN }
                    )
                    AppScreen.HOME -> HomeScreen(
                        onLogout = { currentScreen = AppScreen.LOGIN },
                        hasPreAppSurvey = hasPreAppSurvey,
                        onPreAppSurveyCompleted = { hasPreAppSurvey = true },
                    )
                }
            }
        }
    }
}
