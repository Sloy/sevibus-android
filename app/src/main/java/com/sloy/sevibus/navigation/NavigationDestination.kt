package com.sloy.sevibus.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.sloy.sevibus.R
import com.sloy.sevibus.domain.model.CardId
import com.sloy.sevibus.domain.model.LineId
import com.sloy.sevibus.domain.model.RouteId
import com.sloy.sevibus.domain.model.StopId
import com.sloy.sevibus.ui.icons.CardOutline
import com.sloy.sevibus.ui.icons.DirectionsBusFill
import com.sloy.sevibus.ui.icons.DirectionsBusOutline
import com.sloy.sevibus.ui.icons.SevIcons
import com.sloy.sevibus.ui.icons.ShimmerFill
import com.sloy.sevibus.ui.icons.ShimmerOutline
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

sealed interface NavigationDestination {
    val type: NavigationDestinationType

    @Serializable
    data object ForYou : TopLevelDestination {
        override val selectedIcon = SevIcons.ShimmerFill
        override val unselectedIcon = SevIcons.ShimmerOutline
        override val iconTextId = R.string.navigation_for_you
        override val type: NavigationDestinationType = NavigationDestinationType.MAP_BOTTOM_SHEET
    }

    @Serializable
    data object Lines : TopLevelDestination {
        override val selectedIcon = SevIcons.DirectionsBusFill
        override val unselectedIcon = SevIcons.DirectionsBusOutline
        override val iconTextId = R.string.navigation_lines
        override val type: NavigationDestinationType = NavigationDestinationType.MAP_BOTTOM_SHEET
    }

    @Serializable
    data class Cards(val cardId: CardId? = null) : TopLevelDestination {
        @Transient
        override val selectedIcon = SevIcons.CardOutline

        @Transient
        override val unselectedIcon = SevIcons.CardOutline
        override val iconTextId = R.string.navigation_cards
        override val type: NavigationDestinationType = NavigationDestinationType.FULL_SCREEN
    }


    @Serializable
    data object CardsHelp : NavigationDestination {
        override val type: NavigationDestinationType = NavigationDestinationType.FULL_SCREEN
    }

    @Serializable
    data class LineStops(val lineId: LineId, val routeId: RouteId? = null, val highlightedStop: StopId? = null) : NavigationDestination {
        override val type: NavigationDestinationType = NavigationDestinationType.MAP_BOTTOM_SHEET
    }

    @Serializable
    data class StopDetail(val stopId: StopId, val highlightedLine: LineId? = null) : NavigationDestination {
        override val type: NavigationDestinationType = NavigationDestinationType.MAP_BOTTOM_SHEET
    }

    @Serializable
    data object Settings : NavigationDestination {
        override val type: NavigationDestinationType = NavigationDestinationType.FULL_SCREEN
    }

    @Serializable
    data object Search : NavigationDestination {
        override val type: NavigationDestinationType = NavigationDestinationType.FULL_SCREEN
    }

    @Serializable
    data object EditFavorites : NavigationDestination {
        override val type: NavigationDestinationType = NavigationDestinationType.FULL_SCREEN
    }

}

interface TopLevelDestination : NavigationDestination {
    val selectedIcon: ImageVector
    val unselectedIcon: ImageVector
    val iconTextId: Int

    companion object {
        val topLevelDestinations: List<TopLevelDestination> = listOf(
            NavigationDestination.ForYou,
            NavigationDestination.Lines,
            NavigationDestination.Cards(),
        )
    }
}

enum class NavigationDestinationType {
    MAP_BOTTOM_SHEET, FULL_SCREEN,
}

fun NavigationDestination.isBottomBarVisible() = this is TopLevelDestination
fun NavigationDestination.isTopBarVisible() =
    this.type == NavigationDestinationType.MAP_BOTTOM_SHEET || this is NavigationDestination.Search
