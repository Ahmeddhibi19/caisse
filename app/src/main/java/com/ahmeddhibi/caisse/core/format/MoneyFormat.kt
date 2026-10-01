package com.ahmeddhibi.caisse.core.format

import com.ahmeddhibi.caisse.domain.model.Money
import kotlin.math.abs

private const val NBSP = " "

/** "1 234,50 €" — formatted by hand so the output is identical on every device and in tests. */
fun Money.format(): String {
    val euros = (abs(cents) / 100).toString()
        .reversed()
        .chunked(3)
        .joinToString(NBSP)
        .reversed()
    return "${sign()}$euros,${centsPart()}$NBSP€"
}

/** "1234,50" — receipt printers don't all have the euro sign or thousands separators. */
fun Money.formatAmount(): String = "${sign()}${abs(cents) / 100},${centsPart()}"

private fun Money.sign(): String = if (cents < 0) "-" else ""

private fun Money.centsPart(): String = (abs(cents) % 100).toString().padStart(2, '0')
