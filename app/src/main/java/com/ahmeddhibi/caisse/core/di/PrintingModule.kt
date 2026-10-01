package com.ahmeddhibi.caisse.core.di

import com.ahmeddhibi.caisse.data.printing.DataStorePrinterSettingsRepository
import com.ahmeddhibi.caisse.data.printing.PrintSpooler
import com.ahmeddhibi.caisse.data.printing.SimulatedTicketPrinter
import com.ahmeddhibi.caisse.domain.printing.PrintQueue
import com.ahmeddhibi.caisse.domain.printing.PrinterSettingsRepository
import com.ahmeddhibi.caisse.domain.printing.TicketPrinter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class PrintingModule {

    // Swap this binding for an ESC/POS driver to print on real hardware.
    @Binds
    abstract fun bindTicketPrinter(impl: SimulatedTicketPrinter): TicketPrinter

    @Binds
    abstract fun bindPrintQueue(impl: PrintSpooler): PrintQueue

    @Binds
    abstract fun bindPrinterSettings(impl: DataStorePrinterSettingsRepository): PrinterSettingsRepository
}
