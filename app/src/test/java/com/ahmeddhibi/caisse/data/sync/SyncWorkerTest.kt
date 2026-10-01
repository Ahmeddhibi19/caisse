package com.ahmeddhibi.caisse.data.sync

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.ahmeddhibi.caisse.core.config.AppConfig
import com.ahmeddhibi.caisse.data.local.CaisseDatabase
import com.ahmeddhibi.caisse.data.remote.RemoteException
import com.ahmeddhibi.caisse.testing.FakeRemoteSalesDataSource
import com.ahmeddhibi.caisse.testing.saleEntity
import com.google.common.truth.Truth.assertThat
import java.time.Clock
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class SyncWorkerTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val remote = FakeRemoteSalesDataSource()
    private lateinit var database: CaisseDatabase
    private lateinit var worker: SyncWorker

    @Before
    fun setUp() {
        database = CaisseDatabase.create(context, name = null)
        val synchronizer = SalesSynchronizer(
            saleDao = database.saleDao(),
            remote = remote,
            config = AppConfig(storeId = "test-store", firebaseDatabaseUrl = "https://example.invalid", useFirebaseEmulator = false),
            clock = Clock.systemUTC(),
        )
        val factory = object : WorkerFactory() {
            override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters) =
                SyncWorker(appContext, workerParameters, synchronizer)
        }
        worker = TestListenableWorkerBuilder.from(context, SyncWorker::class.java)
            .setWorkerFactory(factory)
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `succeeds once the outbox is empty`() = runTest {
        database.saleDao().insertSale(saleEntity(sequence = 1))

        assertThat(worker.doWork()).isEqualTo(ListenableWorker.Result.success())
    }

    @Test
    fun `asks WorkManager to retry while the server is unreachable`() = runTest {
        database.saleDao().insertSale(saleEntity(sequence = 1))
        remote.failureFor = { RemoteException(RemoteException.Reason.NETWORK, "Disconnected") }

        assertThat(worker.doWork()).isEqualTo(ListenableWorker.Result.retry())
    }
}
