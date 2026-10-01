package com.ahmeddhibi.caisse.data.repository

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.ahmeddhibi.caisse.data.local.CaisseDatabase
import com.ahmeddhibi.caisse.data.local.mapper.toDomain
import com.ahmeddhibi.caisse.domain.model.CartLine
import com.ahmeddhibi.caisse.domain.model.Money
import com.ahmeddhibi.caisse.domain.model.PrintStatus
import com.ahmeddhibi.caisse.domain.model.SyncStatus
import com.ahmeddhibi.caisse.testing.TestData.croissant
import com.ahmeddhibi.caisse.testing.TestData.espresso
import com.ahmeddhibi.caisse.testing.registerState
import com.ahmeddhibi.caisse.testing.saleEntity
import com.google.common.truth.Truth.assertThat
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class OfflineFirstSaleRepositoryTest {

    private val clock = Clock.fixed(Instant.parse("2026-10-01T09:30:00Z"), ZoneOffset.UTC)
    private lateinit var database: CaisseDatabase
    private lateinit var repository: OfflineFirstSaleRepository

    @Before
    fun setUp() {
        database = CaisseDatabase.create(ApplicationProvider.getApplicationContext(), name = null)
        repository = OfflineFirstSaleRepository(database, clock)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `ticket numbers follow each other on the register`() = runTest {
        database.registerDao().insert(registerState(number = 2))

        val tickets = List(3) { repository.recordSale(listOf(CartLine(croissant, 1))).ticketNumber.value }

        assertThat(tickets).containsExactly("C02-000001", "C02-000002", "C02-000003").inOrder()
        assertThat(database.registerDao().get()?.lastSequence).isEqualTo(3L)
    }

    @Test
    fun `concurrent checkouts get distinct and consecutive numbers`() = runTest {
        database.registerDao().insert(registerState(number = 1))

        val sales = List(100) {
            async(Dispatchers.IO) { repository.recordSale(listOf(CartLine(espresso, 1))) }
        }.awaitAll()

        assertThat(sales.map { it.ticketNumber.sequence }.sorted()).isEqualTo((1L..100L).toList())
        assertThat(database.registerDao().get()?.lastSequence).isEqualTo(100L)
    }

    @Test
    fun `a sale that cannot be stored does not consume a number`() = runTest {
        database.registerDao().insert(registerState(number = 1, lastSequence = 0))
        // A stray row already holds C01-000001, so the insert hits the unique index.
        database.saleDao().insertSale(saleEntity(sequence = 1))

        val error = runCatching { repository.recordSale(listOf(CartLine(espresso, 1))) }.exceptionOrNull()

        assertThat(error).isNotNull()
        assertThat(database.registerDao().get()?.lastSequence).isEqualTo(0L)
    }

    @Test
    fun `stores a snapshot of the lines pending print and sync`() = runTest {
        database.registerDao().insert(registerState(number = 1))

        val sale = repository.recordSale(listOf(CartLine(croissant, 2), CartLine(espresso, 1)))
        val stored = database.saleDao().getWithLines(sale.id)!!.toDomain()

        assertThat(stored.lines.map { it.productName to it.quantity })
            .containsExactly("Croissant" to 2, "Espresso" to 1)
            .inOrder()
        assertThat(stored.total).isEqualTo(Money(140 * 2 + 180))
        assertThat(stored.createdAt).isEqualTo(clock.instant())
        assertThat(stored.printStatus).isEqualTo(PrintStatus.PENDING)
        assertThat(stored.syncStatus).isEqualTo(SyncStatus.PENDING)
    }

    @Test
    fun `history lists the newest ticket first`() = runTest {
        database.registerDao().insert(registerState(number = 1))
        repeat(3) { repository.recordSale(listOf(CartLine(espresso, 1))) }

        val history = repository.observeSales().first()

        assertThat(history.map { it.ticketNumber.sequence }).containsExactly(3L, 2L, 1L).inOrder()
    }

    @Test
    fun `only a failed ticket can be sent back to the printer`() = runTest {
        database.saleDao().insertSale(saleEntity(sequence = 1, printStatus = PrintStatus.PRINTED))
        database.saleDao().insertSale(saleEntity(sequence = 2, printStatus = PrintStatus.FAILED))

        assertThat(repository.requestReprint("sale-1-1")).isFalse()
        assertThat(repository.requestReprint("sale-1-2")).isTrue()
    }

    @Test
    fun `refuses to sell before the register is enrolled`() = runTest {
        val error = runCatching { repository.recordSale(listOf(CartLine(espresso, 1))) }.exceptionOrNull()

        assertThat(error).isInstanceOf(IllegalStateException::class.java)
    }
}
