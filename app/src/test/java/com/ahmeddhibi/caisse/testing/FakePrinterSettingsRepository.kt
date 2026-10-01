package com.ahmeddhibi.caisse.testing

import com.ahmeddhibi.caisse.domain.printing.PrinterMode
import com.ahmeddhibi.caisse.domain.printing.PrinterSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakePrinterSettingsRepository : PrinterSettingsRepository {

    private val state = MutableStateFlow(PrinterMode.NORMAL)

    override val mode: Flow<PrinterMode> = state

    override suspend fun setMode(mode: PrinterMode) {
        state.value = mode
    }
}
