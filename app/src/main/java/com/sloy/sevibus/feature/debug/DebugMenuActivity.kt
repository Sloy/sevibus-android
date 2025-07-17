package com.n26.debug.presentation

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import com.sloy.sevibus.feature.debug.SevDebugMenu
import com.sloy.sevibus.ui.theme.SevTheme

class DebugMenuActivity : AppCompatActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SevTheme() {
                val sheetState = rememberModalBottomSheetState()
                LaunchedEffect(Unit) {
                sheetState.expand()
                }
                ModalBottomSheet(
                    sheetState = sheetState,
                    onDismissRequest = {
                        finish()
                    },
                    //containerColor = NxdTheme.colors.background.neutral,
                ) {
                    SevDebugMenu()
                }
            }
        }
    }
}
