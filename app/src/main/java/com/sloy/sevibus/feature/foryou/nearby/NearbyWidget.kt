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
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.sloy.sevibus.R
import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.StopId
import com.sloy.sevibus.feature.foryou.rememberArrivalsDisplayReporter
import com.sloy.sevibus.feature.foryou.favorites.FavoriteListItemShimmer
import com.sloy.sevibus.infrastructure.analytics.events.Clicks
import com.sloy.sevibus.infrastructure.analytics.events.Events
import com.sloy.sevibus.infrastructure.analytics.events.toPermissionResult
import com.sloy.sevibus.infrastructure.extensions.rememberPermissionStateOnUI
import com.sloy.sevibus.ui.components.SurfaceButton
import com.sloy.sevibus.ui.preview.ScreenPreview
import com.sloy.sevibus.ui.preview.ScreenshotSuite
import com.sloy.sevibus.ui.preview.ScreenshotTest
import com.sloy.sevibus.ui.theme.SevTheme
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NearbyWidget(isShown: Boolean, onStopClicked: (code: Int) -> Unit) {
    if (!LocalView.current.isInEditMode) {
        val viewModel = koinViewModel<NearbyViewModel>()
        val permissionState = rememberPermissionStateOnUI(Manifest.permission.ACCESS_FINE_LOCATION) { isGranted ->
            viewModel.onTrack(Events.LocationPermissionResult(isGranted.toPermissionResult(), Events.LocationPermissionResult.Context.NEARBY))
        }
        val hasPermission = permissionState?.status?.isGranted == true
        NearbyWidget(isShown, onStopClicked, hasPermission, onPermissionButton = {
            viewModel.onTrack(Clicks.NearbyStopsLocationPermissionClicked)
            permissionState?.launchPermissionRequest()
        })
    } else {
        val permissionState = rememberPermissionStateOnUI(Manifest.permission.ACCESS_FINE_LOCATION)
        NearbyWidget(isShown, onStopClicked, permissionState?.status?.isGranted == true, onPermissionButton = {
            permissionState?.launchPermissionRequest()
        })
    }
}

@Composable
fun NearbyWidget(isShown: Boolean, onStopClicked: (code: Int) -> Unit, hasPermission: Boolean, onPermissionButton: () -> Unit) {
    if (hasPermission) {
        NearbyWidgetHasPermission(isShown, onStopClicked)
    } else {
        NearbyWidgetNoPermission(onPermissionButton)
    }
}

@Composable
private fun NearbyWidgetHasPermission(isShown: Boolean, onStopClicked: (code: Int) -> Unit) {
    if (!LocalView.current.isInEditMode) {
        val viewModel = koinViewModel<NearbyViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val stopIds = (state as? NearbyScreenState.Content)?.stops.orEmpty().map { it.stop.code }
        val onArrivalsChanged = rememberArrivalsDisplayReporter(Events.ArrivalsScreen.NEARBY, stopIds, isShown, viewModel::onTrack)
        NearbyWidgetHasPermission(
            state = state,
            onArrivalsChanged = onArrivalsChanged,
            onStopClicked = { stopId ->
                viewModel.onTrack(Clicks.NearbyStopClicked(stopId))
                onStopClicked(stopId)
            }
        )
    } else {
        NearbyWidgetHasPermission(NearbyScreenState.Content(Stubs.nearby), { _, _ -> }, onStopClicked)
    }
}

@Composable
private fun NearbyWidgetHasPermission(
    state: NearbyScreenState,
    onArrivalsChanged: (StopId, Int?) -> Unit,
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
                    NearbyEmptyState(stringResource(R.string.foryou_nearby_no_stops))
                } else {
                    state.stops.forEach { stop ->
                        NearbyListItem(stop, onStopClicked, onArrivalsChanged, Modifier.padding(horizontal = 16.dp))
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
        NearbyEmptyState(stringResource(R.string.foryou_location_permission_message))
        Spacer(Modifier.height(16.dp))
        SurfaceButton(stringResource(R.string.foryou_activate_location), icon = {
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
        Text(stringResource(R.string.foryou_nearby_header), style = SevTheme.typography.headingStandard)
        Spacer(Modifier.height(8.dp))
        Text(
            message,
            style = SevTheme.typography.bodyStandard,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@ScreenshotTest(ScreenshotSuite.Components)
@Preview
@Composable
internal fun NearbyWidgetWithArrivalsPreview() {
    ScreenPreview {
        NearbyWidgetHasPermission(NearbyScreenState.Content(Stubs.nearby), { _, _ -> }, {})
    }
}

@ScreenshotTest(ScreenshotSuite.Components)
@Preview
@Composable
internal fun NearbyWidgetEmptyPreview() {
    ScreenPreview {
        NearbyWidgetHasPermission(NearbyScreenState.Content(emptyList()), { _, _ -> }, {})
    }
}

@Preview
@Composable
internal fun NearbyWidgetLoadingPreview() {
    ScreenPreview {
        NearbyWidgetHasPermission(NearbyScreenState.Loading, { _, _ -> }, {})
    }
}

@ScreenshotTest(ScreenshotSuite.Components)
@Preview
@Composable
internal fun NearbyWidgetNoPermissionPreview() {
    ScreenPreview {
        NearbyWidgetNoPermission({})
    }
}
