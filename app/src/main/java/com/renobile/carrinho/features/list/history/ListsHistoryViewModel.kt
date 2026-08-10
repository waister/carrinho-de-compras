package com.renobile.carrinho.features.list.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renobile.carrinho.repositories.PurchaseListRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ListsHistoryViewModel(
    private val purchaseListRepository: PurchaseListRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListsHistoryState())
    val uiState: StateFlow<ListsHistoryState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val allLists = purchaseListRepository.getAllLists().filter { it.dateClose > 0 }
                val searchTerms = _uiState.value.searchTerms

                val filteredLists = if (searchTerms.isNotEmpty()) {
                    allLists.filter {
                        it.name.contains(searchTerms, ignoreCase = true)
                    }
                } else {
                    allLists
                }.sortedByDescending { it.id }

                _uiState.update { it.copy(isLoading = false, lists = filteredLists) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onSearchTermsChanged(terms: String) {
        _uiState.update { it.copy(searchTerms = terms) }
        loadData()
    }
}
