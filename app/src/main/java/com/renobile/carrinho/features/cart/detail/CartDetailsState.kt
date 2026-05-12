package com.renobile.carrinho.features.cart.detail

import com.renobile.carrinho.database.entities.CartEntity
import com.renobile.carrinho.database.entities.ProductEntity

data class CartDetailsState(
    val isLoading: Boolean = false,
    val cart: CartEntity? = null,
    val products: List<ProductEntity> = emptyList(),
    val searchTerms: String = "",
    val error: String? = null
) {
    val volumes: Double get() = products.sumOf { it.quantity }
    val total: Double get() = products.sumOf { it.price * it.quantity }
}

sealed interface CartDetailsEvents {
    data class ShowSnackbar(val messageResId: Int) : CartDetailsEvents
    data object CartDeleted : CartDetailsEvents
}
