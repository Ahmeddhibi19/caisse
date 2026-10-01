package com.ahmeddhibi.caisse.domain.repository

import com.ahmeddhibi.caisse.domain.model.Product

interface ProductRepository {
    val products: List<Product>
}
