package com.sloy.sevibus.data.api

import com.sloy.sevibus.data.api.model.CardAlertDto
import com.sloy.sevibus.data.api.model.CardInfoDto
import com.sloy.sevibus.data.api.model.FavoriteStopDto
import com.sloy.sevibus.data.api.model.LoggedUserDto
import com.sloy.sevibus.domain.model.CardId
import com.sloy.sevibus.domain.model.CardInfo
import com.sloy.sevibus.domain.model.StopId
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Requires authorization header with the id token from firebase auth.
 */
interface SevibusUserApi {

    @POST("login")
    suspend fun login(@Body user: LoggedUserDto)

    @GET("favorites")
    suspend fun obtainFavorites(): List<FavoriteStopDto>

    @PUT("favorites/{stop}")
    suspend fun addFavorite(@Path("stop") stopId: StopId, @Body stop: FavoriteStopDto)

    @DELETE("favorites/{stop}")
    suspend fun deleteFavorite(@Path("stop") stop: StopId)

    @POST("favorites")
    suspend fun replaceFavorites(@Body favorites: List<FavoriteStopDto>)

    @GET("user/cards")
    suspend fun obtainUserCards(): List<CardInfoDto>

    @POST("user/addUpdateCard/{cardId}")
    suspend fun addUpdateUserCard(@Path("cardId") cardId: CardId, @Body card: CardInfoDto)

    @POST("user/replaceCards")
    suspend fun replaceUserCards(@Body cards: List<CardInfoDto>)

    @DELETE("user/deleteCard/{card}")
    suspend fun deleteUserCard(@Path("card") card: CardId)

    @GET("user/cards/alerts")
    suspend fun getUserCardAlerts(): List<CardAlertDto>

}
