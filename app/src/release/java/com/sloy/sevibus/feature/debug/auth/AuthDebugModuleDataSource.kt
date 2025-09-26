package com.sloy.sevibus.feature.debug.auth

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthDebugModuleDataSource(context: Context) {
    fun observeCurrentState(): StateFlow<AuthDebugModuleState> = MutableStateFlow(AuthDebugModuleState())
}

data class AuthDebugModuleState(
    val placeholder: Boolean = false,
)