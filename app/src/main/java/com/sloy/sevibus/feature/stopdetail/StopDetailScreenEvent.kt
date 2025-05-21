package com.sloy.sevibus.feature.stopdetail

import android.content.Context

sealed interface StopDetailScreenEvent {
    data class LoginRequired(val loginAction: suspend (Context) -> Result<Unit>) : StopDetailScreenEvent
}
