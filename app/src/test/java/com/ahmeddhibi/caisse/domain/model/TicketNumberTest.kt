package com.ahmeddhibi.caisse.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TicketNumberTest {

    @Test
    fun `prefixes the sequence with the register`() {
        assertThat(TicketNumber(registerNumber = 2, sequence = 137).value).isEqualTo("C02-000137")
    }

    @Test
    fun `keeps every digit beyond the padding`() {
        assertThat(TicketNumber(registerNumber = 123, sequence = 1_234_567).value).isEqualTo("C123-1234567")
    }

    @Test
    fun `two registers never share a ticket number`() {
        val first = TicketNumber(registerNumber = 1, sequence = 12)
        val second = TicketNumber(registerNumber = 2, sequence = 12)

        assertThat(first.value).isNotEqualTo(second.value)
    }

    @Test
    fun `register key is padded to two digits`() {
        assertThat(TicketNumber.registerKey(7)).isEqualTo("C07")
        assertThat(Register(storeId = "store", number = 12).key).isEqualTo("C12")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `sequence starts at one`() {
        TicketNumber(registerNumber = 1, sequence = 0)
    }
}
