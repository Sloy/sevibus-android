package com.sloy.sevibus.feature.cards

sealed interface CardsScreenEvent {
    data class ShowMessage(val message: String) : CardsScreenEvent
    data class LaunchUri(val uri: String) : CardsScreenEvent
}
