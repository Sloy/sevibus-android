package com.sloy.sevibus.feature.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sloy.sevibus.domain.model.LoggedUser
import com.sloy.sevibus.infrastructure.nightmode.NightModeDataSource
import com.sloy.sevibus.infrastructure.nightmode.NightModeSetting
import com.sloy.sevibus.infrastructure.session.SessionService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val sessionService: SessionService, private val nightModeDataSource: NightModeDataSource) : ViewModel() {

    private val isInProgress = MutableStateFlow(false)

    val state: StateFlow<SettingsScreenState> =
        combine(sessionService.observeCurrentUser(), isInProgress) { user, isInProgress ->
            when (user) {
                null -> SettingsScreenState.LoggedOut(isInProgress)
                else -> SettingsScreenState.LoggedIn(user)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), SettingsScreenState.LoggedOut(false))

    val currentNightModeState: StateFlow<NightModeSetting> = nightModeDataSource.observeCurrentNightMode()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), NightModeSetting.FOLLOW_SYSTEM)

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

}

sealed class SettingsScreenState {
    data class LoggedOut(val isInProgress: Boolean = false) : SettingsScreenState()
    data class LoggedIn(val user: LoggedUser) : SettingsScreenState()
}
