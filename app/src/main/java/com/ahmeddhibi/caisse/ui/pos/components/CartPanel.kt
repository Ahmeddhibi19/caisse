package com.ahmeddhibi.caisse.ui.pos.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ahmeddhibi.caisse.R
import com.ahmeddhibi.caisse.core.format.format
import com.ahmeddhibi.caisse.domain.model.Cart
import com.ahmeddhibi.caisse.domain.model.CartLine
import com.ahmeddhibi.caisse.domain.model.Product

@Composable
fun CartPanel(
    cart: Cart,
    onIncrement: (Product) -> Unit,
    onDecrement: (String) -> Unit,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier,
    footer: @Composable () -> Unit = {},
) {
    Surface(modifier = modifier, tonalElevation = 2.dp) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(
                text = stringResource(R.string.cart_title),
                style = MaterialTheme.typography.titleLarge,
            )
            if (cart.isEmpty) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.cart_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    items(cart.lines, key = { it.product.id }) { line ->
                        CartLineItem(
                            line = line,
                            onIncrement = { onIncrement(line.product) },
                            onDecrement = { onDecrement(line.product.id) },
                            onRemove = { onRemove(line.product.id) },
                        )
                        HorizontalDivider()
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.cart_total),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = cart.total.format(),
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
            footer()
        }
    }
}

@Composable
fun CartLineItem(
    line: CartLine,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val name = line.product.name
    val decreaseLabel = stringResource(R.string.cart_decrease, name)
    val increaseLabel = stringResource(R.string.cart_increase, name)

    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = line.product.price.format(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        FilledTonalIconButton(
            onClick = onDecrement,
            modifier = Modifier.size(36.dp).semantics { contentDescription = decreaseLabel },
        ) {
            Text(text = "−", style = MaterialTheme.typography.titleMedium)
        }
        Text(
            text = line.quantity.toString(),
            modifier = Modifier.widthIn(min = 32.dp),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        FilledTonalIconButton(
            onClick = onIncrement,
            modifier = Modifier.size(36.dp).semantics { contentDescription = increaseLabel },
        ) {
            Text(text = "+", style = MaterialTheme.typography.titleMedium)
        }
        Text(
            text = line.total.format(),
            modifier = Modifier.widthIn(min = 80.dp),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.End,
        )
        IconButton(onClick = onRemove) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(R.string.cart_remove_line, name),
            )
        }
    }
}
