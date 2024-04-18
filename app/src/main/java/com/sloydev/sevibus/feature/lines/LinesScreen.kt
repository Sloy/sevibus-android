package com.sloydev.sevibus.feature.lines

import android.graphics.ColorMatrix
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloydev.sevibus.R
import com.sloydev.sevibus.ui.SevTopAppBar
import com.sloydev.sevibus.ui.theme.SevTheme

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

                repeat(3) {
                    Text("Transversales", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 8.dp))
                    state.lines.forEach {
                        Card(modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)) {
                            ListItem(
                                headlineContent = { Text("Plg. Norte H. Virgen del Rocio") },
                                //overlineContent = { Text(text = "overline")},
                                leadingContent = {
                                    Box(
                                        Modifier
                                            .size(32.dp)
                                            .clip(MaterialTheme.shapes.small)
                                            .background(Color(0xfff54129)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            it, color = MaterialTheme.colorScheme.onPrimary,
                                            style = MaterialTheme.typography.bodyLarge
                                                .copy(fontWeight = FontWeight.ExtraBold)
                                        )
                                    }
                                },
                                tonalElevation = 8.dp
                            )
                        }
                    }
                    Spacer(Modifier.size(8.dp))
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun LinesScreenPreview() {
    SevTheme {
        LinesScreen(state = LinesState.Content(listOf("01", "02", "05")))
    }
}