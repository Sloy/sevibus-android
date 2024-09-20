package com.sloy.sevibus.feature.debug.http

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.EmptyCoroutineContext

object HttpOverlayState {
    private val scope = CoroutineScope(EmptyCoroutineContext)
    val items = MutableStateFlow<List<HttpOverlayItem>>(emptyList())
    val isVisible by mutableStateOf(true)

    fun put(item: HttpOverlayItem) {
        scope.launch {
            items.update { current ->
                if (current.any { it.id == item.id }) {
                    current.map { if (it.id == item.id) item else it }
                } else {
                    current + item
                }
            }

            if (item.status != null) {
                delay(9_000)
                items.update { it - item }
            }
        }
    }
}
