package com.sloydev.sevibus.feature.lines

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.sloydev.sevibus.R
import com.sloydev.sevibus.ui.SevTopAppBar
import com.sloydev.sevibus.ui.theme.SevibusTheme

@Composable
fun LinesRoute() {
    //TODO inject viewmodel and subscribe to state
    LinesScreen(LinesState.Content(listOf("01", "02", "05")))
}

@Composable
private fun LinesScreen(state: LinesState) {
    when (state) {
        is LinesState.Loading -> {
            CircularProgressIndicator()
        }

        is LinesState.Content -> {
            Column {
                SevTopAppBar(titleRes = R.string.navigation_lines)
                Text("Hello Lines")
                state.lines.forEach {
                    Text(it)
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun LinesScreenPreview() {
    SevibusTheme {
        LinesScreen(state = LinesState.Content(listOf("01", "02", "05")))
    }
}