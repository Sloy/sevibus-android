package com.sloy.sevibus.feature.debug.http

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.EmptyCoroutineContext

interface HttpOverlayState {
    val isVisible: Boolean
    fun setVisibility(visible: Boolean)
    fun put(item: HttpOverlayItem)
    val items: MutableStateFlow<List<HttpOverlayItem>>
}
