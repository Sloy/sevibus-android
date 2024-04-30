package com.sloydev.sevibus.infrastructure

import android.util.Log

object SevLogger {
    fun logW(t: Throwable) {
        Log.w("SEVIBUS", "Error: ${t.message}", t)
    }

    fun logE(t: Throwable, msg: String? = null) {
        Log.e("SEVIBUS", "Error: ${msg ?: t.message}", t)
    }

    fun logD(msg: String) {
        Log.d("SEVIBUS", msg)
    }

    @Deprecated("Don't commit this", ReplaceWith("", ""), DeprecationLevel.WARNING)
    fun logPotato(msg: String) {
        Log.d("SEVIBUS POTATO", msg)
    }
}