package com.sloy.sevibus.feature.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sloy.sevibus.R
import com.sloy.sevibus.domain.model.CardTransaction
import com.sloy.sevibus.domain.model.Fare
import com.sloy.sevibus.domain.model.LineSummary
import com.sloy.sevibus.domain.model.Money
import com.sloy.sevibus.infrastructure.extensions.performHapticSegmentTick
import com.sloy.sevibus.ui.components.LineIndicator
import com.sloy.sevibus.ui.formatter.MoneyFormatter
import com.sloy.sevibus.ui.preview.ScreenPreview
import com.sloy.sevibus.ui.preview.ScreenshotSuite
import com.sloy.sevibus.ui.preview.ScreenshotTest
import com.sloy.sevibus.ui.shimmer.Shimmer
import com.sloy.sevibus.ui.theme.SevTheme

internal const val MINUS = "−"

data class CardTransactionsMonth(val year: Int, val month: Int, val transactions: List<CardTransaction>)

fun List<CardTransaction>.byMonth(): List<CardTransactionsMonth> =
    groupBy { it.date.year to it.date.monthValue }
        .map { (yearMonth, transactions) -> CardTransactionsMonth(yearMonth.first, yearMonth.second, transactions) }

data class AmountText(val text: String, val muted: Boolean)

internal fun signedMoney(money: Money): String =
    if (money.isNegative) MINUS + MoneyFormatter.format(-money) else MoneyFormatter.format(money)

internal fun CardTransaction.Trip.hasPerPersonFare(): Boolean = people > 1 && fare?.isFree == false

internal fun fareText(fare: Fare, people: Int, perPerson: Boolean): String? = when {
    fare.isFree -> null
    perPerson && people > 1 -> MINUS + MoneyFormatter.format(fare.perPerson(people)) + " × $people"
    else -> MINUS + MoneyFormatter.format(fare.total)
}

internal fun purchaseAmountText(transaction: CardTransaction, isBalanceCard: Boolean): AmountText? = when (transaction) {
    is CardTransaction.TopUp -> AmountText(MoneyFormatter.format(transaction.amount, showPlusWhenPositive = true), muted = false)
    is CardTransaction.PendingPurchase -> AmountText(MoneyFormatter.format(transaction.amount, showPlusWhenPositive = isBalanceCard), muted = true)
    is CardTransaction.PassPurchase -> AmountText(MoneyFormatter.format(transaction.amount), muted = false)
    is CardTransaction.PassRenewal -> transaction.amount?.let { AmountText(MoneyFormatter.format(it), muted = false) }
    is CardTransaction.Trip, is CardTransaction.Transfer -> null
}

