package com.sloy.sevibus.feature.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloy.sevibus.R
import com.sloy.sevibus.domain.model.CardTransactionKind
import com.sloy.sevibus.ui.components.LineIndicator
import com.sloy.sevibus.ui.formatter.MoneyFormatter
import com.sloy.sevibus.ui.preview.ScreenPreview
import com.sloy.sevibus.ui.preview.ScreenshotSuite
import com.sloy.sevibus.ui.preview.ScreenshotTest
import com.sloy.sevibus.ui.shimmer.Shimmer
import com.sloy.sevibus.ui.theme.SevTheme

internal const val FREE = "free"
internal const val MINUS = "\u2212"

data class AmountText(val text: String, val muted: Boolean)

internal fun amountText(row: ActivityRow, isBalanceCard: Boolean): AmountText? = when (row.kind) {
    CardTransactionKind.TRIP -> when {
        !isBalanceCard -> null
        row.amount == null || row.amount == 0 -> AmountText(FREE, muted = false)
        else -> AmountText(MINUS + MoneyFormatter.fromCents(row.amount), muted = false)
    }
    CardTransactionKind.TRANSFER -> null
    CardTransactionKind.TOP_UP -> row.amount?.let { AmountText("+" + MoneyFormatter.fromCents(it), muted = row.pending) }
    CardTransactionKind.PASS_PURCHASE, CardTransactionKind.PASS_RENEWAL -> row.amount?.let { AmountText(MoneyFormatter.fromCents(it), muted = row.pending) }
}

@Composable
fun CardActivityElement(months: List<ActivityMonth>, isBalanceCard: Boolean) {
    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        months.forEach { month ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    CardDateFormatter.month(month.year, month.month),
                    style = SevTheme.typography.bodySmallBold,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
                Card(Modifier.fillMaxWidth()) {
                    month.rows.forEachIndexed { index, row ->
                        ActivityRowItem(row, isBalanceCard)
                        if (index != month.rows.lastIndex) HorizontalDivider(Modifier.padding(start = 64.dp))
                    }
                }
            }
        }
        Text(
            stringResource(R.string.cards_history_limit_note),
            style = SevTheme.typography.bodyExtraSmall,
            color = SevTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ActivityRowItem(row: ActivityRow, isBalanceCard: Boolean) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RowLeading(row)
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(rowTitle(row), style = SevTheme.typography.bodyStandardBold)
                if (row.count > 1) CountChip(row.count)
            }
            val dateText = CardDateFormatter.dateTime(row.date)
            val subtitle = if (row.pending) "$dateText · ${stringResource(R.string.cards_pending_suffix)}" else dateText
            Text(subtitle, style = SevTheme.typography.bodySmall, color = SevTheme.colorScheme.onSurfaceVariant)
        }
        Column(horizontalAlignment = Alignment.End) {
            if (row.kind == CardTransactionKind.TRANSFER) {
                Text(stringResource(R.string.cards_transfer), style = SevTheme.typography.bodySmallBold, color = SevTheme.colorScheme.onSurfaceVariant)
            } else {
                amountText(row, isBalanceCard)?.let { amount ->
                    Text(
                        if (amount.text == FREE) stringResource(R.string.cards_free_trip) else amount.text,
                        style = SevTheme.typography.bodyStandardBold,
                        color = if (amount.muted) SevTheme.colorScheme.onSurfaceVariant else SevTheme.colorScheme.onSurface,
                    )
                }
            }
            val balanceAfter = row.balanceAfter
            if (isBalanceCard && balanceAfter != null) {
                Text(
                    stringResource(R.string.cards_balance_after, signedMoney(balanceAfter)),
                    style = SevTheme.typography.bodyExtraSmall,
                    color = SevTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun signedMoney(cents: Int): String = if (cents < 0) MINUS + MoneyFormatter.fromCents(-cents) else MoneyFormatter.fromCents(cents)

@Composable
fun rowTitle(row: ActivityRow): String = when (row.kind) {
    CardTransactionKind.TRIP, CardTransactionKind.TRANSFER -> stringResource(R.string.cards_line_title, row.line?.label.orEmpty())
    CardTransactionKind.TOP_UP -> stringResource(if (row.pending) R.string.cards_top_up_pending else R.string.cards_top_up_transaction)
    CardTransactionKind.PASS_PURCHASE -> stringResource(R.string.cards_pass_purchase)
    CardTransactionKind.PASS_RENEWAL -> stringResource(R.string.cards_pass_renewal)
}

@Composable
private fun RowLeading(row: ActivityRow) {
    val line = row.line
    if (line != null && (row.kind == CardTransactionKind.TRIP || row.kind == CardTransactionKind.TRANSFER)) {
        LineIndicator(line)
    } else {
        Box(
            Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(SevTheme.colorScheme.outlineVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(if (row.pending) Icons.Outlined.Schedule else Icons.Filled.Euro, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun CountChip(count: Int) {
    Row(
        Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(SevTheme.colorScheme.outlineVariant)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(stringResource(R.string.cards_group_count, count), style = SevTheme.typography.bodyExtraSmallBold)
        Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(12.dp))
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


@ScreenshotTest(ScreenshotSuite.Components)
@Preview
@Composable
internal fun CardTransactionsElementLoadingPreview() {
    ScreenPreview {
        CardTransactionsShimmer()
    }
}

@ScreenshotTest(ScreenshotSuite.Components)
@Preview
@Composable
internal fun CardActivityElementBalancePreview() {
    SevTheme {
        Surface {
            CardActivityElement(cardActivity(cardCase("BalanceGroup").transactions), isBalanceCard = true)
        }
    }
}

@ScreenshotTest(ScreenshotSuite.Components)
@Preview
@Composable
internal fun CardActivityElementPassPreview() {
    SevTheme {
        Surface {
            CardActivityElement(cardActivity(cardCase("PassNextPeriod").transactions), isBalanceCard = false)
        }
    }
}
