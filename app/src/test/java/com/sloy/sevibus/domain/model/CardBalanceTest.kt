package com.sloy.sevibus.domain.model

import com.sloy.sevibus.Stubs
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNull
import strikt.assertions.isTrue
import java.time.LocalDateTime

class CardBalanceTest {

    private val line = Stubs.lines[0].toSummary()
    private val earlier = LocalDateTime.of(2026, 3, 1, 8, 0)
    private val later = LocalDateTime.of(2026, 3, 10, 8, 0)

    private fun validation(amount: Int, date: LocalDateTime = later) =
        CardTransaction.Validation(1, date, amount, line, bus = 0, people = 1)

    private fun card(balance: Int?) = CardInfo(serialNumber = 1, code = 31, type = "Bonobús", balance = balance)

    @Test
    fun `card without balance has no balance and is not low`() {
        expectThat(card(null).hasBalance).isFalse()
        expectThat(card(null).isLowBalance).isFalse()
    }

    @Test
    fun `balance just below the threshold is low`() {
        expectThat(card(99).isLowBalance).isTrue()
    }

    @Test
    fun `balance at the threshold is not low`() {
        expectThat(card(100).isLowBalance).isFalse()
    }

    @Test
    fun `negative balance is low`() {
        expectThat(card(-45).isLowBalance).isTrue()
    }

    @Test
    fun `trips use the most recent validation with an amount`() {
        val transactions = listOf(validation(100, earlier), validation(41, later))
        expectThat(estimatedTrips(150, transactions)).isEqualTo(4)
    }

    @Test
    fun `trips skip validations without amount`() {
        val transactions = listOf(validation(0, later), validation(50, earlier))
        expectThat(estimatedTrips(150, transactions)).isEqualTo(3)
    }

    @Test
    fun `trips use the absolute amount of the validation`() {
        expectThat(estimatedTrips(150, listOf(validation(-50)))).isEqualTo(3)
    }

    @Test
    fun `trips ignore transfers and top ups`() {
        val transactions = listOf(
            CardTransaction.TopUp(1, later, 1000),
            CardTransaction.Transfer(1, later, line, bus = 0, people = 1),
        )
        expectThat(estimatedTrips(150, transactions)).isNull()
    }

    @Test
    fun `trips are unavailable without transactions`() {
        expectThat(estimatedTrips(150, emptyList())).isNull()
    }

    @Test
    fun `a positive balance below the trip cost still allows one trip`() {
        expectThat(estimatedTrips(15, listOf(validation(40)))).isEqualTo(1)
    }

    @Test
    fun `a partial last trip counts as a trip`() {
        expectThat(estimatedTrips(60, listOf(validation(40)))).isEqualTo(2)
    }

    @Test
    fun `trips are unavailable with a zero balance`() {
        expectThat(estimatedTrips(0, listOf(validation(40)))).isNull()
    }

    @Test
    fun `trips are unavailable with a negative balance`() {
        expectThat(estimatedTrips(-45, listOf(validation(41)))).isNull()
    }
}
