package com.ahmeddhibi.caisse.testing

import com.ahmeddhibi.caisse.domain.printing.TicketPrinter
import java.util.concurrent.CopyOnWriteArrayList
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update

class FakeTicketPrinter : TicketPrinter {

    val printed: MutableList<String> = CopyOnWriteArrayList()

    @Volatile
    var failure: Exception? = null

    /** Simulates a printer that never answers. */
    @Volatile
    var hangs = false

    private val count = MutableStateFlow(0)

    override suspend fun print(ticket: String) {
        failure?.let { throw it }
        if (hangs) awaitCancellation()
        printed += ticket
        count.update { it + 1 }
    }

    suspend fun awaitPrinted(expected: Int) {
        count.first { it >= expected }
    }
}
