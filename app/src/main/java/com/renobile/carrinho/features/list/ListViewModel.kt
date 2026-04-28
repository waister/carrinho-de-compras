package com.renobile.carrinho.features.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renobile.carrinho.R
import com.renobile.carrinho.database.entities.ProductEntity
import com.renobile.carrinho.database.entities.PurchaseListEntity
import com.renobile.carrinho.repositories.CartRepository
import com.renobile.carrinho.repositories.ProductRepository
import com.renobile.carrinho.repositories.PurchaseListRepository
import com.renobile.carrinho.util.createCartListNameGeneric
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ListViewModel(
    private val purchaseListRepository: PurchaseListRepository,
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListState())
    val uiState: StateFlow<ListState> = _uiState.asStateFlow()

    private val _events = Channel<ListEvents>()
    val events = _events.receiveAsFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val lists = purchaseListRepository.getAllLists()
                val activeList = lists.find { it.dateClose == 0L }
                val products = activeList?.let { productRepository.getProductsByListId(it.id) } ?: emptyList()
                val names = productRepository.getAllProductNames()

                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        list = activeList,
                        products = if (it.searchTerms.isEmpty()) products 
                                   else products.filter { p -> p.name.contains(it.searchTerms, ignoreCase = true) },
                        productNames = names
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onSearchTermsChanged(terms: String) {
        _uiState.update { it.copy(searchTerms = terms) }
        loadData()
    }

    fun createList(name: String) {
        viewModelScope.launch {
            try {
                val currentList = _uiState.value.list
                val currentProducts = _uiState.value.products

                if (currentList != null) {
                    val updatedList = currentList.copy(
                        dateClose = System.currentTimeMillis(),
                        products = currentProducts.size,
                        units = currentProducts.sumOf { it.quantity },
                        valueTotal = currentProducts.sumOf { it.price * it.quantity }
                    )
                    purchaseListRepository.insertList(updatedList)
                }

                val finalName = if (name.isEmpty()) createCartListNameGeneric() else name
                val lists = purchaseListRepository.getAllLists()
                val newId = (lists.firstOrNull()?.id ?: 0L) + 1
                val newList = PurchaseListEntity(
                    id = newId,
                    name = finalName,
                    dateOpen = System.currentTimeMillis(),
                    dateClose = 0L,
                    products = 0,
                    units = 0.0,
                    valueTotal = 0.0
                )
                purchaseListRepository.insertList(newList)
                loadData()
                _events.send(ListEvents.ShowSnackbar(R.string.create_list_success))
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun addOrEditProduct(product: ProductEntity) {
        viewModelScope.launch {
            try {
                productRepository.insertProduct(product)
                loadData()
                _events.send(ListEvents.ShowSnackbar(R.string.product_added))
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            try {
                productRepository.deleteProduct(product)
                loadData()
                _events.send(ListEvents.ShowSnackbar(R.string.success_delete))
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearList() {
        val listId = _uiState.value.list?.id ?: return
        viewModelScope.launch {
            try {
                val products = productRepository.getProductsByListId(listId)
                products.forEach { productRepository.deleteProduct(it) }
                loadData()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun changeQuantity(product: ProductEntity, delta: Double) {
        if (delta < 0 && (product.quantity + delta) < 0) {
            viewModelScope.launch { _events.send(ListEvents.ShowSnackbar(R.string.error_quantity_min)) }
            return
        }
        viewModelScope.launch {
            try {
                val updatedProduct = product.copy(quantity = product.quantity + delta)
                productRepository.insertProduct(updatedProduct)
                loadData()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun moveToCart(product: ProductEntity, quantity: Double, price: Double) {
        viewModelScope.launch {
            try {
                val activeCart = cartRepository.getActiveCart()
                if (activeCart == null) {
                    _events.send(ListEvents.ShowSnackbar(R.string.create_cart_needed))
                    return@launch
                }

                val updatedProduct = product.copy(
                    cartId = activeCart.id,
                    listId = 0L,
                    quantity = quantity,
                    price = price
                )
                productRepository.insertProduct(updatedProduct)
                _events.send(ListEvents.ShowSnackbar(R.string.product_added))
                loadData()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    suspend fun getActiveCartId(): Long? {
        return cartRepository.getActiveCart()?.id
    }
}
