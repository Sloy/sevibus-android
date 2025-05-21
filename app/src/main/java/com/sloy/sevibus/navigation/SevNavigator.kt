package com.sloy.sevibus.navigation

import androidx.annotation.VisibleForTesting
import com.sloy.sevibus.infrastructure.SevLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class SevNavigator {

    // Does NOT contain the current one
    private val destinationStack: MutableList<NavigationDestination> = mutableListOf()

    val destination: MutableStateFlow<NavigationDestination> = MutableStateFlow(INITIAL_DESTINATION)
    val isLastDestination = MutableStateFlow(true)

    fun observeDestination(): Flow<NavigationDestination> {
        return destination
    }

    fun navigate(newDestination: NavigationDestination) {
        SevLogger.logD("Navigating to $newDestination")

        if (destinationStack.isEmpty()) {
            destinationStack.add(current())
            destination.value = newDestination
        } else {
            if (newDestination.isSameClassAs(current())) {
                destination.value = newDestination
            } else {
                if (current() is NavigationDestination.Search) {
                    destination.value = newDestination
                    // Si la pantalla anterior a búsqueda es la misma que la nueva, la quito de la pila
                    if (destinationStack.last().isSameClassAs(newDestination)) {
                        destinationStack.removeAt(destinationStack.size - 1)
                    }
                } else {
                    destinationStack.add(current())
                    destination.value = newDestination
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
        destination.value = destinationStack.removeAt(destinationStack.size - 1)
        isLastDestination.value = destinationStack.isEmpty()
        return true
    }

    fun popToRoot() {
        destinationStack.clear()
        destination.value = INITIAL_DESTINATION
    }

    fun peekPrevious(): NavigationDestination? {
        if (destinationStack.isEmpty()) return null
        return destinationStack.last()
    }

    @VisibleForTesting
    fun current(): NavigationDestination {
        return destination.value
    }

    private fun NavigationDestination.isSameClassAs(navigationDestination: NavigationDestination) =
        this::class == navigationDestination::class
}

private val INITIAL_DESTINATION = NavigationDestination.ForYou
