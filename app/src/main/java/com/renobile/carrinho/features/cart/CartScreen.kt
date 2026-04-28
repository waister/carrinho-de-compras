package com.renobile.carrinho.features.cart

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.renobile.carrinho.R
import com.renobile.carrinho.database.entities.CartEntity
import com.renobile.carrinho.database.entities.ProductEntity
import com.renobile.carrinho.util.addPluralCharacter
import com.renobile.carrinho.util.formatPrice
import java.text.NumberFormat
import com.renobile.carrinho.util.formatQuantity

@Composable
fun CartScreen(
    viewModel: CartViewModel,
    actions: CartActions,
) {
    val state by viewModel.uiState.collectAsState()
    CartContent(state = state, actions = actions)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartContent(
    state: CartState,
    actions: CartActions,
) {
    var showMenu by remember { mutableStateOf(false) }
    var showClearConfirmation by remember { mutableStateOf(false) }
    var showCreateCartDialog by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<ProductEntity?>(null) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var productOptionsToShow by remember { mutableStateOf<ProductEntity?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf<ProductEntity?>(null) }

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

    if (showAddProductDialog || productToEdit != null) {
        AddProductDialog(
            product = productToEdit,
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
                showAddProductDialog = false
                productToEdit = null
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
        topBar = {
            CartTopBar(
                state = state,
                isSearchActive = isSearchActive,
                onSearchActiveChange = { isSearchActive = it },
                actions = actions,
                onShowCreateCart = { showCreateCartDialog = true },
                onShowClearCart = { showClearConfirmation = true },
                onToggleMenu = { showMenu = it },
                showMenu = showMenu,
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (state.cart == null) {
                    showCreateCartDialog = true
                } else {
                    actions.onShowInterstitialAd()
                    showAddProductDialog = true
                }
            }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_product))
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
                EmptyCartView(
                    isCartCreated = state.cart != null,
                    onCreateCart = { showCreateCartDialog = true },
                )
            } else {
                LazyColumn {
                    items(state.products) { product ->
                        ProductItem(
                            product = product,
                            actions = actions,
                            onClick = { productOptionsToShow = product },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartTopBar(
    state: CartState,
    isSearchActive: Boolean,
    onSearchActiveChange: (Boolean) -> Unit,
    actions: CartActions,
    onShowCreateCart: () -> Unit,
    onShowClearCart: () -> Unit,
    onToggleMenu: (Boolean) -> Unit,
    showMenu: Boolean,
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

@Composable
fun EmptyCartView(
    isCartCreated: Boolean,
    onCreateCart: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(if (!isCartCreated) R.string.carts_empty else R.string.products_empty),
                modifier = Modifier.padding(16.dp),
            )
            if (!isCartCreated) {
                Button(onClick = onCreateCart) {
                    Text(stringResource(R.string.create_cart))
                }
            }
        }
    }
}

@Composable
fun ClearCartDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.confirmation)) },
        text = { Text(stringResource(R.string.confirm_delete_all)) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.confirm)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}

@Composable
fun DeleteProductDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.confirmation)) },
        text = { Text(stringResource(R.string.confirm_delete)) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.confirm)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}

@Composable
fun CreateCartDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var cartName by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.create_cart)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.create_cart_notice),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                OutlinedTextField(
                    value = cartName,
                    onValueChange = { cartName = it },
                    label = { Text(stringResource(R.string.cart_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        capitalization = KeyboardCapitalization.Sentences,
                    ),
                    keyboardActions = KeyboardActions(onDone = { onConfirm(cartName) }),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(cartName) }) { Text(stringResource(R.string.confirm)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}

@Composable
fun ProductOptionsDialog(
    product: ProductEntity,
    onDismiss: () -> Unit,
    onEdit: (ProductEntity) -> Unit,
    onChangeQuantity: (ProductEntity, Double) -> Unit,
    onDelete: (ProductEntity) -> Unit,
) {
    val options = stringArrayResource(R.array.product_cart_options)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(product.name) },
        text = {
            Column {
                ListItem(
                    headlineContent = { Text(options.getOrNull(0) ?: stringResource(R.string.edit_product)) },
                    modifier = Modifier.clickable { onEdit(product) },
                )
                ListItem(
                    headlineContent = { Text(options.getOrNull(1) ?: "+ 1.0") },
                    modifier = Modifier.clickable { onChangeQuantity(product, 1.0) },
                )
                ListItem(
                    headlineContent = { Text(options.getOrNull(2) ?: "- 1.0") },
                    modifier = Modifier.clickable { onChangeQuantity(product, -1.0) },
                )
                ListItem(
                    headlineContent = { Text(options.getOrNull(3) ?: "Excluir") },
                    modifier = Modifier.clickable { onDelete(product) },
                )
            }
        },
        confirmButton = {},
    )
}

