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
import androidx.navigation.compose.composable
import com.sloydev.sevibus.feature.cards.CardsRoute
import com.sloydev.sevibus.feature.foryou.ForYouRoute
import com.sloydev.sevibus.feature.lines.LinesRoute
import com.sloydev.sevibus.feature.map.MapRoute
import com.sloydev.sevibus.feature.stops.StopsRoute
import com.sloydev.sevibus.navigation.TopLevelDestination.CARDS
import com.sloydev.sevibus.navigation.TopLevelDestination.FOR_YOU
import com.sloydev.sevibus.navigation.TopLevelDestination.LINES
import com.sloydev.sevibus.navigation.TopLevelDestination.MAP
import com.sloydev.sevibus.navigation.rememberSevAppState
import com.sloydev.sevibus.ui.SevNavigationBar
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
                    composable(FOR_YOU.route) { ForYouRoute() }
                    composable(LINES.route) { LinesRoute(onLineClick = { appState.navController.navigate(LINES.route + "/stops") }) }
                    composable(MAP.route) { MapRoute() }
                    composable(CARDS.route) { CardsRoute() }

                    composable(LINES.route + "/stops") { StopsRoute() }
                }
            }
        }
    }

}


