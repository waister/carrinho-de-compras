package com.renobile.carrinho.features.removeads

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.renobile.carrinho.R
import com.renobile.carrinho.util.ONE_DAY
import com.renobile.carrinho.util.PREF_ADMOB_REMOVE_ADS_ID
import com.renobile.carrinho.util.PREF_PLAN_VIDEO_DURATION
import com.renobile.carrinho.util.PREF_PLAN_VIDEO_MILLIS
import com.renobile.carrinho.util.Prefs
import com.renobile.carrinho.util.appLog
import com.renobile.carrinho.util.havePlan
import com.renobile.carrinho.util.haveVideoPlan
import com.renobile.carrinho.util.isDebug
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RemoveAdsViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(RemoveAdsState())
    val uiState: StateFlow<RemoveAdsState> = _uiState.asStateFlow()

    private val _events = Channel<RemoveAdsEvents>()
    val events = _events.receiveAsFlow()

    private var rewardedAd: RewardedAd? = null

    init {
        val panVideoDuration = Prefs.getValue(PREF_PLAN_VIDEO_DURATION, ONE_DAY)
        val panVideoDurationDays = panVideoDuration / ONE_DAY
        _uiState.update {
            it.copy(
                haveVideoPlan = haveVideoPlan(),
                description = getApplication<Application>().getString(R.string.watch_to_by_body, panVideoDurationDays)
            )
        }
    }

    fun loadAd() {
        if (havePlan()) return

        val adUnitId = if (isDebug()) {
            "ca-app-pub-3940256099942544/5224354917"
        } else {
            Prefs.getValue(PREF_ADMOB_REMOVE_ADS_ID, "")
        }

        if (adUnitId.isEmpty()) return

        _uiState.update { it.copy(isLoading = true) }

        MobileAds.initialize(getApplication()) {
            val deviceId = listOf(AdRequest.DEVICE_ID_EMULATOR)
            val configuration = RequestConfiguration.Builder().setTestDeviceIds(deviceId).build()
            MobileAds.setRequestConfiguration(configuration)

            val request = AdRequest.Builder().build()
            RewardedAd.load(
                getApplication(), adUnitId, request, object : RewardedAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        rewardedAd = null
                        _uiState.update { it.copy(isLoading = false, isAdReady = false) }
                        viewModelScope.launch { _events.send(RemoveAdsEvents.ShowError(R.string.error_load_video)) }
                    }

                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                        _uiState.update { it.copy(isLoading = false, isAdReady = true) }

                        rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                if (haveVideoPlan()) {
                                    viewModelScope.launch { _events.send(RemoveAdsEvents.ShowRestartAlert) }
                                }
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                viewModelScope.launch { _events.send(RemoveAdsEvents.ShowError(R.string.error_load_video)) }
                            }

                            override fun onAdShowedFullScreenContent() {
                                rewardedAd = null
                                _uiState.update { it.copy(isAdReady = false) }
                            }
                        }
                    }
                })
        }
    }

    fun showAd(activity: Activity) {
        rewardedAd?.show(activity) { rewardItem ->
            appLog("RemoveAdsViewModel", "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
            Prefs.putValue(PREF_PLAN_VIDEO_MILLIS, System.currentTimeMillis())
            _uiState.update { it.copy(haveVideoPlan = true) }
        }
    }
}
