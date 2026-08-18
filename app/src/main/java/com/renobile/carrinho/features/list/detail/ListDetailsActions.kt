package com.renobile.carrinho.features.list.detail

import com.renobile.carrinho.database.entities.ProductEntity
import com.renobile.carrinho.util.ProductSortOrder

data class ListDetailsActions(
    val onBack: () -> Unit = {},
    val onDeleteList: () -> Unit = {},
    val onShareList: () -> Unit = {},
    val onMoveToCart: (ProductEntity, Long, Double, Double) -> Unit = { _, _, _, _ -> },
    val onSearchChanged: (String) -> Unit = {},
    val onSortOrderChanged: (ProductSortOrder) -> Unit = {},
)
