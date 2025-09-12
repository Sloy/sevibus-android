package com.sloy.sevibus.infrastructure.analytics.events

import com.sloy.sevibus.infrastructure.analytics.SevEvent

interface Events {
    data object AppStarted : SevEvent(
        "App Started"
    )
}

