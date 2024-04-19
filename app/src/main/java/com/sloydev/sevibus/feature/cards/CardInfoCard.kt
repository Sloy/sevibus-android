package com.sloydev.sevibus.feature.cards

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloydev.sevibus.Stubs
import com.sloydev.sevibus.ui.theme.SevTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CardInfoCard(cardInfo: CardInfo) {
    Card(
        Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        ListItem(
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            overlineContent = { Text("Nombre guardado") },
            headlineContent = { Text("Bono Laura") },
            trailingContent = {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar nombre")
                }
            }
        )
        TitleSubtitleItem("Título", "Saldo sin transbordo")
        TitleSubtitleItem("Nº serie", "794836666")
        TitleSubtitleItem("Caducidad", "8 junio 2024")
        TitleSubtitleItem("Validez", "3 enero 2024")

    }
}

@Composable
private fun TitleSubtitleItem(title: String, subtitle: String) {
    ListItem(
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        overlineContent = { Text(title) },
        headlineContent = { Text(subtitle) },

    )
}

private fun formatDateTime(dateTime: LocalDateTime, locale: Locale): String {
    val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM - HH:mm", locale)
    return dateTime.format(formatter)
}


@Composable
private fun LeadingBox(content: @Composable BoxScope.() -> Unit) {
    Box(Modifier.size(24.dp), contentAlignment = Alignment.CenterStart) {
        content()
    }
}


@Preview
@Composable
private fun CardInfoCardPreview() {
    SevTheme {
        CardInfoCard(Stubs.cards[0])
    }
}