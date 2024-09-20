package com.sloy.sevibus.feature.debug

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloy.sevibus.ui.preview.ScreenPreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugMenu(state: DebugMenuState, modules: List<DebugModule>, modifier: Modifier = Modifier) {
    if (state.isOpen) {
        ModalBottomSheet(
            onDismissRequest = {
                state.closeMenu()
            },
        ) {
            DebugMenuContent(modules, modifier)
        }
    }

}

@Composable
private fun DebugMenuContent(modules: List<DebugModule>, modifier: Modifier = Modifier) {
    Column(modifier.padding(8.dp)) {
        modules.forEach {
            it.Component()
        }
    }
}

@Preview
@Composable
private fun Preview() {
    ScreenPreview {
        DebugMenuContent(
            modules = listOf(
                LocationDebugModule()
            )
        )
    }
}
