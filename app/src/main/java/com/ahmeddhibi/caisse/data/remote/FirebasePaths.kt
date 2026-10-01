package com.ahmeddhibi.caisse.data.remote

internal object FirebasePaths {
    const val REGISTER_COUNTER = "registerCounter"
    const val REGISTERS = "registers"
    const val SALES = "sales"
    const val TICKET_INDEX = "ticketIndex"

    fun store(storeId: String) = "stores/$storeId"
}
