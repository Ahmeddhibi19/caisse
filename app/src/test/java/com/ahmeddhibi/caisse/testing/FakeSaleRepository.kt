package com.ahmeddhibi.caisse.testing

import com.ahmeddhibi.caisse.domain.model.CartLine
import com.ahmeddhibi.caisse.domain.model.PrintStatus
import com.ahmeddhibi.caisse.domain.model.Sale
import com.ahmeddhibi.caisse.domain.model.SaleLine
import com.ahmeddhibi.caisse.domain.model.SyncStatus
import com.ahmeddhibi.caisse.domain.model.TicketNumber
import com.ahmeddhibi.caisse.domain.model.sum
import com.ahmeddhibi.caisse.domain.repository.SaleRepository
import java.time.Instant
import kotlinx.coroutines.CompletableDeferred

class FakeSaleRepository : SaleRepository {

    val recorded = mutableListOf<List<CartLine>>()
    var failure: Exception? = null

    /** When set, recordSale suspends until it completes. */
    var gate: CompletableDeferred<Unit>? = null

    private var sequence = 0L

    override suspend fun recordSale(lines: List<CartLine>): Sale {
        gate?.await()
        failure?.let { throw it }
        recorded += lines
        sequence++
        return Sale(
            id = "sale-$sequence",
            ticketNumber = TicketNumber(registerNumber = 1, sequence = sequence),
            lines = lines.map { SaleLine(it.product.id, it.product.name, it.product.price, it.quantity) },
            total = lines.map { it.total }.sum(),
            createdAt = Instant.EPOCH,
            printStatus = PrintStatus.PENDING,
            printAttempts = 0,
            lastPrintError = null,
            syncStatus = SyncStatus.PENDING,
        )
    }
}
