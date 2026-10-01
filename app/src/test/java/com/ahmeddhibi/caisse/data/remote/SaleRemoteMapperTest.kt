package com.ahmeddhibi.caisse.data.remote

import com.ahmeddhibi.caisse.domain.model.Money
import com.ahmeddhibi.caisse.domain.model.PrintStatus
import com.ahmeddhibi.caisse.domain.model.Sale
import com.ahmeddhibi.caisse.domain.model.SaleLine
import com.ahmeddhibi.caisse.domain.model.SyncStatus
import com.ahmeddhibi.caisse.domain.model.TicketNumber
import com.google.common.truth.Truth.assertThat
import com.google.firebase.database.ServerValue
import java.time.Instant
import org.junit.Test

class SaleRemoteMapperTest {

    private val sale = Sale(
        id = "f3c1",
        ticketNumber = TicketNumber(registerNumber = 2, sequence = 7),
        lines = listOf(SaleLine("espresso", "Espresso", Money(180), quantity = 2)),
        total = Money(360),
        createdAt = Instant.ofEpochMilli(1_790_000_000_000),
        printStatus = PrintStatus.PRINTED,
        printAttempts = 1,
        lastPrintError = null,
        syncStatus = SyncStatus.PENDING,
    )

    @Test
    fun `matches the fields expected by the security rules`() {
        val map = sale.toRemoteMap(uid = "device-uid")

        assertThat(map.keys).containsExactly(
            "ticketNumber", "registerKey", "sequence", "totalCents", "currency", "createdAt", "syncedAt", "uid", "lines",
        )
        assertThat(map["ticketNumber"]).isEqualTo("C02-000007")
        assertThat(map["registerKey"]).isEqualTo("C02")
        assertThat(map["sequence"]).isEqualTo(7L)
        assertThat(map["totalCents"]).isEqualTo(360L)
        assertThat(map["createdAt"]).isEqualTo(1_790_000_000_000L)
        assertThat(map["syncedAt"]).isEqualTo(ServerValue.TIMESTAMP)
        assertThat(map["uid"]).isEqualTo("device-uid")
    }

    @Test
    fun `lines keep the price paid and the quantity`() {
        val lines = sale.toRemoteMap(uid = "device-uid")["lines"] as List<*>

        assertThat(lines).containsExactly(
            mapOf("productId" to "espresso", "name" to "Espresso", "unitPriceCents" to 180L, "quantity" to 2),
        )
    }

    @Test
    fun `print status stays on the device`() {
        assertThat(sale.toRemoteMap(uid = "device-uid")).doesNotContainKey("printStatus")
    }
}
