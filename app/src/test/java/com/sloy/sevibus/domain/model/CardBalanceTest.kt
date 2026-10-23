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

    private fun trip(fare: Int, date: LocalDateTime = later, people: Int = 1) =
        CardTransaction.Trip(1, date, line, people, Fare(Money(fare), Money(500)))

    private fun card(balance: Int) = CardInfo.BalanceCard(serialNumber = 1, code = 31, type = "Bonobús", balance = Money(balance))

    @Test
    fun `balance just below the threshold is low`() {
        expectThat(card(99).isLowBalance).isTrue()
    }

    @Test
    fun `balance at the threshold is not low`() {
        expectThat(card(100).isLowBalance).isFalse()
    }

    @Test
    fun `negative balance is low and empty`() {
        expectThat(card(-45).isLowBalance).isTrue()
        expectThat(card(-45).isEmpty).isTrue()
    }

    @Test
    fun `zero balance is empty`() {
        expectThat(card(0).isEmpty).isTrue()
        expectThat(card(1).isEmpty).isFalse()
    }

    @Test
    fun `trips use the most recent paid trip`() {
        val transactions = listOf(trip(100, earlier), trip(41, later))
        expectThat(estimatedTrips(Money(150), transactions)).isEqualTo(4)
    }

    @Test
    fun `trips skip free trips`() {
        val transactions = listOf(trip(0, later), trip(50, earlier))
        expectThat(estimatedTrips(Money(150), transactions)).isEqualTo(3)
    }

    @Test
    fun `trips use the fare per person of a group trip`() {
        expectThat(estimatedTrips(Money(150), listOf(trip(100, people = 2)))).isEqualTo(3)
    }

    @Test
    fun `trips ignore pass trips, transfers and top ups`() {
        val transactions = listOf(
            CardTransaction.TopUp(1, later, Money(1000), Money(1150)),
            CardTransaction.Transfer(1, later, line),
            CardTransaction.Trip(1, later, line, 1, fare = null),
        )
        expectThat(estimatedTrips(Money(150), transactions)).isNull()
    }

    @Test
    fun `trips are unavailable without transactions`() {
        expectThat(estimatedTrips(Money(150), emptyList())).isNull()
    }

    @Test
    fun `a positive balance below the trip cost still allows one trip`() {
        expectThat(estimatedTrips(Money(15), listOf(trip(40)))).isEqualTo(1)
    }

    @Test
    fun `a partial last trip counts as a trip`() {
        expectThat(estimatedTrips(Money(60), listOf(trip(40)))).isEqualTo(2)
    }

    @Test
    fun `trips are unavailable with a zero balance`() {
        expectThat(estimatedTrips(Money(0), listOf(trip(40)))).isNull()
    }

    @Test
    fun `trips are unavailable with a negative balance`() {
        expectThat(estimatedTrips(Money(-45), listOf(trip(41)))).isNull()
    }
}
