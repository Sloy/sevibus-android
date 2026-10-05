package com.sloy.sevibus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.Route
import com.sloy.sevibus.domain.model.RouteId
import com.sloy.sevibus.ui.theme.SevTheme

@Composable
fun RouteTabsSelector(route1: Route, route2: Route, selected: RouteId, onRouteClicked: (Route) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        RouteTab(directionValue = route2.destination, route2.id == selected, { onRouteClicked(route2) }, Modifier.weight(1f))
        val arrowRotation = if (route2.id == selected) 180f else 0f
        Icon(
            Icons.AutoMirrored.Default.ArrowForward,
            contentDescription = null,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .rotate(arrowRotation),
            tint = SevTheme.colorScheme.onSurfaceVariant
        )
        RouteTab(directionValue = route1.destination, route1.id == selected, { onRouteClicked(route1) }, Modifier.weight(1f))
    }
}

@Composable
fun RouteTabsSegmented(
    route1: Route,
    route2: Route,
    selected: RouteId,
    onRouteClicked: (Route) -> Unit,
    modifier: Modifier = Modifier
) {
    SegmentedControl(
        segmentCount = 2,
        selectedIndex = if (route1.id == selected) 0 else 1,
        modifier = modifier.fillMaxWidth(),
        onOptionSelected = {
            onRouteClicked(if (it == 0) route1 else route2)
        }
    ){ index ->
        val name = if (index == 0) route1.destination else route2.destination
        Row(Modifier.padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            Icon(Icons.Default.ArrowForward, contentDescription = null)
            Text(name)
        }
    }
}

@Composable
private fun RouteTab(directionValue: String, isSelected: Boolean, onRouteClicked: () -> Unit, modifier: Modifier = Modifier) {
    val colors = if (isSelected) {
        CardDefaults.cardColors()
    } else {
        CardDefaults.cardColors(containerColor = Color.Transparent)
    }
    val directionLabel = when (isSelected) {
        true -> "Destino"
        false -> "Origen"
    }
    Card(colors = colors, modifier = modifier, onClick = onRouteClicked) {
        Column(Modifier.padding(8.dp)) {
            Text(directionLabel, style = SevTheme.typography.bodyExtraSmall, color = SevTheme.colorScheme.onSurfaceVariant)
            Text(directionValue, style = SevTheme.typography.bodyStandardBold)
        }
    }
}


@Preview(showBackground = true)
@Composable
internal fun RouteTabsSelectorPreview() {
    SevTheme {
        Column(Modifier.background(SevTheme.colorScheme.background)) {
            RouteTabsSelector(
                route1 = Stubs.routes[0],
                route2 = Stubs.routes[1],
                selected = Stubs.routes[0].id,
                onRouteClicked = {},
                modifier = Modifier.padding(16.dp),
            )
            RouteTabsSelector(
                route1 = Stubs.routes[0],
                route2 = Stubs.routes[1],
                selected = Stubs.routes[1].id,
                onRouteClicked = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
internal fun RouteTabsSegmentedPreview() {
    SevTheme {
        Column(Modifier.background(SevTheme.colorScheme.background)) {
            RouteTabsSegmented(
                route1 = Stubs.routes[0],
                route2 = Stubs.routes[1],
                selected = Stubs.routes[0].id,
                onRouteClicked = {},
                modifier = Modifier.padding(16.dp),
            )
            RouteTabsSegmented(
                route1 = Stubs.routes[0],
                route2 = Stubs.routes[1],
                selected = Stubs.routes[1].id,
                onRouteClicked = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
