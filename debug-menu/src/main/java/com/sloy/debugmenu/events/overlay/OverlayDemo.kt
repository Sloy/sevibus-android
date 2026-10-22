package com.sloy.debugmenu.events.overlay

import com.sloy.debugmenu.events.CapturedEvent

internal object OverlayDemo {
    private val BATCHES: List<Pair<Long, List<String>>> = listOf(
        700L to listOf("Lines Viewed", "Line Paths Displayed"),
        2_500L to listOf("Map Explored"),
        3_400L to listOf("Map Stop Clicked", "Stop Details Viewed", "Bottom Sheet Changed", "Arrivals Displayed"),
        5_800L to listOf("Arrivals Displayed"),
        6_500L to listOf("Bottom Sheet Changed", "Stop Details Closed"),
        8_000L to listOf("Edit Favorites Clicked", "Edit Favorites Viewed"),
        10_000L to listOf("Edit Favorites Cancelled", "For You Viewed", "Arrivals Displayed", "Arrivals Displayed", "Arrivals Displayed"),
        11_200L to listOf("Favorite Stop Clicked", "Stop Details Viewed"),
        11_700L to listOf("Arrivals Displayed"),
        14_300L to listOf("Lines Viewed", "Line Paths Displayed"),
    )

    fun events(startMillis: Long): List<CapturedEvent> =
        BATCHES.flatMap { (at, names) ->
            names.mapIndexed { index, name -> CapturedEvent(name, timestampMillis = startMillis + at, id = "demo-$at-$index") }
        }.asReversed()
}
