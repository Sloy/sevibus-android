package com.sloy.sevibus.data.database

import com.sloy.sevibus.data.api.model.CardInfoDto
import com.sloy.sevibus.data.api.model.NextPeriodDto
import com.sloy.sevibus.domain.model.CardInfo
import com.sloy.sevibus.domain.model.CardStatus
import com.sloy.sevibus.domain.model.NextPeriod
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.time.LocalDate

class CardEntityMappersTest {

    @Test
    fun `round trips a pass through the entity`() {
        val card = CardInfo(900000001, 154, "Familia numerosa",
            CardStatus.Pass(LocalDate.of(2026, 9, 14), LocalDate.of(2026, 10, 13), NextPeriod(null, null, true)),
            customName = "Casa", cardExpiry = LocalDate.of(2028, 5, 21), resultCode = 0)

        expectThat(card.toEntity(order = 3).fromEntity()).isEqualTo(card)
    }

    @Test
    fun `round trips balance and expired cards`() {
        val balance = CardInfo(900000002, 30, "Bonobús con transbordo", CardStatus.Balance(-44, alertCode = 2), resultCode = 0)
        val expired = CardInfo(900000003, 116, "Estudiante mensual", CardStatus.ExpiredCard, cardExpiry = LocalDate.of(2025, 9, 30), resultCode = 9)

        expectThat(balance.toEntity(0).fromEntity()).isEqualTo(balance)
        expectThat(expired.toEntity(0).fromEntity()).isEqualTo(expired)
    }

    @Test
    fun `reads a row cached before the migration like today`() {
        val legacyRow = CardInfoEntity(serialNumber = 900000004, code = 31, type = "Bonobús", balance = 845, customName = null, order = 0)

        expectThat(legacyRow.fromEntity().status).isEqualTo(CardStatus.Balance(845))
    }

    @Test
    fun `stores the remote user card status`() {
        val dto = CardInfoDto(900000005, 150, "Tarjeta 30 días", kind = "pass", validFrom = "2026-10-06", validTo = "2026-11-04",
            nextPeriod = NextPeriodDto("2026-11-05", "2026-12-04", false))

        expectThat(dto.toEntity().fromEntity().status)
            .isEqualTo(CardStatus.Pass(LocalDate.of(2026, 10, 6), LocalDate.of(2026, 11, 4), NextPeriod(LocalDate.of(2026, 11, 5), LocalDate.of(2026, 12, 4), false)))
    }
}
