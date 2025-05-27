package com.sloy.sevibus.feature.login

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloy.sevibus.ui.nightmode.NightModeSetting
import com.sloy.sevibus.ui.theme.SevTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSelectorBottomSheet(
    sheetState: SheetState,
    currentMode: NightModeSetting,
    onDismissRequest: () -> Unit,
    onModeSelected: (NightModeSetting) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(Modifier.padding(bottom = 24.dp)) {
            Text(
                "Modo oscuro automático",
                style = SevTheme.typography.headingSmall,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )
            NightModeSetting.entries.forEach { mode ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onModeSelected(mode) }
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    RadioButton(
                        selected = currentMode == mode,
                        onClick = { onModeSelected(mode) }
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        mode.title,
                        style = SevTheme.typography.bodyStandardBold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview(showSystemUi = true)
@Composable
private fun Preview() {
    SevTheme {
        Scaffold() { _ ->
            ThemeSelectorBottomSheet(
                sheetState = rememberModalBottomSheetState(),
                currentMode = NightModeSetting.FOLLOW_SYSTEM,
                onDismissRequest = {},
                onModeSelected = {}
            )

        }
    }
}