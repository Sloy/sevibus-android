package com.sloy.sevibus.infrastructure.config

import com.sloy.sevibus.infrastructure.BuildVariant
import kotlinx.coroutines.flow.MutableStateFlow

class ApiConfigurationManager {

    companion object {
        private const val DEFAULT_DEBUG_URL = "https://appdev-vd4mgiw7ma-no.a.run.app"
        private const val DEFAULT_RELEASE_URL = "https://app-vd4mgiw7ma-no.a.run.app"
    }

    private val apiUrl = MutableStateFlow(getDefaultApiUrl())

    fun getCurrentApiUrl(): String = apiUrl.value

    fun updateApiUrl(newUrl: String) {
        if (newUrl.isNotBlank() && newUrl != apiUrl.value) {
            apiUrl.value = ensureTrailingSlash(newUrl)
        }
    }

    private fun getDefaultApiUrl(): String {
        return when (BuildVariant.current()) {
            BuildVariant.DEBUG -> DEFAULT_DEBUG_URL
            BuildVariant.RELEASE -> DEFAULT_RELEASE_URL
        }
    }

    private fun ensureTrailingSlash(url: String): String {
        return if (url.endsWith("/")) url else "$url/"
    }
}