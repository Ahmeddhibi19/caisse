package com.ahmeddhibi.caisse.data.repository

import androidx.room.withTransaction
import com.ahmeddhibi.caisse.data.local.CaisseDatabase
import com.ahmeddhibi.caisse.data.local.entity.SaleEntity
import com.ahmeddhibi.caisse.data.local.entity.SaleLineEntity
import com.ahmeddhibi.caisse.data.local.entity.SaleWithLines
import com.ahmeddhibi.caisse.data.local.mapper.toDomain
import com.ahmeddhibi.caisse.domain.model.CartLine
import com.ahmeddhibi.caisse.domain.model.PrintStatus
import com.ahmeddhibi.caisse.domain.model.Sale
import com.ahmeddhibi.caisse.domain.model.SyncStatus
import com.ahmeddhibi.caisse.domain.model.TicketNumber
import com.ahmeddhibi.caisse.domain.model.sum
import com.ahmeddhibi.caisse.domain.repository.SaleRepository
import java.time.Clock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class OfflineFirstSaleRepository @Inject constructor(
    private val database: CaisseDatabase,
    private val clock: Clock,
) : SaleRepository {

    private val saleDao = database.saleDao()
    private val registerDao = database.registerDao()

    override suspend fun recordSale(lines: List<CartLine>): Sale {
        require(lines.isNotEmpty()) { "A sale needs at least one line" }

        return database.withTransaction {
            val register = checkNotNull(registerDao.get()) { "This device is not enrolled as a register" }
            val ticket = TicketNumber(register.registerNumber, register.lastSequence + 1)
            registerDao.updateLastSequence(ticket.sequence)

            val sale = SaleEntity(
                id = UUID.randomUUID().toString(),
                registerNumber = ticket.registerNumber,
                sequence = ticket.sequence,
                ticketNumber = ticket.value,
                totalCents = lines.map { it.total }.sum().cents,
                createdAt = clock.millis(),
                printStatus = PrintStatus.PENDING,
                syncStatus = SyncStatus.PENDING,
            )
            val saleLines = lines.mapIndexed { index, line ->
                SaleLineEntity(
                    saleId = sale.id,
                    position = index,
                    productId = line.product.id,
                    productName = line.product.name,
                    unitPriceCents = line.product.price.cents,
                    quantity = line.quantity,
                )
            }
            saleDao.insertSale(sale)
            saleDao.insertLines(saleLines)

            SaleWithLines(sale, saleLines).toDomain()
        }
    }

    override fun observeSales(): Flow<List<Sale>> =
        saleDao.observeAll().map { sales -> sales.map { it.toDomain() } }

    override suspend fun requestReprint(saleId: String): Boolean = saleDao.requeueFailedPrint(saleId) == 1
}
