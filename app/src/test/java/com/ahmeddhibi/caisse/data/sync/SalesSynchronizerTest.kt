package com.ahmeddhibi.caisse.data.sync

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.ahmeddhibi.caisse.core.config.AppConfig
import com.ahmeddhibi.caisse.data.local.CaisseDatabase
import com.ahmeddhibi.caisse.data.local.dao.SaleDao
import com.ahmeddhibi.caisse.data.local.mapper.toDomain
import com.ahmeddhibi.caisse.data.remote.RemoteException
import com.ahmeddhibi.caisse.domain.model.SyncStatus
import com.ahmeddhibi.caisse.testing.FakeRemoteSalesDataSource
import com.ahmeddhibi.caisse.testing.saleEntity
import com.ahmeddhibi.caisse.testing.saleLineEntity
import com.google.common.truth.Truth.assertThat
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class SalesSynchronizerTest {

    private val remote = FakeRemoteSalesDataSource()
    private val config = AppConfig(storeId = "test-store", firebaseDatabaseUrl = "https://example.invalid", useFirebaseEmulator = false)
    private val clock = Clock.fixed(Instant.parse("2026-10-01T10:00:00Z"), ZoneOffset.UTC)
    private lateinit var database: CaisseDatabase
    private lateinit var dao: SaleDao
    private lateinit var synchronizer: SalesSynchronizer

    @Before
    fun setUp() {
        database = CaisseDatabase.create(ApplicationProvider.getApplicationContext(), name = null)
        dao = database.saleDao()
        synchronizer = SalesSynchronizer(dao, remote, config, clock)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `pushes pending sales oldest first and marks them synced`() = runTest {
        insertSale(sequence = 2)
        insertSale(sequence = 1)
        insertSale(sequence = 3, syncStatus = SyncStatus.SYNCED)

        val outcome = synchronizer.sync()

        assertThat(outcome).isEqualTo(SyncOutcome.DONE)
        assertThat(remote.pushes).containsExactly("sale-1-1", "sale-1-2").inOrder()
        assertThat(syncStatusOf(1)).isEqualTo(SyncStatus.SYNCED)
        assertThat(syncStatusOf(2)).isEqualTo(SyncStatus.SYNCED)
        assertThat(remote.sales.getValue("sale-1-1").lines).hasSize(1)
    }

    @Test
    fun `pushing a sale the server already has does not duplicate it`() = runTest {
        insertSale(sequence = 1)
        // The server acknowledged an earlier push, but the app died before recording it.
        remote.push("test-store", dao.getWithLines("sale-1-1")!!.toDomain(), "device-uid")

        synchronizer.sync()

        assertThat(remote.sales).hasSize(1)
        assertThat(remote.ticketIndex).containsExactly("C01-000001", "sale-1-1")
        assertThat(syncStatusOf(1)).isEqualTo(SyncStatus.SYNCED)
    }

    @Test
    fun `a ticket number held by another sale is a conflict and does not block the others`() = runTest {
        insertSale(sequence = 1)
        insertSale(sequence = 2)
        remote.ticketIndex["C01-000001"] = "sale-from-a-cloned-device"

        val outcome = synchronizer.sync()

        assertThat(outcome).isEqualTo(SyncOutcome.DONE)
        assertThat(syncStatusOf(1)).isEqualTo(SyncStatus.CONFLICT)
        assertThat(syncStatusOf(2)).isEqualTo(SyncStatus.SYNCED)
    }

    @Test
    fun `a rejection for a ticket the server already holds for this sale counts as synced`() = runTest {
        insertSale(sequence = 1)
        remote.ticketIndex["C01-000001"] = "sale-1-1"
        remote.failureFor = { RemoteException(RemoteException.Reason.PERMISSION_DENIED, "Permission denied") }

        synchronizer.sync()

        assertThat(syncStatusOf(1)).isEqualTo(SyncStatus.SYNCED)
    }

    @Test
    fun `an unexplained rejection is retried later rather than flagged`() = runTest {
        insertSale(sequence = 1)
        remote.failureFor = { RemoteException(RemoteException.Reason.PERMISSION_DENIED, "Permission denied") }

        val outcome = synchronizer.sync()

        assertThat(outcome).isEqualTo(SyncOutcome.RETRY)
        assertThat(syncStatusOf(1)).isEqualTo(SyncStatus.PENDING)
    }

    @Test
    fun `a network error stops the run and keeps the sales pending`() = runTest {
        insertSale(sequence = 1)
        insertSale(sequence = 2)
        remote.failureFor = { RemoteException(RemoteException.Reason.NETWORK, "Disconnected") }

        val outcome = synchronizer.sync()

        assertThat(outcome).isEqualTo(SyncOutcome.RETRY)
        assertThat(remote.pushes).containsExactly("sale-1-1")
        assertThat(syncStatusOf(1)).isEqualTo(SyncStatus.PENDING)
        assertThat(syncStatusOf(2)).isEqualTo(SyncStatus.PENDING)
    }

    @Test
    fun `a server that never answers is retried later`() = runTest {
        insertSale(sequence = 1)
        remote.hangs = true

        assertThat(synchronizer.sync()).isEqualTo(SyncOutcome.RETRY)
        assertThat(syncStatusOf(1)).isEqualTo(SyncStatus.PENDING)
    }

    @Test
    fun `without a session nothing is pushed`() = runTest {
        insertSale(sequence = 1)
        remote.signInFailure = RemoteException(RemoteException.Reason.NETWORK, "Offline")

        assertThat(synchronizer.sync()).isEqualTo(SyncOutcome.RETRY)
        assertThat(remote.pushes).isEmpty()
    }

    private suspend fun insertSale(sequence: Long, syncStatus: SyncStatus = SyncStatus.PENDING) {
        val sale = saleEntity(sequence = sequence, syncStatus = syncStatus)
        dao.insertSale(sale)
        dao.insertLines(listOf(saleLineEntity(sale.id)))
    }

    private suspend fun syncStatusOf(sequence: Long) = dao.getWithLines("sale-1-$sequence")!!.sale.syncStatus
}
