package com.ahmeddhibi.caisse.data.remote

import android.os.Build
import com.ahmeddhibi.caisse.domain.model.TicketNumber
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import javax.inject.Inject

class FirebaseRegisterDataSource @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase,
) : RegisterRemoteDataSource {

    override suspend fun claimRegisterNumber(storeId: String): Int {
        val uid = auth.ensureSignedIn()
        val store = database.getReference(FirebasePaths.store(storeId))

        repeat(MAX_ATTEMPTS) {
            val number = store.child(FirebasePaths.REGISTER_COUNTER).incrementAndGet().toInt()
            val register = mapOf(
                "uid" to uid,
                "model" to Build.MODEL.take(MAX_MODEL_LENGTH),
                "enrolledAt" to ServerValue.TIMESTAMP,
            )
            try {
                store.child(FirebasePaths.REGISTERS).child(TicketNumber.registerKey(number)).awaitSet(register)
                return number
            } catch (e: RemoteException) {
                // Registers are write-once: if this key is already taken, move on to the next number.
                if (e.reason != RemoteException.Reason.PERMISSION_DENIED) throw e
            }
        }
        throw RemoteException(RemoteException.Reason.OTHER, "No free register number after $MAX_ATTEMPTS attempts")
    }

    private companion object {
        const val MAX_ATTEMPTS = 5
        const val MAX_MODEL_LENGTH = 100
    }
}
