package com.ahmeddhibi.caisse.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ahmeddhibi.caisse.data.local.entity.RegisterStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RegisterDao {

    @Query("SELECT * FROM register_state WHERE id = 1")
    fun observe(): Flow<RegisterStateEntity?>

    @Query("SELECT * FROM register_state WHERE id = 1")
    suspend fun get(): RegisterStateEntity?

    @Insert
    suspend fun insert(state: RegisterStateEntity)

    @Query("UPDATE register_state SET last_sequence = :sequence WHERE id = 1")
    suspend fun updateLastSequence(sequence: Long)
}
