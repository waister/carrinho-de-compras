package com.renobile.carrinho.features.cart.detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.renobile.carrinho.R
import com.renobile.carrinho.features.cart.components.CartHeader
import com.renobile.carrinho.features.cart.components.ProductItem
import com.renobile.carrinho.features.cart.components.SearchAppBar
import com.renobile.carrinho.features.cart.components.SortOptionsDialog

@Composable
fun CartDetailsScreen(
    viewModel: CartDetailsViewModel,
    actions: CartDetailsActions,
) {
    val state by viewModel.uiState.collectAsState()
    CartDetailsContent(
        state = state,
        actions = actions.copy(
            onSortOrderChanged = { order ->
                state.cart?.id?.let { viewModel.onSortOrderChanged(it, order) }
            },
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartDetailsContent(
    state: CartDetailsState,
    actions: CartDetailsActions,
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showSortOptions by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }

    LaunchedEffect(state.searchTerms) {
        if (state.searchTerms.isNotEmpty()) {
            isSearchActive = true
        }
    }

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

    if (showSortOptions) {
        SortOptionsDialog(
            currentOrder = state.sortOrder,
            onDismiss = { showSortOptions = false },
            onSortOrderSelected = { order ->
                actions.onSortOrderChanged(order)
                showSortOptions = false
            },
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
            ) {
                Column(modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)) {
                    if (isSearchActive) {
                        SearchAppBar(
                            query = state.searchTerms,
                            onQueryChange = actions.onSearchChanged,
                            onCancelSearch = {
                                actions.onSearchChanged("")
                                isSearchActive = false
                            },
                        )
                    } else {
                        TopAppBar(
                            title = { Text(state.cart?.name ?: "") },
                            navigationIcon = {
                                IconButton(onClick = actions.onBack) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                                }
                            },
                            windowInsets = WindowInsets(0, 0, 0, 0),
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent,
                                titleContentColor = Color.White,
                                actionIconContentColor = Color.White,
                                navigationIconContentColor = Color.White,
                            ),
                            actions = {
                                IconButton(onClick = { isSearchActive = true }) {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = stringResource(R.string.search_products),
                                    )
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
                                            text = { Text(stringResource(R.string.sort_order)) },
                                            onClick = {
                                                showMenu = false
                                                showSortOptions = true
                                            },
                                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Sort, null) },
                                        )
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
                    Text(
                        text = if (state.searchTerms.isNotEmpty()) {
                            stringResource(R.string.search_no_results, state.searchTerms)
                        } else {
                            stringResource(R.string.products_empty)
                        },
                    )
                }
            } else {
                LazyColumn {
                    items(state.products) { product ->
                        ProductItem(
                            product = product,
                        )
                    }
                }
            }
        }
    }
}
