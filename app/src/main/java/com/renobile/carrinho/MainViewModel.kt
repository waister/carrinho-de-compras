package com.renobile.carrinho

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.renobile.carrinho.repositories.ConfigRepository
import com.renobile.carrinho.util.PREF_FCM_TOKEN
import com.renobile.carrinho.util.Prefs
import com.renobile.carrinho.util.isDebug
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(private val configRepository: ConfigRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(MainState())
    val uiState: StateFlow<MainState> = _uiState.asStateFlow()

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
        _uiState.update { it.copy(isBottomBarVisible = visible) }
    }

    fun onVersionUpdateHandled() {
        _uiState.update { it.copy(versionUpdate = null) }
    }
}
