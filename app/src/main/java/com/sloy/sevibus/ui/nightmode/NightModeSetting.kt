package com.sloy.sevibus.ui.nightmode

import androidx.appcompat.app.AppCompatDelegate

enum class NightModeSetting(val title: String, val systemUiMode: Int) {
    FOLLOW_SYSTEM("Según sistema", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
    LIGHT("Siempre claro", AppCompatDelegate.MODE_NIGHT_NO),
    DARK("Siempre oscuro", AppCompatDelegate.MODE_NIGHT_YES);

    companion object {
        fun currentThemeMode(): NightModeSetting = when (AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_NO -> LIGHT
            AppCompatDelegate.MODE_NIGHT_YES -> DARK
            else -> FOLLOW_SYSTEM
        }
    }
}
