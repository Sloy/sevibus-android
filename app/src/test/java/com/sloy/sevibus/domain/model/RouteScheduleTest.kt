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

    @Test
    fun `isActiveWithMargin returns true within the margin before start time`() {
        val schedule = Route.Schedule(LocalTime.of(9, 0), LocalTime.of(17, 0))

        expectThat(schedule.isActiveWithMargin(LocalTime.of(8, 0))).isTrue()
        expectThat(schedule.isActiveWithMargin(LocalTime.of(7, 59))).isFalse()
    }

    @Test
    fun `isActiveWithMargin returns true within the margin after end time`() {
        val schedule = Route.Schedule(LocalTime.of(9, 0), LocalTime.of(17, 0))

        expectThat(schedule.isActiveWithMargin(LocalTime.of(18, 0))).isTrue()
        expectThat(schedule.isActiveWithMargin(LocalTime.of(18, 1))).isFalse()
    }

    @Test
    fun `isActiveWithMargin handles margin crossing midnight`() {
        val daySchedule = Route.Schedule(LocalTime.of(6, 0), LocalTime.of(23, 30))

        expectThat(daySchedule.isActiveWithMargin(LocalTime.of(0, 15))).isTrue()
        expectThat(daySchedule.isActiveWithMargin(LocalTime.of(3, 0))).isFalse()
        expectThat(daySchedule.isActiveWithMargin(LocalTime.of(12, 0))).isTrue()
    }

    @Test
    fun `isActiveWithMargin handles night schedule`() {
        val nightSchedule = Route.Schedule(LocalTime.of(23, 45), LocalTime.of(5, 30))

        expectThat(nightSchedule.isActiveWithMargin(LocalTime.of(3, 0))).isTrue()
        expectThat(nightSchedule.isActiveWithMargin(LocalTime.of(22, 50))).isTrue()
        expectThat(nightSchedule.isActiveWithMargin(LocalTime.of(12, 0))).isFalse()
    }

    @Test
    fun `isActiveWithMargin returns true when the widened schedule covers the whole day`() {
        val schedule = Route.Schedule(LocalTime.of(0, 0), LocalTime.of(23, 30))

        expectThat(schedule.isActiveWithMargin(LocalTime.of(12, 0))).isTrue()
        expectThat(schedule.isActiveWithMargin(LocalTime.of(23, 45))).isTrue()
    }

    @Test
    fun `isActiveWithMargin returns true when the schedule is unknown`() {
        val schedule = Route.Schedule(LocalTime.of(10, 0), LocalTime.of(10, 0))

        expectThat(schedule.isActiveWithMargin(LocalTime.of(3, 0))).isTrue()
    }
}
