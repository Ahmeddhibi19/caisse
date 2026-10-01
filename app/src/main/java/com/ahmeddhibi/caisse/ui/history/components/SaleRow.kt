package com.ahmeddhibi.caisse.ui.history.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ahmeddhibi.caisse.R
import com.ahmeddhibi.caisse.core.format.format
import com.ahmeddhibi.caisse.core.format.formatDateTime
import com.ahmeddhibi.caisse.domain.model.PrintStatus
import com.ahmeddhibi.caisse.domain.model.Sale
import com.ahmeddhibi.caisse.ui.common.PrintStatusBadge
import com.ahmeddhibi.caisse.ui.common.SyncStatusLabel

@Composable
fun SaleRow(
    sale: Sale,
    onClick: () -> Unit,
    onReprint: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        modifier = modifier.clickable(onClick = onClick),
        headlineContent = {
            Text(text = sale.ticketNumber.value, style = MaterialTheme.typography.titleMedium)
        },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(
                        R.string.history_sale_details,
                        sale.createdAt.formatDateTime(),
                        pluralStringResource(R.plurals.history_items, sale.itemCount, sale.itemCount),
                    ),
                )
                SyncStatusLabel(status = sale.syncStatus)
            }
        },
        trailingContent = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(text = sale.total.format(), style = MaterialTheme.typography.titleMedium)
                PrintStatusBadge(status = sale.printStatus)
                if (sale.printStatus == PrintStatus.FAILED) {
                    TextButton(onClick = onReprint) {
                        Text(stringResource(R.string.history_reprint))
                    }
                }
            }
        },
    )
}
