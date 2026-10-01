package com.ahmeddhibi.caisse.domain.printing

import com.ahmeddhibi.caisse.core.format.formatAmount
import com.ahmeddhibi.caisse.domain.model.Sale
import java.time.Clock
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

/** Lays a sale out for a 58 mm receipt printer (32 characters per line). */
class TicketFormatter @Inject constructor(private val clock: Clock) {

    fun format(sale: Sale): String = buildString {
        appendLine(center(STORE_NAME))
        appendLine(SEPARATOR)
        appendLine(row("Ticket", sale.ticketNumber.value))
        appendLine(row("Date", DATE_FORMAT.withZone(clock.zone).format(sale.createdAt)))
        appendLine(SEPARATOR)
        sale.lines.forEach { line ->
            appendLine(row("${line.quantity} x ${line.productName}", line.total.formatAmount()))
            if (line.quantity > 1) appendLine("    ${line.unitPrice.formatAmount()} l'unité")
        }
        appendLine(SEPARATOR)
        appendLine(row("TOTAL EUR", sale.total.formatAmount()))
        appendLine(SEPARATOR)
        append(center("Merci de votre visite"))
    }

    private fun row(left: String, right: String): String {
        val room = WIDTH - right.length - 1
        return left.take(room).padEnd(WIDTH - right.length) + right
    }

    private fun center(text: String): String = text.take(WIDTH).let { it.padStart((WIDTH + it.length) / 2) }

    private companion object {
        const val WIDTH = 32
        const val STORE_NAME = "CAISSE DEMO"
        val SEPARATOR = "-".repeat(WIDTH)
        val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.FRANCE)
    }
}
