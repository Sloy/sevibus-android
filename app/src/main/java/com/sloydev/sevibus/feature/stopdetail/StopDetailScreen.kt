package com.sloydev.sevibus.feature.stopdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sloydev.sevibus.Stubs
import com.sloydev.sevibus.domain.Line
import com.sloydev.sevibus.domain.Stop
import com.sloydev.sevibus.ui.ScreenPreview
import com.sloydev.sevibus.ui.components.LineIndicatorMedium

fun NavGraphBuilder.stopDetailRoute() {
    composable("/stop-detail/{code}") { stackEntry ->
        val code = stackEntry.arguments!!.getInt("code")
        StopDetailScreen(Stubs.stops.find { it.code == code } ?: remember { Stubs.stops.random() })
    }
}

fun NavController.navigateToStopDetail(code: Int) {
    navigate("/stop-detail/$code")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopDetailScreen(stop: Stop, embedded: Boolean = false) {
    Column {
        if (!embedded) {
            CenterAlignedTopAppBar(
                title = {
                    Text("Parada 572", maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                navigationIcon = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            Icons.Default.FavoriteBorder, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            )
        }
        if (embedded) {
            Text(
                stop.code.toString(),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
        Text(
            stop.description,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(start = 16.dp)
        )

        Card(Modifier.padding(16.dp)) {
            Column(Modifier.padding(8.dp)) {
                BusArrival(Stubs.lines[0], "HELIOPOLIS", 1)
                HorizontalDivider()
                BusArrival(Stubs.lines[6], "PRADO", 1)
                HorizontalDivider()
                BusArrival(Stubs.lines[0], "HELIOPOLIS", 4)
                HorizontalDivider()
                BusArrival(Stubs.lines[34], "STA JUSTA", 5)
                HorizontalDivider()
                BusArrival(Stubs.lines[6], "PRADO", 7)
                HorizontalDivider()
                BusArrival(Stubs.lines[34], "STA JUSTA", 15)
            }
        }
    }
}

@Composable
private fun BusArrival(line: Line, direction: String, minutes: Int) {
    ListItem(
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        headlineContent = { Text(direction) },
        leadingContent = { LineIndicatorMedium(line) },
        trailingContent = {
            /*Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.WatchLater, contentDescription = "clock")
                Spacer(Modifier.size(4.dp))
                Text("$minutes min", style = MaterialTheme.typography.labelLarge)
            }*/
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$minutes", style = MaterialTheme.typography.bodyLarge)
                Text("min", style = MaterialTheme.typography.labelSmall)
            }
        }
    )
}

@Preview
@Composable
private fun Preview() {
    ScreenPreview {
        StopDetailScreen(Stubs.stops.first())
    }
}

@Preview
@Composable
private fun EmbeddedPreview() {
    ScreenPreview {
        StopDetailScreen(Stubs.stops.first(), embedded = true)
    }
}