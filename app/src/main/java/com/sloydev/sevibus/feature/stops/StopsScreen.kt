package com.sloydev.sevibus.feature.stops

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloydev.sevibus.Stubs
import com.sloydev.sevibus.ui.ScreenPreview
import com.sloydev.sevibus.ui.components.LineIndicatorSmall

@Composable
fun StopsRoute() {
    StopsScreen(
        StopsScreenState(
            line = Stubs.lines[2],
            directions = listOf("HOSPITAL V.ROCIO", "POLIGONO NORTE"),
            stops = Stubs.stops,
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopsScreen(state: StopsScreenState) {
    Column {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LineIndicatorSmall(state.line, Modifier.padding(end = 8.dp))
                    Text(state.line.description, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            },
            navigationIcon = {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        )
        TabRow(selectedTabIndex = 0) {
            Tab(
                selected = true,
                onClick = { },
                text = { Text(text = state.directions[0], maxLines = 2, overflow = TextOverflow.Ellipsis) }
            )
            Tab(
                selected = false,
                onClick = { },
                text = { Text(text = state.directions[1], maxLines = 2, overflow = TextOverflow.Ellipsis) }
            )
        }
        Column(Modifier.verticalScroll(rememberScrollState())) {
            state.stops.forEachIndexed { index, stop ->
                StopListItem(
                    number = stop.code,
                    name = stop.description,
                    lines = Stubs.lines.take(listOf(1, 1, 1, 1, 2, 2, 2, 3, 3, 4).random()),
                    listPosition = when (index) {
                        0 -> ListPosition.Start
                        state.stops.lastIndex -> ListPosition.End
                        else -> ListPosition.Middle
                    },
                    color = Color(state.line.colorHex)
                )
            }
        }
    }
}

@Preview
@Composable
private fun StopsScreenPreview() {
    ScreenPreview {
        StopsScreen(
            StopsScreenState(
                line = Stubs.lines[2],
                directions = listOf("HOSPITAL V.ROCIO", "POLIGONO NORTE"),
                stops = Stubs.stops,
            )
        )
    }
}