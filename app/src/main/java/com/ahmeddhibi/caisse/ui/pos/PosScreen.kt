package com.ahmeddhibi.caisse.ui.pos

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmeddhibi.caisse.R
import com.ahmeddhibi.caisse.core.format.format
import com.ahmeddhibi.caisse.domain.model.Product
import com.ahmeddhibi.caisse.domain.printing.PrinterMode
import com.ahmeddhibi.caisse.ui.pos.components.CartPanel
import com.ahmeddhibi.caisse.ui.pos.components.PrinterModeDialog
import com.ahmeddhibi.caisse.ui.pos.components.ProductGrid

@Composable
fun PosScreen(viewModel: PosViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PosContent(
        uiState = uiState,
        onProductClick = viewModel::onProductClick,
        onDecrement = viewModel::onDecrement,
        onRemove = viewModel::onRemove,
        onCheckout = viewModel::onCheckoutClick,
        onMessageShown = viewModel::onMessageShown,
        onPrinterModeSelected = viewModel::onPrinterModeSelected,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosContent(
    uiState: PosUiState,
    onProductClick: (Product) -> Unit,
    onDecrement: (String) -> Unit,
    onRemove: (String) -> Unit,
    onCheckout: () -> Unit,
    onMessageShown: () -> Unit,
    onPrinterModeSelected: (PrinterMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPrinterSettings by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val messageText = when (val message = uiState.message) {
        is PosMessage.TicketRecorded ->
            stringResource(R.string.pos_ticket_recorded, message.ticketNumber, message.total.format())
        PosMessage.CheckoutFailed -> stringResource(R.string.pos_checkout_failed)
        null -> null
    }
    LaunchedEffect(uiState.message) {
        if (messageText != null) {
            snackbarHostState.showSnackbar(messageText)
            onMessageShown()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.registerKey?.let { stringResource(R.string.pos_title, it) }
                            ?: stringResource(R.string.app_name),
                    )
                },
                actions = {
                    IconButton(onClick = { showPrinterSettings = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.pos_printer_settings),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                ) {
                    Button(
                        onClick = onCheckout,
                        enabled = uiState.canCheckout,
                        modifier = Modifier.fillMaxWidth().height(56.dp).testTag(CHECKOUT_BUTTON_TAG),
                    ) {
                        Text(
                            text = stringResource(R.string.pos_checkout, uiState.cart.total.format()),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
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

    if (showPrinterSettings) {
        PrinterModeDialog(
            current = uiState.printerMode,
            onSelect = onPrinterModeSelected,
            onDismiss = { showPrinterSettings = false },
        )
    }
}

const val CHECKOUT_BUTTON_TAG = "checkout"

private val TwoPaneMinWidth = 600.dp
