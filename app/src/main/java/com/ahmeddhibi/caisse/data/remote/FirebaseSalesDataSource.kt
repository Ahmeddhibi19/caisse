package com.ahmeddhibi.caisse.data.remote

import com.ahmeddhibi.caisse.domain.model.Sale
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import javax.inject.Inject

class FirebaseSalesDataSource @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase,
) : RemoteSalesDataSource {

    override suspend fun currentUserId(): String = auth.ensureSignedIn()

    override suspend fun push(storeId: String, sale: Sale, uid: String) {
        // Both paths succeed or fail together; the rules reject a ticket number already used by another sale.
        database.getReference(FirebasePaths.store(storeId)).awaitUpdate(
            mapOf(
                "${FirebasePaths.SALES}/${sale.id}" to sale.toRemoteMap(uid),
                "${FirebasePaths.TICKET_INDEX}/${sale.ticketNumber.value}" to sale.id,
            ),
        )
    }

    override suspend fun ticketOwner(storeId: String, ticketNumber: String): String? =
        database.getReference(FirebasePaths.store(storeId))
            .child(FirebasePaths.TICKET_INDEX)
            .child(ticketNumber)
            .awaitValue() as? String
}
