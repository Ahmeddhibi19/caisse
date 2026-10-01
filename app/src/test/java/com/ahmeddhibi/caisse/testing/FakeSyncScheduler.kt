package com.ahmeddhibi.caisse.testing

import com.ahmeddhibi.caisse.domain.sync.SyncScheduler

class FakeSyncScheduler : SyncScheduler {

    var requests = 0
        private set

    override fun requestSync() {
        requests++
    }

    override fun schedulePeriodicSync() = Unit
}
