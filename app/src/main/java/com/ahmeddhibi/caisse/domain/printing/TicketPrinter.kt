package com.ahmeddhibi.caisse.domain.printing

interface TicketPrinter {
    /** Returns once the printer has accepted the ticket, throws [PrinterException] otherwise. */
    suspend fun print(ticket: String)
}

class PrinterException(message: String) : Exception(message)
