package com.sloy.sevibus.navigation

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import androidx.navigation.toRoute

@Composable
fun rememberSevAppState(
    navController: NavHostController = rememberNavController()
): SevAppState {
    return remember(navController) { SevAppState(navController) }
}

@Stable
class SevAppState(
    val navController: NavHostController,
) {


    val currentDestination: NavDestination?
        @Composable get() {
            val value = navController.currentBackStackEntryAsState().value
            return value?.destination
        }
    val currentNavigationDestination: NavigationDestination?
        @Composable get() {
            val value = navController.currentBackStackEntryAsState().value
            val route = value?.toNavigationDestination()
            return route
        }


    @SuppressLint("RestrictedApi")
    inline fun NavBackStackEntry.toNavigationDestination(): NavigationDestination? {

        return if (this.destination.hasRoute(NavigationDestination.ForYou::class)) {
            this.toRoute<NavigationDestination.ForYou>()
        } else if (this.destination.hasRoute(NavigationDestination.Lines::class)) {
            this.toRoute<NavigationDestination.Lines>()
        } else if (this.destination.hasRoute(NavigationDestination.Cards::class)) {
            this.toRoute<NavigationDestination.Cards>()
        } else {
            null
        }

    }

    /**
     * Map of top level destinations to be used in the TopBar, BottomBar and NavRail. The key is the
     * route.
     */
    val topLevelDestinations: List<TopLevelDestination> = listOf(
        NavigationDestination.ForYou,
        NavigationDestination.Lines,
        //NavigationDestination.Map,
        NavigationDestination.Cards,
    )


    /**
     * UI logic for navigating to a top level destination in the app. Top level destinations have
     * only one copy of the destination of the back stack, and save and restore state whenever you
     * navigate to and from it.
     *
     * @param destination: The destination the app needs to navigate to.
     */
    fun navigateToTopLevelDestination(destination: NavigationDestination) {
        val topLevelNavOptions = navOptions {
            // Pop up to the start destination of the graph to
            // avoid building up a large stack of destinations
            // on the back stack as users select items
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            // Avoid multiple copies of the same destination when
            // reselecting the same item
            launchSingleTop = true
            // Restore state when reselecting a previously selected item
            restoreState = false
        }

        navController.navigate(destination, topLevelNavOptions)
    }
}