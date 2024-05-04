package com.sloydev.sevibus.feature.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.sloydev.sevibus.domain.model.PositionBounds
import com.sloydev.sevibus.domain.model.Stop
import com.sloydev.sevibus.domain.model.filterInBounds
import com.sloydev.sevibus.domain.model.toLatLng
import com.sloydev.sevibus.ui.icons.SevIcons
import com.sloydev.sevibus.ui.icons.Stop
import com.sloydev.sevibus.ui.icons.StopFilled
import com.sloydev.sevibus.ui.icons.layeredBitmap
import com.sloydev.sevibus.ui.icons.selectedStopBitmap
import com.sloydev.sevibus.ui.theme.SevTheme

@Composable
@GoogleMapComposable
fun MapStops(
    visibleStops: List<Stop>,
    visibleBounds: PositionBounds?,
    selectedStop: Stop?,
    onStopSelected: (Stop) -> Unit,
) {
    val stopIconBitmap = layeredBitmap(
        SevIcons.StopFilled to SevTheme.colorScheme.background,
        SevIcons.Stop to SevTheme.colorScheme.primary
    )
    val stopIcon = remember { BitmapDescriptorFactory.fromBitmap(stopIconBitmap) }
    val selectedIconBitmap = selectedStopBitmap(SevTheme.colorScheme.primary)
    val selectedIcon = remember { BitmapDescriptorFactory.fromBitmap(selectedIconBitmap) }

    visibleStops
        .filterInBounds(visibleBounds)
        .forEach { stop ->
            Marker(
                state = MarkerState(position = stop.position.toLatLng()),
                anchor = Offset(0.5f, 0.8f),
                onClick = {
                    onStopSelected(stop)
                    false
                },
                icon = if (stop != selectedStop) stopIcon else selectedIcon,
                zIndex = if (stop != selectedStop) 0.1f else 1f,
            )
        }
}
