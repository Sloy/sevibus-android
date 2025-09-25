package com.sloy.sevibus

import android.R.attr.bottom
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import com.composables.core.BottomSheet
import com.composables.core.BottomSheetState
import com.composables.core.DragIndication
import com.composables.core.SheetDetent
import com.composables.core.rememberBottomSheetState
import com.sloy.sevibus.feature.map.MapScreen
import com.sloy.sevibus.infrastructure.FeatureFlags
import com.sloy.sevibus.navigation.NavigationDestination
import com.sloy.sevibus.navigation.NavigationDestinationType
import com.sloy.sevibus.navigation.TopLevelDestination
import com.sloy.sevibus.navigation.isBottomBarVisible
import com.sloy.sevibus.navigation.isTopBarVisible
import com.sloy.sevibus.ui.components.AppUpdateButtonState
import com.sloy.sevibus.ui.components.SevNavigationBar
import com.sloy.sevibus.ui.snackbar.LocalSnackbarHostState
import com.sloy.sevibus.ui.theme.SevTheme
import kotlinx.coroutines.launch
import se.warting.inappupdate.compose.InAppUpdateState
import se.warting.inappupdate.compose.Mode
import se.warting.inappupdate.compose.rememberInAppUpdateState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MapBottomSheetScaffold(
    currentDestination: NavigationDestination,
    onNavigate: (NavigationDestination) -> Unit,
    topBar: @Composable (destination: NavigationDestination) -> Unit,
    bottomSheetContent: @Composable (destination: NavigationDestination) -> Unit,
    fullScreenContent: @Composable (destination: NavigationDestination, paddingValues: PaddingValues) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    val insetPaddings = WindowInsets.systemBars.asPaddingValues()
    val bottomInsetPadding by remember { derivedStateOf { insetPaddings.calculateBottomPadding() } }
    val topInsetPadding by remember { derivedStateOf { insetPaddings.calculateTopPadding() } }

    val currentDestinationState by rememberUpdatedState(newValue = currentDestination)
    val bottomBarHeight by remember(currentDestination) { derivedStateOf { if (currentDestinationState.isBottomBarVisible()) 80.dp else 0.dp } }
    val screenPeekingHeight by remember(currentDestination) { derivedStateOf { currentDestinationState.peekingSheetHeight() } }

    val DetentCollapsed = SheetDetent("CollapsedWithBottomBar") { containerHeight, sheetHeight ->
        bottomBarHeight + bottomInsetPadding + DRAG_INDICATOR_HEIGHT + screenPeekingHeight
    }
    val DetentPartiallyCollapsed = SheetDetent("PartiallyCollapsed") { containerHeight, sheetHeight ->
        containerHeight * 0.4f
    }
    val DetentPartiallyExpanded = SheetDetent("PartiallyExpanded") { containerHeight, sheetHeight ->
        containerHeight * 0.6f
    }
    val DetentExpanded = SheetDetent("Expanded") { containerHeight, sheetHeight ->
        containerHeight - topInsetPadding
    }
    val sheetState = rememberBottomSheetState(
        initialDetent = DetentPartiallyExpanded,
        detents = listOf(DetentCollapsed, DetentPartiallyCollapsed, DetentPartiallyExpanded, DetentExpanded),
    )
    val updateState = rememberInAppUpdateState()

    // Bottomsheet expanded state
    var previousSheetDestination by remember { mutableStateOf(currentDestination) }
    LaunchedEffect(currentDestination) {
        if (currentDestination.type == NavigationDestinationType.MAP_BOTTOM_SHEET) {
            if (currentDestination::class != previousSheetDestination::class) {
                sheetState.animateTo(DetentCollapsed)
                sheetState.animateTo(DetentPartiallyExpanded)
                sheetState.invalidateDetents()
            }
            previousSheetDestination = currentDestination
        }
    }
    val snackbarHostState = remember { SnackbarHostState() }
    CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
        Scaffold(
            snackbarHost = {
                SnackbarHost(snackbarHostState)
            },
            bottomBar = {
                AnimatedVisibility(
                    visible = currentDestination.isBottomBarVisible(),
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it }),
                ) {
                    SevNavigationBar(
                        topLevelDestinations = TopLevelDestination.topLevelDestinations.let { destinations ->
                            if (FeatureFlags.bonobus) destinations else destinations.filter { it != NavigationDestination.Cards }
                        },
                        onNavigateToDestination = {
                            onNavigate(it)
                            coroutineScope.launch { sheetState.animateTo(DetentPartiallyExpanded) }
                        },
                        currentNavDestination = currentDestination,
                    )
                }
            },
            topBar = {
                AnimatedVisibility(
                    visible = currentDestination.isTopBarVisible() && sheetState.targetDetent != DetentExpanded,
                    enter = slideInVertically(initialOffsetY = { -it }),
                    exit = slideOutVertically(targetOffsetY = { -it }),
                    content = { topBar(currentDestination) },
                )

            }) { scaffoldInnerPadding -> // Includes the padding from BottomNavigation and Snackbar

            MapContainer(sheetState, scaffoldInnerPadding, onNavigate, onMapClick = {
                if (currentDestination is TopLevelDestination) {
                    coroutineScope.launch { sheetState.animateTo(DetentCollapsed) }
                }
            })
            BottomSheet(
                state = sheetState,
                modifier = Modifier
                    .shadow(4.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(SevTheme.colorScheme.background)
                    .widthIn(max = 640.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = topInsetPadding), // To compensate the top padding applied to the Sheet
                ) {
                    DragIndication(
                        modifier = Modifier
                            .padding(vertical = DRAG_INDICATOR_PADDING)
                            .background(Color.Black.copy(0.4f), RoundedCornerShape(100))
                            .width(32.dp)
                            .height(DRAG_INDICATOR_HANDLE_HEIGHT)
                            .align(Alignment.CenterHorizontally),
                    )
                    // Sheet Content
                    AnimatedBottomSheetContent(
                        currentDestination,
                    ) { destination ->
                        Column(
                            Modifier
                                .navigationBarsPadding()
                                .consumeWindowInsets(PaddingValues(bottom = bottomBarHeight))
                        ) {
                            Box(Modifier.weight(1f)) { // weight=1 to make the content fill the space except for the spacers below
                                bottomSheetContent(destination)
                            }
                            //Spacer(Modifier.height(topInsetPadding)) // <-- Trick to overcome the offset on the Expanded bottomsheet top
                            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.ime))
                            Spacer(
                                Modifier
                                    .height(bottomBarHeight)
                                    .animateContentSize()
                            )
                        }
                    }
                }
            }

            AnimatedFullScreenContent(currentDestination) { destination ->
                fullScreenContent(destination, scaffoldInnerPadding)
            }
        }
    }
}

