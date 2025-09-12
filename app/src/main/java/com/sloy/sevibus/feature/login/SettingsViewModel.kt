package com.sloy.sevibus.feature.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sloy.sevibus.data.api.AdminApi
import com.sloy.sevibus.data.api.model.HealthCheckDto
import com.sloy.sevibus.domain.model.LoggedUser
import com.sloy.sevibus.infrastructure.BuildVariant
import com.sloy.sevibus.infrastructure.analytics.Analytics
import com.sloy.sevibus.infrastructure.analytics.AnalyticsSettingsDataSource
import com.sloy.sevibus.infrastructure.analytics.events.Clicks
import com.sloy.sevibus.infrastructure.nightmode.NightModeDataSource
import com.sloy.sevibus.infrastructure.nightmode.NightModeSetting
import com.sloy.sevibus.infrastructure.session.SessionService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val sessionService: SessionService,
    private val nightModeDataSource: NightModeDataSource,
    private val analyticsSettingsDataSource: AnalyticsSettingsDataSource,
    private val analytics: Analytics,
    private val adminApi: AdminApi,
) : ViewModel() {

    private val isInProgress = MutableStateFlow(false)

    val healthCheckState: StateFlow<HealthCheckState> = flow {
        if (BuildVariant.isDebug()) {
            val state: HealthCheckState = runCatching { adminApi.healthCheck() }
                .map { HealthCheckState.Success(it) }
                .getOrElse { HealthCheckState.Error(it.toString()) }
            emit(state)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), HealthCheckState.NotAvailable)

    val state: StateFlow<SettingsScreenState> =
        combine(sessionService.observeCurrentUser(), isInProgress) { user, isInProgress ->
            when (user) {
                null -> SettingsScreenState.LoggedOut(isInProgress)
                else -> SettingsScreenState.LoggedIn(user)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), SettingsScreenState.LoggedOut(false))

    val currentNightModeState: StateFlow<NightModeSetting> = nightModeDataSource.observeCurrentNightMode()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), NightModeSetting.FOLLOW_SYSTEM)

    val currentAnalyticsState: StateFlow<Boolean> = analyticsSettingsDataSource.observeAnalyticsEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), true)


    fun onLoginClick(context: Context) {
        viewModelScope.launch {
            isInProgress.value = true
            sessionService.manualSignIn(context)
            //TODO show some error message if the login fails
            isInProgress.value = false
        }
    }

    fun onLogoutClick(context: Context) {
        viewModelScope.launch {
            sessionService.signOut(context)
        }
    }

    fun onNightModeChange(mode: NightModeSetting) {
        viewModelScope.launch {
            nightModeDataSource.setNightMode(mode)
        }
    }

    fun onAnalyticsChange(enabled: Boolean) {
        viewModelScope.launch {
            if (!enabled) {
                analytics.track(Clicks.AnalyticsDisabledClicked)
            }
            analyticsSettingsDataSource.setAnalyticsEnabled(enabled)
        }
    }

}

sealed class SettingsScreenState {
    data class LoggedOut(val isInProgress: Boolean = false) : SettingsScreenState()
    data class LoggedIn(val user: LoggedUser) : SettingsScreenState()
}

sealed class HealthCheckState {
    object NotAvailable : HealthCheckState()
    data class Success(val data: HealthCheckDto) : HealthCheckState()
    data class Error(val message: String) : HealthCheckState()
}

