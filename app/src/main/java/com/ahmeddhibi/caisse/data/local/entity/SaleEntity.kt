package com.ahmeddhibi.caisse.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ahmeddhibi.caisse.domain.model.PrintStatus
import com.ahmeddhibi.caisse.domain.model.SyncStatus

@Entity(
    tableName = "sales",
    indices = [
        Index(value = ["register_number", "sequence"], unique = true),
        Index(value = ["ticket_number"], unique = true),
        Index(value = ["print_status"]),
        Index(value = ["sync_status"]),
    ],
)
data class SaleEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "register_number") val registerNumber: Int,
    val sequence: Long,
    @ColumnInfo(name = "ticket_number") val ticketNumber: String,
    @ColumnInfo(name = "total_cents") val totalCents: Long,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "print_status") val printStatus: PrintStatus,
    @ColumnInfo(name = "print_attempts") val printAttempts: Int = 0,
    @ColumnInfo(name = "last_print_error") val lastPrintError: String? = null,
    @ColumnInfo(name = "printed_at") val printedAt: Long? = null,
    @ColumnInfo(name = "sync_status") val syncStatus: SyncStatus,
    @ColumnInfo(name = "synced_at") val syncedAt: Long? = null,
)
