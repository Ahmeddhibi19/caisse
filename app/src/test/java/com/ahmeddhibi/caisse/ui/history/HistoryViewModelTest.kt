package com.ahmeddhibi.caisse.ui.history

import com.ahmeddhibi.caisse.domain.model.PrintStatus
import com.ahmeddhibi.caisse.domain.printing.TicketFormatter
import com.ahmeddhibi.caisse.domain.usecase.RetryPrintUseCase
import com.ahmeddhibi.caisse.domain.usecase.RetrySyncUseCase
import com.ahmeddhibi.caisse.testing.FakePrintQueue
import com.ahmeddhibi.caisse.testing.FakeSaleRepository
import com.ahmeddhibi.caisse.testing.FakeSyncScheduler
import com.ahmeddhibi.caisse.testing.MainDispatcherRule
import com.ahmeddhibi.caisse.testing.sale
import com.google.common.truth.Truth.assertThat
import java.time.Clock
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sales = FakeSaleRepository()
    private val printQueue = FakePrintQueue()
    private val syncScheduler = FakeSyncScheduler()
    private lateinit var viewModel: HistoryViewModel

    @Before
    fun setUp() {
        viewModel = HistoryViewModel(
            saleRepository = sales,
            retryPrint = RetryPrintUseCase(sales, printQueue),
            retrySync = RetrySyncUseCase(sales, syncScheduler),
            formatter = TicketFormatter(Clock.systemUTC()),
        )
    }

    @Test
    fun `lists the stored sales`() = runTest {
        collectState()

        sales.stored.value = listOf(sale(sequence = 2), sale(sequence = 1))

        assertThat(viewModel.uiState.value.isLoading).isFalse()
        assertThat(viewModel.uiState.value.sales.map { it.id }).containsExactly("sale-2", "sale-1").inOrder()
    }

    @Test
    fun `opening a sale shows its ticket until dismissed`() = runTest {
        collectState()
        sales.stored.value = listOf(sale(sequence = 1))

        viewModel.onSaleClick("sale-1")

        val preview = viewModel.uiState.value.preview
        assertThat(preview?.ticketNumber).isEqualTo("C01-000001")
        assertThat(preview?.text).contains("1 x Espresso")

        viewModel.onPreviewDismissed()

        assertThat(viewModel.uiState.value.preview).isNull()
    }

    @Test
    fun `reprinting a failed ticket wakes the print queue`() = runTest {
        sales.stored.value = listOf(sale(sequence = 1, printStatus = PrintStatus.FAILED))

        viewModel.onReprint("sale-1")

        assertThat(sales.reprintRequests).containsExactly("sale-1")
        assertThat(printQueue.wakeUps).isEqualTo(1)
    }

    @Test
    fun `a ticket that cannot be reprinted leaves the queue alone`() = runTest {
        sales.reprintAccepted = false

        viewModel.onReprint("sale-1")

        assertThat(printQueue.wakeUps).isEqualTo(0)
    }

    @Test
    fun `a conflicting sale goes back to the outbox and a sync is requested`() = runTest {
        viewModel.onRetrySync("sale-1")

        assertThat(sales.syncRequeueRequests).containsExactly("sale-1")
        assertThat(syncScheduler.requests).isEqualTo(1)
    }

    private fun TestScope.collectState() {
        backgroundScope.launch(mainDispatcherRule.dispatcher) { viewModel.uiState.collect() }
    }
}
