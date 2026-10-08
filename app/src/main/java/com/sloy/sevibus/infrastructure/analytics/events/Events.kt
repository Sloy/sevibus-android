package com.sloy.sevibus.infrastructure.analytics.events

import com.sloy.sevibus.domain.model.LineId
import com.sloy.sevibus.domain.model.RouteId
import com.sloy.sevibus.domain.model.StopId
import com.sloy.sevibus.infrastructure.analytics.SevEvent
import com.sloy.sevibus.infrastructure.analytics.analyticsValue
import com.sloy.sevibus.infrastructure.nightmode.NightModeSetting

interface Events {
    data object AppStarted : SevEvent(
        "App Started"
    )

    data class AppOpenedFromNfc(val launchType: LaunchType) : SevEvent(
        "App Opened From NFC",
        "launchType" to launchType.analyticsValue,
    ) {
        enum class LaunchType { COLD, WARM }
    }

    data class SessionSummary(
        val sessionType: SessionType,
        val durationSeconds: Long,
        val stopViews: Int,
        val distinctStops: Int,
        val arrivalsViews: Int,
        val entrySources: List<String>,
        val featuresUsed: List<String>,
        val lastScreen: Screen,
    ) : SevEvent(
        "Session Summary",
        "sessionType" to sessionType.analyticsValue,
        "durationSeconds" to durationSeconds,
        "stopViews" to stopViews,
        "distinctStops" to distinctStops,
        "arrivalsViews" to arrivalsViews,
        "entrySources" to entrySources,
        "featuresUsed" to featuresUsed,
        "lastScreen" to lastScreen.analyticsValue,
    ) {
        enum class SessionType { GLANCER, WAITER, EXPLORER, CARD_CHECKER, OTHER }
        enum class Screen { FOR_YOU, LINES, LINE_STOPS, STOP_DETAIL, CARDS, CARDS_HELP, EDIT_FAVORITES, SEARCH, SETTINGS }
    }

    data object AppUpdateAvailable : SevEvent(
        "App Update Available"
    )

    data class ArrivalsDisplayed(
        val screen: ArrivalsScreen,
        val stopId: StopId?,
        val stopCount: Int?,
        val arrivalsCount: Int,
        val latencyMs: Long,
    ) : SevEvent(
        "Arrivals Displayed",
        "screen" to screen.analyticsValue,
        "stopId" to stopId,
        "stopCount" to stopCount,
        "arrivalsCount" to arrivalsCount,
        "latencyMs" to latencyMs,
    )

    data class ArrivalsFailed(val screen: ArrivalsScreen, val stopId: StopId?, val errorType: ErrorType) : SevEvent(
        "Arrivals Failed",
        "screen" to screen.analyticsValue,
        "stopId" to stopId,
        "errorType" to errorType.analyticsValue,
    ) {
        enum class ErrorType { NETWORK, TIMEOUT, SERVER, UNKNOWN }
    }

    enum class ArrivalsScreen { STOP_DETAIL, FAVORITES, NEARBY }

    data class RouteDirectionSwitched(val lineId: LineId, val routeId: RouteId) : SevEvent(
        "Route Direction Switched",
        "lineId" to lineId,
        "routeId" to routeId,
    )

    data object MapExplored : SevEvent(
        "Map Explored"
    )

    data class BottomSheetChanged(val state: SheetState) : SevEvent(
        "Bottom Sheet Changed",
        "state" to state.analyticsValue,
    ) {
        enum class SheetState { COLLAPSED, PARTIAL, EXPANDED }
    }

    data class LocationPermissionResult(val result: Result, val context: Context) : SevEvent(
        "Location Permission Result",
        "result" to result.analyticsValue,
        "context" to context.analyticsValue,
    ) {
        enum class Result { GRANTED, DENIED }
        enum class Context { NEARBY, MAP }
    }

    data class SearchPerformed(
        val queryLength: Int,
        val resultsCount: Int,
        val stopResults: Int,
        val lineResults: Int,
        val query: String?,
    ) : SevEvent(
        "Search Performed",
        "queryLength" to queryLength,
        "resultsCount" to resultsCount,
        "stopResults" to stopResults,
        "lineResults" to lineResults,
        "query" to query,
    )

    data class EditFavoritesSaved(
        val renamed: Int,
        val iconChanged: Int,
        val deleted: Int,
        val linesChanged: Int,
        val reordered: Boolean,
    ) : SevEvent(
        "Edit Favorites Saved",
        "renamed" to renamed,
        "iconChanged" to iconChanged,
        "deleted" to deleted,
        "linesChanged" to linesChanged,
        "reordered" to reordered,
    )

    data object EditFavoritesCancelled : SevEvent(
        "Edit Favorites Cancelled"
    )

    enum class LoginTrigger { FAVORITE, SETTINGS }

    data class LoginPromptShown(val trigger: LoginTrigger) : SevEvent(
        "Login Prompt Shown",
        "trigger" to trigger.analyticsValue,
    )

    data class LoginStarted(val trigger: LoginTrigger) : SevEvent(
        "Login Started",
        "trigger" to trigger.analyticsValue,
    )

    data class LoginCompleted(val trigger: LoginTrigger) : SevEvent(
        "Login Completed",
        "trigger" to trigger.analyticsValue,
    )

    data class LoginFailed(val trigger: LoginTrigger, val reason: Reason) : SevEvent(
        "Login Failed",
        "trigger" to trigger.analyticsValue,
        "reason" to reason.analyticsValue,
    ) {
        enum class Reason { CANCELLED, NO_CREDENTIALS, NETWORK, UNKNOWN }
    }

    data class CardAlertDisplayed(val balanceType: String) : SevEvent(
        "Card Alert Displayed",
        "balanceType" to balanceType
    )

    data class LinePathsDisplayed(val pathCount: Int) : SevEvent(
        "Line Paths Displayed",
        "pathCount" to pathCount
    )

    data object ReviewDialogRequested : SevEvent(
        "Review Dialog Requested"
    )

    data class ReviewDialogDismissed(val durationSeconds: Int) : SevEvent(
        "Review Dialog Dismissed",
        "durationSeconds" to durationSeconds
    )

    data class ReviewDialogFailed(val reason: String) : SevEvent(
        "Review Dialog Failed",
        "reason" to reason
    )

    data class CardAdded(val cardType: String) : SevEvent(
        "Card Added",
        "cardType" to cardType
    )

    data class CardDeleted(val cardType: String) : SevEvent(
        "Card Deleted",
        "cardType" to cardType
    )

    data class CardsReordered(val cardsCount: Int) : SevEvent(
        "Cards Reordered",
        "cardsCount" to cardsCount
    )

    data class CardScanned(val scanMethod: ScanMethod) : SevEvent(
        "Card Scanned",
        "scanMethod" to scanMethod.analyticsValue
    ) {
        enum class ScanMethod {
            NFC,
            MANUAL
        }
    }

    data class CardCheckCompleted(val scanMethod: CardScanned.ScanMethod, val result: Result) : SevEvent(
        "Card Check Completed",
        "scanMethod" to scanMethod.analyticsValue,
        "result" to result.analyticsValue,
    ) {
        enum class Result { ADDED, ALREADY_SAVED, NOT_FOUND, ERROR }
    }

    data class NightModeChanged(val mode: NightModeSetting) : SevEvent(
        "Night Mode Changed",
        "mode" to mode.analyticsValue,
    )
}

fun Boolean.toPermissionResult(): Events.LocationPermissionResult.Result =
    if (this) Events.LocationPermissionResult.Result.GRANTED else Events.LocationPermissionResult.Result.DENIED
