package com.sloydev.sevibus.feature.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.sloydev.sevibus.domain.model.Bus
import com.sloydev.sevibus.domain.model.toLatLng
import com.sloydev.sevibus.ui.icons.busBitmap
import com.sloydev.sevibus.ui.theme.SevTheme

@Composable
@GoogleMapComposable
fun MapBuses(buses: List<Bus>) {
    val busIconBitmap = busBitmap(SevTheme.colorScheme.primary)
    val busIcon = remember(busIconBitmap) { BitmapDescriptorFactory.fromBitmap(busIconBitmap) }

    buses.forEach { bus ->
        Marker(
            state = MarkerState(position = bus.position.toLatLng()),
            onClick = { true },
            anchor = Offset(0.5f, 0.5f),
            icon = busIcon,
            zIndex = 2f,
        )
    }
}
