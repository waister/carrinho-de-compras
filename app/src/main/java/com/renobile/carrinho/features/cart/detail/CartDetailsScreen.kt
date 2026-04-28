package com.renobile.carrinho.features.cart.detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.renobile.carrinho.R
import com.renobile.carrinho.features.cart.*

@Composable
fun CartDetailsScreen(
    viewModel: CartDetailsViewModel,
    actions: CartDetailsActions,
) {
    val state by viewModel.uiState.collectAsState()
    CartDetailsContent(state = state, actions = actions)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartDetailsContent(
    state: CartDetailsState,
    actions: CartDetailsActions,
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }

    if (isSearchActive) {
        BackHandler {
            isSearchActive = false
            actions.onSearchChanged("")
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(R.string.confirmation)) },
            text = { Text(stringResource(R.string.confirm_delete_cart)) },
            confirmButton = {
                TextButton(onClick = {
                    actions.onDeleteCart()
                    showDeleteConfirmation = false
                }) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }

    Scaffold(
        topBar = {
            Column {
                if (isSearchActive) {
                    SearchAppBar(
                        query = state.searchTerms,
                        onQueryChange = actions.onSearchChanged,
                        onCancelSearch = {
                            onSearchActiveChange(false, actions)
                            isSearchActive = false
                        },
                    )
                } else {
                    TopAppBar(
                        title = { Text(state.cart?.name ?: "") },
                        navigationIcon = {
                            IconButton(onClick = actions.onBack) {
                                Icon(Icons.Default.ArrowBack, contentDescription = null)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = Color.White,
                            actionIconContentColor = Color.White,
                            navigationIconContentColor = Color.White,
                        ),
                        actions = {
                            IconButton(onClick = { isSearchActive = true }) {
                                Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search_products))
                            }
                            Box {
                                IconButton(onClick = { showMenu = !showMenu }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = null)
                                }
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false },
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.delete_cart)) },
                                        onClick = {
                                            showMenu = false
                                            showDeleteConfirmation = true
                                        },
                                        leadingIcon = { Icon(Icons.Default.Delete, null) },
                                    )
                                }
                            }
                        },
                    )
                }
                if (state.cart != null) {
                    CartHeader(
                        total = state.total,
                        productCount = state.products.size,
                        volumes = state.volumes,
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = actions.onShareCart) {
                Icon(Icons.Default.Share, contentDescription = stringResource(R.string.send_cart))
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
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
                            actions = CartActions(), // Reusing ProductItem which needs CartActions (unused param)
                            onClick = { /* In details view, maybe just view or do nothing? */ },
                        )
                    }
                }
            }
        }
    }
}

private fun onSearchActiveChange(active: Boolean, actions: CartDetailsActions) {
    if (!active) {
        actions.onSearchChanged("")
    }
}
