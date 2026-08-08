package com.renobile.carrinho.features.cart

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.renobile.carrinho.database.entities.CartEntity
import com.renobile.carrinho.database.entities.ProductEntity
import com.renobile.carrinho.features.cart.components.AddProductDialog
import com.renobile.carrinho.features.cart.components.CartTopBar
import com.renobile.carrinho.features.cart.components.ClearCartDialog
import com.renobile.carrinho.features.cart.components.CreateCartDialog
import com.renobile.carrinho.features.cart.components.DeleteProductDialog
import com.renobile.carrinho.features.cart.components.EmptyCartView
import com.renobile.carrinho.features.cart.components.ProductItem
import com.renobile.carrinho.features.cart.components.ProductOptionsDialog
import com.renobile.carrinho.features.cart.components.SortOptionsDialog
import com.renobile.carrinho.ui.theme.MyAppTheme

@Composable
fun CartScreen(
    state: CartState,
    actions: CartActions,
    areBarsVisible: Boolean = true,
) {
    var showMenu by remember { mutableStateOf(false) }
    var showClearConfirmation by remember { mutableStateOf(false) }
    var showCreateCartDialog by remember { mutableStateOf(false) }
    var showSortOptions by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<ProductEntity?>(null) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var productOptionsToShow by remember { mutableStateOf<ProductEntity?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf<ProductEntity?>(null) }

    val scrollState = rememberLazyListState()
    val nestedScrollConnection = remember(scrollState) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput && (scrollState.canScrollForward || scrollState.canScrollBackward)) {
                    if (available.y < -10) {
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
        ClearCartDialog(
            onDismiss = { showClearConfirmation = false },
            onConfirm = {
                actions.onClearCart()
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

    if (showCreateCartDialog) {
        CreateCartDialog(
            onDismiss = { showCreateCartDialog = false },
            onConfirm = { name ->
                actions.onCreateCart(name)
                showCreateCartDialog = false
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
            }
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
                    cartId = state.cart?.id ?: 0,
                    listId = 0,
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
        ProductOptionsDialog(
            product = productOptionsToShow!!,
            onDismiss = { productOptionsToShow = null },
            onEdit = {
                productToEdit = it
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

    Scaffold(
        modifier = Modifier.nestedScroll(nestedScrollConnection),
        topBar = {
            AnimatedVisibility(
                visible = areBarsVisible,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                CartTopBar(
                    state = state,
                    isSearchActive = isSearchActive,
                    onSearchActiveChange = { isSearchActive = it },
                    actions = actions,
                    onShowCreateCart = { showCreateCartDialog = true },
                    onShowClearCart = { showClearConfirmation = true },
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
                    if (state.cart == null) {
                        showCreateCartDialog = true
                    } else {
                        actions.onShowInterstitialAd()
                        showAddProductDialog = true
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_product))
            }
        },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                if (state.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (state.products.isEmpty()) {
                    EmptyCartView(
                        isCartCreated = state.cart != null,
                        onCreateCart = { showCreateCartDialog = true },
                    )
                } else {
                    LazyColumn(
                        state = scrollState,
                        contentPadding = paddingValues
                    ) {
                        items(state.products) { product ->
                            ProductItem(
                                product = product,
                                onClick = { productOptionsToShow = product },
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
private fun CartScreenPreview() {
    val dummyState = CartState(
        cart = CartEntity(
            id = 1,
            name = "Compras Semanal",
            dateOpen = System.currentTimeMillis(),
            dateClose = 0L,
            products = 2,
            units = 5.0,
            valueTotal = 50.0,
            keywords = "",
        ),
        products = listOf(
            ProductEntity(1, 1, 0, "Arroz", 2.0, 15.0),
            ProductEntity(2, 1, 0, "Feijão", 3.0, 10.0),
        ),
    )
    MyAppTheme {
        CartScreen(state = dummyState, actions = CartActions())
    }
}

@Preview(showBackground = true)
@Composable
private fun CartScreenEmptyPreview() {
    MyAppTheme {
        CartScreen(state = CartState(), actions = CartActions())
    }
}
