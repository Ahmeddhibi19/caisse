package com.ahmeddhibi.caisse.data.remote

import com.ahmeddhibi.caisse.domain.model.Sale
import com.ahmeddhibi.caisse.domain.model.TicketNumber
import com.google.firebase.database.ServerValue

private const val CURRENCY = "EUR"

/** Plain maps rather than reflection: the payload is explicit and survives R8 untouched. */
internal fun Sale.toRemoteMap(uid: String): Map<String, Any?> = mapOf(
    "ticketNumber" to ticketNumber.value,
    "registerKey" to TicketNumber.registerKey(ticketNumber.registerNumber),
    "sequence" to ticketNumber.sequence,
    "totalCents" to total.cents,
    "currency" to CURRENCY,
    "createdAt" to createdAt.toEpochMilli(),
    "syncedAt" to ServerValue.TIMESTAMP,
    "uid" to uid,
    "lines" to lines.map { line ->
        mapOf(
            "productId" to line.productId,
            "name" to line.productName,
            "unitPriceCents" to line.unitPrice.cents,
            "quantity" to line.quantity,
        )
    },
)
