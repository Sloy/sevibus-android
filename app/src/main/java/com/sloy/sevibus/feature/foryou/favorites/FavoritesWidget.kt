package com.sloy.sevibus.feature.foryou.favorites

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sloy.sevibus.R
import com.sloy.sevibus.Stubs
import com.sloy.sevibus.infrastructure.analytics.events.Clicks
import com.sloy.sevibus.ui.components.SurfaceButton
import com.sloy.sevibus.ui.preview.ScreenPreview
import com.sloy.sevibus.ui.theme.SevTheme
import org.koin.androidx.compose.koinViewModel


@Composable
fun FavoritesWidget(onStopClicked: (code: Int) -> Unit, onEditFavoritesClicked: () -> Unit) {
    if (!LocalView.current.isInEditMode) {
        val viewModel = koinViewModel<FavoritesListViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val isLoginLoading by viewModel.isLoginLoading.collectAsStateWithLifecycle()
        FavoritesWidget(
            state = state,
            isLoginLoading = isLoginLoading,
            onStopClicked = { stopId ->
                viewModel.onTrack(Clicks.FavoriteStopClicked(stopId))
                onStopClicked(stopId)
            },
            onEditFavoritesClicked = {
                viewModel.onTrack(Clicks.EditFavoritesClicked)
                onEditFavoritesClicked()
            },
            onLoginClicked = viewModel::onLoginClick
        )
    } else {
        FavoritesWidget(
            FavoritesListState.Content(Stubs.favorites), false, onStopClicked, onEditFavoritesClicked, {}
        )
    }
}

@Composable
fun FavoritesWidget(
    state: FavoritesListState,
    isLoginLoading: Boolean,
    onStopClicked: (code: Int) -> Unit,
    onEditFavoritesClicked: () -> Unit,
    onLoginClicked: (Context) -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        when (state) {
            is FavoritesListState.Loading -> {
                repeat(3) {
                    FavoriteListItemShimmer(Modifier.padding(end = 64.dp))
                    Spacer(Modifier.height(16.dp))
                }
            }

            is FavoritesListState.Content -> {
                if (state.favorites.isNotEmpty()) {
                    state.favorites.forEach { favorite ->
                        FavoriteListItem(favorite, onStopClicked, Modifier.padding(horizontal = 16.dp))
                        Spacer(Modifier.size(16.dp))
                    }
                    EditButton(onClick = onEditFavoritesClicked)
                    Spacer(Modifier.height(16.dp))
                } else {
                    FavoritesEmptyState()
                }
            }

            is FavoritesListState.NotLogged -> {
                val context = LocalContext.current
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    FavoritesEmptyState()
                    Spacer(Modifier.height(16.dp))
                    SurfaceButton(stringResource(R.string.foryou_login_with_google), icon = {
                        if (isLoginLoading) {
                            CircularProgressIndicator(Modifier.size(18.dp))
                        } else {
                            Icon(
                                tint = Color.Unspecified,
                                painter = painterResource(id = com.google.android.gms.base.R.drawable.googleg_standard_color_18),
                                contentDescription = "Google logo",
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }, onClick = { onLoginClicked(context) })
                }

            }
        }
    }
}

@Composable
private fun FavoritesEmptyState() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(24.dp))
        Image(
            painter = painterResource(id = R.drawable.illustration_favorite_stop),
            contentDescription = "Drawing of a stop with a heart",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .height(90.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.foryou_favorites_header), style = SevTheme.typography.headingStandard)
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.foryou_favorites_description),
            style = SevTheme.typography.bodyStandard,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun EditButton(onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth()) {
        SurfaceButton("Editar", onClick = onClick, modifier = Modifier.align(Alignment.Center), icon = {
            Icon(
                Icons.Outlined.Edit,
                contentDescription = "Editar",
                tint = SevTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        })
    }
}

@Preview
@Composable
private fun WithArrivalsPreview() {
    ScreenPreview {
        FavoritesWidget(FavoritesListState.Content(Stubs.favorites.take(3)), false, {}, {}, {})
    }
}

@Preview
@Composable
private fun EmptyPreview() {
    ScreenPreview {
        FavoritesWidget(FavoritesListState.Content(emptyList()), false, {}, {}, {})
    }
}

@Preview
@Composable
private fun NotLoggedPreview() {
    ScreenPreview {
        FavoritesWidget(FavoritesListState.NotLogged, false, {}, {}, {})
    }
}

@Preview
@Composable
private fun LoadingPreview() {
    ScreenPreview {
        FavoritesWidget(FavoritesListState.Loading, false, {}, {}, {})
    }
}
