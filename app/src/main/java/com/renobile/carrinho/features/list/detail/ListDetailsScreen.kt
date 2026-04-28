package com.renobile.carrinho.features.list.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.renobile.carrinho.R
import com.renobile.carrinho.database.entities.ProductEntity
import com.renobile.carrinho.features.cart.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListDetailsScreen(
    viewModel: ListDetailsViewModel,
    actions: ListDetailsActions
) {
    val state by viewModel.uiState.collectAsState()
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var productToMove by remember { mutableStateOf<ProductEntity?>(null) }
    var activeCartId by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        viewModel.getActiveCartId()?.let { activeCartId = it }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(R.string.confirmation)) },
            text = { Text(stringResource(R.string.confirm_delete_list)) },
            confirmButton = {
                TextButton(onClick = {
                    state.list?.id?.let { actions.onDeleteList() }
                    showDeleteConfirmation = false
                }) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    if (productToMove != null) {
        if (activeCartId == 0L) {
            AlertDialog(
                onDismissRequest = { productToMove = null },
                title = { Text(stringResource(R.string.confirmation)) },
                text = { Text(stringResource(R.string.create_cart_needed)) },
                confirmButton = {
                    TextButton(onClick = { productToMove = null }) { Text(stringResource(R.string.confirm)) }
                }
            )
        } else {
            AddProductDialog(
                product = productToMove?.copy(price = 0.0), // Request price when moving
                onDismiss = { productToMove = null },
                onConfirm = { name, quantity, price ->
                    productToMove?.let {
                        viewModel.moveToCart(it, activeCartId, quantity, price)
                    }
                    productToMove = null
                }
            )
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(state.list?.name ?: "") },
                    navigationIcon = {
                        IconButton(onClick = actions.onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    ),
                    actions = {
                        IconButton(onClick = actions.onShareList) {
                            Icon(Icons.Default.Share, contentDescription = null)
                        }
                        var showMenu by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { showMenu = !showMenu }) {
                                Icon(Icons.Default.MoreVert, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.delete_list)) },
                                    onClick = {
                                        showMenu = false
                                        showDeleteConfirmation = true
                                    },
                                    leadingIcon = { Icon(Icons.Default.Delete, null) },
                                )
                            }
                        }
                    }
                )
                if (state.list != null) {
                    CartHeader(
                        total = state.total,
                        productCount = state.products.size,
                        volumes = state.volumes,
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = actions.onShareList) {
                Icon(Icons.Default.Share, contentDescription = stringResource(R.string.send_list))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.products.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.products_empty))
                }
            } else {
                LazyColumn {
                    items(state.products) { product ->
                        ProductItem(
                            product = product,
                            actions = CartActions(),
                            onClick = { productToMove = product }
                        )
                    }
                }
            }
        }
    }
}
