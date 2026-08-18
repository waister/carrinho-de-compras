package com.renobile.carrinho.features.list.detail

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
import com.renobile.carrinho.features.cart.components.SearchAppBar
import com.renobile.carrinho.features.cart.components.SortOptionsDialog
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
            },
            onSortOrderChanged = { order ->
                state.list?.id?.let { viewModel.onSortOrderChanged(it, order) }
            },
        ),
        activeCartId = activeCartId,
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
    var showSortOptions by remember { mutableStateOf(false) }
    var productToMove by remember { mutableStateOf<ProductEntity?>(null) }
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

    if (productToMove != null) {
        if (activeCartId == 0L) {
            AlertDialog(
                onDismissRequest = { productToMove = null },
                containerColor = MaterialTheme.colorScheme.surface,
                title = { Text(stringResource(R.string.confirmation)) },
                text = { Text(stringResource(R.string.create_cart_needed)) },
                confirmButton = {
                    TextButton(onClick = { productToMove = null }) { Text(stringResource(R.string.confirm)) }
                },
            )
        } else {
            AddProductDialog(
                product = productToMove?.copy(price = 0.0),
                suggestions = state.suggestions,
                title = stringResource(R.string.move_to_cart),
                message = stringResource(R.string.move_to_cart_notice),
                onDismiss = { productToMove = null },
                onConfirm = { _, quantity, price ->
                    productToMove?.let {
                        actions.onMoveToCart(it, activeCartId, quantity, price)
                    }
                    productToMove = null
                },
            )
        }
    }

    Scaffold(
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
                            title = { Text(state.list?.name ?: "") },
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
                                IconButton(onClick = actions.onShareList) {
                                    Icon(Icons.Default.Share, contentDescription = null)
                                }
                                IconButton(onClick = { isSearchActive = true }) {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = stringResource(R.string.search_products),
                                    )
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
                                            text = { Text(stringResource(R.string.sort_order)) },
                                            onClick = {
                                                showMenu = false
                                                showSortOptions = true
                                            },
                                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Sort, null) },
                                        )
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
                            },
                        )
                    }
                    if (state.list != null) {
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
            FloatingActionButton(onClick = actions.onShareList) {
                Icon(Icons.Default.Share, contentDescription = stringResource(R.string.send_list))
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
                            onClick = { productToMove = product },
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
            activeCartId = 1L,
        )
    }
}
