package com.sloy.sevibus.infrastructure.analytics.events

import com.sloy.sevibus.infrastructure.analytics.SevEvent

interface Events {
    data object AppStarted : SevEvent(
        "App Started"
    )

    data object AppUpdateAvailable : SevEvent(
        "App Update Available"
    )

    data class CardAlertDisplayed(val balanceType: String) : SevEvent(
        "Card Alert Displayed",
        "balanceType" to balanceType
    )

    data class ReviewDialogDismissed(val durationSeconds: Int) : SevEvent(
        "Review Dialog Dismissed",
        "duration" to durationSeconds
    )

    data class ReviewDialogFailed(val reason: String) : SevEvent(
        "Review Dialog Failed",
        "reason" to reason
    )
}

