@file:OptIn(ExperimentalMaterial3Api::class)

package com.sloy.sevibus.infrastructure.nightmode

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
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.sloy.sevibus.R
import com.sloy.sevibus.ui.theme.SevTheme

@Composable
fun NightModeSelectorBottomSheet(
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
                stringResource(R.string.settings_night_mode_auto),
                style = SevTheme.typography.headingSmall,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
            )
            NightModeSetting.entries.forEach { mode ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onModeSelected(mode) }
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    RadioButton(
                        selected = currentMode == mode,
                        onClick = { onModeSelected(mode) }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(mode.titleRes),
                        style = SevTheme.typography.bodyStandard
                    )
                }
            }
        }
    }
}

@Preview
@Composable
internal fun Preview() {
    SevTheme {
        NightModeSelectorBottomSheet(
            sheetState = rememberStandardBottomSheetState(
                initialValue = SheetValue.Expanded
            ),
            currentMode = NightModeSetting.FOLLOW_SYSTEM,
            onDismissRequest = {},
            onModeSelected = {}
        )
    }
}
