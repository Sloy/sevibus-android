package com.sloy.sevibus.infrastructure.analytics

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.sloy.sevibus.domain.repository.CardsRepository
import com.sloy.sevibus.domain.repository.FavoriteRepository
import com.sloy.sevibus.infrastructure.SevLogger
import com.sloy.sevibus.infrastructure.analytics.events.Events
import com.sloy.sevibus.infrastructure.analytics.events.UserProperty
import com.sloy.sevibus.infrastructure.analytics.events.UserProperty.LocationPermission.State
import com.sloy.sevibus.infrastructure.nfc.NfcStateManager
import com.sloy.sevibus.infrastructure.nightmode.NightModeDataSource
import com.sloy.sevibus.infrastructure.session.SessionService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Keeps the user properties up to date. Receives [Analytics] lazily because it's also one of its trackers,
 * to know the result of the location permission dialog.
 */
class UserPropertiesTracker(
    private val context: Context,
    private val analytics: Lazy<Analytics>,
    private val sessionService: SessionService,
    private val favoriteRepository: FavoriteRepository,
    private val cardsRepository: CardsRepository,
    private val nfcStateManager: NfcStateManager,
    private val nightModeDataSource: NightModeDataSource,
) : Tracker {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_properties_prefs")
    private val locationPermissionAskedKey = booleanPreferencesKey("location_permission_asked")
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun start() {
        sessionService.observeCurrentUser().map { it != null }.report { UserProperty.IsLoggedIn(it) }
        favoriteRepository.observeFavorites().map { it.size }.report { UserProperty.FavoritesCount(it) }
        cardsRepository.observeUserCards().map { it.size }.report { UserProperty.CardsCount(it) }
        nfcStateManager.state.report { UserProperty.NfcState(it) }
        nightModeDataSource.observeCurrentNightMode().report { UserProperty.NightMode(it) }
        scope.launch {
            analytics.value.setUserProperty(UserProperty.LocationPermission(currentLocationPermission()))
        }
    }

    override fun track(event: SevEvent) {
        if (event !is Events.LocationPermissionResult) return
        scope.launch {
            context.dataStore.edit { it[locationPermissionAskedKey] = true }
            val state = when (event.result) {
                Events.LocationPermissionResult.Result.GRANTED -> State.GRANTED
                Events.LocationPermissionResult.Result.DENIED -> State.DENIED
            }
            analytics.value.setUserProperty(UserProperty.LocationPermission(state))
        }
    }

    private suspend fun currentLocationPermission(): State {
        val isGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val wasAsked = context.dataStore.data.first()[locationPermissionAskedKey] ?: false
        return when {
            isGranted -> State.GRANTED
            wasAsked -> State.DENIED
            else -> State.NOT_ASKED
        }
    }

    private fun <T> Flow<T>.report(toProperty: (T) -> UserProperty) {
        distinctUntilChanged()
            .onEach { analytics.value.setUserProperty(toProperty(it)) }
            .catch { SevLogger.logW(it, "Error observing user property") }
            .launchIn(scope)
    }
}
