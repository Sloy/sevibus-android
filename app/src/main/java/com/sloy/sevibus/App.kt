package com.sloy.sevibus

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import com.sloy.sevibus.feature.cards.travelCardsRoute
import com.sloy.sevibus.feature.foryou.forYouRoute
import com.sloy.sevibus.feature.lines.linesRoute
import com.sloy.sevibus.feature.linestops.lineStopsRoute
import com.sloy.sevibus.feature.map.MapContent
import com.sloy.sevibus.feature.map.MapViewModel
import com.sloy.sevibus.feature.stopdetail.stopDetailRoute
import com.sloy.sevibus.infrastructure.BuildVariantDI
import com.sloy.sevibus.infrastructure.DI
import com.sloy.sevibus.navigation.NavigationDestination
import com.sloy.sevibus.navigation.TopLevelDestination
import com.sloy.sevibus.navigation.rememberSevAppState
import com.sloy.sevibus.ui.components.SevNavigationBar
import com.sloy.sevibus.ui.theme.SevTheme
import io.morfly.compose.bottomsheet.material3.BottomSheetScaffold
import io.morfly.compose.bottomsheet.material3.rememberBottomSheetScaffoldState
import io.morfly.compose.bottomsheet.material3.rememberBottomSheetState
import io.morfly.compose.bottomsheet.material3.requireSheetVisibleHeightDp
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.KoinApplication

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun App() {
    val context = LocalContext.current
    KoinApplication(application = {
        androidContext(context)
        modules(DI.viewModelModule, DI.dataModule, DI.infrastructureModule, BuildVariantDI.module)
    }) {
        val appState = rememberSevAppState()
        val coroutineScope = rememberCoroutineScope()
        val currentDestination: NavigationDestination? = appState.currentNavigationDestination

        var bottomBarVisibility by rememberSaveable { (mutableStateOf(true)) }
        bottomBarVisibility = currentDestination is TopLevelDestination

        val sheetState = rememberBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            defineValues = {
                SheetValue.Collapsed at height(120.dp)
                SheetValue.PartiallyExpanded at height(percent = 40)
                SheetValue.Expanded at contentHeight
                //SheetValue.Expanded at height(percent = 100)
            }
        )

        SevTheme {
            Scaffold(bottomBar = {
                AnimatedVisibility(
                    visible = bottomBarVisibility,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it }),
                ) {
                    SevNavigationBar(
                        topLevelDestinations = appState.topLevelDestinations,
                        onNavigateToDestination = {
                            appState.navigateToTopLevelDestination(it)
                            coroutineScope.launch { sheetState.animateTo(SheetValue.PartiallyExpanded) }
                        },
                        currentNavDestination = appState.currentDestination,
                    )
                }
            }) { scaffoldInnerPadding ->
                val mapViewModel: MapViewModel = koinViewModel()


                BottomSheetScaffold(
                    scaffoldState = rememberBottomSheetScaffoldState(sheetState),
                    sheetContainerColor = SevTheme.colorScheme.background,
                    sheetShadowElevation = 8.dp,
                    sheetContent = {
                        NavHost(
                            navController = appState.navController,
                            startDestination = NavigationDestination.ForYou,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(scaffoldInnerPadding),
                            enterTransition = { fadeIn(animationSpec = tween(100)) },
                            exitTransition = { fadeOut(animationSpec = tween(100)) },
                        ) {
                            forYouRoute(appState.navController)
                            linesRoute(appState.navController)
                            travelCardsRoute()
                            lineStopsRoute(appState.navController, onRouteSelected = mapViewModel::onRouteSelected)
                            stopDetailRoute(appState.navController)
                        }
                    },
                ) { contentPadding -> // ignored because we already use the bottom sheet height to offset the map content
                    val sheetHeight by remember { derivedStateOf { sheetState.requireSheetVisibleHeightDp() } }
                    val mapState by mapViewModel.state.collectAsStateWithLifecycle()
                    mapViewModel.ticker.collectAsStateWithLifecycle(Unit)
                    MapContent(
                        mapState,
                        PaddingValues(bottom = sheetHeight),
                        onStopSelected = { appState.navController.navigate(NavigationDestination.StopDetail(it.code)) },
                        onMapClick = {
                            coroutineScope.launch { sheetState.animateTo(SheetValue.Collapsed) }
                        },
                    )
                }
                LaunchedEffect(currentDestination) {
                    if (currentDestination != null) {
                        mapViewModel.setDestination(currentDestination)
                        sheetState.animateTo(SheetValue.PartiallyExpanded)
                    }
                }
            }
        }
    }
}

enum class SheetValue { Collapsed, PartiallyExpanded, Expanded }
