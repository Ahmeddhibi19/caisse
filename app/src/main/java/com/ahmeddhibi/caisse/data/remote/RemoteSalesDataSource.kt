package com.ahmeddhibi.caisse.data.remote

import com.ahmeddhibi.caisse.domain.model.Sale

interface RemoteSalesDataSource {
    /** Signs in if needed and returns the user that owns this register on the server. */
    suspend fun currentUserId(): String

    /** Writes the sale and its ticket index atomically. Writing the same sale again changes nothing. */
    suspend fun push(storeId: String, sale: Sale, uid: String)

    /** Id of the sale holding [ticketNumber] on the server, or null. */
    suspend fun ticketOwner(storeId: String, ticketNumber: String): String?
}
