package com.renobile.carrinho.features.cart

import com.renobile.carrinho.database.entities.ProductEntity

data class CartActions(
    val onSearchChanged: (String) -> Unit = {},
    val onCreateCart: (String) -> Unit = {},
    val onAddOrEditProduct: (ProductEntity) -> Unit = {},
    val onDeleteProduct: (ProductEntity) -> Unit = {},
    val onChangeQuantity: (ProductEntity, Double) -> Unit = { _, _ -> },
    val onSendCart: () -> Unit = {},
    val onClearCart: () -> Unit = {},
    val onOpenHistory: () -> Unit = {},
    val onShareApp: () -> Unit = {},
    val onShowInterstitialAd: () -> Unit = {}
)
