package com.sloy.sevibus.infrastructure.nightmode

import androidx.appcompat.app.AppCompatDelegate

enum class NightModeSetting(val title: String, val systemUiMode: Int) {
    FOLLOW_SYSTEM("Predeterminado del sistema", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
    LIGHT("Nunca", AppCompatDelegate.MODE_NIGHT_NO),
    DARK("Siempre", AppCompatDelegate.MODE_NIGHT_YES);
}
