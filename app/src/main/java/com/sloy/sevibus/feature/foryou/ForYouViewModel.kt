package com.sloy.sevibus.feature.foryou

import androidx.lifecycle.ViewModel
import com.sloy.sevibus.infrastructure.analytics.SevEvent
import com.sloy.sevibus.infrastructure.analytics.Tracker
import com.sloy.sevibus.infrastructure.analytics.events.Clicks
import kotlinx.coroutines.flow.MutableStateFlow

class ForYouViewModel(
    private val tracker: Tracker,
) : ViewModel() {

    val selectedTabIndex = MutableStateFlow(0)

    fun onTabSelected(index: Int) {
        if (selectedTabIndex.value != index) {
            selectedTabIndex.value = index
            val tabName = when (index) {
                0 -> "favorites"
                1 -> "nearby"
                else -> "unknown"
            }
            onTrack(Clicks.ForYouTabClicked(tabName))
        }
    }

    fun onTrack(event: SevEvent) {
        tracker.track(event)
    }
}