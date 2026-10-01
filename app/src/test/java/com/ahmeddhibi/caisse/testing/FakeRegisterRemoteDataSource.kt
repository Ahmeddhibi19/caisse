package com.ahmeddhibi.caisse.testing

import com.ahmeddhibi.caisse.data.remote.RegisterRemoteDataSource
import kotlinx.coroutines.awaitCancellation

class FakeRegisterRemoteDataSource : RegisterRemoteDataSource {

    var nextNumber = 1
    var hangs = false
    var calls = 0
        private set

    override suspend fun claimRegisterNumber(storeId: String): Int {
        calls++
        if (hangs) awaitCancellation()
        return nextNumber++
    }
}
