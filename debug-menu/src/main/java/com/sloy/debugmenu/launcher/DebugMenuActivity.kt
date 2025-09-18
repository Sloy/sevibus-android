package com.sloy.debugmenu.launcher

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState

class DebugMenuActivity : AppCompatActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme() {
                val sheetState = rememberModalBottomSheetState()
                ModalBottomSheet(
                    sheetState = sheetState,
                    onDismissRequest = {
                        finish()
                    },
                ) {
                    DebugMenuLauncher.menuContainer!!.menu()
                }
            }
        }
    }
}
