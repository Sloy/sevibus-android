package com.sloy.sevibus.feature.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.sloy.sevibus.domain.model.Path
import com.sloy.sevibus.domain.model.Stop
import com.sloy.sevibus.domain.model.moveToPath
import com.sloy.sevibus.domain.model.toLatLng
import com.sloy.sevibus.ui.icons.SevIcons
import com.sloy.sevibus.ui.icons.Stop
import com.sloy.sevibus.ui.icons.layeredBitmap
import com.sloy.sevibus.ui.icons.selectedStopBitmap
import com.sloy.sevibus.ui.theme.SevTheme
import myiconpack.StopOutline

@Composable
@GoogleMapComposable
fun MapLineStops(
    lineStops: List<Stop>,
    selectedStop: Stop?,
    path: Path?,
    onStopSelected: (Stop) -> Unit
) {
    val stopIconBitmap = layeredBitmap(
        SevIcons.StopOutline to SevTheme.colorScheme.background,
        SevIcons.Stop to SevTheme.colorScheme.primary
    )
    val stopIcon = remember(stopIconBitmap) { BitmapDescriptorFactory.fromBitmap(stopIconBitmap) }
    val selectedIconBitmap = selectedStopBitmap(SevTheme.colorScheme.primary)
    val selectedIcon = remember(selectedIconBitmap) { BitmapDescriptorFactory.fromBitmap(selectedIconBitmap) }

    lineStops
        .moveStopsToPathConditionally(path)
        .forEach { stop ->
            Marker(
                state = MarkerState(position = stop.position.toLatLng()),
                anchor = Offset(0.5f, 0.8f),
                onClick = {
                    onStopSelected(stop)
                    false
                },
                icon = if (stop.code != selectedStop?.code) stopIcon else selectedIcon,
                zIndex = if (stop.code != selectedStop?.code) 0.1f else 1f,
            )
        }
}

private fun List<Stop>.moveStopsToPathConditionally(path: Path?): List<Stop> {
    if (path == null) return this
    return this.map { stop -> stop.copy(position = stop.position.moveToPath(path)) }
}
