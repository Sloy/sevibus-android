package com.sloy.sevibus

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BrushPainter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.sloy.sevibus.domain.model.LoggedUser
import com.sloy.sevibus.domain.model.SearchResult
import com.sloy.sevibus.feature.cards.CardsHelpScreen
import com.sloy.sevibus.feature.cards.CardsScreen
import com.sloy.sevibus.feature.debug.http.HttpOverlayLayout
import com.sloy.sevibus.feature.foryou.ForYouScreen
import com.sloy.sevibus.feature.foryou.favorites.edit.EditFavoritesScreen
import com.sloy.sevibus.feature.lines.LinesScreen
import com.sloy.sevibus.feature.linestops.LineRouteScreen
import com.sloy.sevibus.feature.login.SettingsScreen
import com.sloy.sevibus.feature.search.RoundedSearchBar
import com.sloy.sevibus.feature.search.SearchScreen
import com.sloy.sevibus.feature.search.SearchViewModel
import com.sloy.sevibus.feature.search.TopBarState
import com.sloy.sevibus.feature.stopdetail.StopDetailScreen
import com.sloy.sevibus.navigation.NavigationDestination
import com.sloy.sevibus.navigation.rememberSevAppState
import com.sloy.sevibus.ui.components.CircularIconButton
import com.sloy.sevibus.ui.theme.SevTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.KoinContext

@Composable
fun App() {
    KoinContext {
        val appState = rememberSevAppState()
        val isLastDestination: Boolean by appState.sevNavigator.isLastDestination.collectAsStateWithLifecycle()
        val currentDestination: NavigationDestination by appState.sevNavigator.destination.collectAsStateWithLifecycle()
        val currentUser = appState.currentUser
        val searchViewModel: SearchViewModel = koinViewModel()
        val topBarState by searchViewModel.topBarState.collectAsStateWithLifecycle()
        val searchResults by searchViewModel.results.collectAsStateWithLifecycle()

        BackHandler(enabled = !isLastDestination) {
            appState.sevNavigator.navigateBack()
        }

        val onNavigate: (NavigationDestination) -> Unit = { appState.navigate(it) }

        SevTheme {
            HttpOverlayLayout() {
                MapBottomSheetScaffold(
                    currentDestination = currentDestination,
                    onNavigate = onNavigate,
                    topBar = {
                        TopBar(currentUser,
                            topBarState,
                            onSearchTermChanged = { searchViewModel.onSearch(it) },
                            onCancelSearch = {
                                appState.sevNavigator.popToRoot()
                                searchViewModel.onSearch("")
                            },
                            onNavigate = onNavigate,
                            onBack = { appState.sevNavigator.navigateBack() })
                    },
                    bottomSheetContent = { destination ->
                        BottomSheetContent(destination, onNavigate)
                    },
                    fullScreenContent = { destination, paddingValues ->
                        FullScreenContent(destination, searchResults, paddingValues, onNavigate)
                    }
                )
            }
        }
    }
}

@Composable
private fun TopBar(
    loggedUser: LoggedUser?,
    topBarState: TopBarState,
    onSearchTermChanged: (String) -> Unit,
    onCancelSearch: () -> Unit,
    onNavigate: (NavigationDestination) -> Unit,
    onBack: () -> Unit,
) {
    val isSearchActive = (topBarState as? TopBarState.Search)?.isSearchActive ?: false
    Row(
        Modifier
            .padding(horizontal = 8.dp)
            .padding(
                top = WindowInsets.safeDrawing
                    .asPaddingValues()
                    .calculateTopPadding()
            )
            .padding(top = 8.dp)
    ) {
        AnimatedVisibility(visible = isSearchActive) {
            CircularIconButton(onClick = onBack) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar")
            }
        }

        RoundedSearchBar(
            topBarState,
            onSearchTermChanged,
            onOpenSearchScreen = { onNavigate(NavigationDestination.Search) },
            onCancelClicked = { onCancelSearch() },
            Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
        )

        AnimatedVisibility(
            visible = !isSearchActive,
            enter = fadeIn() + slideInHorizontally(initialOffsetX = { it }) + expandHorizontally(),
            exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it }) + shrinkHorizontally()
        ) {
            ProfileIcon(loggedUser, onNavigate, modifier = Modifier.padding(end = 8.dp))
        }
    }
}


