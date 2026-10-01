package com.ahmeddhibi.caisse.domain.model

import java.time.Instant

data class Sale(
    val id: String,
    val ticketNumber: TicketNumber,
    val lines: List<SaleLine>,
    val total: Money,
    val createdAt: Instant,
    val printStatus: PrintStatus,
    val printAttempts: Int,
    val lastPrintError: String?,
    val syncStatus: SyncStatus,
) {
    val itemCount: Int get() = lines.sumOf { it.quantity }
}

data class SaleLine(
    val productId: String,
    val productName: String,
    val unitPrice: Money,
    val quantity: Int,
) {
    val total: Money get() = unitPrice * quantity
}

enum class PrintStatus {
    PENDING,

    /** Sent to the printer; still PRINTING after a restart means the outcome is unknown. */
    PRINTING,
    PRINTED,
    FAILED,
}

enum class SyncStatus {
    PENDING,
    SYNCED,

    /** Rejected by the server, e.g. the ticket number is already used by another sale. */
    CONFLICT,
}
