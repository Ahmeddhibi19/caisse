package com.ahmeddhibi.caisse.domain.sync

interface SyncScheduler {
    /** Pushes pending sales as soon as the network allows it. Cheap to call after every sale. */
    fun requestSync()

    /** Safety net in case an immediate request was missed. */
    fun schedulePeriodicSync()
}
