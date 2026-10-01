package com.ahmeddhibi.caisse.testing

import com.ahmeddhibi.caisse.data.remote.RemoteException
import com.ahmeddhibi.caisse.data.remote.RemoteSalesDataSource
import com.ahmeddhibi.caisse.domain.model.Sale
import kotlinx.coroutines.awaitCancellation

/** Behaves like the security rules: a ticket number can only point to one sale. */
class FakeRemoteSalesDataSource : RemoteSalesDataSource {

    val sales = mutableMapOf<String, Sale>()
    val ticketIndex = mutableMapOf<String, String>()
    val pushes = mutableListOf<String>()

    var signInFailure: Exception? = null
    var failureFor: (Sale) -> Exception? = { null }
    var hangs = false

    override suspend fun currentUserId(): String {
        signInFailure?.let { throw it }
        return "device-uid"
    }

    override suspend fun push(storeId: String, sale: Sale, uid: String) {
        pushes += sale.id
        if (hangs) awaitCancellation()
        failureFor(sale)?.let { throw it }

        val owner = ticketIndex[sale.ticketNumber.value]
        if (owner != null && owner != sale.id) {
            throw RemoteException(RemoteException.Reason.PERMISSION_DENIED, "Permission denied")
        }
        sales[sale.id] = sale
        ticketIndex[sale.ticketNumber.value] = sale.id
    }

    override suspend fun ticketOwner(storeId: String, ticketNumber: String): String? = ticketIndex[ticketNumber]
}
