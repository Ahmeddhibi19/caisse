package com.ahmeddhibi.caisse.domain.printing

import com.ahmeddhibi.caisse.domain.model.Money
import com.ahmeddhibi.caisse.domain.model.PrintStatus
import com.ahmeddhibi.caisse.domain.model.Sale
import com.ahmeddhibi.caisse.domain.model.SaleLine
import com.ahmeddhibi.caisse.domain.model.SyncStatus
import com.ahmeddhibi.caisse.domain.model.TicketNumber
import com.google.common.truth.Truth.assertThat
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import org.junit.Test

class TicketFormatterTest {

    private val formatter = TicketFormatter(Clock.system(ZoneId.of("Europe/Paris")))

    private val sale = Sale(
        id = "sale-1",
        ticketNumber = TicketNumber(registerNumber = 2, sequence = 137),
        lines = listOf(
            SaleLine("croissant", "Croissant", Money(140), quantity = 2),
            SaleLine("sandwich", "Sandwich jambon-beurre extra long", Money(550), quantity = 1),
        ),
        total = Money(830),
        createdAt = Instant.parse("2026-10-01T07:45:00Z"),
        printStatus = PrintStatus.PENDING,
        printAttempts = 0,
        lastPrintError = null,
        syncStatus = SyncStatus.PENDING,
    )

    @Test
    fun `prints the ticket number local time lines and total`() {
        val ticket = formatter.format(sale)

        assertThat(ticket).contains("C02-000137")
        assertThat(ticket).contains("01/10/2026 09:45")
        assertThat(ticket).contains("2 x Croissant")
        assertThat(ticket).contains("1,40 l'unité")
        assertThat(ticket).contains("8,30")
    }

    @Test
    fun `no line is wider than the paper`() {
        val lines = formatter.format(sale).lines()

        assertThat(lines.maxOf { it.length }).isAtMost(32)
    }

    @Test
    fun `amounts are aligned on the right edge`() {
        val totalLine = formatter.format(sale).lines().single { it.startsWith("TOTAL") }

        assertThat(totalLine).hasLength(32)
        assertThat(totalLine).endsWith("8,30")
    }

    @Test
    fun `long product names are cut but never the amount`() {
        val line = formatter.format(sale).lines().single { it.startsWith("1 x Sandwich") }

        assertThat(line).hasLength(32)
        assertThat(line).endsWith(" 5,50")
    }
}
