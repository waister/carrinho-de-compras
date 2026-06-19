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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

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

    fun loadData(showLoading: Boolean = true) {
        viewModelScope.launch {
            fetchData(showLoading)
        }
    }

    private suspend fun fetchData(showLoading: Boolean = true) {
        if (showLoading) _uiState.update { it.copy(isLoading = true) }
        try {
            val lists = withContext(Dispatchers.IO) { purchaseListRepository.getAllLists() }
            val activeList = lists.find { it.dateClose == 0L }
            val products = activeList?.let { 
                withContext(Dispatchers.IO) { productRepository.getProductsByListId(it.id) } 
            } ?: emptyList()
            val suggestions = withContext(Dispatchers.IO) { productRepository.getProductSuggestions() }

            _uiState.update { 
                it.copy(
                    isLoading = false,
                    list = activeList,
                    products = if (it.searchTerms.isEmpty()) products 
                               else products.filter { p -> p.name.contains(it.searchTerms, ignoreCase = true) },
                    suggestions = suggestions,
                    error = null
                )
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, error = e.message) }
        }
    }

    fun onSearchTermsChanged(terms: String) {
        _uiState.update { it.copy(searchTerms = terms) }
        loadData(showLoading = false)
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
                    withContext(Dispatchers.IO) { purchaseListRepository.insertList(updatedList) }
                }

                val finalName = name.ifEmpty { createCartListNameGeneric() }
                val lists = withContext(Dispatchers.IO) { purchaseListRepository.getAllLists() }
                val newId = (lists.maxOfOrNull { it.id } ?: 0L) + 1
                val newList = PurchaseListEntity(
                    id = newId,
                    name = finalName,
                    dateOpen = System.currentTimeMillis(),
                    dateClose = 0L,
                    products = 0,
                    units = 0.0,
                    valueTotal = 0.0
                )
                withContext(Dispatchers.IO) { purchaseListRepository.insertList(newList) }
                fetchData()
                _events.send(ListEvents.ShowSnackbar(R.string.create_list_success))
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun addOrEditProduct(product: ProductEntity) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { productRepository.insertProduct(product) }
                fetchData(showLoading = false)
                _events.send(ListEvents.ShowSnackbar(R.string.product_added))
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { productRepository.deleteProduct(product) }
                fetchData(showLoading = false)
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
                val products = withContext(Dispatchers.IO) { productRepository.getProductsByListId(listId) }
                products.forEach { 
                    withContext(Dispatchers.IO) { productRepository.deleteProduct(it) } 
                }
                fetchData(showLoading = false)
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
                withContext(Dispatchers.IO) { productRepository.insertProduct(updatedProduct) }
                fetchData(showLoading = false)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun moveToCart(product: ProductEntity, quantity: Double, price: Double) {
        viewModelScope.launch {
            try {
                val activeCart = withContext(Dispatchers.IO) { cartRepository.getActiveCart() }
                if (activeCart == null) {
                    _events.send(ListEvents.ShowSnackbar(R.string.create_cart_needed))
                    return@launch
                }

                val updatedProduct = product.copy(
                    id = System.currentTimeMillis(),
                    cartId = activeCart.id,
                    listId = 0L,
                    quantity = quantity,
                    price = price
                )
                withContext(Dispatchers.IO) { 
                    productRepository.deleteProduct(product)
                    productRepository.insertProduct(updatedProduct) 
                }
                fetchData(showLoading = false)
                _events.send(ListEvents.ShowSnackbar(R.string.product_added))
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun importList(items: List<String>) {
        if (items.isEmpty()) return
        
        viewModelScope.launch {
            try {
                val currentList = _uiState.value.list
                if (currentList == null) {
                    _events.send(ListEvents.ShowSnackbar(R.string.error_list_not_found))
                    return@launch
                }

                _uiState.update { it.copy(isLoading = true, searchTerms = "") }
                
                val baseTimestamp = System.currentTimeMillis()
                val products = items.mapIndexed { index, itemText ->
                    val trimmed = itemText.trim()
                    val parts = trimmed.split(" ", limit = 2)
                    val (quantity, name) = if (parts.size == 2) {
                        val q = parts[0].replace(',', '.').toDoubleOrNull()
                        if (q != null && parts[1].isNotBlank()) {
                            q to parts[1].trim()
                        } else {
                            1.0 to trimmed
                        }
                    } else {
                        1.0 to trimmed
                    }

                    ProductEntity(
                        id = baseTimestamp + index + Random.nextLong(1000000, 9000000),
                        cartId = 0L,
                        listId = currentList.id,
                        name = name,
                        quantity = quantity,
                        price = 0.0
                    )
                }
                
                withContext(Dispatchers.IO) {
                    productRepository.insertProducts(products)
                }
                
                val updatedProducts = withContext(Dispatchers.IO) {
                    productRepository.getProductsByListId(currentList.id) 
                }
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        products = updatedProducts,
                        error = null
                    )
                }

                _events.send(ListEvents.ShowSnackbar(R.string.import_success))
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    suspend fun getActiveCartId(): Long? {
        return withContext(Dispatchers.IO) { cartRepository.getActiveCart()?.id }
    }
}
