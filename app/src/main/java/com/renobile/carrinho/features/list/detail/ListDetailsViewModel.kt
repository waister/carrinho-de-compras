package com.renobile.carrinho.features.list.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renobile.carrinho.R
import com.renobile.carrinho.repositories.CartRepository
import com.renobile.carrinho.repositories.ProductRepository
import com.renobile.carrinho.repositories.PurchaseListRepository
import com.renobile.carrinho.database.entities.ProductEntity
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ListDetailsViewModel(
    private val purchaseListRepository: PurchaseListRepository,
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListDetailsState())
    val uiState: StateFlow<ListDetailsState> = _uiState.asStateFlow()

    private val _events = Channel<ListDetailsEvents>()
    val events = _events.receiveAsFlow()

    fun init(listId: Long) {
        loadData(listId)
    }

    private fun loadData(listId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val lists = purchaseListRepository.getAllLists()
                val list = lists.find { it.id == listId }
                if (list == null) {
                    _uiState.update { it.copy(isLoading = false, error = "List not found") }
                    return@launch
                }

                val products = productRepository.getProductsByListId(listId)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        list = list,
                        products = products.sortedByDescending { p -> p.id }
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun deleteList(listId: Long) {
        viewModelScope.launch {
            try {
                val products = productRepository.getProductsByListId(listId)
                products.forEach { productRepository.deleteProduct(it) }

                val lists = purchaseListRepository.getAllLists()
                val list = lists.find { it.id == listId }
                list?.let { purchaseListRepository.deleteList(it) }

                _events.send(ListDetailsEvents.ListDeleted)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun moveToCart(product: ProductEntity, cartId: Long, quantity: Double, price: Double) {
        viewModelScope.launch {
            try {
                val updatedProduct = product.copy(
                    cartId = cartId,
                    listId = 0L,
                    quantity = quantity,
                    price = price
                )
                productRepository.insertProduct(updatedProduct)
                _events.send(ListDetailsEvents.ShowSnackbar(R.string.product_added))
                loadData(product.listId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    suspend fun getActiveCartId(): Long? {
        return cartRepository.getActiveCart()?.id
    }
}
