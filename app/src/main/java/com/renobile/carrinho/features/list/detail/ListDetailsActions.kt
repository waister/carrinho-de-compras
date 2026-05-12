package com.renobile.carrinho.features.list.detail

import com.renobile.carrinho.database.entities.ProductEntity

data class ListDetailsActions(
    val onBack: () -> Unit = {},
    val onDeleteList: () -> Unit = {},
    val onShareList: () -> Unit = {},
    val onMoveToCart: (ProductEntity) -> Unit = {}
)