@Composable
private fun BottomSheetContent(
    destination: NavigationDestination,
    onNavigate: (NavigationDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    when (destination) {
        is NavigationDestination.ForYou -> {
            ForYouScreen(onStopClicked = { code ->
                onNavigate(NavigationDestination.StopDetail(code))
            }, onEditFavoritesClicked = { onNavigate(NavigationDestination.EditFavorites) })
        }

        is NavigationDestination.Lines -> {
            LinesScreen(onNavigate)
        }

        is NavigationDestination.LineStops -> {
            LineRouteScreen(
                destination.lineId,
                initialRouteId = destination.routeId,
                highlightedStopId = destination.highlightedStop,
                onStopClick = { onNavigate(NavigationDestination.StopDetail(it.code, highlightedLine = destination.lineId)) },
                onRouteSelected = { route -> onNavigate(destination.copy(routeId = route.id, highlightedStop = null)) },
            )
        }

        is NavigationDestination.StopDetail -> {
            StopDetailScreen(destination.stopId, destination.highlightedLine, onArrivalClick = { arrival, stopId ->
                onNavigate(NavigationDestination.LineStops(arrival.line.id, arrival.route.id, stopId))
            })
        }

        else -> {}
    }
}

@Composable
private fun FullScreenContent(
    destination: NavigationDestination,
    searchResults: List<SearchResult>,
    paddingValues: PaddingValues,
    onNavigate: (NavigationDestination) -> Unit
) {
    when (destination) {
        is NavigationDestination.Cards -> CardsScreen(onNavigateToHelp = { onNavigate(NavigationDestination.CardsHelp) })
        is NavigationDestination.Settings -> SettingsScreen()
        is NavigationDestination.Search -> SearchScreen(
            searchResults, onNavigate, Modifier.padding(top = paddingValues.calculateTopPadding())
        )

        is NavigationDestination.EditFavorites -> EditFavoritesScreen()
        is NavigationDestination.CardsHelp -> CardsHelpScreen()
        else -> {}
    }
}

@Composable
private fun ProfileIcon(currentUser: LoggedUser?, onNavigate: (NavigationDestination) -> Unit, modifier: Modifier = Modifier) {
    FloatingActionButton(
        onClick = { onNavigate(NavigationDestination.Settings) },
        modifier.size(48.dp),
        shape = CircleShape,
        containerColor = SevTheme.colorScheme.background,
        contentColor = SevTheme.colorScheme.onSurfaceVariant,
    ) {
        if (currentUser?.photoUrl == null) {
            Icon(Icons.Filled.Person, contentDescription = "Profile")
        } else {
            AsyncImage(
                model = currentUser.photoUrl,
                modifier = Modifier
                    .size(48.dp - 8.dp)
                    .clip(CircleShape),
                contentDescription = "Profile image",
                placeholder = BrushPainter(
                    Brush.linearGradient(
                        listOf(
                            Color(color = 0xFFFFFFFF),
                            Color(color = 0xFFDDDDDD),
                        )
                    )
                ),
            )

        }
    }

}

@Preview
@Composable
private fun AppPreviewBottomSheet() {
    SevTheme {
        MapBottomSheetScaffold(
            currentDestination = NavigationDestination.ForYou,
            onNavigate = {},
            topBar = { TopBar(Stubs.userLaura, TopBarState.Search("", false), {}, {}, {}, {}) },
            bottomSheetContent = {
                BottomSheetContent(it, {})
            },
            fullScreenContent = { destination, paddingValues ->
                FullScreenContent(destination, Stubs.searchResults, paddingValues) {}
            }
        )
    }
}

@Preview
@Composable
private fun AppPreviewFullScreen() {
    SevTheme {
        MapBottomSheetScaffold(
            currentDestination = NavigationDestination.Search,
            onNavigate = {},
            topBar = { TopBar(Stubs.userLaura, TopBarState.Search("", true), {}, {}, {}, {}) },
            bottomSheetContent = {
                BottomSheetContent(it, {})
            },
            fullScreenContent = { it, paddingValues ->
                FullScreenContent(it, Stubs.searchResults, paddingValues) {}
            }
        )
    }
}
