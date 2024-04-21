package com.sloydev.sevibus.feature.cards

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloydev.sevibus.R
import com.sloydev.sevibus.Stubs
import com.sloydev.sevibus.domain.model.TravelCard
import com.sloydev.sevibus.ui.components.LeadingBoxLayout
import com.sloydev.sevibus.ui.components.LineIndicatorSmall
import com.sloydev.sevibus.ui.formatter.DateFormatter
import com.sloydev.sevibus.ui.formatter.MoneyFormatter
import com.sloydev.sevibus.ui.theme.SevTheme

@Composable
fun TravelCardTransactionsElement(travelCardTransactions: List<TravelCard.Transaction>) {
    Card(
        Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        Column {
            Text(
                "Actividad reciente", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 16.dp, top = 16.dp)
            )
        }
        travelCardTransactions.forEachIndexed { index, transaction ->
            when (transaction) {
                is TravelCard.Transaction.TopUp -> TopUpItem(transaction)
                is TravelCard.Transaction.Trip -> ValidationItem(transaction)
            }
            if (index != travelCardTransactions.lastIndex) {
                HorizontalDivider()
            }
        }
    }
}


@Composable
private fun ValidationItem(transaction: TravelCard.Transaction.Trip) {
    ListItem(
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        leadingContent = { LeadingBoxLayout(24.dp) { LineIndicatorSmall(transaction.line) } },
        headlineContent = {
            Row {
                Text("Viaje")
            }
        },
        supportingContent = {
            Text(DateFormatter.dayMonthTime(transaction.date))
        },
        trailingContent = {
            Text(
                MoneyFormatter.fromMillis(transaction.amountMillis, showPlusWhenPositive = true),
                style = MaterialTheme.typography.labelLarge
            )
        },
    )
}

@Composable
private fun TopUpItem(transaction: TravelCard.Transaction.TopUp) {
    ListItem(colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        leadingContent = {
            LeadingBoxLayout(24.dp) {
                Icon(painterResource(R.drawable.baseline_euro_24), contentDescription = null)
            }
        },
        headlineContent = {
            Row {
                Text("Recarga")
            }
        },
        supportingContent = {
            Text(DateFormatter.dayMonthTime(transaction.date))
        },
        trailingContent = {
            Text(
                MoneyFormatter.fromMillis(transaction.amountMillis, showPlusWhenPositive = true),
                style = MaterialTheme.typography.labelLarge
            )
        })
}



@Preview
@Composable
private fun CardTransactionsCardPreview() {
    SevTheme {
        TravelCardTransactionsElement(Stubs.travelCardTransactions)
    }
}