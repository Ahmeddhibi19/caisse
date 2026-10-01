package com.ahmeddhibi.caisse.data.printing

import android.util.Log
import com.ahmeddhibi.caisse.core.coroutines.ApplicationScope
import com.ahmeddhibi.caisse.data.local.dao.SaleDao
import com.ahmeddhibi.caisse.data.local.mapper.toDomain
import com.ahmeddhibi.caisse.domain.printing.PrintQueue
import com.ahmeddhibi.caisse.domain.printing.TicketFormatter
import com.ahmeddhibi.caisse.domain.printing.TicketPrinter
import java.time.Clock
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * The sales table is the print queue: a ticket is PENDING until the printer confirms it.
 * Printing is therefore at-least-once: if the app dies between the paper and the database
 * update, the ticket comes out again at the next start, it is never lost.
 */
@Singleton
class PrintSpooler @Inject constructor(
    private val saleDao: SaleDao,
    private val printer: TicketPrinter,
    private val formatter: TicketFormatter,
    private val clock: Clock,
    @ApplicationScope private val scope: CoroutineScope,
) : PrintQueue {

    private val started = AtomicBoolean(false)
    private val wakeUps = Channel<Unit>(Channel.CONFLATED)

    override fun start() {
        if (!started.compareAndSet(false, true)) return
        scope.launch {
            // Inside the once-per-process guard: a recreated Activity must not requeue the ticket being printed.
            requeueUnprintedTickets()
            while (isActive) {
                val printed = try {
                    printNext()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "Print queue error", e)
                    false
                }
                if (!printed) wakeUps.receive()
            }
        }
    }

    override fun wake() {
        wakeUps.trySend(Unit)
    }

    internal suspend fun requeueUnprintedTickets() {
        val requeued = saleDao.requeueUnprintedTickets()
        if (requeued > 0) Log.i(TAG, "$requeued ticket(s) sent back to the printer")
    }

    /** Prints the oldest pending ticket, returns false when the queue is empty. */
    internal suspend fun printNext(): Boolean {
        val sale = saleDao.claimNextPrint()?.toDomain() ?: return false

        val error = try {
            val printed = withTimeoutOrNull(PRINT_TIMEOUT_MS) {
                printer.print(formatter.format(sale))
                true
            }
            if (printed == null) TIMEOUT_MESSAGE else null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.message ?: e.javaClass.simpleName
        }

        if (error == null) {
            saleDao.markPrinted(sale.id, printedAt = clock.millis())
        } else {
            Log.w(TAG, "Ticket ${sale.ticketNumber.value} not printed: $error")
            saleDao.markPrintFailed(sale.id, error)
        }
        return true
    }

    internal companion object {
        const val PRINT_TIMEOUT_MS = 15_000L
        const val TIMEOUT_MESSAGE = "Délai d'impression dépassé"
        private const val TAG = "PrintSpooler"
    }
}
