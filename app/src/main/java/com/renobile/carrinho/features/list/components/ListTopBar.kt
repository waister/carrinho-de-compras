package com.renobile.carrinho.features.list.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.renobile.carrinho.R
import com.renobile.carrinho.features.cart.components.CartHeader
import com.renobile.carrinho.features.cart.components.SearchAppBar
import com.renobile.carrinho.features.list.ListActions
import com.renobile.carrinho.features.list.ListState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.statusBars

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListTopBar(
    state: ListState = ListState(),
    isSearchActive: Boolean = false,
    onSearchActiveChange: (Boolean) -> Unit = {},
    actions: ListActions = ListActions(),
    onShowCreateList: () -> Unit = {},
    onShowClearList: () -> Unit = {},
    onShowImportList: () -> Unit = {},
    onShowSortOptions: () -> Unit = {},
    onToggleMenu: (Boolean) -> Unit = {},
    showMenu: Boolean = false,
) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        contentColor = Color.White
    ) {
        Column(modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)) {
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
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
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
                                        text = { Text(stringResource(R.string.import_list)) },
                                        onClick = {
                                            onToggleMenu(false)
                                            onShowImportList()
                                        },
                                        leadingIcon = { Icon(Icons.Default.Add, null) },
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
            if (state.list != null) {
                CartHeader(
                    total = state.total,
                    productCount = state.products.size,
                    volumes = state.volumes,
                )
            }
        }
    }
}

@Preview
@Composable
private fun ListTopBarPreview() {
    MaterialTheme {
        ListTopBar()
    }
}

@Preview(name = "Searching")
@Composable
private fun ListTopBarSearchingPreview() {
    MaterialTheme {
        ListTopBar(isSearchActive = true)
    }
}
