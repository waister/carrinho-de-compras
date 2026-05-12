package com.renobile.carrinho.features.cart.history

import com.renobile.carrinho.database.entities.CartEntity

data class CartsHistoryState(
    val isLoading: Boolean = false,
    val carts: List<CartEntity> = emptyList(),
    val searchTerms: String = "",
    val error: String? = null
)
