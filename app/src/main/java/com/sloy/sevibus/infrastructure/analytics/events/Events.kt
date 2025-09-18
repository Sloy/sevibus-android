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
}

