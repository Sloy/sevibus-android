package com.sloy.sevibus.infrastructure.analytics.events

import com.sloy.sevibus.infrastructure.analytics.analyticsValue
import com.sloy.sevibus.infrastructure.nightmode.NightModeSetting
import com.sloy.sevibus.infrastructure.nfc.NfcState as DeviceNfcState

sealed class UserProperty(val name: String, val value: Any) {

    data class IsLoggedIn(val isLoggedIn: Boolean) : UserProperty("isLoggedIn", isLoggedIn)

    data class FavoritesCount(val count: Int) : UserProperty("favoritesCount", count)

    data class CardsCount(val count: Int) : UserProperty("cardsCount", count)

    data class LocationPermission(val state: State) : UserProperty("locationPermission", state.analyticsValue) {
        enum class State { GRANTED, DENIED, NOT_ASKED }
    }

    data class NfcState(val state: DeviceNfcState) : UserProperty("nfcState", state.analyticsValue)

    data class NightMode(val mode: NightModeSetting) : UserProperty("nightMode", mode.analyticsValue)

    data class UsageProfile(val profile: Profile) : UserProperty("usageProfile", profile.analyticsValue) {
        enum class Profile { GLANCER, WAITER, EXPLORER, CARD_CHECKER, MIXED }
    }

    data class IsCommuter(val isCommuter: Boolean) : UserProperty("isCommuter", isCommuter)
}
