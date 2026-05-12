package com.renobile.carrinho.features.notification.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renobile.carrinho.network.NotificationApiService
import com.renobile.carrinho.network.models.NotificationModel
import com.renobile.carrinho.util.PREF_NOTIFICATION_JSON
import com.renobile.carrinho.util.Prefs
import com.renobile.carrinho.util.getStringVal
import com.renobile.carrinho.util.getValidJSONObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationDetailsState(
    val isLoading: Boolean = false,
    val notification: NotificationModel? = null,
    val error: String? = null
)

class NotificationDetailsViewModel(
    private val apiService: NotificationApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationDetailsState())
    val uiState: StateFlow<NotificationDetailsState> = _uiState.asStateFlow()

    fun init(notificationId: String) {
        val cachedJson = Prefs.getValue(PREF_NOTIFICATION_JSON + notificationId, "")
        val cachedObj = cachedJson.getValidJSONObject()
        
        if (cachedObj != null) {
            val notification = NotificationModel(
                id = notificationId,
                title = cachedObj.getStringVal("title"),
                body = cachedObj.getStringVal("body"),
                date = cachedObj.getStringVal("date"),
                image = cachedObj.getStringVal("image")
            )
            _uiState.update { it.copy(notification = notification) }
        } else {
            loadNotification(notificationId)
        }
    }

    private fun loadNotification(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = apiService.getNotificationDetail(id)
                if (response.success && response.notifications?.isNotEmpty() == true) {
                    val notification = response.notifications.first()
                    _uiState.update { it.copy(isLoading = false, notification = notification) }
                    // Cache if needed (optional since we used Retrofit now)
                } else {
                    _uiState.update { it.copy(isLoading = false, error = response.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
