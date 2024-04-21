package com.sloydev.sevibus

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import com.sloydev.sevibus.feature.cards.travelCardsRoute
import com.sloydev.sevibus.feature.foryou.forYouRoute
import com.sloydev.sevibus.feature.lines.linesRoute
import com.sloydev.sevibus.feature.linestops.lineStopsRoute
import com.sloydev.sevibus.feature.map.mapRoute
import com.sloydev.sevibus.feature.stopdetail.stopDetailRoute
import com.sloydev.sevibus.navigation.TopLevelDestination.FOR_YOU
import com.sloydev.sevibus.navigation.rememberSevAppState
import com.sloydev.sevibus.ui.components.SevNavigationBar
import com.sloydev.sevibus.ui.theme.SevTheme

@Composable
fun App() {
    val appState = rememberSevAppState()

    SevTheme {
        Scaffold(bottomBar = {
            SevNavigationBar(
                destinations = appState.topLevelDestinations,
                onNavigateToDestination = appState::navigateToTopLevelDestination,
                currentDestination = appState.currentDestination,
            )
        }) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .consumeWindowInsets(padding)
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(
                            WindowInsetsSides.Horizontal,
                        ),
                    )
            ) {
                NavHost(
                    navController = appState.navController,
                    startDestination = FOR_YOU.route,
                    modifier = Modifier.fillMaxSize(),
                    enterTransition = { fadeIn(animationSpec = tween(100)) },
                    exitTransition = { fadeOut(animationSpec = tween(100)) },
                ) {
                    forYouRoute(appState.navController)
                    linesRoute(appState.navController)
                    mapRoute()
                    travelCardsRoute()

                    lineStopsRoute(appState.navController)
                    stopDetailRoute()
                }
            }
        }
    }

}


