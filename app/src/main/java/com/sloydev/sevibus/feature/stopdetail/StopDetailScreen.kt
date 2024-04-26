package com.sloydev.sevibus.feature.stopdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sloydev.sevibus.Stubs
import com.sloydev.sevibus.domain.model.Line
import com.sloydev.sevibus.domain.model.StopId
import com.sloydev.sevibus.domain.model.description1
import com.sloydev.sevibus.domain.model.description2
import com.sloydev.sevibus.ui.components.LineIndicatorMedium
import com.sloydev.sevibus.ui.components.SevTopAppBar
import com.sloydev.sevibus.ui.preview.ScreenPreview
import com.sloydev.sevibus.ui.theme.AlexGreyIcons
import com.sloydev.sevibus.ui.theme.AlexGreySurface
import com.sloydev.sevibus.ui.theme.AlexGreySurface2
import com.sloydev.sevibus.ui.theme.AlexPink
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.stopDetailRoute() {
    composable("/stop-detail/{code}") { stackEntry ->
        val code = stackEntry.arguments!!.getString("code")!!
        StopDetailScreen(code.toInt(), embedded = false)
    }
}

fun NavController.navigateToStopDetail(code: Int) {
    navigate("/stop-detail/$code")
}

@Composable
fun StopDetailScreen(code: StopId, embedded: Boolean = false) {
    val viewModel = koinViewModel<StopDetailViewModel> { parametersOf(code) }
    val state by viewModel.state.collectAsState()
    StopDetailScreen(state, embedded)
}

@Composable
fun StopDetailScreen(state: StopDetailScreenState, embedded: Boolean = false) {
    if (state is Error) {
        Text("Error")
    }
    if(state is StopDetailScreenState.Loading){
        CircularProgressIndicator()
    }
    if (state !is StopDetailScreenState.Content) {
        return
    }
    Column {
        SevTopAppBar(
            title = {
                if (embedded) {
                    Text(
                        "Parada " + state.stop.code, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Medium
                    )
                } else {
                    Text("Parada " + state.stop.code, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            },
            navigationIcon = {
                if (!embedded) {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
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

        ListItem(
            colors = ListItemDefaults.colors(containerColor = AlexGreySurface),
            modifier = Modifier
                .padding(16.dp)
                .clip(MaterialTheme.shapes.medium),
            leadingContent = {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = AlexPink)
            },
            headlineContent = {
                Column {
                    Text(state.stop.description1, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    state.stop.description2?.let {
                        Text(it, style = MaterialTheme.typography.bodyMedium, color = AlexGreyIcons)
                    }
                }
            }
        )


        Column(Modifier.padding(16.dp)) {
            Text("Líneas", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.size(8.dp))
            BusArrival(Stubs.lines[0], "Heliopolis", 0)
            Spacer(Modifier.size(4.dp))
            BusArrival(Stubs.lines[6], "Prado", 1)
            Spacer(Modifier.size(4.dp))
            BusArrival(Stubs.lines[0], "Heliopolis", 4)
            Spacer(Modifier.size(4.dp))
            BusArrival(Stubs.lines[34], "Sta Justa", 5)
            Spacer(Modifier.size(4.dp))
            BusArrival(Stubs.lines[6], "Prado", 7)
            Spacer(Modifier.size(4.dp))
            BusArrival(Stubs.lines[34], "Sta Justa", 15)
        }
    }
}

@Composable
private fun BusArrival(line: Line, direction: String, minutes: Int) {
    ListItem(
        colors = ListItemDefaults.colors(containerColor = AlexGreySurface),
        modifier = Modifier.clip(MaterialTheme.shapes.medium),
        headlineContent = { Text(direction) },
        leadingContent = { LineIndicatorMedium(line) },
        trailingContent = {
            Box(
                Modifier
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(AlexGreySurface2)
                    .padding(vertical = 4.dp, horizontal = 8.dp)
            ) {
                val text = if (minutes > 0) "$minutes min" else "Llegando..."
                Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Preview
@Composable
private fun Preview() {
    ScreenPreview {
        StopDetailScreen(StopDetailScreenState.Content(Stubs.stops[1]))
    }
}

@Preview
@Composable
private fun EmbeddedPreview() {
    ScreenPreview {
        StopDetailScreen(StopDetailScreenState.Content(Stubs.stops[0]), embedded = true)
    }
}