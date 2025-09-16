package com.sloy.debugmenu.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlin.collections.plus

class DebugMenuViewModel constructor() : ViewModel() {

    private val menuState = MutableStateFlow(DebugModuleState())

    fun isExpanded(module: String): StateFlow<Boolean> {
        return menuState.map { state -> state.itemsExpanded.getOrDefault(module, false) }
            .stateIn(viewModelScope, SharingStarted.Lazily, initialValue = false)
    }

    fun onExpandedChanged(module: String, expanded: Boolean) {
        val updatedItems = menuState.value.itemsExpanded + (module to expanded)
        menuState.update { it.copy(itemsExpanded = updatedItems) }
    }
}

@Serializable
data class DebugModuleState(val itemsExpanded: Map<String, Boolean> = emptyMap())
