package com.renobile.carrinho.features.list

import com.renobile.carrinho.database.entities.ProductEntity

data class ListActions(
    val onSearchChanged: (String) -> Unit = {},
    val onCreateList: (String) -> Unit = {},
    val onAddOrEditProduct: (ProductEntity) -> Unit = {},
    val onDeleteProduct: (ProductEntity) -> Unit = {},
    val onChangeQuantity: (ProductEntity, Double) -> Unit = { _, _ -> },
    val onClearList: () -> Unit = {},
    val onOpenHistory: () -> Unit = {},
    val onSendList: () -> Unit = {},
    val onShareApp: () -> Unit = {},
    val onMoveToCart: (ProductEntity, Double, Double) -> Unit = { _, _, _ -> }
)
