package com.ahmeddhibi.caisse.data.printing

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.ahmeddhibi.caisse.data.local.CaisseDatabase
import com.ahmeddhibi.caisse.data.local.dao.SaleDao
import com.ahmeddhibi.caisse.domain.model.PrintStatus
import com.ahmeddhibi.caisse.domain.printing.PrinterException
import com.ahmeddhibi.caisse.domain.printing.TicketFormatter
import com.ahmeddhibi.caisse.testing.FakeTicketPrinter
import com.ahmeddhibi.caisse.testing.saleEntity
import com.google.common.truth.Truth.assertThat
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class PrintSpoolerTest {

    private val clock = Clock.fixed(Instant.parse("2026-10-01T09:30:00Z"), ZoneOffset.UTC)
    private val printer = FakeTicketPrinter()
    private val realScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var database: CaisseDatabase
    private lateinit var dao: SaleDao

    @Before
    fun setUp() {
        database = CaisseDatabase.create(ApplicationProvider.getApplicationContext(), name = null)
        dao = database.saleDao()
    }

    @After
    fun tearDown() {
        realScope.cancel()
        database.close()
    }

    @Test
    fun `prints pending tickets in ticket order`() = runTest {
        dao.insertSale(saleEntity(sequence = 2))
        dao.insertSale(saleEntity(sequence = 1))
        val spooler = spooler(backgroundScope)

        assertThat(spooler.printNext()).isTrue()
        assertThat(spooler.printNext()).isTrue()
        assertThat(spooler.printNext()).isFalse()

        assertThat(printer.printed.map { it.ticketNumber() }).containsExactly("C01-000001", "C01-000002").inOrder()
        assertThat(statusOf(1)).isEqualTo(PrintStatus.PRINTED)
        assertThat(statusOf(2)).isEqualTo(PrintStatus.PRINTED)
    }

    @Test
    fun `a printer error marks the ticket failed with its reason`() = runTest {
        dao.insertSale(saleEntity(sequence = 1))
        printer.failure = PrinterException("Bourrage papier")

        spooler(backgroundScope).printNext()

        val sale = dao.getWithLines("sale-1-1")!!.sale
        assertThat(sale.printStatus).isEqualTo(PrintStatus.FAILED)
        assertThat(sale.lastPrintError).isEqualTo("Bourrage papier")
        assertThat(sale.printAttempts).isEqualTo(1)
    }

    @Test
    fun `a printer that never answers times out instead of blocking the queue`() = runTest {
        dao.insertSale(saleEntity(sequence = 1))
        dao.insertSale(saleEntity(sequence = 2))
        val spooler = spooler(backgroundScope)
        printer.hangs = true

        spooler.printNext()
        printer.hangs = false
        spooler.printNext()

        assertThat(statusOf(1)).isEqualTo(PrintStatus.FAILED)
        assertThat(dao.getWithLines("sale-1-1")!!.sale.lastPrintError).isEqualTo(PrintSpooler.TIMEOUT_MESSAGE)
        assertThat(statusOf(2)).isEqualTo(PrintStatus.PRINTED)
    }

    @Test
    fun `at startup pending failed and interrupted tickets are printed but never printed ones`() = runTest {
        dao.insertSale(saleEntity(sequence = 1, printStatus = PrintStatus.PRINTED))
        dao.insertSale(saleEntity(sequence = 2, printStatus = PrintStatus.FAILED))
        dao.insertSale(saleEntity(sequence = 3, printStatus = PrintStatus.PRINTING))
        dao.insertSale(saleEntity(sequence = 4, printStatus = PrintStatus.PENDING))

        spooler(realScope).start()
        awaitStatuses(PrintStatus.PRINTED, PrintStatus.PRINTED, PrintStatus.PRINTED, PrintStatus.PRINTED)

        assertThat(printer.printed.map { it.ticketNumber() })
            .containsExactly("C01-000002", "C01-000003", "C01-000004")
            .inOrder()
    }

    @Test
    fun `a ticket recorded later is printed once the queue is woken up`() = runTest {
        val spooler = spooler(realScope)
        spooler.start()

        dao.insertSale(saleEntity(sequence = 1))
        spooler.wake()

        awaitStatuses(PrintStatus.PRINTED)
        assertThat(printer.printed).hasSize(1)
    }

    @Test
    fun `starting twice does not requeue the ticket being printed`() = runTest {
        dao.insertSale(saleEntity(sequence = 1))
        printer.hangs = true
        val spooler = spooler(realScope)

        spooler.start()
        awaitStatuses(PrintStatus.PRINTING)
        spooler.start()

        assertThat(statusOf(1)).isEqualTo(PrintStatus.PRINTING)
    }

    private fun spooler(scope: CoroutineScope) = PrintSpooler(dao, printer, TicketFormatter(clock), clock, scope)

    private suspend fun statusOf(sequence: Long) = dao.getWithLines("sale-1-$sequence")!!.sale.printStatus

    /** Polls in real time: the spooler runs on real threads in these tests. */
    private suspend fun awaitStatuses(vararg expected: PrintStatus) = withContext(Dispatchers.Default) {
        withTimeout(10_000) {
            while ((1..expected.size).map { statusOf(it.toLong()) } != expected.toList()) delay(20)
        }
    }

    private fun String.ticketNumber(): String = Regex("C\\d{2,}-\\d{6,}").find(this)!!.value
}
