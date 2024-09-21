package com.sloy.sevibus

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import com.sloy.sevibus.feature.cards.travelCardsRoute
import com.sloy.sevibus.feature.foryou.forYouRoute
import com.sloy.sevibus.feature.lines.linesRoute
import com.sloy.sevibus.feature.linestops.lineStopsRoute
import com.sloy.sevibus.feature.map.MapScreen
import com.sloy.sevibus.feature.map.MapViewModel
import com.sloy.sevibus.feature.stopdetail.stopDetailRoute
import com.sloy.sevibus.infrastructure.BuildVariantDI
import com.sloy.sevibus.infrastructure.DI
import com.sloy.sevibus.navigation.NavigationDestination
import com.sloy.sevibus.navigation.rememberSevAppState
import com.sloy.sevibus.ui.components.SevNavigationBar
import com.sloy.sevibus.ui.theme.SevTheme
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.KoinApplication

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val context = LocalContext.current
    KoinApplication(application = {
        androidContext(context)
        modules(DI.viewModelModule, DI.dataModule, DI.infrastructureModule, BuildVariantDI.module)
    }) {
        val appState = rememberSevAppState()

        SevTheme {
            Scaffold(bottomBar = {
                SevNavigationBar(
                    topLevelDestinations = appState.topLevelDestinations,
                    onNavigateToDestination = appState::navigateToTopLevelDestination,
                    currentNavDestination = appState.currentDestination,
                )
            }) { padding ->
                val currentDestination: NavigationDestination? = appState.currentNavigationDestination
                val screenHeight = LocalConfiguration.current.screenHeightDp.dp

                val sheetState = rememberStandardBottomSheetState(
                    initialValue = SheetValue.PartiallyExpanded,
                    skipHiddenState = true
                )
                BottomSheetScaffold(
                    scaffoldState = rememberBottomSheetScaffoldState(sheetState),
                    sheetPeekHeight = screenHeight / 2,
                    sheetContent = {
                        NavHost(
                            navController = appState.navController,
                            startDestination = NavigationDestination.ForYou,
                            modifier = Modifier.fillMaxSize(),
                            enterTransition = { fadeIn(animationSpec = tween(100)) },
                            exitTransition = { fadeOut(animationSpec = tween(100)) },
                        ) {
                            forYouRoute(appState.navController)
                            linesRoute(appState.navController)
                            travelCardsRoute()
                            lineStopsRoute(appState.navController)
                            stopDetailRoute()
                        }
                    },
                    modifier = Modifier.padding(padding)
                ) { contentPadding ->
                    val viewModel: MapViewModel = koinViewModel()
                    LaunchedEffect(currentDestination) {
                        if (currentDestination != null) {
                            viewModel.setDestination(currentDestination)
                        }
                    }
                    val mapState by viewModel.state.collectAsStateWithLifecycle()
                    MapScreen(
                        mapState,
                        { },
                        onStopSelected = { appState.navigateToTopLevelDestination(NavigationDestination.StopDetail(it.code)) },
                        onLineSelected = viewModel::onLineSelected,
                        onRouteSelected = viewModel::onRouteSelected,
                        onDismiss = viewModel::onDismiss,
                    )
                }
            }
        }
    }

}


