package com.sloydev.sevibus.feature.foryou

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloydev.sevibus.Stubs
import com.sloydev.sevibus.domain.Line
import com.sloydev.sevibus.ui.components.LineIndicatorSmall
import com.sloydev.sevibus.ui.theme.SevTheme
import kotlin.random.Random


@Composable
fun FavoritesCard(onStopClicked: (code: Int) -> Unit) {
    Card(
        Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Column {
            Text(
                "Favoritas", style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp)
            )

            FavoriteListItem(42, "Escuela", "Reina Mercedes - ETSII", Stubs.lines.take(2), onStopClicked)
            FavoriteListItem(238, "Casa", "San Juan de Ribera (Macarena)", Stubs.lines.slice(3..5), onStopClicked)
        }
    }
}

@Composable
private fun FavoriteListItem(code: Int, name: String, description: String, lines: List<Line>, onStopClicked: (code: Int) -> Unit) {
    ListItem(
        modifier = Modifier.clickable { onStopClicked(code) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        headlineContent = { Text(name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingContent = { Text(description, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leadingContent = {
            Box(
                Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    code.toString(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodyLarge
                        .copy(fontWeight = FontWeight.Medium)
                )
            }
        },
        trailingContent = {
            Row {
                lines.forEach {
                    Spacer(Modifier.size(6.dp))
                    FavoriteLineTime(it)
                }
            }
        }
    )
}

@Composable
private fun FavoriteLineTime(line: Line) {
    val minutes = remember { Random.nextInt(1, 25) }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        LineIndicatorSmall(line)
        Text("$minutes min", modifier = Modifier.padding(top = 4.dp))
    }
}

@Preview
@Composable
private fun FavoritesCardPreview() {
    SevTheme {
        FavoritesCard({})
    }
}