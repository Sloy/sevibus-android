package com.sloy.sevibus.data.api.model

import com.sloy.sevibus.domain.model.CardInfo
import com.sloy.sevibus.domain.model.Money
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class CardDtoMappersTest {

    @Test
    fun `a card with balance maps to a balance card`() {
        val dto = CardInfoDto(serialNumber = 1, code = 31, type = "Bonobús", balance = 250, customName = "Mine")

        expectThat(dto.fromDto()).isEqualTo(CardInfo.BalanceCard(1, 31, "Bonobús", Money(250), "Mine"))
    }

    @Test
    fun `a card without balance maps to a pass card`() {
        val dto = CardInfoDto(serialNumber = 2, code = 150, type = "Tarjeta 30 días", balance = null)

        expectThat(dto.fromDto()).isEqualTo(CardInfo.PassCard(2, 150, "Tarjeta 30 días"))
    }
}
