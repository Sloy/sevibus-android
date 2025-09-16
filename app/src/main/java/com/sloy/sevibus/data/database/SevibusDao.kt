package com.sloy.sevibus.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sloy.sevibus.domain.model.CardId
import com.sloy.sevibus.domain.model.StopId
import kotlinx.coroutines.flow.Flow

@Dao
interface SevibusDao {
    @Query("SELECT * FROM favorites ORDER BY `order` ASC")
    fun observeFavorites(): Flow<List<FavoriteStopEntity>>

    @Query("SELECT * FROM favorites ORDER BY `order` ASC")
    suspend fun getFavorites(): List<FavoriteStopEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putFavorite(stop: FavoriteStopEntity)

    @Query("DELETE FROM favorites WHERE stopId = :stopId")
    suspend fun deleteFavorite(stopId: StopId)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(favorites: List<FavoriteStopEntity>)

    @Query("DELETE FROM favorites")
    suspend fun deleteAllFavorites()

    @Transaction
    suspend fun replaceAllFavorites(favorites: List<FavoriteStopEntity>) {
        deleteAllFavorites()
        insertAll(favorites)
    }

    @Query("SELECT * FROM cards ORDER BY `order` ASC")
    fun observeCards(): Flow<List<CardInfoEntity>>

    @Query("SELECT * FROM cards ORDER BY `order` ASC")
    suspend fun getCards(): List<CardInfoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putCard(card: CardInfoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCards(cards: List<CardInfoEntity>)

    @Query("DELETE FROM cards WHERE serialNumber = :cardId")
    suspend fun deleteCard(cardId: CardId)

    @Query("DELETE FROM cards")
    suspend fun deleteAllCards()

    @Transaction
    suspend fun replaceAllCards(cards: List<CardInfoEntity>) {
        deleteAllCards()
        insertAllCards(cards)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDismissedAlert(alert: DismissedAlertEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDismissedAlerts(alerts: List<DismissedAlertEntity>)

    @Query("SELECT cardId FROM dismissed_alerts")
    suspend fun getDismissedCardIds(): List<CardId>

    @Query("SELECT cardId FROM dismissed_alerts")
    fun observeDismissedCardIds(): Flow<List<CardId>>

    @Query("DELETE FROM dismissed_alerts WHERE cardId = :cardId")
    suspend fun clearDismissedAlert(cardId: CardId)

    @Query("DELETE FROM dismissed_alerts WHERE cardId IN (:cardIds)")
    suspend fun clearDismissedAlerts(cardIds: List<CardId>)

}
