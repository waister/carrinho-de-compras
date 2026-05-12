package com.renobile.carrinho.features.list

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.renobile.carrinho.R
import com.renobile.carrinho.database.entities.ProductEntity
import com.renobile.carrinho.features.cart.*

@Composable
fun ListScreen(
    viewModel: ListViewModel,
    actions: ListActions,
) {
    val state by viewModel.uiState.collectAsState()
    ListContent(state = state, actions = actions, viewModel = viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListContent(
    state: ListState,
    actions: ListActions,
    viewModel: ListViewModel
) {
    var showMenu by remember { mutableStateOf(false) }
    var showClearConfirmation by remember { mutableStateOf(false) }
    var showCreateListDialog by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<ProductEntity?>(null) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var productOptionsToShow by remember { mutableStateOf<ProductEntity?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf<ProductEntity?>(null) }
    var productToMove by remember { mutableStateOf<ProductEntity?>(null) }
    var activeCartId by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        viewModel.getActiveCartId()?.let { activeCartId = it }
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
                    cartId = 0L,
                    listId = state.list?.id ?: 0,
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
        ProductListOptionsDialog(
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
                }
            )
        } else {
            AddProductDialog(
                product = productToMove?.copy(price = 0.0),
                onDismiss = { productToMove = null },
                onConfirm = { _, quantity, price ->
                    productToMove?.let {
                        actions.onMoveToCart(it, quantity, price)
                    }
                    productToMove = null
                }
            )
        }
    }

    Scaffold(
        topBar = {
            ListTopBar(
                state = state,
                isSearchActive = isSearchActive,
                onSearchActiveChange = { isSearchActive = it },
                actions = actions,
                onShowCreateList = { showCreateListDialog = true },
                onShowClearList = { showClearConfirmation = true },
                onToggleMenu = { showMenu = it },
                showMenu = showMenu,
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (state.list == null) {
                    showCreateListDialog = true
                } else {
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
            } else if (state.list == null) {
                EmptyListView(
                    onCreateList = { showCreateListDialog = true },
                )
            } else if (state.products.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.products_empty))
                }
            } else {
                LazyColumn {
                    items(state.products) { product ->
                        ProductItem(
                            product = product,
                            actions = CartActions(),
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
fun ListTopBar(
    state: ListState,
    isSearchActive: Boolean,
    onSearchActiveChange: (Boolean) -> Unit,
    actions: ListActions,
    onShowCreateList: () -> Unit,
    onShowClearList: () -> Unit,
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
                        state.list?.let { stringResource(R.string.label_list, it.name) }
                            ?: stringResource(R.string.purchase_list),
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
                    IconButton(onClick = onShowCreateList) {
                        Icon(
                            painter = painterResource(R.drawable.ic_playlist_plus),
                            contentDescription = stringResource(R.string.new_list),
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
                                text = { Text(stringResource(R.string.lists_history)) },
                                onClick = {
                                    onToggleMenu(false)
                                    actions.onOpenHistory()
                                },
                                leadingIcon = { Icon(Icons.Default.List, null) },
                            )
                            if (state.list != null) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.send_list)) },
                                    onClick = {
                                        onToggleMenu(false)
                                        actions.onSendList()
                                    },
                                    leadingIcon = { Icon(Icons.Default.Share, null) },
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.clear_list)) },
                                    onClick = {
                                        onToggleMenu(false)
                                        onShowClearList()
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
        if (state.list != null) {
            CartHeader(
                total = state.total,
                productCount = state.products.size,
                volumes = state.volumes,
            )
        }
    }
}

@Composable
fun EmptyListView(
    onCreateList: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.lists_empty),
                modifier = Modifier.padding(16.dp),
            )
            Button(onClick = onCreateList) {
                Text(stringResource(R.string.create_list))
            }
        }
    }
}

@Composable
fun ClearListDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
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
fun CreateListDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var listName by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.create_list)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.create_list_notice),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                OutlinedTextField(
                    value = listName,
                    onValueChange = { listName = it },
                    label = { Text(stringResource(R.string.list_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        capitalization = KeyboardCapitalization.Sentences,
                    ),
                    keyboardActions = KeyboardActions(onDone = { onConfirm(listName) }),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(listName) }) { Text(stringResource(R.string.confirm)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}

@Composable
fun ProductListOptionsDialog(
    product: ProductEntity,
    onDismiss: () -> Unit,
    onEdit: (ProductEntity) -> Unit,
    onMoveToCart: (ProductEntity) -> Unit,
    onChangeQuantity: (ProductEntity, Double) -> Unit,
    onDelete: (ProductEntity) -> Unit,
) {
    val options = stringArrayResource(R.array.product_list_options)
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
                    headlineContent = { Text(options.getOrNull(1) ?: stringResource(R.string.move_to_cart)) },
                    modifier = Modifier.clickable { onMoveToCart(product) },
                )
                ListItem(
                    headlineContent = { Text(options.getOrNull(2) ?: "+ 1.0") },
                    modifier = Modifier.clickable { onChangeQuantity(product, 1.0) },
                )
                ListItem(
                    headlineContent = { Text(options.getOrNull(3) ?: "- 1.0") },
                    modifier = Modifier.clickable { onChangeQuantity(product, -1.0) },
                )
                ListItem(
                    headlineContent = { Text(options.getOrNull(4) ?: "Excluir") },
                    modifier = Modifier.clickable { onDelete(product) },
                )
            }
        },
        confirmButton = {},
    )
}
