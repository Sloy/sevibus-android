package com.sloy.sevibus.feature.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.sloy.sevibus.domain.model.Bus
import com.sloy.sevibus.domain.model.toLatLng
import com.sloy.sevibus.ui.icons.busBitmap
import com.sloy.sevibus.ui.theme.SevTheme

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
