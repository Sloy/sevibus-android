package com.sloy.sevibus.feature.map

import com.sloy.sevibus.domain.model.Bus
import com.sloy.sevibus.domain.model.Line
import com.sloy.sevibus.domain.model.LineColor
import com.sloy.sevibus.domain.model.LineSummary
import com.sloy.sevibus.domain.model.Path
import com.sloy.sevibus.domain.model.Position
import com.sloy.sevibus.domain.model.Stop
import org.junit.Assert.assertEquals
import org.junit.Test

class MapScreenStateTest {

    @Test
    fun `selectedStops returns current stop only when it's the only stop in line`() {
        val stop = createStop(1, "Stop 1")
        val lineSelectedState = createLineSelectedState(listOf(stop))
        val state = MapScreenState.StopAndLineSelected(stop, lineSelectedState)

        val result = state.selectedStops()

        assertEquals(listOf(stop), result)
    }

    @Test
    fun `selectedStops returns current and next stop when current is first in line`() {
        val stop1 = createStop(1, "Stop 1")
        val stop2 = createStop(2, "Stop 2")
        val stop3 = createStop(3, "Stop 3")
        val lineStops = listOf(stop1, stop2, stop3)
        val lineSelectedState = createLineSelectedState(lineStops)
        val state = MapScreenState.StopAndLineSelected(stop1, lineSelectedState)

        val result = state.selectedStops()

        assertEquals(listOf(stop1, stop2), result)
    }

    @Test
    fun `selectedStops returns previous and current stop when current is last in line`() {
        val stop1 = createStop(1, "Stop 1")
        val stop2 = createStop(2, "Stop 2")
        val stop3 = createStop(3, "Stop 3")
        val lineStops = listOf(stop1, stop2, stop3)
        val lineSelectedState = createLineSelectedState(lineStops)
        val state = MapScreenState.StopAndLineSelected(stop3, lineSelectedState)

        val result = state.selectedStops()

        assertEquals(listOf(stop2, stop3), result)
    }

    @Test
    fun `selectedStops returns previous, current and next stop when current is in middle`() {
        val stop1 = createStop(1, "Stop 1")
        val stop2 = createStop(2, "Stop 2")
        val stop3 = createStop(3, "Stop 3")
        val stop4 = createStop(4, "Stop 4")
        val lineStops = listOf(stop1, stop2, stop3, stop4)
        val lineSelectedState = createLineSelectedState(lineStops)
        val state = MapScreenState.StopAndLineSelected(stop2, lineSelectedState)

        val result = state.selectedStops()

        assertEquals(listOf(stop1, stop2, stop3), result)
    }

    @Test
    fun `selectedStops returns current and next when only two stops and current is first`() {
        val stop1 = createStop(1, "Stop 1")
        val stop2 = createStop(2, "Stop 2")
        val lineStops = listOf(stop1, stop2)
        val lineSelectedState = createLineSelectedState(lineStops)
        val state = MapScreenState.StopAndLineSelected(stop1, lineSelectedState)

        val result = state.selectedStops()

        assertEquals(listOf(stop1, stop2), result)
    }

    @Test
    fun `selectedStops returns previous and current when only two stops and current is last`() {
        val stop1 = createStop(1, "Stop 1")
        val stop2 = createStop(2, "Stop 2")
        val lineStops = listOf(stop1, stop2)
        val lineSelectedState = createLineSelectedState(lineStops)
        val state = MapScreenState.StopAndLineSelected(stop2, lineSelectedState)

        val result = state.selectedStops()

        assertEquals(listOf(stop1, stop2), result)
    }

    @Test
    fun `selectedStops throws error when selected stop is not found in line`() {
        val stop1 = createStop(1, "Stop 1")
        val stop2 = createStop(2, "Stop 2")
        val stop3 = createStop(3, "Stop 3")
        val unknownStop = createStop(999, "Unknown Stop")
        val lineStops = listOf(stop1, stop2, stop3)
        val lineSelectedState = createLineSelectedState(lineStops)
        val state = MapScreenState.StopAndLineSelected(unknownStop, lineSelectedState)

        val exception = org.junit.Assert.assertThrows(IllegalStateException::class.java) {
            state.selectedStops()
        }

        assertEquals("Stop 999 not found in line 999 stops", exception.message)
    }

    private fun createStop(id: Int, description: String): Stop {
        return Stop(
            code = id,
            description = description,
            position = Position(37.4, -6.0),
            lines = emptyList()
        )
    }

    private fun createLineSelectedState(lineStops: List<Stop>): MapScreenState.LineSelected {
        return MapScreenState.LineSelected(
            otherStops = emptyList(),
            line = Line(
                label = "Test Line",
                description = "Test Line Description",
                color = LineColor.Red
            ),
            lineStops = lineStops,
            path = null,
            buses = emptyList()
        )
    }
}
