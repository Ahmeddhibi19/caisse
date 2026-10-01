package com.ahmeddhibi.caisse.data.repository

import com.ahmeddhibi.caisse.core.config.AppConfig
import com.ahmeddhibi.caisse.data.local.dao.RegisterDao
import com.ahmeddhibi.caisse.data.local.entity.RegisterStateEntity
import com.ahmeddhibi.caisse.data.local.mapper.toDomain
import com.ahmeddhibi.caisse.data.remote.RegisterRemoteDataSource
import com.ahmeddhibi.caisse.data.remote.RemoteException
import com.ahmeddhibi.caisse.domain.model.Register
import com.ahmeddhibi.caisse.domain.repository.RegisterRepository
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull

@Singleton
class DefaultRegisterRepository @Inject constructor(
    private val registerDao: RegisterDao,
    private val remote: RegisterRemoteDataSource,
    private val config: AppConfig,
    private val clock: Clock,
) : RegisterRepository {

    private val mutex = Mutex()

    override val register: Flow<Register?> = registerDao.observe().map { it?.toDomain() }

    override suspend fun enroll(): Register = mutex.withLock {
        registerDao.get()?.let { return@withLock it.toDomain() }

        // Offline, Firebase waits instead of failing, hence the explicit timeout.
        val number = withTimeoutOrNull(ENROLL_TIMEOUT_MS) { remote.claimRegisterNumber(config.storeId) }
            ?: throw RemoteException(RemoteException.Reason.NETWORK, "Enrolment timed out")

        val state = RegisterStateEntity(
            storeId = config.storeId,
            registerNumber = number,
            lastSequence = 0,
            enrolledAt = clock.millis(),
        )
        registerDao.insert(state)
        state.toDomain()
    }

    private companion object {
        const val ENROLL_TIMEOUT_MS = 20_000L
    }
}
