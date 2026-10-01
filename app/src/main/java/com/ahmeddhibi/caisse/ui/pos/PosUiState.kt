package com.ahmeddhibi.caisse.ui.pos

import com.ahmeddhibi.caisse.domain.model.Cart
import com.ahmeddhibi.caisse.domain.model.Product

data class PosUiState(
    val products: List<Product> = emptyList(),
    val cart: Cart = Cart(),
)
