package com.ahmeddhibi.caisse.domain.repository

import com.ahmeddhibi.caisse.domain.model.Register
import kotlinx.coroutines.flow.Flow

interface RegisterRepository {
    /** Null until this installation has been enrolled. */
    val register: Flow<Register?>

    /** One-time enrolment: needs the network, every sale made afterwards works offline. */
    suspend fun enroll(): Register
}
