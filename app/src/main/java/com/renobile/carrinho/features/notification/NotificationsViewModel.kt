package com.renobile.carrinho.features.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renobile.carrinho.network.NotificationApiService
import com.renobile.carrinho.network.models.NotificationModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationsState(
    val isLoading: Boolean = false,
    val notifications: List<NotificationModel> = emptyList(),
    val error: String? = null
)

class NotificationsViewModel(
    private val apiService: NotificationApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsState())
    val uiState: StateFlow<NotificationsState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = apiService.getNotifications()
                if (response.success) {
                    _uiState.update { it.copy(isLoading = false, notifications = response.notifications ?: emptyList()) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = response.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
