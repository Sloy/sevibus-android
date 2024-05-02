package com.sloydev.sevibus.feature.debug

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditLocationAlt
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloydev.sevibus.ui.preview.ScreenPreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugMenu(state: DebugMenuState, modifier: Modifier = Modifier) {

    val scope = rememberCoroutineScope()

    if (state.isOpen) {
        ModalBottomSheet(
            onDismissRequest = {
                state.closeMenu()
            },
            sheetState = rememberModalBottomSheetState(),
        ) {
            DebugMenuContent(modifier)

        }
    }

}

@Composable
fun DebugMenuContent(modifier: Modifier = Modifier) {
    Column(modifier.padding(16.dp)) {
        ListItem(

            headlineContent = { Text("Fake location in Sevilla") },
            leadingContent = { Icon(Icons.Default.EditLocationAlt, contentDescription = null) },
            trailingContent = { Switch(checked = true, onCheckedChange = {}) }
        )
    }
}

@Preview
@Composable
private fun Preview() {
    ScreenPreview {
        DebugMenuContent()
    }
}