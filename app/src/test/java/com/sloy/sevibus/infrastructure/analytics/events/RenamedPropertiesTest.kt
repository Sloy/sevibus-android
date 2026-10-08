package com.sloy.sevibus.infrastructure.analytics.events

import com.sloy.sevibus.infrastructure.analytics.SevEvent
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo

class RenamedPropertiesTest {

    @Test
    fun `card top up clicked sends cardType instead of type`() {
        expectThat(Clicks.CardTopUpClicked("Bonobús", 1250).propertyNames()).containsExactly("cardType", "balance")
    }

    @Test
    fun `line list clicked sends lineLabel instead of line`() {
        expectThat(Clicks.LineListClicked("C1").propertyNames()).containsExactly("lineLabel")
    }

    @Test
    fun `review dialog dismissed sends durationSeconds instead of duration`() {
        expectThat(Events.ReviewDialogDismissed(12).propertyNames()).containsExactly("durationSeconds")
    }

    @Test
    fun `session summary sends lastScreen in snake case`() {
        val summary = Events.SessionSummary(
            sessionType = Events.SessionSummary.SessionType.GLANCER,
            durationSeconds = 20,
            stopViews = 0,
            distinctStops = 0,
            arrivalsViews = 1,
            entrySources = emptyList(),
            featuresUsed = listOf("favorites"),
            lastScreen = Events.SessionSummary.Screen.FOR_YOU,
        )

        expectThat(summary.properties.toMap()["lastScreen"]).isEqualTo("for_you")
    }

    private fun SevEvent.propertyNames() = properties.map { it.first }
}
