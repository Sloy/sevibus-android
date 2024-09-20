package com.sloy.sevibus.feature.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.sloy.sevibus.domain.model.PositionBounds
import com.sloy.sevibus.domain.model.Stop
import com.sloy.sevibus.domain.model.filterInBounds
import com.sloy.sevibus.domain.model.toLatLng
import com.sloy.sevibus.ui.icons.SevIcons
import com.sloy.sevibus.ui.icons.Stop
import com.sloy.sevibus.ui.icons.StopFilled
import com.sloy.sevibus.ui.icons.layeredBitmap
import com.sloy.sevibus.ui.icons.selectedStopBitmap
import com.sloy.sevibus.ui.theme.SevTheme

@Composable
@GoogleMapComposable
fun MapStops(
    visibleStops: Collection<Stop>,
    visibleBounds: PositionBounds?,
    selectedStop: Stop?,
    onStopSelected: (Stop) -> Unit,
    stopColor: Color,
) {
    val stopIconBitmap = layeredBitmap(
        SevIcons.StopFilled to SevTheme.colorScheme.background,
        SevIcons.Stop to stopColor
    )
    val stopIcon = remember { BitmapDescriptorFactory.fromBitmap(stopIconBitmap) }
    val selectedIconBitmap = selectedStopBitmap(stopColor)
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
