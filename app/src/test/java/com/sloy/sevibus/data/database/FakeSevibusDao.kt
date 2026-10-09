package com.sloy.sevibus.data.database

import com.sloy.sevibus.domain.model.CardId
import com.sloy.sevibus.domain.model.StopId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeSevibusDao : SevibusDao {

    val favorites = MutableStateFlow<List<FavoriteStopEntity>>(emptyList())

    override fun observeFavorites(): Flow<List<FavoriteStopEntity>> = favorites

    override suspend fun getFavorites(): List<FavoriteStopEntity> = favorites.value

    override suspend fun putFavorite(stop: FavoriteStopEntity) {
        favorites.update { current -> current.filterNot { it.stopId == stop.stopId } + stop }
    }

    override suspend fun deleteFavorite(stopId: StopId) {
        favorites.update { current -> current.filterNot { it.stopId == stopId } }
    }

    override suspend fun insertAll(favorites: List<FavoriteStopEntity>) {
        favorites.forEach { putFavorite(it) }
    }

    override suspend fun deleteAllFavorites() {
        favorites.value = emptyList()
    }

    override suspend fun replaceAllFavorites(favorites: List<FavoriteStopEntity>) {
        this.favorites.value = favorites
    }

    override fun observeCards(): Flow<List<CardInfoEntity>> = TODO()
    override suspend fun getCards(): List<CardInfoEntity> = TODO()
    override suspend fun putCard(card: CardInfoEntity) = TODO()
    override suspend fun insertAllCards(cards: List<CardInfoEntity>) = TODO()
    override suspend fun deleteCard(cardId: CardId) = TODO()
    override suspend fun deleteAllCards() = TODO()
    override suspend fun replaceAllCards(cards: List<CardInfoEntity>) = TODO()
    override suspend fun insertDismissedAlert(alert: DismissedAlertEntity) = TODO()
    override suspend fun insertDismissedAlerts(alerts: List<DismissedAlertEntity>) = TODO()
    override suspend fun getDismissedCardIds(): List<CardId> = TODO()
    override fun observeDismissedCardIds(): Flow<List<CardId>> = TODO()
    override suspend fun clearDismissedAlert(cardId: CardId) = TODO()
    override suspend fun clearDismissedAlerts(cardIds: List<CardId>) = TODO()
}
