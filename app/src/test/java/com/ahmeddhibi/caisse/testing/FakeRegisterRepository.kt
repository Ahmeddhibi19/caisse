package com.ahmeddhibi.caisse.testing

import com.ahmeddhibi.caisse.domain.model.Register
import com.ahmeddhibi.caisse.domain.repository.RegisterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeRegisterRepository(initial: Register? = Register(storeId = "test-store", number = 1)) : RegisterRepository {

    private val state = MutableStateFlow(initial)
    var failure: Exception? = null
    var enrollCalls = 0
        private set

    override val register: Flow<Register?> = state

    override suspend fun enroll(): Register {
        enrollCalls++
        failure?.let { throw it }
        return Register(storeId = "test-store", number = 1).also { state.value = it }
    }
}
