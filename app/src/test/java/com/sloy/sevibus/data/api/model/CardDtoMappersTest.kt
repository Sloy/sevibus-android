package com.sloy.sevibus.data.api.model

import com.sloy.sevibus.domain.model.CardStatus
import com.sloy.sevibus.domain.model.NextPeriod
import kotlinx.serialization.json.Json
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isNull
import java.time.LocalDate

class CardDtoMappersTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `maps an active pass with a renewal waiting for first use`() {
        val dto = json.decodeFromString<CardInfoDto>(
            """{"serialNumber":900000001,"code":154,"type":"Familia numerosa","balance":null,"kind":"pass","resultCode":0,"alertCode":0,
               "validFrom":"2026-09-14","validTo":"2026-10-13","nextPeriod":{"from":null,"to":null,"pendingFirstUse":true},"cardExpiry":"2028-05-21"}"""
        )

        val card = dto.toDomain()

        expectThat(card.status).isEqualTo(
            CardStatus.Pass(LocalDate.of(2026, 9, 14), LocalDate.of(2026, 10, 13), NextPeriod(null, null, pendingFirstUse = true))
        )
        expectThat(card.cardExpiry).isEqualTo(LocalDate.of(2028, 5, 21))
        expectThat(card.balance).isNull()
    }

    @Test
    fun `maps a zero balance card as a balance card`() {
        val card = CardInfoDto(serialNumber = 900000002, code = 31, type = "Bonobús sin transbordo", balance = 0, kind = "balance").toDomain()

        expectThat(card.status).isEqualTo(CardStatus.Balance(0))
        expectThat(card.balance).isEqualTo(0)
    }

    @Test
    fun `maps an expired card`() {
        val card = CardInfoDto(serialNumber = 900000003, code = 116, type = "Estudiante mensual", kind = "expired_card", resultCode = 9, cardExpiry = "2025-09-30").toDomain()

        expectThat(card.status).isA<CardStatus.ExpiredCard>()
        expectThat(card.cardExpiry).isEqualTo(LocalDate.of(2025, 9, 30))
    }

    @Test
    fun `maps an old backend balance card without kind`() {
        val dto = json.decodeFromString<CardInfoDto>("""{"serialNumber":900000004,"code":30,"type":"Bonobús con transbordo","balance":845,"order":2}""")

        expectThat(dto.toDomain().status).isEqualTo(CardStatus.Balance(845))
    }

    @Test
    fun `maps an old backend pass without kind or dates`() {
        val card = CardInfoDto(serialNumber = 900000005, code = 150, type = "Tarjeta 30 días").toDomain()

        expectThat(card.status).isEqualTo(CardStatus.Pass(null, null, null))
    }

    @Test
    fun `ignores unparseable dates and unknown kinds`() {
        val card = CardInfoDto(serialNumber = 900000006, code = 150, type = "x", kind = "something_new", validFrom = "6/10/2026", validTo = "2026-11-04").toDomain()

        expectThat(card.status).isEqualTo(CardStatus.Pass(null, LocalDate.of(2026, 11, 4), null))
    }
}
