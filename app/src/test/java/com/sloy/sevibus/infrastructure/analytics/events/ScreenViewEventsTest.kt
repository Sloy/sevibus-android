package com.sloy.sevibus.infrastructure.analytics.events

import com.sloy.sevibus.infrastructure.analytics.SevEvent
import com.sloy.sevibus.navigation.NavigationDestination
import com.sloy.sevibus.navigation.NavigationTrigger
import com.sloy.sevibus.navigation.SevNavigator
import com.sloy.sevibus.navigation.StopDetailSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isA
import strikt.assertions.isEqualTo

class ScreenViewEventsTest {

    private val navigator = SevNavigator()

    @Test
    fun `start destination is tracked with launch trigger`() = runTest {
        expectThat(lastScreenView()).isEqualTo(Screens.ForYouViewed(NavigationTrigger.LAUNCH))
    }

    @Test
    fun `forward navigation is tracked with navigation trigger`() = runTest {
        navigator.navigate(NavigationDestination.Lines)

        expectThat(lastScreenView()).isEqualTo(Screens.LinesViewed(NavigationTrigger.NAVIGATION))
    }

    @Test
    fun `back navigation is tracked with back trigger`() = runTest {
        navigator.navigate(NavigationDestination.Lines)
        navigator.navigateBack()

        expectThat(lastScreenView()).isEqualTo(Screens.ForYouViewed(NavigationTrigger.BACK))
    }

    @Test
    fun `cancelling search is tracked with back trigger`() = runTest {
        navigator.navigate(NavigationDestination.Search)
        navigator.popToRoot()

        expectThat(lastScreenView()).isEqualTo(Screens.ForYouViewed(NavigationTrigger.BACK))
    }

    @Test
    fun `stop details source comes from the entry point`() = runTest {
        val sources = StopDetailSource.entries.map { source ->
            navigator.navigate(NavigationDestination.StopDetail(STOP_ID + source.ordinal, source = source))
            (lastScreenView() as Screens.StopDetailsViewed).source
        }

        expectThat(sources).containsExactly("favorites", "nearby", "map", "search", "line_route", "other")
    }

    @Test
    fun `stop details source is back when coming back to the stop`() = runTest {
        navigator.navigate(NavigationDestination.StopDetail(STOP_ID, source = StopDetailSource.MAP))
        navigator.navigate(NavigationDestination.LineStops(LINE_ID))
        navigator.navigateBack()

        expectThat(lastScreenView()).isEqualTo(
            Screens.StopDetailsViewed(STOP_ID, "back", highlightedLineId = null, trigger = NavigationTrigger.BACK)
        )
    }

    @Test
    fun `stop details from a line route has the line highlighted`() = runTest {
        navigator.navigate(NavigationDestination.LineStops(LINE_ID))
        navigator.navigate(NavigationDestination.StopDetail(STOP_ID, highlightedLine = LINE_ID, source = StopDetailSource.LINE_ROUTE))

        expectThat(lastScreenView()).isEqualTo(
            Screens.StopDetailsViewed(STOP_ID, "line_route", highlightedLineId = LINE_ID, trigger = NavigationTrigger.NAVIGATION)
        )
    }

    @Test
    fun `line stops is tracked when opening a line`() = runTest {
        navigator.navigate(NavigationDestination.LineStops(LINE_ID, routeId = ROUTE_ID))

        expectThat(lastScreenView()).isEqualTo(Screens.LineStopsViewed(LINE_ID, ROUTE_ID, NavigationTrigger.NAVIGATION))
    }

    @Test
    fun `line stops is not tracked again when switching route or highlighted stop`() = runTest {
        navigator.navigate(NavigationDestination.LineStops(LINE_ID, routeId = ROUTE_ID))
        navigator.navigate(NavigationDestination.LineStops(LINE_ID, routeId = OTHER_ROUTE_ID))
        expectThat(lastScreenView()).isEqualTo(null)

        navigator.navigate(NavigationDestination.LineStops(LINE_ID, routeId = OTHER_ROUTE_ID, highlightedStop = STOP_ID))
        expectThat(lastScreenView()).isEqualTo(null)
    }

    @Test
    fun `line stops is tracked when switching to a different line`() = runTest {
        navigator.navigate(NavigationDestination.LineStops(LINE_ID))
        navigator.navigate(NavigationDestination.LineStops(OTHER_LINE_ID))

        expectThat(lastScreenView()).isA<Screens.LineStopsViewed>().get { lineId }.isEqualTo(OTHER_LINE_ID)
    }

    private suspend fun lastScreenView(): SevEvent? = navigator.transitions.first().toScreenViewEvent()
}

private const val STOP_ID = 11
private const val LINE_ID = 1
private const val OTHER_LINE_ID = 2
private const val ROUTE_ID = "1_1"
private const val OTHER_ROUTE_ID = "1_2"
