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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSaleRepository : SaleRepository {

    val recorded = mutableListOf<List<CartLine>>()
    var failure: Exception? = null

    /** When set, recordSale suspends until it completes. */
    var gate: CompletableDeferred<Unit>? = null

    val stored = MutableStateFlow<List<Sale>>(emptyList())
    val reprintRequests = mutableListOf<String>()
    var reprintAccepted = true
    val pendingSyncCount = MutableStateFlow(0)
    val syncRequeueRequests = mutableListOf<String>()
    var syncRequeueAccepted = true

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

    override fun observeSales(): Flow<List<Sale>> = stored

    override suspend fun requestReprint(saleId: String): Boolean {
        reprintRequests += saleId
        return reprintAccepted
    }

    override fun observePendingSyncCount(): Flow<Int> = pendingSyncCount

    override suspend fun requeueSync(saleId: String): Boolean {
        syncRequeueRequests += saleId
        return syncRequeueAccepted
    }
}
