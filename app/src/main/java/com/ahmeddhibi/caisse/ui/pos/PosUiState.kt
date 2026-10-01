package com.ahmeddhibi.caisse.ui.pos

import com.ahmeddhibi.caisse.domain.model.Cart
import com.ahmeddhibi.caisse.domain.model.Money
import com.ahmeddhibi.caisse.domain.model.Product
import com.ahmeddhibi.caisse.domain.printing.PrinterMode

data class PosUiState(
    val products: List<Product> = emptyList(),
    val cart: Cart = Cart(),
    val registerKey: String? = null,
    val isCheckingOut: Boolean = false,
    val message: PosMessage? = null,
    val printerMode: PrinterMode = PrinterMode.NORMAL,
    val isOnline: Boolean = true,
    val pendingSyncCount: Int = 0,
) {
    val canCheckout: Boolean get() = !cart.isEmpty && !isCheckingOut
}

sealed interface PosMessage {
    data class TicketRecorded(val ticketNumber: String, val total: Money) : PosMessage
    data object CheckoutFailed : PosMessage
}
