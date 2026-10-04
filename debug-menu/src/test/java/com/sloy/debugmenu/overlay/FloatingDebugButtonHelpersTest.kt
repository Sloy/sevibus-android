package com.sloy.debugmenu.overlay

import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class FloatingDebugButtonHelpersTest {

    private val buttonSize = 48f
    private val screenWidth = 1000f

    @Test
    fun `snapX leaves one third of the button off screen on the left`() {
        expectThat(snapX(Edge.LEFT, buttonSize, screenWidth)).isEqualTo(-16f)
    }

    @Test
    fun `snapX leaves one third of the button off screen on the right`() {
        expectThat(snapX(Edge.RIGHT, buttonSize, screenWidth)).isEqualTo(968f)
    }

    @Test
    fun `hiddenX moves the button fully off screen`() {
        expectThat(hiddenX(Edge.LEFT, buttonSize, screenWidth)).isEqualTo(-48f)
        expectThat(hiddenX(Edge.RIGHT, buttonSize, screenWidth)).isEqualTo(1000f)
    }

    @Test
    fun `nearestEdge uses the button center`() {
        expectThat(nearestEdge(400f, buttonSize, screenWidth)).isEqualTo(Edge.LEFT)
        expectThat(nearestEdge(480f, buttonSize, screenWidth)).isEqualTo(Edge.RIGHT)
    }

    @Test
    fun `clampY keeps the button inside the bounds`() {
        expectThat(clampY(-10f, buttonSize, 0f, 800f)).isEqualTo(0f)
        expectThat(clampY(300f, buttonSize, 0f, 800f)).isEqualTo(300f)
        expectThat(clampY(900f, buttonSize, 0f, 800f)).isEqualTo(752f)
    }

    @Test
    fun `clampY falls back to min when the area is smaller than the button`() {
        expectThat(clampY(10f, buttonSize, 0f, 30f)).isEqualTo(0f)
    }
}
