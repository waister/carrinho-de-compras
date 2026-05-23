package com.renobile.carrinho.features.list.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.renobile.carrinho.R
import com.renobile.carrinho.database.entities.ProductEntity
import com.renobile.carrinho.features.cart.components.AddProductDialog
import com.renobile.carrinho.features.cart.components.CartHeader
import com.renobile.carrinho.features.cart.components.ProductItem
import com.renobile.carrinho.features.list.listPreview
import com.renobile.carrinho.features.list.listProductsPreview
import com.renobile.carrinho.ui.theme.MyAppTheme

@Composable
fun ListDetailsScreen(
    viewModel: ListDetailsViewModel,
    actions: ListDetailsActions,
) {
    val state by viewModel.uiState.collectAsState()
    var activeCartId by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        viewModel.getActiveCartId()?.let { activeCartId = it }
    }

    ListDetailsContent(
        state = state,
        actions = actions.copy(
            onMoveToCart = { product, cartId, quantity, price ->
                viewModel.moveToCart(product, cartId, quantity, price)
            }
        ),
        activeCartId = activeCartId
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListDetailsContent(
    state: ListDetailsState,
    actions: ListDetailsActions,
    activeCartId: Long = 0L,
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var productToMove by remember { mutableStateOf<ProductEntity?>(null) }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            containerColor = MaterialTheme.colorScheme.surface,
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
                containerColor = MaterialTheme.colorScheme.surface,
                title = { Text(stringResource(R.string.confirmation)) },
                text = { Text(stringResource(R.string.create_cart_needed)) },
                confirmButton = {
                    TextButton(onClick = { productToMove = null }) { Text(stringResource(R.string.confirm)) }
                }
            )
        } else {
            AddProductDialog(
                product = productToMove?.copy(price = 0.0),
                onDismiss = { productToMove = null },
                onConfirm = { _, quantity, price ->
                    productToMove?.let {
                        actions.onMoveToCart(it, activeCartId, quantity, price)
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
                            onClick = { productToMove = product }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ListDetailsPreview() {
    val dummyState = ListDetailsState(
        list = listPreview,
        products = listProductsPreview,
    )
    MyAppTheme {
        ListDetailsContent(
            state = dummyState,
            actions = ListDetailsActions(),
            activeCartId = 1L
        )
    }
}
