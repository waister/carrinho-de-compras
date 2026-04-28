package com.renobile.carrinho

import androidx.lifecycle.ViewModel
import com.github.kittinunf.fuel.httpGet
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.renobile.carrinho.util.API_ROUTE_IDENTIFY
import com.renobile.carrinho.util.API_SUCCESS
import com.renobile.carrinho.util.API_TOKEN
import com.renobile.carrinho.util.API_VERSION_LAST
import com.renobile.carrinho.util.API_VERSION_MIN
import com.renobile.carrinho.util.PREF_FCM_TOKEN
import com.renobile.carrinho.util.Prefs
import com.renobile.carrinho.util.getBooleanVal
import com.renobile.carrinho.util.getIntVal
import com.renobile.carrinho.util.getValidJSONObject
import com.renobile.carrinho.util.isDebug
import com.renobile.carrinho.util.printFuelLog
import com.renobile.carrinho.util.saveAppData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MainState())
    val uiState: StateFlow<MainState> = _uiState.asStateFlow()

    fun checkVersion() {
        val token = Prefs.getValue(PREF_FCM_TOKEN, "")

        if (token.isNotEmpty()) {
            val params = listOf(API_TOKEN to token)

            API_ROUTE_IDENTIFY.httpGet(params).responseString { request, response, result ->
                printFuelLog(request, response, result)

                val (data, error) = result

                if (error == null) {
                    val apiObj = data.getValidJSONObject()

                    saveAppData(result)

                    if (apiObj.getBooleanVal(API_SUCCESS)) {
                        val versionLast = apiObj.getIntVal(API_VERSION_LAST)
                        val versionMin = apiObj.getIntVal(API_VERSION_MIN)

                        if (BuildConfig.VERSION_CODE < versionMin) {
                            _uiState.update { it.copy(versionUpdate = VersionUpdate.Needed) }
                        } else if (BuildConfig.VERSION_CODE < versionLast) {
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
