package com.ahmeddhibi.caisse.ui.pos

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmeddhibi.caisse.R
import com.ahmeddhibi.caisse.domain.model.Product
import com.ahmeddhibi.caisse.ui.pos.components.CartPanel
import com.ahmeddhibi.caisse.ui.pos.components.ProductGrid

@Composable
fun PosScreen(viewModel: PosViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PosContent(
        uiState = uiState,
        onProductClick = viewModel::onProductClick,
        onDecrement = viewModel::onDecrement,
        onRemove = viewModel::onRemove,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosContent(
    uiState: PosUiState,
    onProductClick: (Product) -> Unit,
    onDecrement: (String) -> Unit,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.app_name)) })
        },
    ) { padding ->
        BoxWithConstraints(modifier = Modifier.padding(padding).fillMaxSize()) {
            val grid = @Composable { gridModifier: Modifier ->
                ProductGrid(
                    products = uiState.products,
                    cart = uiState.cart,
                    onProductClick = onProductClick,
                    modifier = gridModifier,
                )
            }
            val cart = @Composable { cartModifier: Modifier ->
                CartPanel(
                    cart = uiState.cart,
                    onIncrement = onProductClick,
                    onDecrement = onDecrement,
                    onRemove = onRemove,
                    modifier = cartModifier,
                )
            }
            if (maxWidth >= TwoPaneMinWidth) {
                Row(modifier = Modifier.fillMaxSize()) {
                    grid(Modifier.weight(1.6f).fillMaxHeight())
                    cart(Modifier.weight(1f).fillMaxHeight())
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    grid(Modifier.weight(1f).fillMaxWidth())
                    cart(Modifier.weight(0.9f).fillMaxWidth())
                }
            }
        }
    }
}

private val TwoPaneMinWidth = 600.dp
