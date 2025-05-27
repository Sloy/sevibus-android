package com.sloy.sevibus.infrastructure.nightmode

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class NightModeDataSource(private val context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "night_mode_prefs")
    private val nightModeKey = stringPreferencesKey("night_mode")


    fun observeCurrentNightMode(): Flow<NightModeSetting> {
        return context.dataStore.data.map { preferences ->
            val storedName = preferences[nightModeKey]
            NightModeSetting.entries.find { it.name == storedName } ?: NightModeSetting.FOLLOW_SYSTEM
        }
    }

    suspend fun obtainCurrentNightMode(): NightModeSetting {
        return observeCurrentNightMode().first()
    }

    suspend fun setNightMode(mode: NightModeSetting) {
        context.dataStore.edit { preferences ->
            preferences[nightModeKey] = mode.name
        }
    }

}
