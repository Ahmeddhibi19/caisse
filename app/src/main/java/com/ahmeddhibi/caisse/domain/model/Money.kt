package com.ahmeddhibi.caisse.domain.model

@JvmInline
value class Money(val cents: Long) : Comparable<Money> {

    operator fun plus(other: Money): Money = Money(cents + other.cents)

    operator fun times(quantity: Int): Money = Money(cents * quantity)

    override fun compareTo(other: Money): Int = cents.compareTo(other.cents)

    companion object {
        val ZERO = Money(0)
    }
}

fun Iterable<Money>.sum(): Money = fold(Money.ZERO) { total, amount -> total + amount }
