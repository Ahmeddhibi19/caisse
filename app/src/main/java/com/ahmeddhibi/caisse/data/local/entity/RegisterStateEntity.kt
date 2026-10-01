package com.ahmeddhibi.caisse.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "register_state")
data class RegisterStateEntity(
    @PrimaryKey val id: Int = SINGLE_ROW_ID,
    @ColumnInfo(name = "store_id") val storeId: String,
    @ColumnInfo(name = "register_number") val registerNumber: Int,
    @ColumnInfo(name = "last_sequence") val lastSequence: Long,
    @ColumnInfo(name = "enrolled_at") val enrolledAt: Long,
) {
    companion object {
        const val SINGLE_ROW_ID = 1
    }
}
