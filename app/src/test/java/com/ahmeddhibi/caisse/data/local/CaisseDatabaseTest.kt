package com.ahmeddhibi.caisse.data.local

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class CaisseDatabaseTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var database: CaisseDatabase

    @Before
    fun setUp() {
        database = CaisseDatabase.create(context, name = DB_NAME)
    }

    @After
    fun tearDown() {
        database.close()
        context.deleteDatabase(DB_NAME)
    }

    @Test
    fun `commits are fsynced on the connection that writes`() {
        val db = database.openHelper.writableDatabase
        // A transaction pins the primary connection, the one every write goes through.
        db.beginTransaction()
        try {
            db.query("PRAGMA synchronous").use { cursor ->
                assertThat(cursor.moveToFirst()).isTrue()
                assertThat(cursor.getInt(0)).isEqualTo(SYNCHRONOUS_FULL)
            }
        } finally {
            db.endTransaction()
        }
    }

    private companion object {
        const val DB_NAME = "durability-test.db"
        const val SYNCHRONOUS_FULL = 2
    }
}
