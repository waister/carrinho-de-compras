package com.renobile.carrinho.features.cart.detail

data class CartDetailsActions(
    val onSearchChanged: (String) -> Unit = {},
    val onDeleteCart: () -> Unit = {},
    val onShareCart: () -> Unit = {},
    val onBack: () -> Unit = {}
)
