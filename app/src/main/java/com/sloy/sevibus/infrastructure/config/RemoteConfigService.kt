package com.sloy.sevibus.infrastructure.config

import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.sloy.sevibus.infrastructure.SevLogger

class RemoteConfigService(
    private val apiConfigurationManager: ApiConfigurationManager
) {

    companion object {
        private const val API_URL_KEY = "api_url"
        private const val FETCH_TIMEOUT_SECONDS = 60L
        private const val MIN_FETCH_INTERVAL_SECONDS = 3600L // 1 hour
    }

    private val remoteConfig = FirebaseRemoteConfig.getInstance()

    fun initialize() {
        setupRemoteConfig()
        setupDefaultValues()
        setupConfigUpdateListener()
        fetchAndActivate()
    }

    private fun setupRemoteConfig() {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = MIN_FETCH_INTERVAL_SECONDS
            fetchTimeoutInSeconds = FETCH_TIMEOUT_SECONDS
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
    }

    private fun setupDefaultValues() {
        val defaults = mapOf(API_URL_KEY to apiConfigurationManager.getCurrentApiUrl())
        remoteConfig.setDefaultsAsync(defaults)
    }

    private fun setupConfigUpdateListener() {
        remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
            override fun onUpdate(configUpdate: ConfigUpdate) {
                SevLogger.logD("Config update received: ${configUpdate.updatedKeys}")

                remoteConfig.activate().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val newApiUrl = remoteConfig.getString(API_URL_KEY)
                        SevLogger.logD("Updating API URL to: $newApiUrl")
                        apiConfigurationManager.updateApiUrl(newApiUrl)
                    } else {
                        task.exception?.let {
                            SevLogger.logE(it, "Failed to activate config update")
                        }
                    }
                }
            }

            override fun onError(error: FirebaseRemoteConfigException) {
                SevLogger.logE(error, "Config update listener failed")
            }
        })
    }

    private fun fetchAndActivate() {
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val updated = task.result
                SevLogger.logD("Config fetch successful. Updated: $updated")

                val newApiUrl = remoteConfig.getString(API_URL_KEY)
                apiConfigurationManager.updateApiUrl(newApiUrl)
            } else {
                task.exception?.let {
                    SevLogger.logE(it, "Config fetch failed")
                }
            }
        }
    }

}