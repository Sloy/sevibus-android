package com.sloy.sevibus.feature.debug.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sloy.sevibus.infrastructure.SevLogger
import com.sloy.sevibus.infrastructure.session.FirebaseAuthService
import kotlinx.coroutines.launch

class AuthDebugModuleViewModel(
    private val firebaseAuthService: FirebaseAuthService,
) : ViewModel() {

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