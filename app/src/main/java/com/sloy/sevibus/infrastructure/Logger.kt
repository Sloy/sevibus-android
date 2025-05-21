package com.sloy.sevibus.infrastructure

import android.util.Log

interface Logger {
    fun logW(t: Throwable? = null, msg: String? = null)
    fun logE(t: Throwable, msg: String? = null)
    fun logD(msg: String)

    @Deprecated("Don't commit this", ReplaceWith("", ""), DeprecationLevel.WARNING)
    fun logPotato(msg: String)
}

object SevLogger : Logger {

    private var logger: Logger? = null

    internal fun setLogger(logger: Logger) {
        this.logger = logger
    }

    override fun logW(t: Throwable?, msg: String?) {
        logger?.logW(t, msg)
    }

    override fun logE(t: Throwable, msg: String?) {
        logger?.logE(t, msg)
    }

    override fun logD(msg: String) {
        logger?.logD(msg)
    }

    @Deprecated("Don't commit this", replaceWith = ReplaceWith("", ""), level = DeprecationLevel.WARNING)
    override fun logPotato(msg: String) {
        logger?.logPotato(msg)
    }


}

class AndroidLogger : Logger {
    override fun logW(t: Throwable?, msg: String?) {
        Log.w("SEVIBUS", "Error: ${msg ?: t?.message}${t?.let { " (${it.javaClass.simpleName})" } ?: ""}", t)
    }

    override fun logE(t: Throwable, msg: String?) {
        Log.e("SEVIBUS", "Error: ${msg ?: t.message} (${t.javaClass.simpleName})", t)
    }

    override fun logD(msg: String) {
        Log.d("SEVIBUS", msg)
    }

    @Deprecated("Don't commit this", ReplaceWith("", ""), DeprecationLevel.WARNING)
    override fun logPotato(msg: String) {
        Log.d("SEVIBUS POTATO", msg)
    }
}

class TestLogger : Logger {
    override fun logW(t: Throwable?, msg: String?) {
        println("Warning: ${msg ?: t?.message}")
    }

    override fun logE(t: Throwable, msg: String?) {
        println("Error: ${msg ?: t.message}")
    }

    override fun logD(msg: String) {
        println("Debug: $msg")
    }

    override fun logPotato(msg: String) {
        TODO("Not yet implemented")
    }
}
