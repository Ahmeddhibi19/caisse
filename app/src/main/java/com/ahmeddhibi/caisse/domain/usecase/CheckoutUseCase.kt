package com.ahmeddhibi.caisse.domain.usecase

import com.ahmeddhibi.caisse.core.coroutines.ApplicationScope
import com.ahmeddhibi.caisse.domain.model.Sale
import com.ahmeddhibi.caisse.domain.repository.CartRepository
import com.ahmeddhibi.caisse.domain.repository.SaleRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async

class CheckoutUseCase @Inject constructor(
    private val cartRepository: CartRepository,
    private val saleRepository: SaleRepository,
    @ApplicationScope private val appScope: CoroutineScope,
) {

    // Runs in the application scope: once the cart is drained, leaving the screen
    // must neither cancel the sale half-way nor lose what was in the cart.
    suspend operator fun invoke(): CheckoutResult = appScope.async {
        val lines = cartRepository.drain()
        if (lines.isEmpty()) return@async CheckoutResult.EmptyCart

        val sale = try {
            saleRepository.recordSale(lines)
        } catch (e: Exception) {
            cartRepository.restore(lines)
            throw e
        }
        CheckoutResult.Success(sale)
    }.await()
}

sealed interface CheckoutResult {
    data object EmptyCart : CheckoutResult
    data class Success(val sale: Sale) : CheckoutResult
}
