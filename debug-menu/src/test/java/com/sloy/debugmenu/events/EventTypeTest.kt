package com.sloy.debugmenu.events

import androidx.compose.ui.graphics.Color
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

    @Test
    fun `light colors use the base accent and its tint`() {
        val colors = EventType.VIEW.colors(dark = false)
        expectThat(colors.accent).isEqualTo(Color(0xFF3F51B5))
        expectThat(colors.tint).isEqualTo(Color(0xFF3F51B5).copy(alpha = 0.16f))
        expectThat(colors.ink).isEqualTo(Color(0xFF2F3D8F))
    }

    @Test
    fun `dark colors use the on dark accent`() {
        val colors = EventType.OTHER.colors(dark = true)
        expectThat(colors.accent).isEqualTo(Color(0xFFFFB547))
        expectThat(colors.tint).isEqualTo(Color(0xFFFFB547).copy(alpha = 0.18f))
        expectThat(colors.ink).isEqualTo(Color(0xFFFFC977))
    }

    @Test
    fun `click on dark is green`() {
        expectThat(EventType.CLICK.colors(dark = true).accent).isEqualTo(Color(0xFF7BD67F))
    }
}
