package com.sloydev.sevibus.feature.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloydev.sevibus.Stubs
import com.sloydev.sevibus.domain.model.Line
import com.sloydev.sevibus.ui.components.LineIndicatorMedium
import com.sloydev.sevibus.ui.components.LineIndicatorSmall
import com.sloydev.sevibus.ui.icons.DirectionsBusFill
import com.sloydev.sevibus.ui.icons.SevIcons
import com.sloydev.sevibus.ui.theme.AlexGreyIcons
import com.sloydev.sevibus.ui.theme.SevTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun LineSelectorWidget(onLineSelected: (Line) -> Unit, modifier: Modifier = Modifier) {
    val viewModel: LineSelectorViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    LineSelectorWidget(state, onLineSelected = {
        viewModel.onSelectLine(it)
        onLineSelected(it)
    }, modifier)
}

@Composable
private fun LineSelectorWidget(
    state: LineSelectorState,
    onLineSelected: (Line) -> Unit,
    modifier: Modifier = Modifier,
    defaultExpanded: Boolean = false
) {
    Box(modifier = modifier) {
        var dropdownExpanded by remember { mutableStateOf(defaultExpanded) }

        DropdownMenu(
            expanded = dropdownExpanded,
            onDismissRequest = { dropdownExpanded = false }
        ) {
            state.lines.forEach { line ->
                DropdownMenuItem(
                    text = { Text(line.description) },
                    onClick = {
                        onLineSelected(line)
                        dropdownExpanded = false
                    },
                    leadingIcon = {
                        LineIndicatorMedium(line = line)
                    })
            }
        }

        Card(
            onClick = { dropdownExpanded = true },
            elevation = CardDefaults.cardElevation(8.dp),
            shape = MaterialTheme.shapes.extraSmall,
        ) {
            Row(
                Modifier.padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state.lineSelected == null) {
                    NoLineIndicator()
                    Text("Todas las líneas", fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = AlexGreyIcons)
                } else {
                    LineIndicatorSmall(line = state.lineSelected)
                    Text(
                        state.lineSelected.description,
                        fontWeight = FontWeight.Bold,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = AlexGreyIcons)
                }
            }
        }
    }
}

@Composable
fun LineDropdownMenu() {

}

@Composable
fun NoLineIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .background(MaterialTheme.colorScheme.primary)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(SevIcons.DirectionsBusFill, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
    }
}

@Preview
@Composable
private fun Preview() {
    SevTheme {
        Surface {
            Column(Modifier.padding(32.dp)) {
                LineSelectorWidget(LineSelectorState(Stubs.lines, null), {})
                Spacer(Modifier.size(32.dp))
                LineSelectorWidget(LineSelectorState(Stubs.lines, Stubs.lines[2]), {})
            }
        }
    }
}