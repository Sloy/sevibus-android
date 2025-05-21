package com.sloy.sevibus.infrastructure.session

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthService {

    private val firebaseAuth = Firebase.auth

    fun currentUser(): FirebaseUser? = firebaseAuth.currentUser

    fun observeCurrentUser(): Flow<FirebaseUser?> = callbackFlow {
        trySend(firebaseAuth.currentUser)
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        firebaseAuth.addAuthStateListener(authStateListener)
        awaitClose {
            firebaseAuth.removeAuthStateListener(authStateListener)
        }
    }

    suspend fun signInWithCredential(credential: AuthCredential): AuthResult {
        return firebaseAuth.signInWithCredential(credential).await()
    }

    fun signOut() {
        firebaseAuth.signOut()
    }

}