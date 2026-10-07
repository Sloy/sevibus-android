package com.sloy.sevibus.feature.cards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sloy.sevibus.R
import com.sloy.sevibus.Stubs
import com.sloy.sevibus.domain.model.CardInfo
import com.sloy.sevibus.domain.model.estimatedTrips
import com.sloy.sevibus.domain.model.hasBalance
import com.sloy.sevibus.domain.model.isLowBalance
import com.sloy.sevibus.ui.formatter.MoneyFormatter
import com.sloy.sevibus.ui.preview.ScreenshotSuite
import com.sloy.sevibus.ui.preview.ScreenshotTest
import com.sloy.sevibus.ui.theme.SevTheme

internal val CssEase = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f)
private val ExpandEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
private val CollapseEasing = CubicBezierEasing(0.3f, 0f, 0.1f, 1f)
private val ShrinkContentEasing = CubicBezierEasing(0.4f, 0f, 1f, 1f)
private val OvershootEasing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)
private val ContentPivot = TransformOrigin(0.5f, 0.4f)
private const val SECTION_REVEAL_DELAY_MILLIS = 260

@Composable
fun CardBalanceSection(
    card: CardInfo,
    transactionsState: TransactionsState,
    previousCardHadBalance: Boolean,
    onTopUpClicked: (CardInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    val displayed = rememberLastNonNull(if (card.hasBalance) card to transactionsState else null)
    AnimatedVisibility(
        visible = card.hasBalance,
        modifier = modifier.fillMaxWidth(),
        enter = expandVertically(tween(380, easing = ExpandEasing), expandFrom = Alignment.Top) +
            fadeIn(tween(220, delayMillis = 160, easing = CssEase)) +
            scaleIn(tween(560, delayMillis = 160, easing = OvershootEasing), initialScale = 0.82f, transformOrigin = ContentPivot),
        exit = fadeOut(tween(170, easing = CssEase)) +
            scaleOut(tween(230, easing = ShrinkContentEasing), targetScale = 0.82f, transformOrigin = ContentPivot) +
            shrinkVertically(tween(360, delayMillis = 150, easing = CollapseEasing), shrinkTowards = Alignment.Top),
    ) {
        displayed?.let { (displayedCard, displayedTransactions) ->
            BalanceContent(displayedCard, displayedTransactions, previousCardHadBalance, onTopUpClicked)
        }
    }
}

@Composable
private fun BalanceContent(
    card: CardInfo,
    transactionsState: TransactionsState,
    previousCardHadBalance: Boolean,
    onTopUpClicked: (CardInfo) -> Unit,
) {
    val balance = card.balance ?: return
    val isLow = card.isLowBalance
    val tripsMemory = remember(card.serialNumber) { TripsMemory() }
    val trips = when (transactionsState) {
        is TransactionsState.Loading -> tripsMemory.lastKnownTrips
        is TransactionsState.Loaded -> estimatedTrips(balance, transactionsState.transactions)
        else -> null
    }
    tripsMemory.lastKnownTrips = trips
    val showChip = isLow && trips != null
    val revealDelay = if (previousCardHadBalance) 0 else SECTION_REVEAL_DELAY_MILLIS

    val chipVisibility = remember { MutableTransitionState(showChip && previousCardHadBalance) }
    chipVisibility.targetState = showChip

    val lowBalanceProgress = remember { Animatable(if (isLow && previousCardHadBalance) 1f else 0f) }
    LaunchedEffect(isLow) {
        if (!previousCardHadBalance) lowBalanceProgress.snapTo(0f)
        lowBalanceProgress.animateTo(if (isLow) 1f else 0f, tween(220, delayMillis = revealDelay, easing = CssEase))
    }

    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.cards_available_balance), style = SevTheme.typography.bodySmallBold)
        FormattedBalance(balance)
        TripsChip(chipVisibility, rememberLastNonNull(trips.takeIf { showChip }) ?: 0, revealDelay)
        TopUpButton(
            lowBalanceProgress = lowBalanceProgress.value,
            onClick = { onTopUpClicked(card) },
            modifier = Modifier.padding(top = 10.dp, bottom = 8.dp),
        )
    }
}

