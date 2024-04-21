package com.sloydev.sevibus.infrastructure

import android.util.Log

object SevLogger {
    fun logW(t: Throwable){
        Log.w("SEVIBUS", "Error", t)
    }
}