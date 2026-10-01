package com.ahmeddhibi.caisse.core.format

import com.ahmeddhibi.caisse.domain.model.Money
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MoneyFormatTest {

    @Test
    fun `formats euros with a comma and a non breaking space`() {
        assertThat(Money(0).format()).isEqualTo("0,00 €")
        assertThat(Money(5).format()).isEqualTo("0,05 €")
        assertThat(Money(1250).format()).isEqualTo("12,50 €")
    }

    @Test
    fun `groups thousands`() {
        assertThat(Money(123_456).format()).isEqualTo("1 234,56 €")
        assertThat(Money(100_000_000).format()).isEqualTo("1 000 000,00 €")
    }

    @Test
    fun `keeps the sign of negative amounts`() {
        assertThat(Money(-1250).format()).isEqualTo("-12,50 €")
        assertThat(Money(-5).formatAmount()).isEqualTo("-0,05")
    }

    @Test
    fun `receipt amount has no symbol nor grouping`() {
        assertThat(Money(123_456).formatAmount()).isEqualTo("1234,56")
    }
}
