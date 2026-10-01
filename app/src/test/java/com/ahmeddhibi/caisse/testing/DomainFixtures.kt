package com.ahmeddhibi.caisse.testing

import com.ahmeddhibi.caisse.domain.model.Money
import com.ahmeddhibi.caisse.domain.model.PrintStatus
import com.ahmeddhibi.caisse.domain.model.Sale
import com.ahmeddhibi.caisse.domain.model.SaleLine
import com.ahmeddhibi.caisse.domain.model.SyncStatus
import com.ahmeddhibi.caisse.domain.model.TicketNumber
import java.time.Instant

fun sale(
    sequence: Long,
    printStatus: PrintStatus = PrintStatus.PENDING,
    syncStatus: SyncStatus = SyncStatus.PENDING,
) = Sale(
    id = "sale-$sequence",
    ticketNumber = TicketNumber(registerNumber = 1, sequence = sequence),
    lines = listOf(SaleLine("espresso", "Espresso", Money(180), quantity = 1)),
    total = Money(180),
    createdAt = Instant.parse("2026-10-01T08:00:00Z"),
    printStatus = printStatus,
    printAttempts = if (printStatus == PrintStatus.FAILED) 1 else 0,
    lastPrintError = if (printStatus == PrintStatus.FAILED) "Bourrage papier" else null,
    syncStatus = syncStatus,
)
