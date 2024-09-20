package com.sloy.sevibus.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.Route
import com.sloy.sevibus.domain.model.RouteId
import com.sloy.sevibus.ui.preview.ScreenPreview
import com.sloy.sevibus.ui.theme.AlexGreyIcons
import com.sloy.sevibus.ui.theme.SevTheme

@Composable
fun RouteTabsSelector(route1: Route, route2: Route, selected: RouteId, onRouteClicked: (Route) -> Unit, modifier: Modifier = Modifier) {
    val rotation = if (route2.id == selected) 180f else 0f
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        RouteTab(directionValue = route1.destination, route1.id == selected, { onRouteClicked(route1) }, Modifier.weight(1f))
        Icon(
            Icons.AutoMirrored.Default.ArrowForward,
            contentDescription = null,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .rotate(rotation),
            tint = AlexGreyIcons
        )
        RouteTab(directionValue = route2.destination, route2.id == selected, { onRouteClicked(route2) }, Modifier.weight(1f))
    }
}

@Composable
private fun RouteTab(directionValue: String, isSelected: Boolean, onRouteClicked: () -> Unit, modifier: Modifier = Modifier) {
    val colors = if (isSelected) {
        CardDefaults.cardColors(containerColor = SevTheme.colorScheme.surfaceContainer)
    } else {
        CardDefaults.cardColors(containerColor = Color.Transparent)
    }
    val directionLabel = when (isSelected) {
        true -> "Desde"
        false -> "Hasta"
    }
    Card(colors = colors, modifier = modifier, onClick = onRouteClicked) {
        Column(Modifier.padding(8.dp)) {
            Text(directionLabel, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Light)
            Text(directionValue, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun Preview() {
    ScreenPreview {
        Column {

            RouteTabsSelector(
                route1 = Stubs.routes[0],
                route2 = Stubs.routes[1],
                selected = Stubs.routes[0].id,
                onRouteClicked = {},
                modifier = Modifier.padding(16.dp),
            )
        }
        Spacer(Modifier.size(32.dp))
        RouteTabsSelector(
            route1 = Stubs.routes[0],
            route2 = Stubs.routes[1],
            selected = Stubs.routes[1].id,
            onRouteClicked = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
