package com.ahmeddhibi.caisse.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ahmeddhibi.caisse.data.local.dao.RegisterDao
import com.ahmeddhibi.caisse.data.local.dao.SaleDao
import com.ahmeddhibi.caisse.data.local.entity.RegisterStateEntity
import com.ahmeddhibi.caisse.data.local.entity.SaleEntity
import com.ahmeddhibi.caisse.data.local.entity.SaleLineEntity

@Database(
    entities = [RegisterStateEntity::class, SaleEntity::class, SaleLineEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class CaisseDatabase : RoomDatabase() {

    abstract fun registerDao(): RegisterDao

    abstract fun saleDao(): SaleDao

    companion object {
        private const val NAME = "caisse.db"

        /** [name] = null gives an in-memory database, for tests. */
        fun create(context: Context, name: String? = NAME): CaisseDatabase {
            val builder = if (name == null) {
                Room.inMemoryDatabaseBuilder(context, CaisseDatabase::class.java)
            } else {
                Room.databaseBuilder(context, CaisseDatabase::class.java, name)
            }
            return builder.addCallback(FullSyncCallback).build()
        }
    }
}

/**
 * WAL commits are only guaranteed to survive a power cut with synchronous=FULL. Without it the
 * last sale could vanish after its ticket was printed, and its number would be handed out again.
 */
private object FullSyncCallback : RoomDatabase.Callback() {
    override fun onOpen(db: SupportSQLiteDatabase) {
        db.execSQL("PRAGMA synchronous = FULL")
    }
}
