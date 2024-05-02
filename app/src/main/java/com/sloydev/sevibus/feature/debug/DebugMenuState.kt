package com.sloydev.sevibus.feature.debug

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun rememberDebugMenuState(): DebugMenuState {
    return remember { DebugMenuState }
}


object DebugMenuState {
    var isOpen by mutableStateOf(false)


    fun openMenu() {
        isOpen = true
    }

    fun closeMenu() {
        isOpen = false
    }
}