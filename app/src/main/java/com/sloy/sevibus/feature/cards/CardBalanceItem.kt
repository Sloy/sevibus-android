package com.sloy.sevibus.feature.cards

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.CardInfo
import com.sloy.sevibus.ui.formatter.MoneyFormatter
import com.sloy.sevibus.ui.theme.SevTheme

@Composable
fun CardBalanceItem(card: CardInfo, modifier: Modifier = Modifier) {
    if (card.balance == null) return
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        MoneyItem(card.balance)
    }
}

@Composable
private fun ColumnScope.MoneyItem(balance: Int) {
    Text("Saldo disponible", style = SevTheme.typography.bodySmallBold)
    Row(verticalAlignment = Alignment.Bottom) {
        FormattedBalance(balance)
    }
}

@Composable
private fun ColumnScope.TripsItem(balanceTrips: Int) {
    Text("Viajes restantes", style = SevTheme.typography.bodySmallBold)
    Row(verticalAlignment = Alignment.Bottom) {
        Text(balanceTrips.toString(), style = SevTheme.typography.bodyStandard.copy(fontSize = 36.sp))
    }
}

@Composable
private fun FormattedBalance(balance: Int) {
    val balanceText = MoneyFormatter.fromCents(balance)
    val textColor = if (balance <= 0) Color.Red else Color.Unspecified
    balanceText.split(",").let { (part1, part2) ->
        Text(buildAnnotatedString {
            withStyle(style = SevTheme.typography.bodyStandard.copy(fontSize = 45.sp).toSpanStyle()) {
                append(part1)
            }
            withStyle(style = SevTheme.typography.bodyStandard.copy(fontSize = 36.sp).toSpanStyle()) {
                append(",")
                append(part2)
            }
        }, color = textColor)

    }
}

@Preview
@Composable
private fun CardBalanceItemPreview() {
    SevTheme {
        Surface {
            Column {
                CardBalanceItem(Stubs.cards[0])
                Spacer(Modifier.size(64.dp))
                CardBalanceItem(Stubs.cards[1])

            }
        }
    }
}
