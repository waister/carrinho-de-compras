package com.renobile.carrinho.features.cart.detail

import com.renobile.carrinho.util.ProductSortOrder

data class CartDetailsActions(
    val onSearchChanged: (String) -> Unit = {},
    val onDeleteCart: () -> Unit = {},
    val onShareCart: () -> Unit = {},
    val onBack: () -> Unit = {},
    val onSortOrderChanged: (ProductSortOrder) -> Unit = {},
)
