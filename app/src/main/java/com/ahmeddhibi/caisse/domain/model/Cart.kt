package com.ahmeddhibi.caisse.domain.model

data class CartLine(
    val product: Product,
    val quantity: Int,
) {
    init {
        require(quantity > 0) { "Quantity must be positive, was $quantity" }
    }

    val total: Money get() = product.price * quantity
}

data class Cart(val lines: List<CartLine> = emptyList()) {

    val total: Money get() = lines.map { it.total }.sum()

    val itemCount: Int get() = lines.sumOf { it.quantity }

    val isEmpty: Boolean get() = lines.isEmpty()

    fun quantityOf(productId: String): Int =
        lines.firstOrNull { it.product.id == productId }?.quantity ?: 0

    fun add(product: Product, quantity: Int = 1): Cart {
        require(quantity > 0) { "Quantity must be positive, was $quantity" }
        val index = lines.indexOfFirst { it.product.id == product.id }
        if (index == -1) return copy(lines = lines + CartLine(product, quantity))
        return copy(
            lines = lines.toMutableList().also { it[index] = it[index].copy(quantity = it[index].quantity + quantity) },
        )
    }

    fun decrement(productId: String): Cart {
        val line = lines.firstOrNull { it.product.id == productId } ?: return this
        if (line.quantity == 1) return remove(productId)
        return copy(lines = lines.map { if (it === line) it.copy(quantity = it.quantity - 1) else it })
    }

    fun remove(productId: String): Cart = copy(lines = lines.filterNot { it.product.id == productId })

    fun merge(other: List<CartLine>): Cart = other.fold(this) { cart, line -> cart.add(line.product, line.quantity) }
}