@Composable
fun BottomSheetContainer(modifier: Modifier = Modifier) {

}

@Composable
private fun MapContainer(sheetState: BottomSheetState, scaffoldInnerPadding: PaddingValues, onNavigate: (NavigationDestination) -> Unit, onMapClick: ()->Unit) {
    with(LocalDensity.current) {
        // Map padded content
        val topSystemBarPadding = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()
        val bottomPadding = (sheetState.offset.toDp() - topSystemBarPadding).coerceAtLeast(0.dp)
        val topPadding =scaffoldInnerPadding.calculateTopPadding()


        MapScreen(
            sheetState,
            PaddingValues(bottom = bottomPadding, top = topPadding),
            onStopSelected = {
                onNavigate(NavigationDestination.StopDetail(it.code))
            },
            onMapClick,
        )
    }
}

private fun InAppUpdateState.toButtonState(): AppUpdateButtonState {
    return when (this) {
        is InAppUpdateState.OptionalUpdate -> AppUpdateButtonState.Available(onClick = { this.onStartUpdate(Mode.FLEXIBLE) })
        is InAppUpdateState.RequiredUpdate -> AppUpdateButtonState.Available(onClick = { this.onStartUpdate() })
        is InAppUpdateState.DownloadedUpdate -> AppUpdateButtonState.Ready(onClick = { this.appUpdateResult.completeUpdate() })
        is InAppUpdateState.InProgressUpdate -> AppUpdateButtonState.Downloading(
            installState.bytesDownloaded,
            installState.totalBytesToDownload
        )

        else -> AppUpdateButtonState.Hidden
    }
}

private fun NavigationDestination.peekingSheetHeight(): Dp {
    return when (this) {
        is NavigationDestination.ForYou -> 0.dp
        is NavigationDestination.Lines -> 0.dp
        is NavigationDestination.LineStops -> 92.dp
        is NavigationDestination.StopDetail -> 68.dp
        else -> 0.dp
    }
}

@Composable
private fun AnimatedFullScreenContent(
    currentDestination: NavigationDestination,
    modifier: Modifier = Modifier,
    content: @Composable (NavigationDestination) -> Unit
) {
    WithRetainedDestination(currentDestination, shouldShow = { it.type == NavigationDestinationType.FULL_SCREEN }) { destination, visible ->
        val animationOffset = with(LocalDensity.current) { 48.dp.toPx().toInt() }
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { animationOffset }),
            exit = slideOutVertically(targetOffsetY = { animationOffset }) + fadeOut(),
        ) {
            Surface(
                modifier.fillMaxSize(),
                color = SevTheme.colorScheme.background,
            ) { content(destination) }
        }
    }
}

@Composable
private fun AnimatedBottomSheetContent(
    currentDestination: NavigationDestination,
    modifier: Modifier = Modifier,
    content: @Composable (destination: NavigationDestination) -> Unit,
) {
    WithRetainedDestination(
        currentDestination,
        shouldShow = { it.type == NavigationDestinationType.MAP_BOTTOM_SHEET }) { targetDestination, visible ->
        Crossfade(targetState = targetDestination, label = "BottomSheetContent", modifier = modifier) { destination ->
            content(destination)
        }
    }
}


/**
 * This composable is used to retain the previous destination when the current destination is not shown.
 *
 * This is useful to animate the content out of the screen when the content depends on the NavigationDestination.
 * When the new destination doesn't match the [shouldShow] predicate the [content] lambda will receive the previous destination,
 * but [shouldShow] will be false. This way we can animate the content out using [AnimatedVisibility] or similar.
 *
 */
@Composable
private fun WithRetainedDestination(
    destination: NavigationDestination,
    shouldShow: (NavigationDestination) -> Boolean,
    content: @Composable (previous: NavigationDestination, shouldShow: Boolean) -> Unit
) {
    val previousDestination = remember { mutableStateOf(destination) }
    LaunchedEffect(destination) {
        if (shouldShow(destination)) {
            previousDestination.value = destination
        }
    }
    content(previousDestination.value, shouldShow(destination))
}

val DRAG_INDICATOR_HANDLE_HEIGHT = 4.dp
val DRAG_INDICATOR_PADDING = 16.dp
val DRAG_INDICATOR_HEIGHT = DRAG_INDICATOR_HANDLE_HEIGHT + DRAG_INDICATOR_PADDING * 2
