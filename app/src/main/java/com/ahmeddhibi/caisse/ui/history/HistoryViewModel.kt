package com.ahmeddhibi.caisse.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmeddhibi.caisse.domain.printing.TicketFormatter
import com.ahmeddhibi.caisse.domain.repository.SaleRepository
import com.ahmeddhibi.caisse.domain.usecase.RetryPrintUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HistoryViewModel @Inject constructor(
    saleRepository: SaleRepository,
    private val retryPrint: RetryPrintUseCase,
    private val formatter: TicketFormatter,
) : ViewModel() {

    private val previewedSaleId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<HistoryUiState> = combine(
        saleRepository.observeSales(),
        previewedSaleId,
    ) { sales, previewedId ->
        HistoryUiState(
            isLoading = false,
            sales = sales,
            preview = sales.firstOrNull { it.id == previewedId }?.let { sale ->
                TicketPreview(ticketNumber = sale.ticketNumber.value, text = formatter.format(sale))
            },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState())

    fun onSaleClick(saleId: String) {
        previewedSaleId.value = saleId
    }

    fun onPreviewDismissed() {
        previewedSaleId.value = null
    }

    fun onReprint(saleId: String) {
        viewModelScope.launch { retryPrint(saleId) }
    }
}
