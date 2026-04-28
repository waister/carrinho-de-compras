package com.renobile.carrinho.features.cart

import com.renobile.carrinho.database.entities.CartEntity
import com.renobile.carrinho.database.entities.ProductEntity

data class CartState(
    val isLoading: Boolean = false,
    val cart: CartEntity? = null,
    val products: List<ProductEntity> = emptyList(),
    val productNames: List<String> = emptyList(),
    val error: String? = null,
    val searchTerms: String = ""
) {
    val volumes: Double get() = products.sumOf { it.quantity }
    val total: Double get() = products.sumOf { it.price * it.quantity }
}

sealed interface CartEvents {
    data class ShowSnackbar(val messageResId: Int) : CartEvents
    data object ShowInterstitialAd : CartEvents
}
