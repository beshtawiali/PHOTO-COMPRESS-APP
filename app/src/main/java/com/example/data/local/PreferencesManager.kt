package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("photo_compressor_prefs", Context.MODE_PRIVATE)

    private val _onboardingCompleted = MutableStateFlow(
        prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    )
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    private val _themeMode = MutableStateFlow(
        prefs.getString(KEY_THEME_MODE, "system") ?: "system"
    )
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _defaultQuality = MutableStateFlow(
        prefs.getInt(KEY_DEFAULT_QUALITY, 80)
    )
    val defaultQuality: StateFlow<Int> = _defaultQuality.asStateFlow()

    private val _defaultFormat = MutableStateFlow(
        prefs.getString(KEY_DEFAULT_FORMAT, "JPEG") ?: "JPEG"
    )
    val defaultFormat: StateFlow<String> = _defaultFormat.asStateFlow()

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
        _onboardingCompleted.value = completed
    }

    fun setThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
        _themeMode.value = mode
    }

    fun setDefaultQuality(quality: Int) {
        prefs.edit().putInt(KEY_DEFAULT_QUALITY, quality).apply()
        _defaultQuality.value = quality
    }

    fun setDefaultFormat(format: String) {
        prefs.edit().putString(KEY_DEFAULT_FORMAT, format).apply()
        _defaultFormat.value = format
    }

    companion object {
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_DEFAULT_QUALITY = "default_quality"
        private const val KEY_DEFAULT_FORMAT = "default_format"
    }
}
