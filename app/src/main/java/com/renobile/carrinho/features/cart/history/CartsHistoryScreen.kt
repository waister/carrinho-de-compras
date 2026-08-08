package com.renobile.carrinho.features.cart.history

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.renobile.carrinho.R
import com.renobile.carrinho.database.entities.CartEntity
import com.renobile.carrinho.features.cart.components.SearchAppBar
import com.renobile.carrinho.util.addPluralCharacter
import com.renobile.carrinho.util.formatDate
import com.renobile.carrinho.util.formatPrice
import com.renobile.carrinho.util.formatQuantity
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartsHistoryScreen(
    viewModel: CartsHistoryViewModel,
    onBackClick: () -> Unit,
    onCartClick: (CartEntity) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    var isSearchActive by remember { mutableStateOf(false) }

    if (isSearchActive) {
        BackHandler {
            isSearchActive = false
            viewModel.onSearchTermsChanged("")
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Box(modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)) {
                    if (isSearchActive) {
                        SearchAppBar(
                            query = state.searchTerms,
                            onQueryChange = { viewModel.onSearchTermsChanged(it) },
                            onCancelSearch = {
                                isSearchActive = false
                                viewModel.onSearchTermsChanged("")
                            }
                        )
                    } else {
                        TopAppBar(
                            title = { Text(stringResource(R.string.carts_history)) },
                            navigationIcon = {
                                IconButton(onClick = onBackClick) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                                }
                            },
                            windowInsets = WindowInsets(0, 0, 0, 0),
                            actions = {
                                IconButton(onClick = { isSearchActive = true }) {
                                    Icon(Icons.Default.Search, contentDescription = null)
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent,
                                titleContentColor = Color.White,
                                navigationIconContentColor = Color.White,
                                actionIconContentColor = Color.White
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.carts.isEmpty()) {
                Text(
                    text = if (state.searchTerms.isNotEmpty()) {
                        stringResource(R.string.search_no_results, state.searchTerms)
                    } else {
                        stringResource(R.string.carts_archive_empty)
                    },
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.carts) { cart ->
                        CartHistoryItem(cart = cart, onClick = { onCartClick(cart) })
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
fun CartHistoryItem(cart: CartEntity, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(cart.name) },
        supportingContent = {
            Column {
                Text(
                    stringResource(
                        R.string.products_details,
                        cart.products,
                        cart.products.addPluralCharacter(),
                        cart.units.formatQuantity(),
                        cart.units.addPluralCharacter()
                    )
                )
                Text(
                    text = "Data: ${cart.dateOpen.formatDate()}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        },
        trailingContent = {
            Text(
                text = cart.valueTotal.formatPrice(),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
        },
        modifier = Modifier.clickable { onClick() }
    )
}
