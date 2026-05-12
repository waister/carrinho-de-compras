package com.renobile.carrinho.features.list.detail

import com.renobile.carrinho.database.entities.ProductEntity
import com.renobile.carrinho.database.entities.PurchaseListEntity

data class ListDetailsState(
    val isLoading: Boolean = false,
    val list: PurchaseListEntity? = null,
    val products: List<ProductEntity> = emptyList(),
    val error: String? = null
) {
    val volumes: Double get() = products.sumOf { it.quantity }
    val total: Double get() = products.sumOf { it.price * it.quantity }
}

sealed interface ListDetailsEvents {
    data class ShowSnackbar(val messageResId: Int) : ListDetailsEvents
    data object ListDeleted : ListDetailsEvents
}
