package com.sloy.sevibus.infrastructure.analytics.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sloy.sevibus.domain.model.StopId
import com.sloy.sevibus.infrastructure.analytics.events.Events.SessionSummary.SessionType
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class SessionHistoryDataSource(private val context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session_history_prefs")
    private val historyKey = stringPreferencesKey("session_history")
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun obtainHistory(): SessionHistory {
        val stored = context.dataStore.data.first()[historyKey] ?: return SessionHistory()
        return runCatching { json.decodeFromString<SessionHistory>(stored) }.getOrDefault(SessionHistory())
    }

    suspend fun saveHistory(history: SessionHistory) {
        context.dataStore.edit { preferences ->
            preferences[historyKey] = json.encodeToString(history)
        }
    }
}

@Serializable
data class SessionHistory(
    val sessions: List<SessionRecord> = emptyList(),
    val recentStopViews: List<StopView> = emptyList(),
)

@Serializable
data class SessionRecord(val epochDay: Long, val sessionType: SessionType)

@Serializable
data class StopView(val stopId: StopId, val viewedAt: Long)
