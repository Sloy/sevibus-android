package com.sloy.sevibus.data.api.model

import com.sloy.sevibus.domain.model.CardInfo
import com.sloy.sevibus.domain.model.Money
import com.sloy.sevibus.domain.model.NextPeriod
import com.sloy.sevibus.domain.model.PassPeriod
import com.sloy.sevibus.domain.model.PassValidity
import kotlinx.serialization.json.Json
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.time.LocalDate

class CardDtoMappersTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `maps an active pass with a next period starting on first use`() {
        val dto = json.decodeFromString<CardInfoDto>(
            """{"serialNumber":900000001,"code":154,"type":"Familia numerosa","balance":null,"kind":"pass","expired":false,
               "pass":{"from":"2026-09-14","to":"2026-10-13","nextPeriod":{"type":"on_first_use"}},"expiresOn":"2028-05-21"}"""
        )

        expectThat(dto.toDomain()).isEqualTo(
            CardInfo.PassCard(
                900000001, 154, "Familia numerosa",
                PassValidity(PassPeriod(LocalDate.of(2026, 9, 14), LocalDate.of(2026, 10, 13)), NextPeriod.OnFirstUse),
                expiresOn = LocalDate.of(2028, 5, 21),
            )
        )
    }

    @Test
    fun `maps a scheduled next period`() {
        val pass = CardPassDto("2026-10-01", "2026-10-31", CardNextPeriodDto("scheduled", "2026-11-01", "2026-11-30"))

        expectThat(pass.toDomain()?.nextPeriod)
            .isEqualTo(NextPeriod.Scheduled(PassPeriod(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30))))
    }

    @Test
    fun `ignores a next period of an unknown type`() {
        val pass = CardPassDto("2026-10-01", "2026-10-31", CardNextPeriodDto("something_new"))

        expectThat(pass.toDomain()?.nextPeriod).isEqualTo(null)
    }

    @Test
    fun `maps a zero balance card as a balance card`() {
        val card = CardInfoDto(serialNumber = 900000002, code = 31, type = "Bonobús sin transbordo", balance = 0, kind = "balance").toDomain()

        expectThat(card).isEqualTo(CardInfo.BalanceCard(900000002, 31, "Bonobús sin transbordo", Money(0)))
    }

    @Test
    fun `maps an expired card with its expiry date`() {
        val card = CardInfoDto(serialNumber = 900000003, code = 116, type = "Estudiante mensual", kind = "pass", expired = true, expiresOn = "2025-09-30").toDomain()

        expectThat(card).isEqualTo(CardInfo.ExpiredCard(900000003, 116, "Estudiante mensual", expiresOn = LocalDate.of(2025, 9, 30)))
    }

    @Test
    fun `maps a pass whose period can't be read without validity`() {
        val card = CardInfoDto(serialNumber = 900000006, code = 150, type = "x", kind = "pass", pass = CardPassDto("6/10/2026", "2026-11-04")).toDomain()

        expectThat(card).isEqualTo(CardInfo.PassCard(900000006, 150, "x", validity = null))
    }
}
