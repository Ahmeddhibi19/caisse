package com.ahmeddhibi.caisse.data.local

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.test.core.app.ApplicationProvider
import com.ahmeddhibi.caisse.data.local.dao.SaleDao
import com.ahmeddhibi.caisse.domain.model.PrintStatus
import com.ahmeddhibi.caisse.domain.model.SyncStatus
import com.ahmeddhibi.caisse.testing.saleEntity
import com.ahmeddhibi.caisse.testing.saleLineEntity
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class SaleDaoTest {

    private lateinit var database: CaisseDatabase
    private lateinit var dao: SaleDao

    @Before
    fun setUp() {
        database = CaisseDatabase.create(ApplicationProvider.getApplicationContext(), name = null)
        dao = database.saleDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `claims the oldest pending ticket and flags it printing`() = runTest {
        dao.insertSale(saleEntity(sequence = 2))
        dao.insertSale(saleEntity(sequence = 1))
        dao.insertLines(listOf(saleLineEntity("sale-1-1")))

        val claimed = dao.claimNextPrint()

        assertThat(claimed?.sale?.sequence).isEqualTo(1L)
        assertThat(claimed?.sale?.printStatus).isEqualTo(PrintStatus.PRINTING)
        assertThat(claimed?.lines).hasSize(1)
        assertThat(dao.claimNextPrint()?.sale?.sequence).isEqualTo(2L)
        assertThat(dao.claimNextPrint()).isNull()
    }

    @Test
    fun `startup requeue resets failed and interrupted tickets but never printed ones`() = runTest {
        dao.insertSale(saleEntity(sequence = 1, printStatus = PrintStatus.PRINTED))
        dao.insertSale(saleEntity(sequence = 2, printStatus = PrintStatus.FAILED))
        dao.insertSale(saleEntity(sequence = 3, printStatus = PrintStatus.PRINTING))
        dao.insertSale(saleEntity(sequence = 4, printStatus = PrintStatus.PENDING))

        val requeued = dao.requeueUnprintedTickets()

        assertThat(requeued).isEqualTo(2)
        assertThat(statusOf("sale-1-1")).isEqualTo(PrintStatus.PRINTED)
        assertThat(statusOf("sale-1-2")).isEqualTo(PrintStatus.PENDING)
        assertThat(statusOf("sale-1-3")).isEqualTo(PrintStatus.PENDING)
        assertThat(statusOf("sale-1-4")).isEqualTo(PrintStatus.PENDING)
    }

    @Test
    fun `manual reprint only applies to failed tickets`() = runTest {
        dao.insertSale(saleEntity(sequence = 1, printStatus = PrintStatus.PRINTED))
        dao.insertSale(saleEntity(sequence = 2, printStatus = PrintStatus.FAILED))

        assertThat(dao.requeueFailedPrint("sale-1-1")).isEqualTo(0)
        assertThat(dao.requeueFailedPrint("sale-1-2")).isEqualTo(1)
        assertThat(statusOf("sale-1-1")).isEqualTo(PrintStatus.PRINTED)
        assertThat(statusOf("sale-1-2")).isEqualTo(PrintStatus.PENDING)
    }

    @Test
    fun `print outcome is recorded with the attempt count`() = runTest {
        dao.insertSale(saleEntity(sequence = 1))
        dao.insertSale(saleEntity(sequence = 2))

        dao.markPrintFailed("sale-1-1", "Bourrage papier")
        dao.markPrinted("sale-1-2", printedAt = 42)

        val failed = dao.getWithLines("sale-1-1")!!.sale
        val printed = dao.getWithLines("sale-1-2")!!.sale
        assertThat(failed.printStatus).isEqualTo(PrintStatus.FAILED)
        assertThat(failed.printAttempts).isEqualTo(1)
        assertThat(failed.lastPrintError).isEqualTo("Bourrage papier")
        assertThat(printed.printStatus).isEqualTo(PrintStatus.PRINTED)
        assertThat(printed.printedAt).isEqualTo(42L)
    }

    @Test
    fun `pending sync is ordered by ticket and skips synced and conflicting sales`() = runTest {
        dao.insertSale(saleEntity(sequence = 3))
        dao.insertSale(saleEntity(sequence = 1))
        dao.insertSale(saleEntity(sequence = 2, syncStatus = SyncStatus.SYNCED))
        dao.insertSale(saleEntity(sequence = 4, syncStatus = SyncStatus.CONFLICT))

        val pending = dao.pendingSync(limit = 10)

        assertThat(pending.map { it.sale.sequence }).containsExactly(1L, 3L).inOrder()
    }

    @Test
    fun `a conflict can be queued again for sync`() = runTest {
        dao.insertSale(saleEntity(sequence = 1, syncStatus = SyncStatus.CONFLICT))
        dao.insertSale(saleEntity(sequence = 2, syncStatus = SyncStatus.SYNCED))

        assertThat(dao.requeueConflict("sale-1-1")).isEqualTo(1)
        assertThat(dao.requeueConflict("sale-1-2")).isEqualTo(0)
        assertThat(dao.pendingSync(limit = 10).map { it.sale.id }).containsExactly("sale-1-1")
    }

    @Test
    fun `a ticket number can only be used once`() = runTest {
        dao.insertSale(saleEntity(sequence = 1))

        val error = runCatching { dao.insertSale(saleEntity(sequence = 1).copy(id = "other-id")) }.exceptionOrNull()

        assertThat(error).isInstanceOf(SQLiteConstraintException::class.java)
    }

    private suspend fun statusOf(id: String) = dao.getWithLines(id)!!.sale.printStatus
}
