package com.sloy.sevibus.infrastructure.analytics.events

import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.firebase.FirebaseNetworkException
import com.sloy.sevibus.infrastructure.analytics.Analytics
import com.sloy.sevibus.infrastructure.analytics.events.Events.LoginFailed.Reason
import java.io.IOException

suspend fun Analytics.trackLogin(trigger: Events.LoginTrigger, signIn: suspend () -> Result<Unit>): Result<Unit> {
    track(Events.LoginStarted(trigger))
    return signIn()
        .onSuccess { track(Events.LoginCompleted(trigger)) }
        .onFailure { track(Events.LoginFailed(trigger, it.toLoginFailureReason())) }
}

private fun Throwable.toLoginFailureReason(): Reason = when (this) {
    is GetCredentialCancellationException -> Reason.CANCELLED
    is NoCredentialException -> Reason.NO_CREDENTIALS
    is FirebaseNetworkException, is IOException -> Reason.NETWORK
    else -> Reason.UNKNOWN
}
