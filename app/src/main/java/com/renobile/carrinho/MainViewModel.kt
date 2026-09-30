package com.renobile.carrinho

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.renobile.carrinho.database.entities.ProductEntity
import com.renobile.carrinho.database.entities.PurchaseListEntity
import com.renobile.carrinho.repositories.ConfigRepository
import com.renobile.carrinho.repositories.ProductRepository
import com.renobile.carrinho.repositories.PurchaseListRepository
import com.renobile.carrinho.util.PREF_FCM_TOKEN
import com.renobile.carrinho.util.Prefs
import com.renobile.carrinho.util.createCartListNameGeneric
import com.renobile.carrinho.util.havePlan
import com.renobile.carrinho.util.isDebug
import kotlin.random.Random
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(
    private val configRepository: ConfigRepository,
    private val purchaseListRepository: PurchaseListRepository? = null,
    private val productRepository: ProductRepository? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainState(havePlan = havePlan()))
    val uiState: StateFlow<MainState> = _uiState.asStateFlow()

    fun updatePlanStatus() {
        _uiState.update { it.copy(havePlan = havePlan()) }
    }

    fun checkVersion() {
        val token = Prefs.getValue(PREF_FCM_TOKEN, "")

        if (token.isNotEmpty()) {
            viewModelScope.launch {
                configRepository.identify(token).onSuccess { response ->
                    configRepository.saveConfig(response)

                    if (response.success) {
                        if (BuildConfig.VERSION_CODE < response.versionMin) {
                            _uiState.update { it.copy(versionUpdate = VersionUpdate.Needed) }
                        } else if (BuildConfig.VERSION_CODE < response.versionLast) {
                            _uiState.update { it.copy(versionUpdate = VersionUpdate.Available) }
                        }
                    }
                }
            }
        }
    }

    fun checkTokenFcm() {
        val lastToken = Prefs.getValue(PREF_FCM_TOKEN, "")

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result

                try {
                    if (token != lastToken) {
                        Prefs.putValue(PREF_FCM_TOKEN, token)
                        checkVersion()
                    }
                } catch (e: Exception) {
                    if (isDebug()) e.printStackTrace()
                }
            }
        }
    }

    fun setBottomBarVisible(visible: Boolean) {
        _uiState.update { it.copy(isBottomBarVisible = visible, areBarsVisible = true) }
    }

    fun setBarsVisible(visible: Boolean) {
        if (_uiState.value.areBarsVisible != visible) {
            _uiState.update { it.copy(areBarsVisible = visible) }
        }
    }

    fun onVersionUpdateHandled() {
        _uiState.update { it.copy(versionUpdate = null) }
    }

    fun handleSharedText(text: String?) {
        if (!text.isNullOrBlank()) {
            _uiState.update { it.copy(pendingImportText = text) }
        }
    }

    fun handleSharedImage(context: android.content.Context, imageUri: android.net.Uri?) {
        if (imageUri != null) {
            viewModelScope.launch {
                com.renobile.carrinho.util.TextRecognitionHelper.extractShoppingListFromImage(context, imageUri)
                    .onSuccess { items ->
                        if (items.isNotEmpty()) {
                            _uiState.update { it.copy(pendingImportText = items.joinToString("\n")) }
                        }
                    }
            }
        }
    }

    fun clearPendingImport() {
        _uiState.update { it.copy(pendingImportText = null) }
    }

    fun addVoiceProductToList(productName: String) {
        val cleanName = productName.trim()
        if (cleanName.isEmpty() || purchaseListRepository == null || productRepository == null) return

        viewModelScope.launch {
            try {
                val lists = withContext(ioDispatcher) { purchaseListRepository.getAllLists() }
                var activeList = lists.find { it.dateClose == 0L }
                if (activeList == null) {
                    val newId = (lists.maxOfOrNull { it.id } ?: 0L) + 1
                    val newList = PurchaseListEntity(
                        id = newId,
                        name = createCartListNameGeneric(),
                        dateOpen = System.currentTimeMillis(),
                        dateClose = 0L,
                        products = 0,
                        units = 0.0,
                        valueTotal = 0.0,
                    )
                    withContext(ioDispatcher) { purchaseListRepository.insertList(newList) }
                    activeList = newList
                }

                val existingProducts = withContext(ioDispatcher) {
                    productRepository.getProductsByListId(activeList.id)
                }

                val existing = existingProducts.find { it.name.trim().equals(cleanName, ignoreCase = true) }
                if (existing != null) {
                    val updated = existing.copy(quantity = existing.quantity + 1.0)
                    withContext(ioDispatcher) { productRepository.insertProduct(updated) }
                } else {
                    val newProduct = ProductEntity(
                        id = System.currentTimeMillis() + Random.nextLong(1000, 9999),
                        cartId = 0L,
                        listId = activeList.id,
                        name = cleanName,
                        quantity = 1.0,
                        price = 0.0,
                    )
                    withContext(ioDispatcher) { productRepository.insertProduct(newProduct) }
                }

                _uiState.update { it.copy(voiceProductAdded = cleanName) }
            } catch (e: Exception) {
                if (isDebug()) e.printStackTrace()
            }
        }
    }

    fun clearVoiceProductAdded() {
        _uiState.update { it.copy(voiceProductAdded = null) }
    }
}
