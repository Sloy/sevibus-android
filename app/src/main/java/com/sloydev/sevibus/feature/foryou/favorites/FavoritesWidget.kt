package com.sloydev.sevibus.feature.foryou.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloydev.sevibus.Stubs
import com.sloydev.sevibus.domain.model.BusArrival
import com.sloydev.sevibus.domain.model.FavoriteStop
import com.sloydev.sevibus.domain.model.StopId
import com.sloydev.sevibus.domain.model.descriptionSeparator
import com.sloydev.sevibus.ui.components.ArrivalElement
import com.sloydev.sevibus.ui.preview.ScreenPreview
import com.sloydev.sevibus.ui.theme.AlexGreyIcons
import com.sloydev.sevibus.ui.theme.SevTheme
import org.koin.androidx.compose.koinViewModel


@Composable
fun FavoritesWidget(onStopClicked: (code: Int) -> Unit) {
    if (!LocalView.current.isInEditMode) {
        val viewModel = koinViewModel<FavoritesViewModel>()
        val state by viewModel.state.collectAsState()
        FavoritesWidget(state, onStopClicked)
    } else {
        FavoritesWidget(FavoriteSubScreenState.Content.WithArrivals(
            Stubs.favorites, Stubs.favorites.associate { it.stop.code to Stubs.arrivals.take(2) }
        ), onStopClicked)
    }
}

@Composable
fun FavoritesWidget(state: FavoriteSubScreenState, onStopClicked: (code: Int) -> Unit) {
    when (state) {
        is FavoriteSubScreenState.Initial -> return
        is FavoriteSubScreenState.Empty -> Text("Empty state favorites")
        is FavoriteSubScreenState.Content -> {
            Column {
                Text(
                    "Paradas favoritas", style = SevTheme.typography.headingSmall,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )

                state.favorites.forEach { favorite ->
                    val arrivals = if (state is FavoriteSubScreenState.Content.WithArrivals) {
                        state.allArrivals[favorite.stop.code]
                    } else null
                    FavoriteListItem(favorite, arrivals, onStopClicked)
                    Spacer(Modifier.size(4.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FavoriteListItem(favorite: FavoriteStop, arrivals: List<BusArrival>?, onStopClicked: (code: StopId) -> Unit) {
    Card(
        onClick = { onStopClicked(favorite.stop.code) },
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        Row(Modifier.padding(16.dp)) {
            Icon(favorite.icon, contentDescription = null, tint = SevTheme.colorScheme.primary)
            Column(Modifier.padding(start = 16.dp)) {
                Text(
                    favorite.customName, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    style = SevTheme.typography.bodyStandardBold
                )
                Text(
                    favorite.stop.descriptionSeparator(), maxLines = 1, overflow = TextOverflow.Ellipsis,
                    style = SevTheme.typography.bodySmall, color = SevTheme.colorScheme.onSurfaceVariant
                )
                if (arrivals != null) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                        arrivals
                            .forEach {
                                ArrivalElement(it, showLine = true)
                            }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun WithArrivalsPreview() {
    ScreenPreview {
        val allArrivals = Stubs.favorites.associate { favorite -> favorite.stop.code to Stubs.arrivals.shuffled().take(3).sorted() }
        FavoritesWidget(FavoriteSubScreenState.Content.WithArrivals(Stubs.favorites, allArrivals), {})

    }
}

@Preview
@Composable
private fun WithoutArrivalsPreview() {
    ScreenPreview {
        FavoritesWidget(FavoriteSubScreenState.Content.LoadingArrivals(Stubs.favorites), {})

    }
}
