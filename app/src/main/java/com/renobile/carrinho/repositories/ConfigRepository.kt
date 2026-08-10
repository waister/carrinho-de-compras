package com.renobile.carrinho.repositories

import com.renobile.carrinho.network.ConfigApiService
import com.renobile.carrinho.network.ConfigResponse
import com.renobile.carrinho.util.PREF_ADMOB_AD_MAIN_ID
import com.renobile.carrinho.util.PREF_ADMOB_ID
import com.renobile.carrinho.util.PREF_ADMOB_INTERSTITIAL_ID
import com.renobile.carrinho.util.PREF_ADMOB_OPEN_APP_ID
import com.renobile.carrinho.util.PREF_ADMOB_REMOVE_ADS_ID
import com.renobile.carrinho.util.PREF_APP_NAME
import com.renobile.carrinho.util.PREF_PLAN_VIDEO_DURATION
import com.renobile.carrinho.util.PREF_SHARE_LINK
import com.renobile.carrinho.util.Prefs
import com.renobile.carrinho.util.appLog

interface ConfigRepository {
    suspend fun identify(token: String): Result<ConfigResponse>
    fun saveConfig(response: ConfigResponse)
}

class ConfigRepositoryImpl(private val apiService: ConfigApiService) : ConfigRepository {
    override suspend fun identify(token: String): Result<ConfigResponse> {
        return try {
            val response = apiService.identify(token)
            Result.success(response)
        } catch (e: Exception) {
            appLog("ConfigRepository", "Error identify: ${e.message}")
            Result.failure(e)
        }
    }

    override fun saveConfig(response: ConfigResponse) {
        if (response.success) {
            response.configs?.let { configs ->
                Prefs.putValue(PREF_SHARE_LINK, configs.storeLink ?: "")
                Prefs.putValue(PREF_APP_NAME, configs.appName ?: "")
                Prefs.putValue(PREF_ADMOB_ID, configs.admobId ?: "")
                Prefs.putValue(PREF_ADMOB_AD_MAIN_ID, configs.admobAdMainId ?: "")
                Prefs.putValue(PREF_ADMOB_INTERSTITIAL_ID, configs.admobInterstitialId ?: "")
                Prefs.putValue(PREF_ADMOB_REMOVE_ADS_ID, configs.admobRemoveAdsId ?: "")
                Prefs.putValue(PREF_ADMOB_OPEN_APP_ID, configs.admobOpenAppId ?: "")
                Prefs.putValue(PREF_PLAN_VIDEO_DURATION, configs.planVideoDuration ?: 0L)
            }
        }
    }
}
