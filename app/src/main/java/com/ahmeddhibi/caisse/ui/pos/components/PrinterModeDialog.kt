package com.ahmeddhibi.caisse.ui.pos.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.ahmeddhibi.caisse.R
import com.ahmeddhibi.caisse.domain.printing.PrinterMode

@Composable
fun PrinterModeDialog(
    current: PrinterMode,
    onSelect: (PrinterMode) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_close)) }
        },
        title = { Text(stringResource(R.string.printer_dialog_title)) },
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                Text(
                    text = stringResource(R.string.printer_dialog_explanation),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                PrinterMode.entries.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(selected = mode == current, onClick = { onSelect(mode) }, role = Role.RadioButton)
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = mode == current, onClick = null)
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text(text = stringResource(mode.label), style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = stringResource(mode.description),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        },
    )
}

private val PrinterMode.label: Int
    get() = when (this) {
        PrinterMode.NORMAL -> R.string.printer_mode_normal
        PrinterMode.UNSTABLE -> R.string.printer_mode_unstable
        PrinterMode.OFFLINE -> R.string.printer_mode_offline
    }

private val PrinterMode.description: Int
    get() = when (this) {
        PrinterMode.NORMAL -> R.string.printer_mode_normal_description
        PrinterMode.UNSTABLE -> R.string.printer_mode_unstable_description
        PrinterMode.OFFLINE -> R.string.printer_mode_offline_description
    }
