package com.ahmeddhibi.caisse.ui.pos.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Badge
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ahmeddhibi.caisse.core.format.format
import com.ahmeddhibi.caisse.domain.model.Cart
import com.ahmeddhibi.caisse.domain.model.Product

@Composable
fun ProductGrid(
    products: List<Product>,
    cart: Cart,
    onProductClick: (Product) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(products, key = { it.id }) { product ->
            ProductCard(
                product = product,
                quantityInCart = cart.quantityOf(product.id),
                onClick = { onProductClick(product) },
            )
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    quantityInCart: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 16.dp, end = 12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = product.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (quantityInCart > 0) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Text(text = quantityInCart.toString())
                }
            }
        }
        Text(
            text = product.price.format(),
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
