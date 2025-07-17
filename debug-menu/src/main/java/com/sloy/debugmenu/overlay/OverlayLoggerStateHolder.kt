package com.sloy.sevibus.feature.debug.network.overlay

import com.sloy.sevibus.feature.debug.network.overlay.OverlayLoggerStateHolder.put
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


/**
 * Holds the current state of the overlay logger, including the items currently displayed on screen and settings like the overlay visibility.
 * It also handles auto-hiding of items after a few seconds.
 *
 * New items can be added to the overlay using the [put] function, which will update the state and manage the auto-hide functionality.
 */
object OverlayLoggerStateHolder {
    private val scope = CoroutineScope(SupervisorJob())
    val items = MutableStateFlow<List<OverlayItem>>(emptyList())

    private val visibilityCriteria: MutableStateFlow<List<Flow<Boolean>>> = MutableStateFlow(emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val isVisible: StateFlow<Boolean> = visibilityCriteria
        .flatMapLatest { flows -> combine(flows) { it.any() } }
        .stateIn(scope, SharingStarted.Lazily, false)

    fun visibleWhen(flow: Flow<Boolean>) {
        visibilityCriteria.update { it + flow }
    }

    fun put(item: OverlayItem) {
        scope.launch {
            items.update { current ->
                if (current.any { it.id == item.id }) {
                    current.map { if (it.id == item.id) item else it }
                } else {
                    current + item
                }
            }

            if (item.autoHide) {
                delay(AUTO_HIDE_DELAY_MILLIS)
                items.update { it - item }
            }
        }
    }
}

private const val AUTO_HIDE_DELAY_MILLIS: Long = 9_000
