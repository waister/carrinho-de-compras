package com.renobile.carrinho.features.start

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renobile.carrinho.network.ConfigApiService
import com.renobile.carrinho.util.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

class StartViewModel(
    private val configApiService: ConfigApiService
) : ViewModel() {

    private val _events = Channel<StartEvents>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun start() {
        createDeviceID()
        if (Prefs.getValue(PREF_ADMOB_ID, "").isEmpty()) {
            identifyApp()
        } else {
            _events.trySend(StartEvents.NavigateToMain)
        }
    }

    private fun identifyApp() {
        viewModelScope.launch {
            try {
                val token = Prefs.getValue(PREF_FCM_TOKEN, "")
                val response = configApiService.identify(token)
                if (response.success && response.configs != null) {
                    val configs = response.configs
                    Prefs.putValue(PREF_SHARE_LINK, configs.storeLink ?: "")
                    Prefs.putValue(PREF_APP_NAME, configs.appName ?: "")
                    Prefs.putValue(PREF_ADMOB_ID, configs.admobId ?: "")
                    Prefs.putValue(PREF_ADMOB_AD_MAIN_ID, configs.admobAdMainId ?: "")
                    Prefs.putValue(PREF_ADMOB_INTERSTITIAL_ID, configs.admobInterstitialId ?: "")
                    Prefs.putValue(PREF_ADMOB_REMOVE_ADS_ID, configs.admobRemoveAdsId ?: "")
                    Prefs.putValue(PREF_ADMOB_OPEN_APP_ID, configs.admobOpenAppId ?: "")
                    Prefs.putValue(PREF_PLAN_VIDEO_DURATION, configs.planVideoDuration ?: FIVE_DAYS)
                }
            } catch (e: Exception) {
                appLog("StartViewModel", "identifyApp error: ${e.message}")
            } finally {
                _events.send(StartEvents.NavigateToMain)
            }
        }
    }

    private fun createDeviceID() {
        val currentDeviceID = Prefs.getValue(PREF_DEVICE_ID, "")
        val isIdentifierV3 = currentDeviceID.contains(StartFragment.IDENTIFIER_VERSION)

        if (currentDeviceID.isEmpty() || !isIdentifierV3) {
            val newDeviceId = generateDeviceIdentifier()
            Prefs.putValue(PREF_DEVICE_ID, newDeviceId)
            // Note: CustomApplication().updateFuelParams() might still be needed if Fuel is used elsewhere
            appLog("GENERATE_DEVICE_ID", "New device ID: $newDeviceId")
        }
    }

    private fun generateDeviceIdentifier(): String {
        val deviceID = try {
            val uniqueDevicePseudoID =
                "35" + Build.BOARD.length % 10 + Build.BRAND.length % 10 + Build.DEVICE.length % 10 +
                        Build.DISPLAY.length % 10 + Build.HOST.length % 10 + Build.ID.length % 10 +
                        Build.MANUFACTURER.length % 10 + Build.MODEL.length % 10 + Build.PRODUCT.length % 10 +
                        Build.TAGS.length % 10 + Build.TYPE.length % 10 + Build.USER.length % 10
            val serial = Build.getRadioVersion() ?: "serial"
            UUID(uniqueDevicePseudoID.hashCode().toLong(), serial.hashCode().toLong()).toString()
        } catch (e: Exception) {
            UUID(System.currentTimeMillis(), Random.Default.nextLong()).toString()
        }
        return "$deviceID${StartFragment.IDENTIFIER_VERSION}"
    }
}

sealed interface StartEvents {
    data object NavigateToMain : StartEvents
}
