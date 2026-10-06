package com.sloy.sevibus

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.sloy.sevibus.feature.cards.CardBalanceItemPreview
import com.sloy.sevibus.feature.cards.CardInfoElementPreview
import com.sloy.sevibus.feature.cards.CardTransactionsElementLoadingPreview
import com.sloy.sevibus.feature.cards.CardTransactionsElementPreview
import com.sloy.sevibus.feature.foryou.alert.AlertWidgetPreview
import com.sloy.sevibus.feature.foryou.favorites.FavoriteListItemLoadedPreview
import com.sloy.sevibus.feature.foryou.favorites.FavoriteListItemLoadingArrivalsPreview
import com.sloy.sevibus.feature.foryou.favorites.FavoritesWidgetEmptyPreview
import com.sloy.sevibus.feature.foryou.favorites.FavoritesWidgetNotLoggedPreview
import com.sloy.sevibus.feature.foryou.favorites.FavoritesWidgetWithArrivalsPreview
import com.sloy.sevibus.feature.foryou.favorites.edit.EditFavoritesScreenItemPreview
import com.sloy.sevibus.feature.foryou.nearby.NearbyListItemLoadedPreview
import com.sloy.sevibus.feature.foryou.nearby.NearbyListItemLoadingArrivalsPreview
import com.sloy.sevibus.feature.foryou.nearby.NearbyWidgetEmptyPreview
import com.sloy.sevibus.feature.foryou.nearby.NearbyWidgetNoPermissionPreview
import com.sloy.sevibus.feature.foryou.nearby.NearbyWidgetWithArrivalsPreview
import com.sloy.sevibus.feature.linestops.component.StopTimelineElementPreview
import com.sloy.sevibus.feature.map.icons.CircularStopIconPreview
import com.sloy.sevibus.feature.map.icons.OutlinedStopIconPreview
import com.sloy.sevibus.feature.map.icons.OutlinedStopIconShadowPreview
import com.sloy.sevibus.feature.map.icons.ShapedStopIconPreview
import com.sloy.sevibus.feature.search.RoundedSearchBarEmptyPreview
import com.sloy.sevibus.feature.search.RoundedSearchBarLinePreview
import com.sloy.sevibus.feature.search.RoundedSearchBarStopPreview
import com.sloy.sevibus.feature.search.RoundedSearchBarTextPreview
import com.sloy.sevibus.ui.components.AppUpdateButtonAvailablePreview
import com.sloy.sevibus.ui.components.AppUpdateButtonDownloadingPreview
import com.sloy.sevibus.ui.components.AppUpdateButtonReadyPreview
import com.sloy.sevibus.ui.components.ArrivalTimeElementPreview
import com.sloy.sevibus.ui.components.BusArrivalAvailablePreview
import com.sloy.sevibus.ui.components.BusArrivalHighlightedPreview
import com.sloy.sevibus.ui.components.BusArrivalLastBusPreview
import com.sloy.sevibus.ui.components.BusArrivalLoadingPreview
import com.sloy.sevibus.ui.components.BusArrivalNotAvailablePreview
import com.sloy.sevibus.ui.components.CircularIconButtonPreview
import com.sloy.sevibus.ui.components.IconPickerPreview
import com.sloy.sevibus.ui.components.InfoBannerOneLinePreview
import com.sloy.sevibus.ui.components.InfoBannerTwoLinesActionPreview
import com.sloy.sevibus.ui.components.LineElementPreview
import com.sloy.sevibus.ui.components.LineIndicatorPreview
import com.sloy.sevibus.ui.components.SegmentedControlPreview
import com.sloy.sevibus.ui.components.SevNavigationBarPreview
import com.sloy.sevibus.ui.components.StopCardElementHighlightedPreview
import com.sloy.sevibus.ui.components.StopCardElementSingleLinePreview
import com.sloy.sevibus.ui.components.SurfaceButtonIconPreview
import com.sloy.sevibus.ui.components.SurfaceButtonTextPreview

/**
 * Screenshot tests for reusable components and screen sections.
 * Each test wraps a preview function defined in the main source set.
 * Previews are pinned to Spanish, the app's default language, so renders don't depend on the host locale.
 */
class ComponentsScreenshotTests {

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun lineIndicator() {
        LineIndicatorPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun busArrivalAvailable() {
        BusArrivalAvailablePreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun busArrivalHighlighted() {
        BusArrivalHighlightedPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun busArrivalLastBus() {
        BusArrivalLastBusPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun busArrivalNotAvailable() {
        BusArrivalNotAvailablePreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun busArrivalLoading() {
        BusArrivalLoadingPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun infoBannerOneLine() {
        InfoBannerOneLinePreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun infoBannerTwoLinesAction() {
        InfoBannerTwoLinesActionPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun appUpdateButtonAvailable() {
        AppUpdateButtonAvailablePreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun appUpdateButtonDownloading() {
        AppUpdateButtonDownloadingPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun appUpdateButtonReady() {
        AppUpdateButtonReadyPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun arrivalTimeElement() {
        ArrivalTimeElementPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun circularIconButton() {
        CircularIconButtonPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun iconPicker() {
        IconPickerPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun lineElement() {
        LineElementPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun segmentedControl() {
        SegmentedControlPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun sevNavigationBar() {
        SevNavigationBarPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun stopCardElementSingleLine() {
        StopCardElementSingleLinePreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun stopCardElementHighlighted() {
        StopCardElementHighlightedPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun surfaceButtonText() {
        SurfaceButtonTextPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun surfaceButtonIcon() {
        SurfaceButtonIconPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun cardBalanceItem() {
        CardBalanceItemPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun cardInfoElement() {
        CardInfoElementPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun cardTransactionsElement() {
        CardTransactionsElementPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun cardTransactionsElementLoading() {
        CardTransactionsElementLoadingPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun alertWidget() {
        AlertWidgetPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun favoriteListItemLoaded() {
        FavoriteListItemLoadedPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun favoriteListItemLoadingArrivals() {
        FavoriteListItemLoadingArrivalsPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun favoritesWidgetWithArrivals() {
        FavoritesWidgetWithArrivalsPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun favoritesWidgetEmpty() {
        FavoritesWidgetEmptyPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun favoritesWidgetNotLogged() {
        FavoritesWidgetNotLoggedPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun editFavoriteItem() {
        EditFavoritesScreenItemPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun nearbyListItemLoaded() {
        NearbyListItemLoadedPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun nearbyListItemLoadingArrivals() {
        NearbyListItemLoadingArrivalsPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun nearbyWidgetWithArrivals() {
        NearbyWidgetWithArrivalsPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun nearbyWidgetEmpty() {
        NearbyWidgetEmptyPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun nearbyWidgetNoPermission() {
        NearbyWidgetNoPermissionPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun stopTimelineElement() {
        StopTimelineElementPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun circularStopIcon() {
        CircularStopIconPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun outlinedStopIcon() {
        OutlinedStopIconPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun outlinedStopIconShadow() {
        OutlinedStopIconShadowPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun shapedStopIcon() {
        ShapedStopIconPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun roundedSearchBarEmpty() {
        RoundedSearchBarEmptyPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun roundedSearchBarText() {
        RoundedSearchBarTextPreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun roundedSearchBarLine() {
        RoundedSearchBarLinePreview()
    }

    @Preview(locale = "es")
    @PreviewTest
    @Composable
    fun roundedSearchBarStop() {
        RoundedSearchBarStopPreview()
    }
}
