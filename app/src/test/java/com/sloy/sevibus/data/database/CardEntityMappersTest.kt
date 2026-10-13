package com.sloy.sevibus.data.database

import com.sloy.sevibus.data.api.model.CardInfoDto
import com.sloy.sevibus.data.api.model.CardNextPeriodDto
import com.sloy.sevibus.data.api.model.CardPassDto
import com.sloy.sevibus.domain.model.CardInfo
import com.sloy.sevibus.domain.model.Money
import com.sloy.sevibus.domain.model.NextPeriod
import com.sloy.sevibus.domain.model.PassPeriod
import com.sloy.sevibus.domain.model.PassValidity
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.time.LocalDate

class CardEntityMappersTest {

    @Test
    fun `round trips a pass through the entity`() {
        val card = CardInfo.PassCard(
            900000001, 154, "Familia numerosa",
            PassValidity(PassPeriod(LocalDate.of(2026, 9, 14), LocalDate.of(2026, 10, 13)), NextPeriod.OnFirstUse),
            customName = "Casa", expiresOn = LocalDate.of(2028, 5, 21),
        )

        expectThat(card.toEntity(order = 3).fromEntity()).isEqualTo(card)
    }

    @Test
    fun `round trips balance and expired cards`() {
        val balance = CardInfo.BalanceCard(900000002, 30, "Bonobús con transbordo", Money(-44))
        val expired = CardInfo.ExpiredCard(900000003, 116, "Estudiante mensual", expiresOn = LocalDate.of(2025, 9, 30))

        expectThat(balance.toEntity(0).fromEntity()).isEqualTo(balance)
        expectThat(expired.toEntity(0).fromEntity()).isEqualTo(expired)
    }

    @Test
    fun `reads rows cached before the migration by their balance`() {
        val balanceRow = CardInfoEntity(serialNumber = 900000004, code = 31, type = "Bonobús", balance = 845, order = 0)
        val passRow = CardInfoEntity(serialNumber = 900000005, code = 150, type = "Tarjeta 30 días", order = 1)

        expectThat(balanceRow.fromEntity()).isEqualTo(CardInfo.BalanceCard(900000004, 31, "Bonobús", Money(845)))
        expectThat(passRow.fromEntity()).isEqualTo(CardInfo.PassCard(900000005, 150, "Tarjeta 30 días", validity = null))
    }

    @Test
    fun `stores the remote user card pass`() {
        val dto = CardInfoDto(
            900000005, 150, "Tarjeta 30 días", kind = "pass",
            pass = CardPassDto("2026-10-06", "2026-11-04", CardNextPeriodDto("scheduled", "2026-11-05", "2026-12-04")),
        )

        expectThat(dto.toEntity().fromEntity()).isEqualTo(
            CardInfo.PassCard(
                900000005, 150, "Tarjeta 30 días",
                PassValidity(
                    PassPeriod(LocalDate.of(2026, 10, 6), LocalDate.of(2026, 11, 4)),
                    NextPeriod.Scheduled(PassPeriod(LocalDate.of(2026, 11, 5), LocalDate.of(2026, 12, 4))),
                ),
            )
        )
    }
}
