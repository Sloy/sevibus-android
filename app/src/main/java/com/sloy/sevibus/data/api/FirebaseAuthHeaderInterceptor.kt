package com.sloy.sevibus.data.api

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.perf.ktx.performance
import com.google.firebase.perf.trace
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import okhttp3.Interceptor
import okhttp3.Response
import okio.IOException

class FirebaseAuthHeaderInterceptor() : Interceptor {

    private val firebaseAuth = Firebase.auth
    private val perf = Firebase.performance

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()

        runBlocking {
            perf.newTrace("getIdToken").trace {
                start()
                val currentUser = firebaseAuth.currentUser
                val token = runCatching { currentUser?.getIdToken(true)?.await()?.token }
                    .onSuccess {
                        incrementMetric("success", 1)
                    }
                    .onFailure {
                        incrementMetric("failure", 1)
                    }
                    .getOrElse {
                        throw IOException("User not logged in")
                    }
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }
        }

        val request = requestBuilder.build()
        return chain.proceed(request)
    }
}
