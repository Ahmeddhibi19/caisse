package com.ahmeddhibi.caisse.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.ahmeddhibi.caisse.data.local.entity.SaleEntity
import com.ahmeddhibi.caisse.data.local.entity.SaleLineEntity
import com.ahmeddhibi.caisse.data.local.entity.SaleWithLines
import kotlinx.coroutines.flow.Flow

@Dao
abstract class SaleDao {

    @Insert
    abstract suspend fun insertSale(sale: SaleEntity)

    @Insert
    abstract suspend fun insertLines(lines: List<SaleLineEntity>)

    @Transaction
    @Query("SELECT * FROM sales ORDER BY register_number DESC, sequence DESC")
    abstract fun observeAll(): Flow<List<SaleWithLines>>

    @Transaction
    @Query("SELECT * FROM sales WHERE id = :id")
    abstract suspend fun getWithLines(id: String): SaleWithLines?

    @Query("SELECT * FROM sales WHERE print_status = 'PENDING' ORDER BY register_number, sequence LIMIT 1")
    abstract suspend fun firstPendingPrint(): SaleEntity?

    @Query("UPDATE sales SET print_status = 'PRINTING' WHERE id = :id AND print_status = 'PENDING'")
    abstract suspend fun markPrinting(id: String): Int

    /** Takes the oldest pending ticket and flags it PRINTING in the same transaction. */
    @Transaction
    open suspend fun claimNextPrint(): SaleWithLines? {
        val next = firstPendingPrint() ?: return null
        if (markPrinting(next.id) == 0) return null
        return getWithLines(next.id)
    }

    @Query(
        "UPDATE sales SET print_status = 'PRINTED', printed_at = :printedAt, " +
            "print_attempts = print_attempts + 1, last_print_error = NULL WHERE id = :id",
    )
    abstract suspend fun markPrinted(id: String, printedAt: Long)

    @Query(
        "UPDATE sales SET print_status = 'FAILED', print_attempts = print_attempts + 1, " +
            "last_print_error = :error WHERE id = :id",
    )
    abstract suspend fun markPrintFailed(id: String, error: String)

    /** Run once at startup: PRINTING here means the app died mid-print, so the ticket goes out again. */
    @Query("UPDATE sales SET print_status = 'PENDING' WHERE print_status IN ('FAILED', 'PRINTING')")
    abstract suspend fun requeueUnprintedTickets(): Int

    @Query("UPDATE sales SET print_status = 'PENDING' WHERE id = :id AND print_status = 'FAILED'")
    abstract suspend fun requeueFailedPrint(id: String): Int

    @Transaction
    @Query("SELECT * FROM sales WHERE sync_status = 'PENDING' ORDER BY register_number, sequence LIMIT :limit")
    abstract suspend fun pendingSync(limit: Int): List<SaleWithLines>

    @Query("UPDATE sales SET sync_status = 'SYNCED', synced_at = :syncedAt WHERE id = :id")
    abstract suspend fun markSynced(id: String, syncedAt: Long)

    @Query("UPDATE sales SET sync_status = 'CONFLICT' WHERE id = :id")
    abstract suspend fun markConflict(id: String)

    @Query("UPDATE sales SET sync_status = 'PENDING' WHERE id = :id AND sync_status = 'CONFLICT'")
    abstract suspend fun requeueConflict(id: String): Int

    @Query("SELECT COUNT(*) FROM sales WHERE sync_status = 'PENDING'")
    abstract fun observePendingSyncCount(): Flow<Int>
}
