package com.ahmeddhibi.caisse.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MoneyTest {

    @Test
    fun `arithmetic stays in cents`() {
        assertThat(Money(140) + Money(180)).isEqualTo(Money(320))
        assertThat(Money(140) * 3).isEqualTo(Money(420))
    }

    @Test
    fun `sum of an empty list is zero`() {
        assertThat(emptyList<Money>().sum()).isEqualTo(Money.ZERO)
    }

    @Test
    fun `sum adds every amount`() {
        assertThat(listOf(Money(10), Money(20), Money(30)).sum()).isEqualTo(Money(60))
    }

    @Test
    fun `amounts are ordered by value`() {
        assertThat(Money(99)).isLessThan(Money(100))
    }
}
