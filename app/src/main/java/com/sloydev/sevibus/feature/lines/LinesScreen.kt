package com.sloydev.sevibus.feature.lines

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloydev.sevibus.R
import com.sloydev.sevibus.Stubs
import com.sloydev.sevibus.ui.SevTopAppBar
import com.sloydev.sevibus.ui.theme.SevTheme

@Composable
fun LinesRoute() {
    //TODO inject viewmodel and subscribe to state
    LinesScreen(LinesState.Content(Stubs.lines))
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

                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Stubs.lineTypes.forEach { lineType ->
                        val lines = state.lines.filter { it.type == lineType }
                        if (lines.isNotEmpty()) {
                            LineTypeTitle(lineType)
                            lines.forEach {
                                LineItem(it)
                            }
                            Spacer(Modifier.size(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LineTypeTitle(lineType: String) {
    Text(lineType, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 8.dp))
}

@Composable
private fun LineItem(it: Line) {
    Card(
        onClick = { /*TODO*/ },
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
    ) {
        ListItem(
            tonalElevation = 8.dp,
            headlineContent = { Text(it.description) },
            //overlineContent = { Text(text = "overline")},
            leadingContent = {
                LineIndicator(it)
            },
        )
    }
}

@Composable
private fun LineIndicator(it: Line) {
    Box(
        Modifier
            //.size(32.dp)
            .clip(MaterialTheme.shapes.small)
            .background(Color(it.colorHex))
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            it.label, color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.bodyLarge
                .copy(fontWeight = FontWeight.ExtraBold)
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun LinesScreenPreview() {
    SevTheme {
        LinesScreen(state = LinesState.Content(Stubs.lines))
    }
}