package com.sloy.debugmenu.events

import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class EventTypeTest {

    @Test
    fun `classifies events by name suffix`() {
        expectThat(EventType.of("Add Favorite Clicked")).isEqualTo(EventType.CLICK)
        expectThat(EventType.of("Stop Details Viewed")).isEqualTo(EventType.VIEW)
        expectThat(EventType.of("App Started")).isEqualTo(EventType.OTHER)
    }
}
