package com.sloy.sevibus.feature.cards

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.sloy.sevibus.R
import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.CardTransaction
import com.sloy.sevibus.ui.components.LineIndicator
import com.sloy.sevibus.ui.formatter.DateFormatter
import com.sloy.sevibus.ui.formatter.MoneyFormatter
import com.sloy.sevibus.ui.preview.ScreenPreview
import com.sloy.sevibus.ui.shimmer.Shimmer
import com.sloy.sevibus.ui.theme.SevTheme

@Composable
fun CardTransactionsElement(cardInfoTransactions: List<CardTransaction>) {
    Card(
        Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        cardInfoTransactions.forEachIndexed { index, transaction ->
            when (transaction) {
                is CardTransaction.TopUp -> TopUpItem(transaction)
                is CardTransaction.Validation -> ValidationItem(transaction)
                is CardTransaction.Transfer -> TransferItem(transaction)
            }
            if (index != cardInfoTransactions.lastIndex) {
                HorizontalDivider(Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}


@Composable
fun CardTransactionsShimmer() {
    Card(
        Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        TitleSubtitleShimmer()
        HorizontalDivider(Modifier.padding(horizontal = 16.dp))
        TitleSubtitleShimmer()
        HorizontalDivider(Modifier.padding(horizontal = 16.dp))
        TitleSubtitleShimmer()
    }
}


@Composable
private fun TitleSubtitleItem(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    startAccessory: @Composable (() -> Unit)? = null,
    endAccessory: @Composable (() -> Unit)? = null,
) {
    val clickModifier = if (onClick != null) modifier.clickable(onClick = onClick, role = Role.Button) else modifier
    Row(clickModifier, verticalAlignment = Alignment.CenterVertically) {
        if (startAccessory != null) {
            Box(Modifier.padding(start = 16.dp)) {
                CompositionLocalProvider(LocalContentColor provides SevTheme.colorScheme.onSurfaceVariant) {
                    startAccessory()
                }
            }

        }
        Column(
            Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            Text(title, style = SevTheme.typography.bodySmallBold)
            Text(
                subtitle,
                style = SevTheme.typography.bodyStandard,
                color = SevTheme.colorScheme.onSurfaceVariant
            )
        }
        if (endAccessory != null) {
            Box(Modifier.padding(horizontal = 16.dp)) {
                CompositionLocalProvider(LocalContentColor provides SevTheme.colorScheme.onSurfaceVariant) {
                    endAccessory()
                }
            }

        }
    }
}

@Composable
private fun TitleSubtitleShimmer() {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(64.dp)) {
        Shimmer(
            Modifier
                .padding(start = 16.dp)
                .size(24.dp)
        )

        Column(
            Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            Shimmer(
                Modifier
                    .size(width = 100.dp, height = 20.dp)
                    .padding(bottom = 8.dp)
            )
            Shimmer(Modifier.size(width = 150.dp, height = 16.dp))
        }
        Shimmer(
            Modifier
                .padding(horizontal = 16.dp)
                .size(24.dp)
        )
    }
}


@Composable
private fun ValidationItem(transaction: CardTransaction.Validation) {
    TitleSubtitleItem(stringResource(R.string.cards_validation), DateFormatter.dayMonthTime(transaction.date),
        startAccessory = {
            LineIndicator(transaction.line)
        },
        endAccessory = {
            Text(
                MoneyFormatter.fromCents(transaction.amount * -1, showPlusWhenPositive = true),
                style = SevTheme.typography.bodySmallBold
            )
        }
    )
}

@Composable
private fun TransferItem(transaction: CardTransaction.Transfer) {
    TitleSubtitleItem(stringResource(R.string.cards_transfer), DateFormatter.dayMonthTime(transaction.date),
        startAccessory = {
            LineIndicator(transaction.line)
        }
    )
}

@Composable
private fun TopUpItem(transaction: CardTransaction.TopUp) {
    TitleSubtitleItem(stringResource(R.string.cards_top_up_transaction), DateFormatter.dayMonthTime(transaction.date),
        startAccessory = {
            Icon(Icons.Filled.Euro, contentDescription = "Euro")
        },
        endAccessory = {
            Text(
                MoneyFormatter.fromCents(transaction.amount, showPlusWhenPositive = true),
                style = SevTheme.typography.bodySmallBold
            )
        })
}


@Preview
@Composable
private fun CardTransactionsCardPreview() {
    ScreenPreview {
        CardTransactionsElement(Stubs.cardInfoTransactions)
    }
}

@Preview
@Composable
private fun CardTransactionsLoadingPreview() {
    ScreenPreview {
        CardTransactionsShimmer()
    }
}
