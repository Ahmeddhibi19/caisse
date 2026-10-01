package com.ahmeddhibi.caisse.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ahmeddhibi.caisse.R

/** Network state and number of sales still waiting for the server. */
@Composable
fun ConnectivityChip(
    isOnline: Boolean,
    pendingSyncCount: Int,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val dotColor = if (isOnline) colors.primary else colors.error
    val label = buildString {
        append(stringResource(if (isOnline) R.string.network_online else R.string.network_offline))
        if (pendingSyncCount > 0) {
            append(" · ")
            append(pluralStringResource(R.plurals.network_pending_sync, pendingSyncCount, pendingSyncCount))
        }
    }
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = colors.surfaceVariant,
        contentColor = colors.onSurfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(Modifier.size(8.dp).background(dotColor, CircleShape))
            Text(text = label, style = MaterialTheme.typography.labelMedium)
        }
    }
}
