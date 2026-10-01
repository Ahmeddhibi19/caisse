package com.ahmeddhibi.caisse.domain.repository

import com.ahmeddhibi.caisse.domain.model.CartLine
import com.ahmeddhibi.caisse.domain.model.Sale
import kotlinx.coroutines.flow.Flow

interface SaleRepository {
    /**
     * Gives the sale the next ticket number of this register and stores it, in one transaction:
     * either both happen or neither does, so a number is never lost nor handed out twice.
     */
    suspend fun recordSale(lines: List<CartLine>): Sale

    /** Newest ticket first. */
    fun observeSales(): Flow<List<Sale>>

    /** Puts a failed ticket back in the print queue. Returns false if it was not failed. */
    suspend fun requestReprint(saleId: String): Boolean

    /** Sales recorded but not yet acknowledged by the server. */
    fun observePendingSyncCount(): Flow<Int>

    /** Puts a sale rejected by the server back in the outbox. Returns false if it was not in conflict. */
    suspend fun requeueSync(saleId: String): Boolean
}
