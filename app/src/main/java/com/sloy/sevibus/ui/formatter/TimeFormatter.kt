package com.sloy.sevibus.ui.formatter

import com.sloy.sevibus.domain.model.Route
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object TimeFormatter {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun hourMinutes(time: LocalTime): String {
        return time.format(timeFormatter)
    }

    fun schedule(schedule: Route.Schedule): String {
        return hourMinutes(schedule.startTime) + " — " + hourMinutes(schedule.endTime)
    }

}
