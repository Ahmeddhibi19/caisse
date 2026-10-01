package com.ahmeddhibi.caisse.domain.model

data class Register(
    val storeId: String,
    val number: Int,
) {
    val key: String get() = TicketNumber.registerKey(number)
}
