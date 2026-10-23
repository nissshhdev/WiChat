package com.nissshh.wichat.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nissshh.wichat.core.designsystem.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "wichat_preferences")

class PreferencesRepository(private val context: Context) {
    companion object {
        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val KEY_PASSCODE_HASH = stringPreferencesKey("passcode_hash")
        private val KEY_PASSCODE_SALT = stringPreferencesKey("passcode_salt")
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_REDUCE_MOTION = booleanPreferencesKey("reduce_motion")
        private val KEY_DISCOVERY_ENABLED = booleanPreferencesKey("discovery_enabled")
        private val KEY_DOWNLOAD_OVER_WIFI_ONLY = booleanPreferencesKey("download_over_wifi_only")
    }


    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_COMPLETED] ?: false
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        val str = prefs[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.name
        try {
            ThemeMode.valueOf(str)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }

    val isReduceMotion: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_REDUCE_MOTION] ?: false
    }

    val isDiscoveryEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_DISCOVERY_ENABLED] ?: true
    }

    val isDownloadOverWifiOnly: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_DOWNLOAD_OVER_WIFI_ONLY] ?: true
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun savePasscode(hash: String, salt: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_PASSCODE_HASH] = hash
            prefs[KEY_PASSCODE_SALT] = salt
        }
    }

    fun getPasscodeData(): Flow<Pair<String?, String?>> = context.dataStore.data.map { prefs ->
        Pair(prefs[KEY_PASSCODE_HASH], prefs[KEY_PASSCODE_SALT])
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode.name
        }
    }

    suspend fun setReduceMotion(reduce: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_REDUCE_MOTION] = reduce
        }
    }

    suspend fun setDiscoveryEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DISCOVERY_ENABLED] = enabled
        }
    }

    suspend fun setDownloadOverWifiOnly(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DOWNLOAD_OVER_WIFI_ONLY] = enabled
        }
    }
}

