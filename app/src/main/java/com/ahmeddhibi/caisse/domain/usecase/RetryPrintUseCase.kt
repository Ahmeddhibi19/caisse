package com.ahmeddhibi.caisse.domain.usecase

import com.ahmeddhibi.caisse.domain.printing.PrintQueue
import com.ahmeddhibi.caisse.domain.repository.SaleRepository
import javax.inject.Inject

class RetryPrintUseCase @Inject constructor(
    private val saleRepository: SaleRepository,
    private val printQueue: PrintQueue,
) {
    /** Only a failed ticket can be sent again: a printed one never is. */
    suspend operator fun invoke(saleId: String) {
        if (saleRepository.requestReprint(saleId)) printQueue.wake()
    }
}
