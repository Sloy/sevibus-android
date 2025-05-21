package com.sloy.sevibus.feature.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditLocationAlt
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sloy.sevibus.feature.debug.LocationDebugModule
import com.sloy.sevibus.infrastructure.BuildVariant
import com.sloy.sevibus.ui.theme.SevTheme

@Composable
fun DebugLocationSetting(modifier: Modifier = Modifier) {
    if (BuildVariant.isRelease()) error("DebugSettings is using debug implementation on release variant")

    if (BuildVariant.isDebug()) {
        HorizontalDivider(Modifier.padding(horizontal = 16.dp))
        //For debugging:
        val debugLocationState by LocationDebugModule.locationState.collectAsStateWithLifecycle()

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
            .clickable {
                LocationDebugModule.setFakeLocation(!debugLocationState.isFakeLocation)
            }
            .padding(16.dp)) {
            Icon(
                Icons.Outlined.EditLocationAlt,
                contentDescription = null,
                tint = SevTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(24.dp)
            )
            Column(Modifier.weight(1f)) {
                Text("Ubicación falsa \uD83D\uDC1E", style = SevTheme.typography.bodyStandardBold)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Fuerza una ubicación falsa en Sevilla para facilitar el desarrollo",
                    style = SevTheme.typography.bodySmall,
                    color = SevTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = debugLocationState.isFakeLocation,
                onCheckedChange = { LocationDebugModule.setFakeLocation(it) },
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
