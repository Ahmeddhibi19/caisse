package com.ahmeddhibi.caisse.ui.pos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmeddhibi.caisse.core.network.NetworkMonitor
import com.ahmeddhibi.caisse.domain.model.Product
import com.ahmeddhibi.caisse.domain.printing.PrinterMode
import com.ahmeddhibi.caisse.domain.printing.PrinterSettingsRepository
import com.ahmeddhibi.caisse.domain.repository.CartRepository
import com.ahmeddhibi.caisse.domain.repository.ProductRepository
import com.ahmeddhibi.caisse.domain.repository.RegisterRepository
import com.ahmeddhibi.caisse.domain.repository.SaleRepository
import com.ahmeddhibi.caisse.domain.usecase.CheckoutResult
import com.ahmeddhibi.caisse.domain.usecase.CheckoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PosViewModel @Inject constructor(
    productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    registerRepository: RegisterRepository,
    private val checkout: CheckoutUseCase,
    private val printerSettings: PrinterSettingsRepository,
    networkMonitor: NetworkMonitor,
    saleRepository: SaleRepository,
) : ViewModel() {

    private val products = productRepository.products
    private val checkoutState = MutableStateFlow(CheckoutState())
    private var checkoutJob: Job? = null

    private val status = combine(
        printerSettings.mode,
        networkMonitor.isOnline,
        saleRepository.observePendingSyncCount(),
    ) { printerMode, isOnline, pendingSyncCount -> TillStatus(printerMode, isOnline, pendingSyncCount) }

    val uiState: StateFlow<PosUiState> = combine(
        cartRepository.cart,
        registerRepository.register,
        checkoutState,
        status,
    ) { cart, register, state, till ->
        PosUiState(
            products = products,
            cart = cart,
            registerKey = register?.key,
            isCheckingOut = state.inProgress,
            message = state.message,
            printerMode = till.printerMode,
            isOnline = till.isOnline,
            pendingSyncCount = till.pendingSyncCount,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PosUiState(products = products, cart = cartRepository.cart.value),
    )

    fun onProductClick(product: Product) = cartRepository.add(product)

    fun onDecrement(productId: String) = cartRepository.decrement(productId)

    fun onRemove(productId: String) = cartRepository.remove(productId)

    fun onCheckoutClick() {
        // Checked and set on the main thread, so a double tap can never start two sales.
        if (checkoutJob?.isActive == true || cartRepository.cart.value.isEmpty) return

        checkoutJob = viewModelScope.launch {
            checkoutState.update { it.copy(inProgress = true) }
            val message = try {
                when (val result = checkout()) {
                    is CheckoutResult.Success -> PosMessage.TicketRecorded(
                        ticketNumber = result.sale.ticketNumber.value,
                        total = result.sale.total,
                    )
                    CheckoutResult.EmptyCart -> null
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                PosMessage.CheckoutFailed
            }
            checkoutState.update { CheckoutState(inProgress = false, message = message ?: it.message) }
        }
    }

    fun onMessageShown() = checkoutState.update { it.copy(message = null) }

    fun onPrinterModeSelected(mode: PrinterMode) {
        viewModelScope.launch { printerSettings.setMode(mode) }
    }

    private data class TillStatus(
        val printerMode: PrinterMode,
        val isOnline: Boolean,
        val pendingSyncCount: Int,
    )

    private data class CheckoutState(
        val inProgress: Boolean = false,
        val message: PosMessage? = null,
    )
}
