package com.sloy.debugmenu.events.viewer

import com.sloy.debugmenu.events.CapturedEvent
import java.time.LocalDateTime
import java.time.ZoneId

internal object ViewerSampleData {
    val startMillis: Long = LocalDateTime.of(2026, 10, 8, 16, 21, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private val DATA: List<Triple<Long, String, Map<String, String>>> = listOf(
        Triple(1_050, "App Started", emptyMap()),
        Triple(1_120, "For You Viewed", mapOf("trigger" to "launch")),
        Triple(1_910, "Arrivals Displayed", mapOf("screen" to "favorites", "stopCount" to "3", "arrivalsCount" to "9", "latencyMs" to "388")),
        Triple(2_400, "Lines Viewed", mapOf("trigger" to "navigation")),
        Triple(2_850, "Line Paths Displayed", mapOf("pathCount" to "15")),
        Triple(3_600, "Map Explored", emptyMap()),
        Triple(4_240, "Map Stop Clicked", mapOf("stopId" to "412")),
        Triple(4_254, "Stop Details Viewed", mapOf("stopId" to "412", "source" to "map", "trigger" to "navigation")),
        Triple(4_900, "Arrivals Displayed", mapOf("screen" to "stop_detail", "stopId" to "412", "arrivalsCount" to "6", "latencyMs" to "301")),
        Triple(4_910, "Bottom Sheet Changed", mapOf("state" to "partial")),
        Triple(7_480, "Stop Details Closed", mapOf("stopId" to "412", "durationSeconds" to "3", "refreshes" to "0")),
        Triple(7_500, "For You Viewed", mapOf("trigger" to "navigation")),
        Triple(8_100, "Arrivals Displayed", mapOf("screen" to "favorites", "stopCount" to "3", "arrivalsCount" to "9", "latencyMs" to "405")),
        Triple(9_320, "Favorite Stop Clicked", mapOf("stopId" to "136")),
        Triple(9_336, "Stop Details Viewed", mapOf("stopId" to "136", "source" to "favorites", "trigger" to "navigation")),
        Triple(9_950, "Arrivals Displayed", mapOf("screen" to "stop_detail", "stopId" to "136", "arrivalsCount" to "4", "latencyMs" to "212")),
        Triple(12_700, "Stop Details Closed", mapOf("stopId" to "136", "durationSeconds" to "3", "refreshes" to "0")),
        Triple(12_720, "For You Viewed", mapOf("trigger" to "back")),
        Triple(
            15_000, "Session Summary", mapOf(
                "sessionType" to "explorer", "durationSeconds" to "14", "stopViews" to "2", "distinctStops" to "2",
                "arrivalsViews" to "4", "entrySources" to "[map, favorites]", "featuresUsed" to "[lines, map, favorites]",
                "lastScreen" to "for_you",
            )
        ),
        Triple(145_300, "Arrivals Displayed", mapOf("screen" to "favorites", "stopCount" to "3", "arrivalsCount" to "8", "latencyMs" to "420")),
    )

    fun id(index: Int): String = "sample-$index"

    val events: List<CapturedEvent> = DATA.mapIndexed { index, (at, name, properties) ->
        CapturedEvent(name, properties, startMillis + at, id(index))
    }.asReversed()

    val expandedEventId: String = id(12)
    val sessionSummaryId: String = id(18)
}