@Composable
fun AddProductDialog(
    product: ProductEntity?,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Double) -> Unit,
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var quantityText by remember { mutableStateOf(product?.quantity?.formatQuantity() ?: "1") }
    
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance() }
    
    var priceTextFieldValue by remember {
        val initialText = product?.price?.let { if (it == 0.0) "" else it.formatPrice() } ?: ""
        mutableStateOf(TextFieldValue(text = initialText, selection = TextRange(initialText.length)))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(if (product == null) R.string.add_product else R.string.edit_product))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next,
                    ),
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.matches(Regex("^\\d*[.,]?\\d*$"))) {
                                quantityText = newValue
                            }
                        },
                        label = { Text(stringResource(R.string.quantity)) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next,
                        ),
                    )
                    IconButton(onClick = {
                        val current = quantityText.parseToDouble()
                        quantityText = (current + 1).formatQuantity()
                    }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }
                    IconButton(onClick = {
                        val current = quantityText.parseToDouble()
                        if (current > 1) {
                            quantityText = (current - 1).formatQuantity()
                        }
                    }) {
                        Icon(painter = painterResource(R.drawable.ic_minus), contentDescription = null)
                    }
                }

                OutlinedTextField(
                    value = priceTextFieldValue,
                    onValueChange = { newValue ->
                        val digits = newValue.text.replace(Regex("\\D"), "")
                        val newText = if (digits.isEmpty()) {
                            ""
                        } else {
                            try {
                                val value = digits.toDouble() / 100
                                currencyFormatter.format(value)
                            } catch (_: Exception) {
                                ""
                            }
                        }
                        priceTextFieldValue = TextFieldValue(
                            text = newText,
                            selection = TextRange(newText.length)
                        )
                    },
                    label = { Text(stringResource(R.string.price)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (name.isNotBlank()) {
                                onConfirm(
                                    name,
                                    quantityText.parseToDouble(),
                                    priceTextFieldValue.text.parseCurrencyToDouble(),
                                )
                            }
                        },
                    ),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(
                            name,
                            quantityText.parseToDouble(),
                            priceTextFieldValue.text.parseCurrencyToDouble(),
                        )
                    }
                },
            ) {
                Text(stringResource(if (product == null) R.string.add else R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(if (product == null) R.string.cancel else R.string.discard))
            }
        },
    )
}

private fun String.parseToDouble(): Double {
    val clean = this.replace(Regex("[^0-9,.]"), "").replace(",", ".")
    return clean.toDoubleOrNull() ?: 0.0
}

private fun String.parseCurrencyToDouble(): Double {
    val digits = this.replace(Regex("\\D"), "")
    return if (digits.isEmpty()) 0.0 else digits.toDouble() / 100
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchAppBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onCancelSearch: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    TopAppBar(
        title = {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        text = stringResource(R.string.search_products),
                        color = Color.White.copy(alpha = 0.7f),
                    )
                },
                textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    cursorColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { /* Keyboard is hidden by default on enter */ }),
            )
        },
        navigationIcon = {
            IconButton(onClick = onCancelSearch) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
        },
        actions = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = null)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            navigationIconContentColor = Color.White,
            actionIconContentColor = Color.White,
        ),
    )
}

@Composable
fun CartHeader(
    total: Double,
    productCount: Int,
    volumes: Double,
) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        contentColor = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = total.formatPrice(),
                fontSize = 22.sp,
            )
            Text(
                text = stringResource(
                    R.string.products_details,
                    productCount,
                    productCount.addPluralCharacter(),
                    volumes.formatQuantity(),
                    volumes.addPluralCharacter(),
                ),
                fontSize = 13.sp,
            )
        }
    }
}

@Composable
fun ProductItem(
    product: ProductEntity,
    @Suppress("UNUSED_PARAMETER") actions: CartActions,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(product.name) },
        supportingContent = {
            Text(
                stringResource(
                    R.string.product_details,
                    product.quantity.formatQuantity(),
                    product.quantity.addPluralCharacter(),
                    product.price.formatPrice(),
                ),
            )
        },
        trailingContent = {
            Text((product.quantity * product.price).formatPrice())
        },
        modifier = Modifier.clickable { onClick() },
    )
}

@Preview(showBackground = true)
@Composable
fun CartScreenPreview() {
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
    MaterialTheme {
        CartContent(state = dummyState, actions = CartActions())
    }
}

@Preview(showBackground = true)
@Composable
fun CartScreenEmptyPreview() {
    MaterialTheme {
        CartContent(state = CartState(), actions = CartActions())
    }
}
