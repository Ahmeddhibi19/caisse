package com.ahmeddhibi.caisse.data.cart

import com.ahmeddhibi.caisse.domain.model.Cart
import com.ahmeddhibi.caisse.domain.model.CartLine
import com.ahmeddhibi.caisse.domain.model.Product
import com.ahmeddhibi.caisse.domain.repository.CartRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update

@Singleton
class InMemoryCartRepository @Inject constructor() : CartRepository {

    private val state = MutableStateFlow(Cart())

    override val cart: StateFlow<Cart> = state.asStateFlow()

    override fun add(product: Product) = state.update { it.add(product) }

    override fun decrement(productId: String) = state.update { it.decrement(productId) }

    override fun remove(productId: String) = state.update { it.remove(productId) }

    override fun drain(): List<CartLine> = state.getAndUpdate { Cart() }.lines

    override fun restore(lines: List<CartLine>) = state.update { it.merge(lines) }
}
