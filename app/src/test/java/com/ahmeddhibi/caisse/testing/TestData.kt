package com.ahmeddhibi.caisse.testing

import com.ahmeddhibi.caisse.domain.model.Money
import com.ahmeddhibi.caisse.domain.model.Product

object TestData {
    val croissant = Product(id = "croissant", name = "Croissant", price = Money(140))
    val espresso = Product(id = "espresso", name = "Espresso", price = Money(180))
    val sandwich = Product(id = "sandwich", name = "Sandwich", price = Money(550))
}
