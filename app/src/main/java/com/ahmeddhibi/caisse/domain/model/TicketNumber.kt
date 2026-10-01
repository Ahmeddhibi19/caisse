package com.ahmeddhibi.caisse.domain.model

/**
 * A ticket is numbered per register: `C02-000137` is the 137th sale of register 2.
 * Registers never share a prefix, so two tablets selling offline can't produce the same number.
 */
data class TicketNumber(val registerNumber: Int, val sequence: Long) {

    init {
        require(registerNumber > 0) { "Register number must be positive, was $registerNumber" }
        require(sequence > 0) { "Sequence must be positive, was $sequence" }
    }

    val value: String
        get() = "${registerKey(registerNumber)}-${sequence.toString().padStart(SEQUENCE_DIGITS, '0')}"

    companion object {
        private const val SEQUENCE_DIGITS = 6

        fun registerKey(registerNumber: Int): String = "C" + registerNumber.toString().padStart(2, '0')
    }
}
