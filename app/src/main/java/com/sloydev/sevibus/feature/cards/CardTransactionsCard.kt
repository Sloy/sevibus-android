package com.sloydev.sevibus.feature.cards

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Money
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloydev.sevibus.R
import com.sloydev.sevibus.Stubs
import com.sloydev.sevibus.ui.components.LineIndicatorSmall
import com.sloydev.sevibus.ui.theme.SevTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CardTransactionsCard(cardTransactions: List<CardTransaction>) {
    Card(
        Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        LazyColumn {
            item {
                Text(
                    "Actividad reciente", style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp)
                )
            }
            itemsIndexed(cardTransactions) { index, transaction ->
                when(transaction){
                    is CardTransaction.TopUp -> TopUpItem(transaction)
                    is CardTransaction.Validation -> ValidationItem(transaction)
                }
                if(index != cardTransactions.lastIndex){
                    HorizontalDivider()
                }
            }
        }
    }
}

private fun formatDateTime(dateTime: LocalDateTime, locale: Locale): String {
    val formatter = DateTimeFormatter.ofPattern("EEEE d MMMM, HH:mm", locale)
    return dateTime.format(formatter)
}

@Composable
private fun ValidationItem(transaction: CardTransaction.Validation) {
    ListItem(
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        leadingContent = { LeadingBox { LineIndicatorSmall(transaction.line) }},
        headlineContent = {
            Row {
                Text("Validación de viaje")
            }
        },
        overlineContent = { Text(formatDateTime(transaction.date, Locale.getDefault())) },
        trailingContent = {
            Text(transaction.formattedAmount(), style = MaterialTheme.typography.labelLarge)
        }
    )
}

@Composable
private fun TopUpItem(transaction: CardTransaction.TopUp) {
    ListItem(
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        leadingContent = { LeadingBox{
            Icon(painterResource(R.drawable.baseline_euro_24), contentDescription = null)
        } },
        headlineContent = {
            Row {
                Text("Recarga")
            }
        },
        overlineContent = { Text(formatDateTime(transaction.date, Locale.getDefault())) },
        trailingContent = {
            Text(transaction.formattedAmount(), style = MaterialTheme.typography.labelLarge)
        }
    )
}

@Composable
private fun LeadingBox(content: @Composable BoxScope.()->Unit) {
    Box(Modifier.size(24.dp), contentAlignment = Alignment.CenterStart){
        content()
    }
}


@Preview
@Composable
private fun CardTransactionsCardPreview() {
    SevTheme {
        CardTransactionsCard(Stubs.cardTransactions)
    }
}