package com.sloy.debugmenu.overlay

import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
 * Default implementation of [OverlayLoggerStateHolder].
 * It holds the current list of [OverlayItem] to be displayed in the overlay logger.
 * It also holds the visibility state, which is determined by combining multiple criteria
 * to allow different types of overlays.
 *
 * New items can be added via the [put] method. If the item has [OverlayItem.autoHide] set to true,
 * it will be automatically removed after a predefined delay.
 *
 * The visibility of the overlay can be controlled by adding boolean flows via the [visibleWhen] method.
 * The overlay will be visible if any of the added flows emit true.
 */
class OverlayLoggerStateHolderImpl constructor() : OverlayLoggerStateHolder {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)

    override val items = MutableStateFlow<List<OverlayItem>>(emptyList())

    private val visibilityCriteria: MutableStateFlow<List<Flow<Boolean>>> = MutableStateFlow(emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    override val isVisible: StateFlow<Boolean> = visibilityCriteria
        .flatMapLatest { flows ->
            combine(flows) { values ->
                val result = values.any { it }
                result
            }
        }
        .stateIn(scope, SharingStarted.WhileSubscribed(), false)

    override fun visibleWhen(flow: Flow<Boolean>) {
        visibilityCriteria.update { it + flow }
    }

    override fun put(item: OverlayItem) {
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

    @VisibleForTesting
    internal fun reset() {
        visibilityCriteria.value = emptyList()
        items.value = emptyList()
    }
}

private const val AUTO_HIDE_DELAY_MILLIS: Long = 9_000
