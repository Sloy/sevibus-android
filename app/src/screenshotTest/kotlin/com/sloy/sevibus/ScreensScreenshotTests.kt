package com.sloy.sevibus

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.sloy.sevibus.feature.cards.CardsHelpScreenPreview
import com.sloy.sevibus.feature.cards.CardsScreenErrorPreview
import com.sloy.sevibus.feature.cards.CardsScreenLoadedPreview
import com.sloy.sevibus.feature.cards.CardsScreenLoadedWithTransactionsPreview
import com.sloy.sevibus.feature.foryou.ForYouScreenPreview
import com.sloy.sevibus.feature.lines.LinesScreenPreview
import com.sloy.sevibus.feature.linestops.LineRouteScreenWithRoutesPreview
import com.sloy.sevibus.feature.linestops.LineRouteScreenWithoutRoutesPreview
import com.sloy.sevibus.feature.login.SettingsScreenLoggedInPreview
import com.sloy.sevibus.feature.login.SettingsScreenLoggedOutPreview
import com.sloy.sevibus.feature.search.SearchScreenResultsPreview
import com.sloy.sevibus.feature.stopdetail.StopDetailScreenFailedArrivalsPreview
import com.sloy.sevibus.feature.stopdetail.StopDetailScreenFailedStopPreview
import com.sloy.sevibus.feature.stopdetail.StopDetailScreenLoadedArrivalsPreview
import com.sloy.sevibus.feature.stopdetail.StopDetailScreenLoadingStopPreview
import com.sloy.sevibus.feature.stopdetail.StopDetailScreenSelectedLinePreview

/**
 * Screenshot tests for full screens.
 * Each test wraps a preview function defined in the main source set.
 * Previews are pinned to Spanish, the app's default language, so renders don't depend on the host locale.
 */
class ScreensScreenshotTests {

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun cardsLoaded() {
        CardsScreenLoadedPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun cardsLoadedWithTransactions() {
        CardsScreenLoadedWithTransactionsPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun cardsError() {
        CardsScreenErrorPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun cardsHelp() {
        CardsHelpScreenPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun forYou() {
        ForYouScreenPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun lines() {
        LinesScreenPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun lineRouteWithRoutes() {
        LineRouteScreenWithRoutesPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun lineRouteWithoutRoutes() {
        LineRouteScreenWithoutRoutesPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun searchResults() {
        SearchScreenResultsPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun settingsLoggedIn() {
        SettingsScreenLoggedInPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun settingsLoggedOut() {
        SettingsScreenLoggedOutPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun stopDetailLoaded() {
        StopDetailScreenLoadedArrivalsPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun stopDetailSelectedLine() {
        StopDetailScreenSelectedLinePreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun stopDetailFailedArrivals() {
        StopDetailScreenFailedArrivalsPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun stopDetailLoadingStop() {
        StopDetailScreenLoadingStopPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun stopDetailFailedStop() {
        StopDetailScreenFailedStopPreview()
    }
}
