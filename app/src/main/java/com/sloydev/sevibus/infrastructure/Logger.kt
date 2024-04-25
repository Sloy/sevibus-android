package com.sloydev.sevibus.infrastructure

import android.util.Log

object SevLogger {
    fun logW(t: Throwable) {
        Log.e("SEVIBUS", "Error: ${t.message}", t)
    }

    @Deprecated("Don't commit this", ReplaceWith("", ""), DeprecationLevel.ERROR)
    fun logPotato(msg: String) {
        Log.w("SEVIBUS POTATO", msg)
    }
}