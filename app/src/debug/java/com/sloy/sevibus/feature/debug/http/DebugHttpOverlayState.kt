package com.sloy.sevibus.feature.debug.http

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.EmptyCoroutineContext

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class DebugHttpOverlayState(private val context: Context) : HttpOverlayState {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "debug_prefs")
    private val isVisibleKey = booleanPreferencesKey("isVisible")
    private val scope = CoroutineScope(SupervisorJob())
    override val items = MutableStateFlow<List<HttpOverlayItem>>(emptyList())
    override var isVisible by mutableStateOf(runBlocking {
        context.dataStore.data.first()[isVisibleKey] ?: true
    })
        private set

    override fun setVisibility(visible: Boolean) {
        isVisible = visible
        scope.launch {
            context.dataStore.edit { preferences ->
                preferences[isVisibleKey] = visible
            }
        }
    }

    override fun put(item: HttpOverlayItem) {
        scope.launch {
            items.update { current ->
                if (current.any { it.id == item.id }) {
                    current.map { if (it.id == item.id) item else it }
                } else {
                    current + item
                }
            }

            if (item.status != null) {
                delay(5_000)
                items.update { it - item }
            }
        }
    }
}
