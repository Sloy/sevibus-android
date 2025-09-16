package com.sloy.debugmenu.base

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json

/**
 * Utility base class to manage and persist the state of a [DebugModule] options.
 *
 * For each module, create a subclass of this class and implement the abstract functions and values.
 * They are needed to overcome limitations of Kotlin serialization with generics.
 *
 * This class keeps a memory cache of the state. You can subscribe to the current state using [observeCurrentState],
 * and update it with [updateState]. The state is also persisted in shared preferences.
 */
abstract class DebugModuleDataSource<T : Any>(context: Context) {

    abstract val defaultValue: T

    abstract fun Json.decode(jsonString: String): T
    abstract fun Json.encode(value: T): String

    private val prefs = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
    private val prefKey: String
        get() = PREFERENCES_KEY_PREFIX + defaultValue::class.simpleName
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val memoryState = MutableStateFlow<T>(readStoredState())

    fun observeCurrentState(): StateFlow<T> {
        return memoryState
    }

    fun getCurrentState(): T {
        return memoryState.value
    }

    fun updateState(newState: T) {
        memoryState.value = newState
        val jsonString = json.encode(newState)
        prefs.edit(commit = true) {
            putString(prefKey, jsonString)
        }
    }

    private fun readStoredState(): T {
        return prefs.getString(prefKey, null)
            ?.let { jsonString ->
                json.decode(jsonString)
            } ?: defaultValue
    }
}

private const val PREFERENCES_FILE = "debug_menu"
private const val PREFERENCES_KEY_PREFIX = "debug_module_state_"
