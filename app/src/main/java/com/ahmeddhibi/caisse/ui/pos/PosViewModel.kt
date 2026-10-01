package com.ahmeddhibi.caisse.ui.pos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmeddhibi.caisse.domain.model.Product
import com.ahmeddhibi.caisse.domain.repository.CartRepository
import com.ahmeddhibi.caisse.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class PosViewModel @Inject constructor(
    productRepository: ProductRepository,
    private val cartRepository: CartRepository,
) : ViewModel() {

    private val products = productRepository.products

    val uiState: StateFlow<PosUiState> = cartRepository.cart
        .map { cart -> PosUiState(products = products, cart = cart) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PosUiState(products = products, cart = cartRepository.cart.value),
        )

    fun onProductClick(product: Product) = cartRepository.add(product)

    fun onDecrement(productId: String) = cartRepository.decrement(productId)

    fun onRemove(productId: String) = cartRepository.remove(productId)
}
