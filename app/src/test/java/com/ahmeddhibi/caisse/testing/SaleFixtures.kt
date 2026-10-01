package com.ahmeddhibi.caisse.testing

import com.ahmeddhibi.caisse.data.local.entity.RegisterStateEntity
import com.ahmeddhibi.caisse.data.local.entity.SaleEntity
import com.ahmeddhibi.caisse.data.local.entity.SaleLineEntity
import com.ahmeddhibi.caisse.domain.model.PrintStatus
import com.ahmeddhibi.caisse.domain.model.SyncStatus
import com.ahmeddhibi.caisse.domain.model.TicketNumber

fun saleEntity(
    sequence: Long,
    registerNumber: Int = 1,
    printStatus: PrintStatus = PrintStatus.PENDING,
    syncStatus: SyncStatus = SyncStatus.PENDING,
    totalCents: Long = 320,
) = SaleEntity(
    id = "sale-$registerNumber-$sequence",
    registerNumber = registerNumber,
    sequence = sequence,
    ticketNumber = TicketNumber(registerNumber, sequence).value,
    totalCents = totalCents,
    createdAt = 1_700_000_000_000 + sequence,
    printStatus = printStatus,
    syncStatus = syncStatus,
)

fun saleLineEntity(saleId: String, position: Int = 0) = SaleLineEntity(
    saleId = saleId,
    position = position,
    productId = "espresso",
    productName = "Espresso",
    unitPriceCents = 160,
    quantity = 2,
)

fun registerState(number: Int = 1, lastSequence: Long = 0) = RegisterStateEntity(
    storeId = "test-store",
    registerNumber = number,
    lastSequence = lastSequence,
    enrolledAt = 1_700_000_000_000,
)
