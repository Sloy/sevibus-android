package com.sloy.sevibus.domain.model

import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty

class PathTest {

    @Test
    fun `splitting path in the middle should generate two lists including split point`() {
        val path = Path(
            routeId = ROUTE_ID,
            points = listOf(position0, position1, position2, position3),
            line = LINE,
        )

        val (path1, path2) = path.splitBy(position1)

        expectThat(path1).containsExactly(position0, position1)
        expectThat(path2).containsExactly(position1, position2, position3)
    }

    @Test
    fun `splitting path from first point generates one empty list, and one list with all points`() {
        val path = Path(
            routeId = ROUTE_ID,
            points = listOf(position0, position1, position2, position3),
            line = LINE,
        )

        val (path1, path2) = path.splitBy(position0)

        expectThat(path1).isEmpty()
        expectThat(path2).containsExactly(position0, position1, position2, position3)
    }

    @Test
    fun `splitting path from last point generates one list with all points, and one empty list`() {
        val path = Path(
            routeId = ROUTE_ID,
            points = listOf(position0, position1, position2, position3),
            line = LINE,
        )

        val (path1, path2) = path.splitBy(position3)

        expectThat(path1).containsExactly(position0, position1, position2, position3)
        expectThat(path2).isEmpty()
    }
}

private const val ROUTE_ID = "1.1"
private val position0 = Position(0.0, 0.0)
private val position1 = Position(1.0, 1.0)
private val position2 = Position(2.0, 2.0)
private val position3 = Position(3.0, 3.0)
private val LINE = LineSummary(1, "Line 1", LineColor.Red)
