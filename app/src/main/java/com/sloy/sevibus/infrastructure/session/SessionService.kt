package com.sloy.sevibus.infrastructure.session

import android.content.Context
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import com.sloy.sevibus.data.api.SevibusUserApi
import com.sloy.sevibus.domain.model.LoggedUser
import com.sloy.sevibus.domain.model.toDto
import com.sloy.sevibus.infrastructure.SevLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Based on these docs: https://developer.android.com/identity/sign-in/credential-manager-siwg
 *
 * Better explanation of the different options in https://stackoverflow.com/a/77643555/835787
 */
class SessionService(
    private val firebase: FirebaseAuthService,
    private val google: GoogleAuthService,
    private val api: SevibusUserApi,
) {

    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun getCurrentUser(): LoggedUser? = firebase.currentUser()?.toLoggedUser()

    fun isLogged(): Boolean = getCurrentUser() != null

    fun observeCurrentUser(): Flow<LoggedUser?> =
        firebase.observeCurrentUser()
            .map { firebaseUser: FirebaseUser? ->
                firebaseUser?.toLoggedUser()
            }

    suspend fun autoSignIn(context: Context): Result<Unit> {
        if (firebase.currentUser() != null) return Result.success(Unit)
        return google.autoSignIn(context)
            .mapCatching { googleAuthCredential: AuthCredential ->
                firebase.signInWithCredential(googleAuthCredential)
            }.map { sendLoginAsync() }
    }

    suspend fun manualSignIn(context: Context): Result<Unit> {
        return google.manualSignUp(context)
            .mapCatching { googleAuthCredential: AuthCredential ->
                firebase.signInWithCredential(googleAuthCredential)
            }.map { sendLoginAsync() }
    }

    suspend fun googleSignIn(context: Context): Result<Unit> {
        return google.googleSignIn(context)
            .mapCatching { googleAuthCredential: AuthCredential ->
                firebase.signInWithCredential(googleAuthCredential)
            }.map { sendLoginAsync() }
    }

    suspend fun signOut(context: Context) {
        firebase.signOut()
        google.signOut(context)
    }

    private fun sendLoginAsync() {
        getCurrentUser()?.let { user ->
            backgroundScope.launch {
                runCatching {
                    api.login(user.toDto())
                }.onFailure {
                    SevLogger.logE(it, "Error sending login to server")
                }
            }
        }
    }

    private fun FirebaseUser.toLoggedUser() = LoggedUser(
        id = this.uid,
        displayName = this.displayName!!,
        email = this.email!!,
        photoUrl = this.photoUrl?.toString(),
    )


}
