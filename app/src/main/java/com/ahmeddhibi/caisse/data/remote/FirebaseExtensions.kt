package com.ahmeddhibi.caisse.data.remote

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await

// The Task based APIs only expose a message on failure; listeners give the DatabaseError code,
// which is what tells a rule rejection apart from a network problem.

internal suspend fun DatabaseReference.awaitSet(value: Any?) {
    suspendCancellableCoroutine<Unit> { continuation ->
        setValue(value, continuation.completionListener())
    }
}

internal suspend fun DatabaseReference.awaitUpdate(values: Map<String, Any?>) {
    suspendCancellableCoroutine<Unit> { continuation ->
        updateChildren(values, continuation.completionListener())
    }
}

private fun CancellableContinuation<Unit>.completionListener() =
    DatabaseReference.CompletionListener { error, _ ->
        if (error == null) resume(Unit) else resumeWithException(error.toRemoteException())
    }

internal suspend fun DatabaseReference.awaitValue(): Any? = suspendCancellableCoroutine { continuation ->
    addListenerForSingleValueEvent(
        object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                continuation.resume(snapshot.value)
            }

            override fun onCancelled(error: DatabaseError) {
                continuation.resumeWithException(error.toRemoteException())
            }
        },
    )
}

/** Atomically increments a numeric node and returns the new value. */
internal suspend fun DatabaseReference.incrementAndGet(): Long = suspendCancellableCoroutine { continuation ->
    runTransaction(
        object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                // The first call often sees the local cache (null), the server then replays it with the real value.
                val current = (currentData.value as? Number)?.toLong() ?: 0L
                currentData.value = current + 1
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                val value = (currentData?.value as? Number)?.toLong()
                when {
                    error != null -> continuation.resumeWithException(error.toRemoteException())
                    !committed || value == null -> continuation.resumeWithException(
                        RemoteException(RemoteException.Reason.OTHER, "Transaction on $key was not committed"),
                    )
                    else -> continuation.resume(value)
                }
            }
        },
        false,
    )
}

internal suspend fun FirebaseAuth.ensureSignedIn(): String {
    currentUser?.let { return it.uid }
    val result = try {
        signInAnonymously().await()
    } catch (e: FirebaseNetworkException) {
        throw RemoteException(RemoteException.Reason.NETWORK, "Anonymous sign-in needs the network", e)
    }
    return result.user?.uid ?: throw RemoteException(RemoteException.Reason.OTHER, "Anonymous sign-in returned no user")
}
