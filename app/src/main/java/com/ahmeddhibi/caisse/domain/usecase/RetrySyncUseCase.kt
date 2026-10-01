package com.ahmeddhibi.caisse.domain.usecase

import com.ahmeddhibi.caisse.domain.repository.SaleRepository
import com.ahmeddhibi.caisse.domain.sync.SyncScheduler
import javax.inject.Inject

/** Puts a sale rejected by the server back in the outbox, e.g. once the rules have been fixed. */
class RetrySyncUseCase @Inject constructor(
    private val saleRepository: SaleRepository,
    private val syncScheduler: SyncScheduler,
) {
    suspend operator fun invoke(saleId: String) {
        if (saleRepository.requeueSync(saleId)) syncScheduler.requestSync()
    }
}
