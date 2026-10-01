package com.ahmeddhibi.caisse.ui.history

import com.ahmeddhibi.caisse.domain.model.Sale

data class HistoryUiState(
    val isLoading: Boolean = true,
    val sales: List<Sale> = emptyList(),
    val preview: TicketPreview? = null,
)

data class TicketPreview(
    val ticketNumber: String,
    val text: String,
)
