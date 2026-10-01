package com.ahmeddhibi.caisse.data.remote

import com.google.firebase.database.DatabaseError

class RemoteException(
    val reason: Reason,
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {

    enum class Reason { PERMISSION_DENIED, NETWORK, OTHER }
}

internal fun DatabaseError.toRemoteException(): RemoteException {
    val reason = when (code) {
        DatabaseError.PERMISSION_DENIED -> RemoteException.Reason.PERMISSION_DENIED
        DatabaseError.DISCONNECTED,
        DatabaseError.NETWORK_ERROR,
        DatabaseError.UNAVAILABLE,
        DatabaseError.WRITE_CANCELED,
        DatabaseError.MAX_RETRIES,
        -> RemoteException.Reason.NETWORK
        else -> RemoteException.Reason.OTHER
    }
    return RemoteException(reason, message, toException())
}
