package com.ahmeddhibi.caisse.domain.repository

import com.ahmeddhibi.caisse.domain.model.Cart
import com.ahmeddhibi.caisse.domain.model.CartLine
import com.ahmeddhibi.caisse.domain.model.Product
import kotlinx.coroutines.flow.StateFlow

interface CartRepository {
    val cart: StateFlow<Cart>

    fun add(product: Product)

    fun decrement(productId: String)

    fun remove(productId: String)

    /** Empties the cart atomically and returns its lines, so nothing added meanwhile can be lost. */
    fun drain(): List<CartLine>

    fun restore(lines: List<CartLine>)
}
