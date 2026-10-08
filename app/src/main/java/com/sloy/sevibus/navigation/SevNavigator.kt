package com.sloy.sevibus.navigation

import androidx.annotation.VisibleForTesting
import com.sloy.sevibus.infrastructure.SevLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class SevNavigator {

    // Does NOT contain the current one
    private val destinationStack: MutableList<NavigationDestination> = mutableListOf()

    val destination: MutableStateFlow<NavigationDestination> = MutableStateFlow(INITIAL_DESTINATION)
    val isLastDestination = MutableStateFlow(true)

    private val _transitions = MutableSharedFlow<NavigationTransition>(replay = 1, extraBufferCapacity = 64).apply {
        tryEmit(NavigationTransition(INITIAL_DESTINATION, NavigationTrigger.LAUNCH, previous = null))
    }
    val transitions: SharedFlow<NavigationTransition> = _transitions.asSharedFlow()

    fun observeDestination(): Flow<NavigationDestination> {
        return destination
    }

    fun navigate(newDestination: NavigationDestination) {
        SevLogger.logD("Navigating to $newDestination")

        if (destinationStack.isEmpty()) {
            destinationStack.add(current())
            show(newDestination, NavigationTrigger.NAVIGATION)
        } else {
            if (newDestination.isSameClassAs(current())) {
                show(newDestination, NavigationTrigger.NAVIGATION)
            } else {
                if (current() is NavigationDestination.Search) {
                    show(newDestination, NavigationTrigger.NAVIGATION)
                    // Si la pantalla anterior a búsqueda es la misma que la nueva, la quito de la pila
                    if (destinationStack.last().isSameClassAs(newDestination)) {
                        destinationStack.removeAt(destinationStack.size - 1)
                    }
                } else {
                    destinationStack.add(current())
                    show(newDestination, NavigationTrigger.NAVIGATION)
                }
            }
        }
        isLastDestination.value = destinationStack.isEmpty() && newDestination !is NavigationDestination.Search
    }

    fun navigateBack(): Boolean {
        if (destinationStack.isEmpty()) {
            SevLogger.logW(msg = "No destinations left in the back stack")
            return false
        }
        show(destinationStack.removeAt(destinationStack.size - 1), NavigationTrigger.BACK)
        isLastDestination.value = destinationStack.isEmpty()
        return true
    }

    fun popToRoot() {
        destinationStack.clear()
        show(INITIAL_DESTINATION, NavigationTrigger.BACK)
    }

    fun peekPrevious(): NavigationDestination? {
        if (destinationStack.isEmpty()) return null
        return destinationStack.last()
    }

    @VisibleForTesting
    fun current(): NavigationDestination {
        return destination.value
    }

    private fun show(newDestination: NavigationDestination, trigger: NavigationTrigger) {
        val previous = current()
        if (newDestination == previous) return
        _transitions.tryEmit(NavigationTransition(newDestination, trigger, previous))
        destination.value = newDestination
    }

    private fun NavigationDestination.isSameClassAs(navigationDestination: NavigationDestination) =
        this::class == navigationDestination::class
}

data class NavigationTransition(
    val destination: NavigationDestination,
    val trigger: NavigationTrigger,
    val previous: NavigationDestination?,
)

enum class NavigationTrigger {
    LAUNCH, NAVIGATION, BACK
}

private val INITIAL_DESTINATION = NavigationDestination.ForYou
