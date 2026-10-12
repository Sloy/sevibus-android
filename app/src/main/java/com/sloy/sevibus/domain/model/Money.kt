package com.sloy.sevibus.domain.model

@JvmInline
value class Money(val cents: Int) : Comparable<Money> {
    operator fun plus(other: Money): Money = Money(cents + other.cents)
    operator fun div(divisor: Int): Money = Money(cents / divisor)
    operator fun unaryMinus(): Money = Money(-cents)
    val isPositive: Boolean get() = cents > 0
    val isNegative: Boolean get() = cents < 0
    override fun compareTo(other: Money): Int = cents.compareTo(other.cents)
}
