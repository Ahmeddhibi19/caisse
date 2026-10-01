package com.ahmeddhibi.caisse.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.ahmeddhibi.caisse.core.coroutines.ApplicationScope
import com.ahmeddhibi.caisse.domain.sync.SyncScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class WorkManagerSyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    @ApplicationScope private val scope: CoroutineScope,
) : SyncScheduler {

    // Looked up on first use: the Application must have been injected with the worker factory by then.
    private val workManager by lazy { WorkManager.getInstance(context) }
    private val mutex = Mutex()

    override fun requestSync() {
        scope.launch {
            mutex.withLock {
                val alreadyQueued = workManager.getWorkInfosForUniqueWorkFlow(SYNC_WORK).first()
                    .any { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.BLOCKED }
                // A run that hasn't started yet will see the sale we just committed. Otherwise chain one
                // after the running worker: KEEP would drop this request if that worker had already
                // read an empty outbox.
                if (!alreadyQueued) {
                    workManager.enqueueUniqueWork(SYNC_WORK, ExistingWorkPolicy.APPEND_OR_REPLACE, oneTimeRequest())
                }
            }
        }
    }

    override fun schedulePeriodicSync() {
        val request = PeriodicWorkRequestBuilder<SyncWorker>(PERIODIC_INTERVAL_MINUTES, TimeUnit.MINUTES)
            .setConstraints(NETWORK)
            .build()
        workManager.enqueueUniquePeriodicWork(PERIODIC_SYNC_WORK, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    private fun oneTimeRequest() = OneTimeWorkRequestBuilder<SyncWorker>()
        .setConstraints(NETWORK)
        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
        .build()

    private companion object {
        const val SYNC_WORK = "sales-sync"
        const val PERIODIC_SYNC_WORK = "sales-sync-periodic"
        const val PERIODIC_INTERVAL_MINUTES = 15L
        val NETWORK: Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    }
}
