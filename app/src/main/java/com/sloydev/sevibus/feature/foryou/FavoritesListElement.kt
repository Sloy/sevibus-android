package com.sloydev.sevibus.feature.foryou

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloydev.sevibus.Stubs
import com.sloydev.sevibus.domain.model.FavoriteStop
import com.sloydev.sevibus.domain.model.Line
import com.sloydev.sevibus.domain.model.StopId
import com.sloydev.sevibus.domain.model.descriptionSeparator
import com.sloydev.sevibus.ui.components.ArrivalElement
import com.sloydev.sevibus.ui.components.LineIndicatorSmall
import com.sloydev.sevibus.ui.preview.ScreenPreview
import com.sloydev.sevibus.ui.theme.AlexGreyIcons
import kotlin.random.Random


@Composable
fun FavoritesListElement(onStopClicked: (code: Int) -> Unit) {
    Column {
        Text(
            "Paradas favoritas", style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )

        Stubs.favorites.forEach {
            FavoriteListItem(it, onStopClicked)
            Spacer(Modifier.size(4.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FavoriteListItem(favorite: FavoriteStop, onStopClicked: (code: StopId) -> Unit) {
    Card(
        onClick = { onStopClicked(favorite.stop.code) },
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        Row(Modifier.padding(16.dp)) {
            Icon(favorite.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(Modifier.padding(start = 16.dp)) {
                Text(
                    favorite.customName, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold
                )
                Text(
                    favorite.stop.descriptionSeparator(), maxLines = 1, overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium, color = AlexGreyIcons
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top=4.dp)) {
                    favorite.stop.lines.forEach {line ->
                        ArrivalElement(minutes = 1, line = line)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun FavoritesCardPreview() {
    ScreenPreview {
        FavoritesListElement({})

    }
}