package com.sloy.sevibus.infrastructure.nightmode

import androidx.appcompat.app.AppCompatDelegate
import com.sloy.sevibus.R

enum class NightModeSetting(val titleRes: Int, val systemUiMode: Int) {
    FOLLOW_SYSTEM(R.string.settings_night_mode_system, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
    LIGHT(R.string.settings_night_mode_never, AppCompatDelegate.MODE_NIGHT_NO),
    DARK(R.string.settings_night_mode_always, AppCompatDelegate.MODE_NIGHT_YES);
}
