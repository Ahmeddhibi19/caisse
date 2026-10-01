package com.ahmeddhibi.caisse.domain.printing

interface PrintQueue {
    /** Starts printing, once per process: pending, failed and interrupted tickets go out first. */
    fun start()

    /** Signals that a new ticket is waiting. */
    fun wake()
}
