package com.sloydev.sevibus.feature.cards

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloydev.sevibus.Stubs
import com.sloydev.sevibus.domain.model.TravelCard
import com.sloydev.sevibus.ui.formatter.DateFormatter
import com.sloydev.sevibus.ui.theme.SevTheme

@Composable
fun TravelCardInfoElement(card: TravelCard) {
    Card(
        Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        ListItem(
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            overlineContent = { Text("Nombre guardado") },
            headlineContent = { Text(card.customName ?: "") },
            trailingContent = {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar nombre")
                }
            }
        )
        TitleSubtitleItem("Título", card.title)
        card.expiration?.let { date ->
            TitleSubtitleItem("Caducidad", DateFormatter.dayMonthYear(date))
        }
        card.validityEnd?.let { date ->
            TitleSubtitleItem("Validez", DateFormatter.dayMonthYear(date))

        }
        card.extensionEnd?.let { date ->
            TitleSubtitleItem("Fin ampliación", DateFormatter.dayMonthYear(date))

        }
        TitleSubtitleItem("Nº serie", card.serialNumber.toString())

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

@Preview
@Composable
private fun CardInfoCardPreview() {
    SevTheme {
        TravelCardInfoElement(Stubs.cardWithAllFields)
    }
}