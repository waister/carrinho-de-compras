package com.renobile.carrinho.features.cart.history

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.renobile.carrinho.R
import com.renobile.carrinho.database.entities.CartEntity
import com.renobile.carrinho.features.cart.SearchAppBar
import com.renobile.carrinho.util.addPluralCharacter
import com.renobile.carrinho.util.formatDate
import com.renobile.carrinho.util.formatPrice
import com.renobile.carrinho.util.formatQuantity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartsHistoryScreen(
    viewModel: CartsHistoryViewModel,
    onBack: () -> Unit,
    onCartClick: (CartEntity) -> Unit
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
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
                    },
                    actions = {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Default.Search, contentDescription = null)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
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
                    text = stringResource(R.string.carts_archive_empty),
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
