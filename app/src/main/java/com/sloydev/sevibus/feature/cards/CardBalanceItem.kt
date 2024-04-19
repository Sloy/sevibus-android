package com.sloydev.sevibus.feature.cards

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import com.sloydev.sevibus.Stubs
import com.sloydev.sevibus.ui.formatter.MoneyFormatter
import com.sloydev.sevibus.ui.theme.SevTheme

@Composable
fun CardBalanceItem(card: CardInfo, modifier: Modifier = Modifier) {
    if (card.balanceMillis == null && card.balanceTrips == null) return
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        if (card.balanceMillis != null) {
            MoneyItem(card.balanceMillis)
        } else if (card.balanceTrips != null) {
            TripsItem(card.balanceTrips)
        }
    }
}

@Composable
private fun MoneyItem(balanceMillis: Int) {
    Text("Saldo disponible", style = MaterialTheme.typography.labelMedium)
    Row(verticalAlignment = Alignment.Bottom) {
        val balance = MoneyFormatter.fromMillis(balanceMillis)
        balance.split(",").let { (part1, part2) ->
            Text(buildAnnotatedString {
                withStyle(style = MaterialTheme.typography.displayMedium.toSpanStyle()) {
                    append(part1)
                }
                withStyle(style = MaterialTheme.typography.displaySmall.toSpanStyle()) {
                    append(",")
                    append(part2)
                }
            })

        }
    }
}

@Composable
private fun TripsItem(balanceTrips: Int) {
    Text("Viajes restantes", style = MaterialTheme.typography.labelMedium)
    Row(verticalAlignment = Alignment.Bottom) {
        Text(balanceTrips.toString(), style = MaterialTheme.typography.displayMedium)
    }
}

@Preview
@Composable
private fun CardBalanceItemPreview() {
    SevTheme {
        Surface {
            Column {
                CardBalanceItem(Stubs.cards[0])
                CardBalanceItem(Stubs.cards[1])

            }
        }
    }
}