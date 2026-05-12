package com.renobile.carrinho.features.list

import com.renobile.carrinho.database.entities.ProductEntity
import com.renobile.carrinho.database.entities.PurchaseListEntity

data class ListState(
    val isLoading: Boolean = false,
    val list: PurchaseListEntity? = null,
    val products: List<ProductEntity> = emptyList(),
    val productNames: List<String> = emptyList(),
    val error: String? = null,
    val searchTerms: String = ""
) {
    val volumes: Double get() = products.sumOf { it.quantity }
    val total: Double get() = products.sumOf { it.price * it.quantity }
}

sealed interface ListEvents {
    data class ShowSnackbar(val messageResId: Int) : ListEvents
}

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
