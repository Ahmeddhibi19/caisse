package com.ahmeddhibi.caisse.data.catalog

import com.ahmeddhibi.caisse.domain.model.Money
import com.ahmeddhibi.caisse.domain.model.Product
import com.ahmeddhibi.caisse.domain.repository.ProductRepository
import javax.inject.Inject

class HardcodedProductRepository @Inject constructor() : ProductRepository {

    override val products: List<Product> = listOf(
        Product(id = "espresso", name = "Espresso", price = Money(180)),
        Product(id = "cappuccino", name = "Cappuccino", price = Money(320)),
        Product(id = "the-menthe", name = "Thé à la menthe", price = Money(250)),
        Product(id = "jus-orange", name = "Jus d'orange pressé", price = Money(400)),
        Product(id = "croissant", name = "Croissant", price = Money(140)),
        Product(id = "pain-chocolat", name = "Pain au chocolat", price = Money(160)),
        Product(id = "sandwich", name = "Sandwich jambon-beurre", price = Money(550)),
        Product(id = "eau", name = "Eau minérale 50 cl", price = Money(150)),
    )
}
