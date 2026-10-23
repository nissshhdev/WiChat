package com.nissshh.wichat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.nissshh.wichat.core.designsystem.theme.ThemeMode
import com.nissshh.wichat.core.designsystem.theme.WiChatTheme
import com.nissshh.wichat.core.security.SecurityManager
import com.nissshh.wichat.feature.navigation.MainNavigationContainer
import com.nissshh.wichat.feature.onboarding.PasscodeSetupScreen
import com.nissshh.wichat.feature.onboarding.PermissionsOnboardingScreen
import com.nissshh.wichat.feature.onboarding.ProfileCreationScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as WiChatApplication
        val repository = app.repository
        val preferencesRepository = app.preferencesRepository

        setContent {
            val themeMode by preferencesRepository.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            val isOnboardingCompleted by preferencesRepository.isOnboardingCompleted.collectAsState(initial = false)
            val passcodeData by preferencesRepository.getPasscodeData().collectAsState(initial = Pair(null, null))

            val (savedHash, savedSalt) = passcodeData
            val hasPasscode = savedHash != null && savedSalt != null

            var isUnlocked by remember { mutableStateOf(false) }
            var onboardingStep by remember { mutableStateOf(0) } // 0: Passcode, 1: Permissions, 2: Profile

            val scope = rememberCoroutineScope()

            WiChatTheme(themeMode = themeMode) {
                com.nissshh.wichat.core.designsystem.component.TexturedBackground {
                    Crossfade(
                        targetState = when {
                            !hasPasscode -> "ONBOARDING_PASSCODE"
                            hasPasscode && !isOnboardingCompleted -> "ONBOARDING_FLOW"
                            hasPasscode && isOnboardingCompleted && !isUnlocked -> "UNLOCK"
                            else -> "MAIN"
                        },
                        label = "app_root_state"
                    ) { state ->

                        when (state) {
                            "ONBOARDING_PASSCODE" -> {
                                PasscodeSetupScreen(
                                    isUnlockMode = false,
                                    onPasscodeSuccess = { pin ->
                                        val (hash, salt) = SecurityManager.hashPasscode(pin)
                                        scope.launch {
                                            preferencesRepository.savePasscode(hash, salt)
                                            onboardingStep = 1
                                        }
                                    }
                                )
                            }
                            "ONBOARDING_FLOW" -> {
                                when (onboardingStep) {
                                    1 -> {
                                        PermissionsOnboardingScreen(
                                            onContinue = { onboardingStep = 2 }
                                        )
                                    }
                                    2 -> {
                                        ProfileCreationScreen(
                                            onProfileCreated = { profile ->
                                                scope.launch {
                                                    repository.saveProfile(profile)
                                                    preferencesRepository.setOnboardingCompleted(true)
                                                    isUnlocked = true
                                                }
                                            }
                                        )
                                    }
                                    else -> {
                                        PermissionsOnboardingScreen(onContinue = { onboardingStep = 2 })
                                    }
                                }
                            }
                            "UNLOCK" -> {
                                PasscodeSetupScreen(
                                    isUnlockMode = true,
                                    expectedHash = savedHash,
                                    expectedSalt = savedSalt,
                                    onPasscodeSuccess = {
                                        isUnlocked = true
                                    }
                                )
                            }
                            "MAIN" -> {
                                MainNavigationContainer(
                                    repository = repository,
                                    preferencesRepository = preferencesRepository
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
