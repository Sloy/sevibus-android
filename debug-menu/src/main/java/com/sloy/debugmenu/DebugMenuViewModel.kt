package com.sloy.debugmenu

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class DebugMenuViewModel constructor(val dataSource: DebugMenuDataSource) : ViewModel() {

    private val menuState: StateFlow<DebugModuleState> = dataSource.observeCurrentState()

    fun isExpanded(module: String): StateFlow<Boolean> {
        return menuState.map { state -> state.itemsExpanded.getOrDefault(module, false) }
            .stateIn(viewModelScope, SharingStarted.Lazily, initialValue = false)
    }

    fun onExpandedChanged(module: String, expanded: Boolean) {
        val updatedItems = menuState.value.itemsExpanded + (module to expanded)
        dataSource.updateState(menuState.value.copy(itemsExpanded = updatedItems))
    }
}

@Serializable
data class DebugModuleState(val itemsExpanded: Map<String, Boolean> = emptyMap())

class DebugMenuDataSource constructor(context: Context) : DebugModuleDataSource<DebugModuleState>(context) {
    override val defaultValue
        get() = DebugModuleState()

    override fun Json.decode(jsonString: String): DebugModuleState = decodeFromString(jsonString)
    override fun Json.encode(value: DebugModuleState): String = encodeToString(value)
}
