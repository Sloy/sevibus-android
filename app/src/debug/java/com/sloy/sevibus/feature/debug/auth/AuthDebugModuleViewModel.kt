package com.sloy.sevibus.feature.debug.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sloy.sevibus.infrastructure.SevLogger
import com.sloy.sevibus.infrastructure.session.FirebaseAuthService
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthDebugModuleViewModel(
    private val dataSource: AuthDebugModuleDataSource,
    private val firebaseAuthService: FirebaseAuthService,
) : ViewModel() {

    val state: StateFlow<AuthDebugModuleState> = dataSource.observeCurrentState()

    fun onFirebaseLogoutClick() {
        SevLogger.logD("AuthDebugModule: Firebase logout clicked")
        viewModelScope.launch {
            try {
                firebaseAuthService.signOut()
                SevLogger.logD("AuthDebugModule: Firebase logout completed")
            } catch (e: Exception) {
                SevLogger.logE(e, "AuthDebugModule: Firebase logout failed")
            }
        }
    }
}