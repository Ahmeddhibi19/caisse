package com.ahmeddhibi.caisse.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ahmeddhibi.caisse.R
import com.ahmeddhibi.caisse.domain.model.PrintStatus
import com.ahmeddhibi.caisse.domain.model.SyncStatus

@Composable
fun PrintStatusBadge(status: PrintStatus, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val (label, container, content) = when (status) {
        // PRINTING is an internal step: for the cashier the ticket is still waiting for the printer.
        PrintStatus.PENDING, PrintStatus.PRINTING ->
            Triple(stringResource(R.string.print_status_pending), colors.tertiaryContainer, colors.onTertiaryContainer)
        PrintStatus.PRINTED ->
            Triple(stringResource(R.string.print_status_printed), colors.primaryContainer, colors.onPrimaryContainer)
        PrintStatus.FAILED ->
            Triple(stringResource(R.string.print_status_failed), colors.errorContainer, colors.onErrorContainer)
    }
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = container,
        contentColor = content,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
fun SyncStatusLabel(status: SyncStatus, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val (label, tint) = when (status) {
        SyncStatus.PENDING -> stringResource(R.string.sync_status_pending) to colors.onSurfaceVariant
        SyncStatus.SYNCED -> stringResource(R.string.sync_status_synced) to colors.primary
        SyncStatus.CONFLICT -> stringResource(R.string.sync_status_conflict) to colors.error
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = when (status) {
                SyncStatus.PENDING -> Icons.Default.Refresh
                SyncStatus.SYNCED -> Icons.Default.CheckCircle
                SyncStatus.CONFLICT -> Icons.Default.Warning
            },
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = tint,
        )
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = tint)
    }
}
