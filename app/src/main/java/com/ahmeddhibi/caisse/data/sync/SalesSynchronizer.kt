package com.ahmeddhibi.caisse.data.sync

import com.ahmeddhibi.caisse.core.config.AppConfig
import com.ahmeddhibi.caisse.data.local.dao.SaleDao
import com.ahmeddhibi.caisse.data.local.mapper.toDomain
import com.ahmeddhibi.caisse.data.remote.RemoteException
import com.ahmeddhibi.caisse.data.remote.RemoteSalesDataSource
import com.ahmeddhibi.caisse.domain.model.Sale
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull

enum class SyncOutcome { DONE, RETRY }

/**
 * Drains the outbox (sales still PENDING), oldest ticket first.
 *
 * - A sale is marked SYNCED only after the server acknowledged it: nothing is lost.
 * - The server key is the local UUID, so pushing it again overwrites the same node: no duplicate.
 */
@Singleton
class SalesSynchronizer @Inject constructor(
    private val saleDao: SaleDao,
    private val remote: RemoteSalesDataSource,
    private val config: AppConfig,
    private val clock: Clock,
) {

    // The one-off and the periodic workers may overlap.
    private val mutex = Mutex()

    suspend fun sync(): SyncOutcome = mutex.withLock { drain() }

    private suspend fun drain(): SyncOutcome {
        val uid = try {
            remote.currentUserId()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return SyncOutcome.RETRY
        }

        while (true) {
            val batch = saleDao.pendingSync(BATCH_SIZE)
            if (batch.isEmpty()) return SyncOutcome.DONE

            for (entry in batch) {
                val sale = entry.toDomain()
                when (push(sale, uid)) {
                    PushResult.ACCEPTED -> saleDao.markSynced(sale.id, syncedAt = clock.millis())
                    PushResult.CONFLICT -> saleDao.markConflict(sale.id)
                    PushResult.RETRY_LATER -> return SyncOutcome.RETRY
                }
            }
        }
    }

    private suspend fun push(sale: Sale, uid: String): PushResult = try {
        // Without a connection Firebase keeps the write pending instead of failing.
        withTimeoutOrNull(PUSH_TIMEOUT_MS) { remote.push(config.storeId, sale, uid) }
            ?.let { PushResult.ACCEPTED }
            ?: PushResult.RETRY_LATER
    } catch (e: RemoteException) {
        if (e.reason == RemoteException.Reason.PERMISSION_DENIED) diagnoseRejection(sale) else PushResult.RETRY_LATER
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        PushResult.RETRY_LATER
    }

    // A rejection is not always a conflict: an expired session or a rules deployment issue ends up here too.
    private suspend fun diagnoseRejection(sale: Sale): PushResult {
        val owner = try {
            withTimeoutOrNull(PUSH_TIMEOUT_MS) {
                remote.ticketOwner(config.storeId, sale.ticketNumber.value) ?: NO_OWNER
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
        return when (owner) {
            null, NO_OWNER -> PushResult.RETRY_LATER
            sale.id -> PushResult.ACCEPTED
            else -> PushResult.CONFLICT
        }
    }

    private enum class PushResult { ACCEPTED, CONFLICT, RETRY_LATER }

    internal companion object {
        const val BATCH_SIZE = 50
        const val PUSH_TIMEOUT_MS = 30_000L
        private const val NO_OWNER = ""
    }
}