@Composable
private fun FormattedBalance(balance: Int) {
    val textColor = if (balance <= 0) Color.Red else Color.Unspecified
    val (integerPart, decimalPart) = MoneyFormatter.fromCents(balance).split(",")
    Text(
        buildAnnotatedString {
            withStyle(SevTheme.typography.bodyStandard.copy(fontSize = 45.sp).toSpanStyle()) {
                append(integerPart)
            }
            withStyle(SevTheme.typography.bodyStandard.copy(fontSize = 36.sp).toSpanStyle()) {
                append(",")
                append(decimalPart)
            }
        },
        color = textColor,
    )
}

@Composable
private fun TripsChip(visibility: MutableTransitionState<Boolean>, trips: Int, revealDelay: Int) {
    val colors = SevTheme.extendedColors
    AnimatedVisibility(
        visibleState = visibility,
        enter = expandVertically(tween(300, delayMillis = revealDelay, easing = ExpandEasing), expandFrom = Alignment.Top) +
            fadeIn(tween(200, delayMillis = revealDelay + 60, easing = CssEase)) +
            scaleIn(tween(260, delayMillis = revealDelay + 60, easing = ExpandEasing), initialScale = 0.85f),
        exit = fadeOut(tween(140, easing = CssEase)) +
            scaleOut(tween(200, easing = CssEase), targetScale = 0.85f) +
            shrinkVertically(tween(260, delayMillis = 80, easing = CollapseEasing), shrinkTowards = Alignment.Top),
    ) {
        Text(
            text = pluralStringResource(R.plurals.cards_trips_left, trips, trips),
            style = SevTheme.typography.bodySmallBold.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
            color = colors.onWarningContainer,
            modifier = Modifier
                .padding(top = 2.dp, bottom = 6.dp)
                .background(colors.warningContainer, CircleShape)
                .padding(vertical = 3.dp, horizontal = 10.dp),
        )
    }
}

@Composable
private fun TopUpButton(lowBalanceProgress: Float, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = SevTheme.extendedColors
    val containerColor = lerp(SevTheme.colorScheme.outlineVariant, colors.tussamRed, lowBalanceProgress)
    val contentColor = lerp(SevTheme.colorScheme.onSurface, Color.White, lowBalanceProgress)
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
    ) {
        Text(
            text = stringResource(R.string.cards_top_up_button),
            style = SevTheme.typography.bodyStandardBold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(18.dp),
        )
    }
}

private class TripsMemory {
    var lastKnownTrips: Int? = null
}

@Composable
private fun <T : Any> rememberLastNonNull(value: T?): T? {
    val last = remember { mutableStateOf(value) }
    if (value != null) last.value = value
    return last.value
}

@ScreenshotTest(ScreenshotSuite.Components)
@PreviewLightDark
@Composable
internal fun CardBalanceSectionLowBalancePreview() {
    SevTheme {
        Surface {
            CardBalanceSection(
                card = Stubs.cards[0].copy(balance = 80),
                transactionsState = TransactionsState.Loaded(Stubs.cardTripTransactions),
                previousCardHadBalance = true,
                onTopUpClicked = {},
            )
        }
    }
}

@ScreenshotTest(ScreenshotSuite.Components)
@PreviewLightDark
@Composable
internal fun CardBalanceSectionLowBalanceWithoutTripsPreview() {
    SevTheme {
        Surface {
            CardBalanceSection(
                card = Stubs.cards[0].copy(balance = 80),
                transactionsState = TransactionsState.Loading,
                previousCardHadBalance = true,
                onTopUpClicked = {},
            )
        }
    }
}

@ScreenshotTest(ScreenshotSuite.Components)
@PreviewLightDark
@Composable
internal fun CardBalanceSectionNormalBalancePreview() {
    SevTheme {
        Surface {
            CardBalanceSection(
                card = Stubs.cards[0],
                transactionsState = TransactionsState.Loaded(Stubs.cardTripTransactions),
                previousCardHadBalance = true,
                onTopUpClicked = {},
            )
        }
    }
}
