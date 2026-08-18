package com.renobile.carrinho.features.list

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.renobile.carrinho.R
import com.renobile.carrinho.database.entities.ProductEntity
import com.renobile.carrinho.features.cart.components.AddProductDialog
import com.renobile.carrinho.features.cart.components.DeleteProductDialog
import com.renobile.carrinho.features.cart.components.ProductItem
import com.renobile.carrinho.features.cart.components.SortOptionsDialog
import com.renobile.carrinho.features.list.components.ClearListDialog
import com.renobile.carrinho.features.list.components.CreateListDialog
import com.renobile.carrinho.features.list.components.EmptyListView
import com.renobile.carrinho.features.list.components.ImportListDialog
import com.renobile.carrinho.features.list.components.ListOptionsDialog
import com.renobile.carrinho.features.list.components.ListTopBar
import com.renobile.carrinho.ui.theme.MyAppTheme

@Composable
fun ListScreen(
    viewModel: ListViewModel,
    actions: ListActions,
    areBarsVisible: Boolean = true,
) {
    val state by viewModel.uiState.collectAsState()
    var activeCartId by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        viewModel.getActiveCartId()?.let { activeCartId = it }
    }

    ListScreen(
        state = state,
        actions = actions,
        activeCartId = activeCartId,
        areBarsVisible = areBarsVisible,
    )
}

@Composable
fun ListScreen(
    state: ListState,
    actions: ListActions,
    activeCartId: Long = 0L,
    areBarsVisible: Boolean = true,
) {
    var showMenu by remember { mutableStateOf(false) }
    var showClearConfirmation by remember { mutableStateOf(false) }
    var showCreateListDialog by remember { mutableStateOf(false) }
    var showSortOptions by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<ProductEntity?>(null) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var productOptionsToShow by remember { mutableStateOf<ProductEntity?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf<ProductEntity?>(null) }
    var productToMove by remember { mutableStateOf<ProductEntity?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }

    val scrollState = rememberLazyListState()
    val nestedScrollConnection = remember(scrollState, state.products.size, actions) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput) {
                    if (available.y < -10 && scrollState.canScrollForward && state.products.size > 10) {
                        actions.onScroll(false)
                    } else if (available.y > 10) {
                        actions.onScroll(true)
                    }
                }
                return Offset.Zero
            }
        }
    }

    if (isSearchActive) {
        BackHandler {
            isSearchActive = false
            actions.onSearchChanged("")
        }
    }

    if (showClearConfirmation) {
        ClearListDialog(
            onDismiss = { showClearConfirmation = false },
            onConfirm = {
                actions.onClearList()
                showClearConfirmation = false
            },
        )
    }

    if (showDeleteConfirmation != null) {
        DeleteProductDialog(
            onDismiss = { showDeleteConfirmation = null },
            onConfirm = {
                showDeleteConfirmation?.let { actions.onDeleteProduct(it) }
                showDeleteConfirmation = null
            },
        )
    }

    if (showCreateListDialog) {
        CreateListDialog(
            onDismiss = { showCreateListDialog = false },
            onConfirm = { name ->
                actions.onCreateList(name)
                showCreateListDialog = false
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

    if (showAddProductDialog || productToEdit != null) {
        AddProductDialog(
            product = productToEdit,
            suggestions = state.suggestions,
            onDismiss = {
                showAddProductDialog = false
                productToEdit = null
            },
            onConfirm = { name, quantity, price ->
                val newProduct = productToEdit?.copy(
                    name = name,
                    quantity = quantity,
                    price = price,
                ) ?: ProductEntity(
                    id = System.currentTimeMillis(),
                    cartId = 0L,
                    listId = state.list?.id ?: 0,
                    name = name,
                    quantity = quantity,
                    price = price,
                )
                actions.onAddOrEditProduct(newProduct)

                if (productToEdit != null) {
                    productToEdit = null
                }
            },
        )
    }

    if (productOptionsToShow != null) {
        ListOptionsDialog(
            product = productOptionsToShow!!,
            onDismiss = { productOptionsToShow = null },
            onEdit = {
                productToEdit = it
                productOptionsToShow = null
            },
            onMoveToCart = {
                productToMove = it
                productOptionsToShow = null
            },
            onChangeQuantity = { product, delta ->
                actions.onChangeQuantity(product, delta)
                productOptionsToShow = null
            },
            onDelete = {
                showDeleteConfirmation = it
                productOptionsToShow = null
            },
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
                        actions.onMoveToCart(it, quantity, price)
                    }
                    productToMove = null
                },
            )
        }
    }

    if (showImportDialog) {
        ImportListDialog(
            onDismiss = { showImportDialog = false },
            onConfirm = { items ->
                actions.onImportList(items)
                showImportDialog = false
            },
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(nestedScrollConnection),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            AnimatedVisibility(
                visible = areBarsVisible,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                ListTopBar(
                    state = state,
                    isSearchActive = isSearchActive,
                    onSearchActiveChange = { isSearchActive = it },
                    actions = actions,
                    onShowCreateList = { showCreateListDialog = true },
                    onShowClearList = { showClearConfirmation = true },
                    onShowImportList = { showImportDialog = true },
                    onShowSortOptions = { showSortOptions = true },
                    onToggleMenu = { showMenu = it },
                    showMenu = showMenu,
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.padding(bottom = 16.dp),
                onClick = {
                    if (state.list == null) {
                        showCreateListDialog = true
                    } else {
                        showAddProductDialog = true
                    }
                },
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_product))
            }
        },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                if (state.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (state.error != null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = state.error,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                } else if (state.list == null) {
                    EmptyListView(
                        onCreateList = { showCreateListDialog = true },
                    )
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
                    LazyColumn(
                        state = scrollState,
                        contentPadding = paddingValues,
                    ) {
                        items(state.products, key = { it.id }) { product ->
                            ProductItem(
                                product = product,
                                onClick = { productOptionsToShow = product },
                                onMoveToCart = { productToMove = product },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ListScreenPreview() {
    val dummyState = ListState(
        list = listPreview,
        products = listProductsPreview,
    )
    MyAppTheme {
        ListScreen(state = dummyState, actions = ListActions())
    }
}

@Preview(showBackground = true)
@Composable
private fun ListScreenEmptyPreview() {
    MyAppTheme {
        ListScreen(state = ListState(), actions = ListActions())
    }
}
