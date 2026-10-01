package com.ahmeddhibi.caisse.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class SaleWithLines(
    @Embedded val sale: SaleEntity,
    @Relation(parentColumn = "id", entityColumn = "sale_id")
    val lines: List<SaleLineEntity>,
)
