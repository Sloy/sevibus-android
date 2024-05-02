package com.sloydev.sevibus

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import com.sloydev.sevibus.feature.cards.travelCardsRoute
import com.sloydev.sevibus.feature.debug.DebugMenu
import com.sloydev.sevibus.feature.debug.rememberDebugMenuState
import com.sloydev.sevibus.feature.foryou.forYouRoute
import com.sloydev.sevibus.feature.lines.linesRoute
import com.sloydev.sevibus.feature.linestops.lineStopsRoute
import com.sloydev.sevibus.feature.map.mapRoute
import com.sloydev.sevibus.feature.stopdetail.stopDetailRoute
import com.sloydev.sevibus.infrastructure.BuildVariantDI
import com.sloydev.sevibus.infrastructure.DI
import com.sloydev.sevibus.infrastructure.SevLogger
import com.sloydev.sevibus.navigation.TopLevelDestination.FOR_YOU
import com.sloydev.sevibus.navigation.rememberSevAppState
import com.sloydev.sevibus.ui.components.SevNavigationBar
import com.sloydev.sevibus.ui.theme.SevTheme
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

@Composable
fun App() {
    val context = LocalContext.current
    KoinApplication(application = {
        androidContext(context)
        modules(DI.viewModelModule, DI.dataModule, DI.infrastructureModule, BuildVariantDI.module)
    }) {
        val appState = rememberSevAppState()
        val debugMenuState = rememberDebugMenuState()

        SevTheme {
            Scaffold(bottomBar = {
                AnimatedVisibility(
                    visible = appState.navigationBarVisible,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it }),
                ) {
                    SevNavigationBar(
                        destinations = appState.topLevelDestinations,
                        onNavigateToDestination = appState::navigateToTopLevelDestination,
                        currentDestination = appState.currentDestination,
                    )
                }
            }) { padding ->
                DebugMenu(debugMenuState, koinInject(), Modifier.padding(padding))
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
                        mapRoute(setNavigationBarVisibility = { visible ->
                            SevLogger.logD("setNavigationBarVisibility: $visible")
                            appState.navigationBarVisible = visible
                        })
                        travelCardsRoute()

                        lineStopsRoute(appState.navController)
                        stopDetailRoute()
                    }
                }
            }
        }
    }

}


