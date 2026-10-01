package com.ahmeddhibi.caisse.data.local.mapper

import com.ahmeddhibi.caisse.data.local.entity.RegisterStateEntity
import com.ahmeddhibi.caisse.data.local.entity.SaleWithLines
import com.ahmeddhibi.caisse.domain.model.Money
import com.ahmeddhibi.caisse.domain.model.Register
import com.ahmeddhibi.caisse.domain.model.Sale
import com.ahmeddhibi.caisse.domain.model.SaleLine
import com.ahmeddhibi.caisse.domain.model.TicketNumber
import java.time.Instant

fun SaleWithLines.toDomain(): Sale = Sale(
    id = sale.id,
    ticketNumber = TicketNumber(sale.registerNumber, sale.sequence),
    lines = lines.sortedBy { it.position }.map { line ->
        SaleLine(
            productId = line.productId,
            productName = line.productName,
            unitPrice = Money(line.unitPriceCents),
            quantity = line.quantity,
        )
    },
    total = Money(sale.totalCents),
    createdAt = Instant.ofEpochMilli(sale.createdAt),
    printStatus = sale.printStatus,
    printAttempts = sale.printAttempts,
    lastPrintError = sale.lastPrintError,
    syncStatus = sale.syncStatus,
)

fun RegisterStateEntity.toDomain(): Register = Register(storeId = storeId, number = registerNumber)
