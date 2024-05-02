package com.sloydev.sevibus.feature.debug

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditLocationAlt
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sloydev.sevibus.infrastructure.SevLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class LocationDebugModule : DebugModule {

    companion object {
        private val _locationState = MutableStateFlow(LocationDebugState())
        val locationState = _locationState as StateFlow<LocationDebugState>
    }

    @Composable
    override fun Component() {
        val state by locationState.collectAsState()
        Column(Modifier.padding(8.dp)) {
            Text(
                "Location",
                Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleLarge
            )
            Card {
                ListItem(
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    headlineContent = { Text("Fake location") },
                    leadingContent = {
                        Icon(
                            Icons.Default.EditLocationAlt,
                            contentDescription = null
                        )
                    },
                    trailingContent = {
                        Switch(checked = state.isFakeLocation, onCheckedChange = { checked ->
                            SevLogger.logD("onCheckedChange = $checked")
                            _locationState.update { it.copy(isFakeLocation = checked) }
                        })
                    }
                )
            }
        }
    }
}
