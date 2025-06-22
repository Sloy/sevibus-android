package com.sloy.sevibus.feature.login

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditLocationAlt
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sloy.sevibus.feature.debug.LocationDebugModule
import com.sloy.sevibus.infrastructure.BuildVariant

@Composable
fun DebugLocationSetting(modifier: Modifier = Modifier) {
    if (BuildVariant.isRelease()) error("DebugSettings is using debug implementation on release variant")

    if (BuildVariant.isDebug()) {
        val debugLocationState by LocationDebugModule.locationState.collectAsStateWithLifecycle()
        HorizontalDivider(Modifier.padding(horizontal = 16.dp))
        SettingsItem(
            title = "Ubicación falsa",
            subtitle = "Fuerza una ubicación falsa en Sevilla para facilitar el desarrollo",
            leadingIcon = Icons.Outlined.EditLocationAlt,
            onClick = { LocationDebugModule.setFakeLocation(!debugLocationState.isFakeLocation) },
            endComponent = {
                Switch(
                    checked = debugLocationState.isFakeLocation,
                    onCheckedChange = { LocationDebugModule.setFakeLocation(it) },
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        )
    }
}
