package com.sloy.sevibus.infrastructure.session

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider

class GoogleAuthService {

    suspend fun autoSignIn(context: Context): Result<AuthCredential> {
        // Attempt automatic sign in with already authorized user
        return attemptSignIn(signInCredentialRequest(), context)

    }

    suspend fun manualSignUp(context: Context): Result<AuthCredential> {
        return attemptSignIn(signUpCredentialRequest(), context)
    }

    suspend fun googleSignIn(context: Context): Result<AuthCredential> {
        return attemptSignIn(signInWithButtonCredentialRequest(), context)
    }

    private suspend fun attemptSignIn(request: GetCredentialRequest, context: Context): Result<AuthCredential> {
        return runCatching {
            val credentialManager = CredentialManager.create(context)
            val result: GetCredentialResponse = credentialManager.getCredential(context, request)
            val credential: Credential = result.credential
            check(credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            return@runCatching GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
        }
    }

    private fun signInCredentialRequest(): GetCredentialRequest {
        val googleIdOption =
            GetGoogleIdOption.Builder()
                .setServerClientId(clientId)
                .setFilterByAuthorizedAccounts(true)
                .setAutoSelectEnabled(true)
                .build()
        return GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()
    }

    private fun signUpCredentialRequest(): GetCredentialRequest {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(clientId)
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .build()
        return GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()
    }

    private fun signInWithButtonCredentialRequest(): GetCredentialRequest {
        val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(clientId).build()
        return GetCredentialRequest.Builder().addCredentialOption(signInWithGoogleOption).build()
    }

    suspend fun signOut(context: Context) {
        val credentialManager = CredentialManager.create(context)
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }


    companion object {
        private const val clientId = "255022367225-g00bjo9aoo9tke4siolik1ikck2unefj.apps.googleusercontent.com"
    }
}

