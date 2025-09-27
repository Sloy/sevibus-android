package com.sloy.sevibus.domain.model

import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isFalse
import strikt.assertions.isTrue
import java.time.LocalTime

class RouteScheduleTest {

    @Test
    fun `isCurrentyActive returns true when current time is between start and end time`() {
        val schedule = Route.Schedule(
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(17, 0)
        )
        val currentTime = LocalTime.of(12, 30)

        val result = schedule.isCurrentyActive(currentTime)

        expectThat(result).isTrue()
    }

    @Test
    fun `isCurrentyActive returns false when current time is before start time`() {
        val schedule = Route.Schedule(
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(17, 0)
        )
        val currentTime = LocalTime.of(8, 30)

        val result = schedule.isCurrentyActive(currentTime)

        expectThat(result).isFalse()
    }

    @Test
    fun `isCurrentyActive returns false when current time is after end time`() {
        val schedule = Route.Schedule(
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(17, 0)
        )
        val currentTime = LocalTime.of(18, 30)

        val result = schedule.isCurrentyActive(currentTime)

        expectThat(result).isFalse()
    }

    @Test
    fun `isCurrentyActive handles cross-midnight schedule correctly`() {
        val schedule = Route.Schedule(
            startTime = LocalTime.of(22, 0),
            endTime = LocalTime.of(2, 0)
        )
        val currentTime = LocalTime.of(1, 0)

        val result = schedule.isCurrentyActive(currentTime)

        expectThat(result).isTrue()
    }
}