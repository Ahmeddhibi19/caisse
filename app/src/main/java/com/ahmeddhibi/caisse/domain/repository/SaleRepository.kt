package com.ahmeddhibi.caisse.domain.repository

import com.ahmeddhibi.caisse.domain.model.CartLine
import com.ahmeddhibi.caisse.domain.model.Sale

interface SaleRepository {
    /**
     * Gives the sale the next ticket number of this register and stores it, in one transaction:
     * either both happen or neither does, so a number is never lost nor handed out twice.
     */
    suspend fun recordSale(lines: List<CartLine>): Sale
}
