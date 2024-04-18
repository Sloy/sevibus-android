package com.sloydev.sevibus

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sloydev.sevibus.feature.lines.LinesRoute
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
                    startDestination = FOR_YOU.route
                ) {
                    composable(FOR_YOU.route) { Text("home") }
                    composable(LINES.route) { LinesRoute() }
                    composable(MAP.route) { Text("mapa") }
                    composable(CARDS.route) { Text("bonobus") }
                }
            }
        }
    }

}


