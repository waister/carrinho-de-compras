package com.renobile.carrinho.features.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renobile.carrinho.R
import com.renobile.carrinho.database.entities.ProductEntity
import com.renobile.carrinho.database.entities.PurchaseListEntity
import com.renobile.carrinho.repositories.CartRepository
import com.renobile.carrinho.repositories.ProductRepository
import com.renobile.carrinho.repositories.PurchaseListRepository
import com.renobile.carrinho.util.PREF_SORT_ORDER
import com.renobile.carrinho.util.Prefs
import com.renobile.carrinho.util.ProductSortOrder
import com.renobile.carrinho.util.createCartListNameGeneric
import kotlinx.coroutines.CoroutineDispatcher
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
    private val cartRepository: CartRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
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
            val lists = withContext(ioDispatcher) { purchaseListRepository.getAllLists() }
            val activeList = lists.find { it.dateClose == 0L }
            val products = activeList?.let { 
                withContext(ioDispatcher) { productRepository.getProductsByListId(it.id) } 
            } ?: emptyList()
            val suggestions = withContext(ioDispatcher) { productRepository.getProductSuggestions() }
            val sortOrder = ProductSortOrder.valueOf(
                Prefs.getValue(PREF_SORT_ORDER, ProductSortOrder.NEWEST.name)
            )

            _uiState.update { 
                it.copy(
                    isLoading = false,
                    list = activeList,
                    products = products
                        .filter { p -> it.searchTerms.isEmpty() || p.name.contains(it.searchTerms, ignoreCase = true) }
                        .sort(sortOrder),
                    suggestions = suggestions,
                    sortOrder = sortOrder,
                    error = null
                )
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, error = e.message) }
        }
    }

    private fun List<ProductEntity>.sort(order: ProductSortOrder): List<ProductEntity> {
        return when (order) {
            ProductSortOrder.NEWEST -> sortedByDescending { it.id }
            ProductSortOrder.OLDEST -> sortedBy { it.id }
            ProductSortOrder.NAME_ASC -> sortedBy { it.name.lowercase() }
            ProductSortOrder.NAME_DESC -> sortedByDescending { it.name.lowercase() }
            ProductSortOrder.PRICE_ASC -> sortedBy { it.price }
            ProductSortOrder.PRICE_DESC -> sortedByDescending { it.price }
        }
    }

    fun onSortOrderChanged(order: ProductSortOrder) {
        Prefs.putValue(PREF_SORT_ORDER, order.name)
        loadData(showLoading = false)
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
                    withContext(ioDispatcher) { purchaseListRepository.insertList(updatedList) }
                }

                val finalName = name.ifEmpty { createCartListNameGeneric() }
                val lists = withContext(ioDispatcher) { purchaseListRepository.getAllLists() }
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
                withContext(ioDispatcher) { purchaseListRepository.insertList(newList) }
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
                withContext(ioDispatcher) { productRepository.insertProduct(product) }
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
                withContext(ioDispatcher) { productRepository.deleteProduct(product) }
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
                val products = withContext(ioDispatcher) { productRepository.getProductsByListId(listId) }
                products.forEach { 
                    withContext(ioDispatcher) { productRepository.deleteProduct(it) } 
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
                withContext(ioDispatcher) { productRepository.insertProduct(updatedProduct) }
                fetchData(showLoading = false)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun moveToCart(product: ProductEntity, quantity: Double, price: Double) {
        viewModelScope.launch {
            try {
                val activeCart = withContext(ioDispatcher) { cartRepository.getActiveCart() }
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
                withContext(ioDispatcher) { 
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
                
                withContext(ioDispatcher) {
                    productRepository.insertProducts(products)
                }
                
                val updatedProducts = withContext(ioDispatcher) {
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
        return withContext(ioDispatcher) { cartRepository.getActiveCart()?.id }
    }
}
