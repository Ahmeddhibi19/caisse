package com.ahmeddhibi.caisse.core.format

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DATE_TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.FRANCE)

fun Instant.formatDateTime(zone: ZoneId = ZoneId.systemDefault()): String = DATE_TIME.withZone(zone).format(this)
