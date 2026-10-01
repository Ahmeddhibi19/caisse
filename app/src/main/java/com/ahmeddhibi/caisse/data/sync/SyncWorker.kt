package com.ahmeddhibi.caisse.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val synchronizer: SalesSynchronizer,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = when (synchronizer.sync()) {
        SyncOutcome.DONE -> Result.success()
        SyncOutcome.RETRY -> Result.retry()
    }
}