@Composable
fun CardTransactionsElement(months: List<CardTransactionsMonth>, isBalanceCard: Boolean) {
    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        months.forEach { month ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    CardDateFormatter.month(month.year, month.month),
                    style = SevTheme.typography.bodySmallBold,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
                Card(Modifier.fillMaxWidth()) {
                    month.transactions.forEachIndexed { index, transaction ->
                        TransactionItem(transaction, isBalanceCard)
                        if (index != month.transactions.lastIndex) HorizontalDivider(Modifier.padding(start = 64.dp))
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
private fun TransactionItem(transaction: CardTransaction, isBalanceCard: Boolean) {
    when (transaction) {
        is CardTransaction.Trip -> TripItem(transaction)
        is CardTransaction.Transfer -> TransactionRow(
            leading = { LineIndicator(transaction.line) },
            title = lineTitle(transaction.line),
            subtitle = "${stringResource(R.string.cards_transfer)} · ${CardDateFormatter.dateTime(transaction.date)}",
        )
        else -> PurchaseItem(transaction, isBalanceCard)
    }
}

@Composable
private fun TripItem(trip: CardTransaction.Trip) {
    val view = LocalView.current
    var showPerPerson by rememberSaveable(trip) { mutableStateOf(false) }
    val fare = trip.fare
    TransactionRow(
        leading = { LineIndicator(trip.line) },
        title = lineTitle(trip.line),
        subtitle = CardDateFormatter.dateTime(trip.date),
        chip = { if (trip.people > 1) PeopleChip(trip.people) },
        trailing = fare?.let {
            {
                Text(
                    fareText(it, trip.people, showPerPerson) ?: stringResource(R.string.cards_free_trip),
                    style = SevTheme.typography.bodyStandardBold,
                )
                BalanceAfter(it.balanceAfter)
            }
        },
        onClick = if (trip.hasPerPersonFare()) {
            {
                view.performHapticSegmentTick()
                showPerPerson = !showPerPerson
            }
        } else {
            null
        },
        connector = if (trip.transfers.isEmpty()) Connector.NONE else Connector.BELOW,
    )
    trip.transfers.forEachIndexed { index, transfer ->
        TransactionRow(
            leading = { LineIndicator(transfer.line) },
            title = lineTitle(transfer.line),
            subtitle = "${stringResource(R.string.cards_transfer)} · ${CardDateFormatter.time(transfer.date)}",
            connector = if (index == trip.transfers.lastIndex) Connector.ABOVE else Connector.ABOVE_AND_BELOW,
        )
    }
}

@Composable
private fun PurchaseItem(transaction: CardTransaction, isBalanceCard: Boolean) {
    val isPending = transaction is CardTransaction.PendingPurchase
    val title = when (transaction) {
        is CardTransaction.TopUp -> stringResource(R.string.cards_top_up_transaction)
        is CardTransaction.PendingPurchase ->
            stringResource(if (isBalanceCard) R.string.cards_top_up_pending else R.string.cards_pass_purchase)
        is CardTransaction.PassRenewal -> stringResource(R.string.cards_pass_renewal)
        else -> stringResource(R.string.cards_pass_purchase)
    }
    val dateText = CardDateFormatter.dateTime(transaction.date)
    val amount = purchaseAmountText(transaction, isBalanceCard)
    TransactionRow(
        leading = { PurchaseIcon(if (isPending) Icons.Outlined.Schedule else Icons.Filled.Euro) },
        title = title,
        subtitle = if (isPending) "$dateText · ${stringResource(R.string.cards_pending_suffix)}" else dateText,
        trailing = {
            amount?.let {
                Text(
                    it.text,
                    style = SevTheme.typography.bodyStandardBold,
                    color = if (it.muted) SevTheme.colorScheme.onSurfaceVariant else SevTheme.colorScheme.onSurface,
                )
            }
            (transaction as? CardTransaction.TopUp)?.let { BalanceAfter(it.balanceAfter) }
        },
    )
}

private enum class Connector(val above: Boolean, val below: Boolean) {
    NONE(above = false, below = false),
    BELOW(above = false, below = true),
    ABOVE(above = true, below = false),
    ABOVE_AND_BELOW(above = true, below = true),
}

@Composable
private fun TransactionRow(
    leading: @Composable () -> Unit,
    title: String,
    subtitle: String,
    chip: @Composable () -> Unit = {},
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    connector: Connector = Connector.NONE,
) {
    val connectorColor = SevTheme.colorScheme.outlineVariant
    Row(
        Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .width(32.dp)
                .fillMaxHeight()
                .drawBehind { drawConnector(connector, connectorColor) },
            contentAlignment = Alignment.Center,
        ) {
            leading()
        }
        Column(
            Modifier
                .weight(1f)
                .padding(vertical = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(title, style = SevTheme.typography.bodyStandardBold)
                chip()
            }
            Text(subtitle, style = SevTheme.typography.bodySmall, color = SevTheme.colorScheme.onSurfaceVariant)
        }
        if (trailing != null) {
            Column(Modifier.padding(vertical = 14.dp), horizontalAlignment = Alignment.End) {
                trailing()
            }
        }
    }
}

private fun DrawScope.drawConnector(connector: Connector, color: Color) {
    val x = size.width / 2
    val strokeWidth = 2.dp.toPx()
    if (connector.above) drawLine(color, Offset(x, 0f), Offset(x, size.height / 2), strokeWidth)
    if (connector.below) drawLine(color, Offset(x, size.height / 2), Offset(x, size.height), strokeWidth)
}

@Composable
private fun lineTitle(line: LineSummary): String = stringResource(R.string.cards_line_title, line.label)

@Composable
private fun BalanceAfter(balance: Money) {
    Text(
        stringResource(R.string.cards_balance_after, signedMoney(balance)),
        style = SevTheme.typography.bodyExtraSmall,
        color = SevTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun PurchaseIcon(icon: ImageVector) {
    Box(
        Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(SevTheme.colorScheme.outlineVariant),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun PeopleChip(people: Int) {
    Row(
        Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(SevTheme.colorScheme.outlineVariant)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(stringResource(R.string.cards_group_count, people), style = SevTheme.typography.bodyExtraSmallBold)
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
            CardTransactionsElement(cardCase("BalanceGroup").transactions.byMonth(), isBalanceCard = true)
        }
    }
}

@ScreenshotTest(ScreenshotSuite.Components)
@Preview
@Composable
internal fun CardActivityElementPassPreview() {
    SevTheme {
        Surface {
            CardTransactionsElement(cardCase("PassNextPeriod").transactions.byMonth(), isBalanceCard = false)
        }
    }
}

@ScreenshotTest(ScreenshotSuite.Components)
@Preview
@Composable
internal fun CardTransactionsElementTransfersPreview() {
    SevTheme {
        Surface {
            CardTransactionsElement(cardCase("PassTransfers").transactions.byMonth(), isBalanceCard = false)
        }
    }
}
