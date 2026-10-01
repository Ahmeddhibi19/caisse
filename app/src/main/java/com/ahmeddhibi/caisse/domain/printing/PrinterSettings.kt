package com.ahmeddhibi.caisse.domain.printing

import kotlinx.coroutines.flow.Flow

/** Behaviour of the simulated printer, to demonstrate failures and reprints without hardware. */
enum class PrinterMode { NORMAL, UNSTABLE, OFFLINE }

interface PrinterSettingsRepository {
    val mode: Flow<PrinterMode>

    suspend fun setMode(mode: PrinterMode)
}
