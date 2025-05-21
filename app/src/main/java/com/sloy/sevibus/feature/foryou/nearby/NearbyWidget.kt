package com.sloy.sevibus.feature.foryou.nearby

import android.Manifest
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.sloy.sevibus.R
import com.sloy.sevibus.Stubs
import com.sloy.sevibus.feature.foryou.favorites.FavoriteListItemShimmer
import com.sloy.sevibus.infrastructure.extensions.rememberPermissionStateOnUI
import com.sloy.sevibus.ui.components.SurfaceButton
import com.sloy.sevibus.ui.preview.ScreenPreview
import com.sloy.sevibus.ui.theme.SevTheme
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NearbyWidget(onStopClicked: (code: Int) -> Unit) {
    val permissionState = rememberPermissionStateOnUI(Manifest.permission.ACCESS_FINE_LOCATION)
    val hasPermission = permissionState?.status?.isGranted == true
    NearbyWidget(onStopClicked, hasPermission, onPermissionButton = {
        permissionState?.launchPermissionRequest()
    })
}

@Composable
fun NearbyWidget(onStopClicked: (code: Int) -> Unit, hasPermission: Boolean, onPermissionButton: () -> Unit) {
    if (hasPermission) {
        NearbyWidgetHasPermission(onStopClicked)
    } else {
        NearbyWidgetNoPermission(onPermissionButton)
    }
}

@Composable
private fun NearbyWidgetHasPermission(onStopClicked: (code: Int) -> Unit) {
    if (!LocalView.current.isInEditMode) {
        val viewModel = koinViewModel<NearbyViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        NearbyWidgetHasPermission(state, onStopClicked)
    } else {
        NearbyWidgetHasPermission(NearbyScreenState.Content(Stubs.nearby), onStopClicked)
    }
}

@Composable
private fun NearbyWidgetHasPermission(
    state: NearbyScreenState,
    onStopClicked: (code: Int) -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        when (state) {
            is NearbyScreenState.Loading -> {
                repeat(3) {
                    FavoriteListItemShimmer(Modifier.padding(end = 64.dp))
                    Spacer(Modifier.height(16.dp))
                }
            }

            is NearbyScreenState.Content -> {
                if (state.stops.isEmpty()) {
                    NearbyEmptyState("No hay paradas cercanas a tu posición.\n¿Tú estás en Sevilla ni ná?")
                } else {
                    state.stops.forEach { stop ->
                        NearbyListItem(stop, onStopClicked, Modifier.padding(horizontal = 16.dp))
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }

}

@Composable
private fun NearbyWidgetNoPermission(onPermissionButton: () -> Unit, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        NearbyEmptyState("Activa los servicios de localización para ver las paradas más cercanas a tu posición.")
        Spacer(Modifier.height(16.dp))
        SurfaceButton("Activar localización", icon = {
            Icon(
                tint = SevTheme.colorScheme.primary,
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = "Location icon",
            )
        }, onClick = onPermissionButton)
    }
}

@Composable
private fun NearbyEmptyState(message: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(24.dp))
        Image(
            painter = painterResource(id = R.drawable.illustration_nearby_stop),
            contentDescription = "Drawing of a stop with a location pin",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .height(90.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text("Paradas cercanas", style = SevTheme.typography.headingStandard)
        Spacer(Modifier.height(8.dp))
        Text(
            message,
            style = SevTheme.typography.bodyStandard,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Preview
@Composable
private fun WithArrivalsPreview() {
    ScreenPreview {
        NearbyWidgetHasPermission(NearbyScreenState.Content(Stubs.nearby), {})
    }
}

@Preview
@Composable
private fun EmptyPreview() {
    ScreenPreview {
        NearbyWidgetHasPermission(NearbyScreenState.Content(emptyList()), {})
    }
}

@Preview
@Composable
private fun LoadingPreview() {
    ScreenPreview {
        NearbyWidgetHasPermission(NearbyScreenState.Loading, {})
    }
}

@Preview
@Composable
private fun NoPemissionPreview() {
    ScreenPreview {
        NearbyWidgetNoPermission({})
    }
}
