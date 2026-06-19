package com.renobile.carrinho.features.cart.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.renobile.carrinho.R
import com.renobile.carrinho.database.entities.CartEntity
import com.renobile.carrinho.database.entities.ProductEntity
import com.renobile.carrinho.features.cart.CartActions
import com.renobile.carrinho.features.cart.CartState
import com.renobile.carrinho.ui.theme.MyAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartTopBar(
    state: CartState,
    isSearchActive: Boolean = false,
    onSearchActiveChange: (Boolean) -> Unit = {},
    actions: CartActions = CartActions(),
    onShowCreateCart: () -> Unit = {},
    onShowClearCart: () -> Unit = {},
    onShowSortOptions: () -> Unit = {},
    onToggleMenu: (Boolean) -> Unit = {},
    showMenu: Boolean = false,
) {
    Column {
        if (isSearchActive) {
            SearchAppBar(
                query = state.searchTerms,
                onQueryChange = actions.onSearchChanged,
                onCancelSearch = {
                    onSearchActiveChange(false)
                    actions.onSearchChanged("")
                },
            )
        } else {
            TopAppBar(
                title = {
                    Text(
                        state.cart?.let { stringResource(R.string.label_cart, it.name) }
                            ?: stringResource(R.string.app_name),
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White,
                ),
                actions = {
                    IconButton(onClick = { onSearchActiveChange(true) }) {
                        Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search_products))
                    }
                    IconButton(onClick = onShowCreateCart) {
                        Icon(
                            painter = painterResource(R.drawable.ic_cart_plus),
                            contentDescription = stringResource(R.string.new_cart),
                        )
                    }
                    Box {
                        IconButton(onClick = { onToggleMenu(!showMenu) }) {
                            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.more_options))
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { onToggleMenu(false) },
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.carts_history)) },
                                onClick = {
                                    onToggleMenu(false)
                                    actions.onOpenHistory()
                                },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.List, null) },
                            )
                            if (state.cart != null) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.send_cart)) },
                                    onClick = {
                                        onToggleMenu(false)
                                        actions.onSendCart()
                                    },
                                    leadingIcon = { Icon(Icons.Default.Share, null) },
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.clear_cart)) },
                                    onClick = {
                                        onToggleMenu(false)
                                        onShowClearCart()
                                    },
                                    leadingIcon = { Icon(Icons.Default.Delete, null) },
                                )
                            }
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.share_app)) },
                                onClick = {
                                    onToggleMenu(false)
                                    actions.onShareApp()
                                },
                                leadingIcon = { Icon(Icons.Default.Share, null) },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.sort_order)) },
                                onClick = {
                                    onToggleMenu(false)
                                    onShowSortOptions()
                                },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Sort, null) },
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

@Preview(showBackground = true)
@Composable
private fun CartTopBarNoCartPreview() {
    MyAppTheme {
        CartTopBar(
            state = CartState(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CartTopBarWithCartPreview() {
    MyAppTheme {
        CartTopBar(
            state = CartState(
                cart = CartEntity(1, "Mercado", 0, 0, 0, 0.0, 100.0, ""),
                products = listOf(
                    ProductEntity(1, 1, 0, "Arroz", 2.0, 15.0),
                    ProductEntity(2, 1, 0, "Feijão", 3.0, 10.0),
                )
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CartTopBarSearchPreview() {
    MyAppTheme {
        CartTopBar(
            state = CartState(searchTerms = "Arroz"),
            isSearchActive = true,
        )
    }
}
