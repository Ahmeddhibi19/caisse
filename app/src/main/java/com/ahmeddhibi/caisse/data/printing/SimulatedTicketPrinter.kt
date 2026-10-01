package com.ahmeddhibi.caisse.data.printing

import android.util.Log
import com.ahmeddhibi.caisse.domain.printing.PrinterException
import com.ahmeddhibi.caisse.domain.printing.PrinterMode
import com.ahmeddhibi.caisse.domain.printing.PrinterSettingsRepository
import com.ahmeddhibi.caisse.domain.printing.TicketPrinter
import javax.inject.Inject
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

/**
 * Stands in for a receipt printer: same contract as a real driver (suspends while printing,
 * throws on failure), the ticket itself goes to Logcat.
 */
class SimulatedTicketPrinter @Inject constructor(
    private val settings: PrinterSettingsRepository,
) : TicketPrinter {

    override suspend fun print(ticket: String) {
        when (settings.mode.first()) {
            PrinterMode.NORMAL -> delay(PRINT_DURATION_MS)
            PrinterMode.UNSTABLE -> {
                delay(PRINT_DURATION_MS)
                if (Random.nextInt(100) < UNSTABLE_FAILURE_PERCENT) throw PrinterException("Bourrage papier")
            }
            PrinterMode.OFFLINE -> {
                delay(CONNECTION_ATTEMPT_MS)
                throw PrinterException("Imprimante injoignable")
            }
        }
        Log.i(TAG, "Ticket imprimé\n$ticket")
    }

    private companion object {
        const val TAG = "TicketPrinter"
        const val PRINT_DURATION_MS = 1_200L
        const val CONNECTION_ATTEMPT_MS = 2_000L
        const val UNSTABLE_FAILURE_PERCENT = 35
    }
}
