package com.ahmeddhibi.caisse.data.repository

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.ahmeddhibi.caisse.core.config.AppConfig
import com.ahmeddhibi.caisse.data.local.CaisseDatabase
import com.ahmeddhibi.caisse.data.remote.RemoteException
import com.ahmeddhibi.caisse.domain.model.Register
import com.ahmeddhibi.caisse.testing.FakeRegisterRemoteDataSource
import com.google.common.truth.Truth.assertThat
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class DefaultRegisterRepositoryTest {

    private val remote = FakeRegisterRemoteDataSource()
    private val config = AppConfig(storeId = "test-store", firebaseDatabaseUrl = "https://example.invalid", useFirebaseEmulator = false)
    private val clock = Clock.fixed(Instant.parse("2026-10-01T08:00:00Z"), ZoneOffset.UTC)
    private lateinit var database: CaisseDatabase
    private lateinit var repository: DefaultRegisterRepository

    @Before
    fun setUp() {
        database = CaisseDatabase.create(ApplicationProvider.getApplicationContext(), name = null)
        repository = DefaultRegisterRepository(database.registerDao(), remote, config, clock)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `enrolment keeps the number reserved by the server`() = runTest {
        remote.nextNumber = 3

        val register = repository.enroll()

        assertThat(register).isEqualTo(Register(storeId = "test-store", number = 3))
        assertThat(repository.register.first()).isEqualTo(register)
        assertThat(database.registerDao().get()?.lastSequence).isEqualTo(0L)
    }

    @Test
    fun `an enrolled register never asks the server again`() = runTest {
        repository.enroll()
        repository.enroll()

        assertThat(remote.calls).isEqualTo(1)
    }

    @Test
    fun `a server that never answers ends in a network error`() = runTest {
        remote.hangs = true

        val error = runCatching { repository.enroll() }.exceptionOrNull()

        assertThat(error).isInstanceOf(RemoteException::class.java)
        assertThat((error as RemoteException).reason).isEqualTo(RemoteException.Reason.NETWORK)
        assertThat(repository.register.first()).isNull()
    }
}
