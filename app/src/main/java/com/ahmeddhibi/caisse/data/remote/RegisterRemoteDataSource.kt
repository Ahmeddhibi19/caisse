package com.ahmeddhibi.caisse.data.remote

interface RegisterRemoteDataSource {
    /** Reserves a register number that no other installation will ever get. */
    suspend fun claimRegisterNumber(storeId: String): Int
}
