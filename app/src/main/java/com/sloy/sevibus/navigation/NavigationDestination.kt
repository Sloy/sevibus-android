package com.sloy.sevibus.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.sloy.sevibus.R
import com.sloy.sevibus.domain.model.LineId
import com.sloy.sevibus.domain.model.StopId
import com.sloy.sevibus.ui.icons.CardOutline
import com.sloy.sevibus.ui.icons.DirectionsBusFill
import com.sloy.sevibus.ui.icons.DirectionsBusOutline
import com.sloy.sevibus.ui.icons.MapFill
import com.sloy.sevibus.ui.icons.MapOutline
import com.sloy.sevibus.ui.icons.SevIcons
import com.sloy.sevibus.ui.icons.ShimmerFill
import com.sloy.sevibus.ui.icons.ShimmerOutline
import kotlinx.serialization.Serializable

sealed interface NavigationDestination {

    @Serializable
    data object ForYou : TopLevelDestination {
        override val selectedIcon = SevIcons.ShimmerFill
        override val unselectedIcon = SevIcons.ShimmerOutline
        override val iconTextId = R.string.navigation_for_you
    }

    @Serializable
    data object Lines : TopLevelDestination {
        override val selectedIcon = SevIcons.DirectionsBusFill
        override val unselectedIcon = SevIcons.DirectionsBusOutline
        override val iconTextId = R.string.navigation_lines
    }

    @Serializable
    data object Map : TopLevelDestination {
        override val selectedIcon = SevIcons.MapFill
        override val unselectedIcon = SevIcons.MapOutline
        override val iconTextId = R.string.navigation_map
    }

    @Serializable
    data object Cards : TopLevelDestination {
        override val selectedIcon = SevIcons.CardOutline
        override val unselectedIcon = SevIcons.CardOutline
        override val iconTextId = R.string.navigation_cards
    }

    @Serializable
    data class LineStops(val lineId: LineId) : NavigationDestination

    @Serializable
    data class StopDetail(val stopId: StopId) : NavigationDestination

}

interface TopLevelDestination : NavigationDestination {
    val selectedIcon: ImageVector
    val unselectedIcon: ImageVector
    val iconTextId: Int
}