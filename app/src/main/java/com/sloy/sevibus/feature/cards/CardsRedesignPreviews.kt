package com.sloy.sevibus.feature.cards

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview
import com.sloy.sevibus.Stubs
import com.sloy.sevibus.infrastructure.nfc.NfcState
import com.sloy.sevibus.ui.preview.ScreenPreview
import com.sloy.sevibus.ui.preview.ScreenshotSuite
import com.sloy.sevibus.ui.preview.ScreenshotTest

internal fun cardCase(name: String): Stubs.CardCase = Stubs.cardCases.first { it.name == name }

@Composable
internal fun CardCasePreview(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalToday provides Stubs.cardCasesToday, content = content)
}

@Composable
private fun CardsScreenCase(name: String) {
    val case = cardCase(name)
    CardCasePreview {
        ScreenPreview {
            CardsScreen(
                CardsScreenState.Content(
                    listOf(CardAndTransactions(case.card, TransactionsState.Loaded(case.transactions))),
                    isReordering = false,
                    scrollToCard = null,
                ),
                CardsScreenNewCardState.InputForm(),
                NfcState.ENABLED,
            )
        }
    }
}

@ScreenshotTest(ScreenshotSuite.Screens)
@Preview
@Composable
internal fun CardsScreenBalanceNormalPreview() = CardsScreenCase("BalanceNormal")

@ScreenshotTest(ScreenshotSuite.Screens)
@Preview
@Composable
internal fun CardsScreenBalanceGroupPreview() = CardsScreenCase("BalanceGroup")

@ScreenshotTest(ScreenshotSuite.Screens)
@Preview
@Composable
internal fun CardsScreenBalanceLowPreview() = CardsScreenCase("BalanceLow")

@ScreenshotTest(ScreenshotSuite.Screens)
@Preview
@Composable
internal fun CardsScreenBalanceNegativePreview() = CardsScreenCase("BalanceNegative")

@ScreenshotTest(ScreenshotSuite.Screens)
@Preview
@Composable
internal fun CardsScreenPassActivePreview() = CardsScreenCase("PassActive")

@ScreenshotTest(ScreenshotSuite.Screens)
@Preview
@Composable
internal fun CardsScreenPassNextPeriodPreview() = CardsScreenCase("PassNextPeriod")

@ScreenshotTest(ScreenshotSuite.Screens)
@Preview
@Composable
internal fun CardsScreenPassExpiredPreview() = CardsScreenCase("PassExpired")

@ScreenshotTest(ScreenshotSuite.Screens)
@Preview
@Composable
internal fun CardsScreenCardExpiredPreview() = CardsScreenCase("CardExpired")

@ScreenshotTest(ScreenshotSuite.Screens)
@Preview
@Composable
internal fun CardsScreenPassTransfersPreview() = CardsScreenCase("PassTransfers")
