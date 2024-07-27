package com.renobile.carrinho.util

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.orhanobut.hawk.Hawk
import com.renobile.carrinho.application.CustomApplication
import java.util.Date

class AppOpenManager(private var application: CustomApplication) : DefaultLifecycleObserver,
    Application.ActivityLifecycleCallbacks {

    private var appOpenAd: AppOpenAd? = null
    private lateinit var loadCallback: AppOpenAd.AppOpenAdLoadCallback
    private var currentActivity: Activity? = null
    private var isShowingAd: Boolean = false
    private var loadTime: Long = 0

    init {
        this.application.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        showAdIfAvailable()

        super.onStart(owner)
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityDestroyed(activity: Activity) {
        currentActivity = null
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    private fun showAdIfAvailable() {
        if (!isShowingAd && isAdAvailable()) {
            appLog(LOG_TAG, "Will show ad")

            appOpenAd?.show(currentActivity!!)
        } else {
            appLog(LOG_TAG, "Can not show ad")

            fetchAd()
        }
    }

    private fun fetchAd() {
        if (isAdAvailable()) {
            appLog(LOG_TAG, "No ad loaded or expired")

            return
        }

        var adUnitId = Hawk.get(PREF_ADMOB_OPEN_APP_ID, "")

        if (isDebug()) adUnitId = "ca-app-pub-3940256099942544/9257395921"

        if (adUnitId.isEmpty()) {
            appLog(LOG_TAG, "No ad unit id configured")

            return
        }

        loadCallback = object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdLoaded(ad: AppOpenAd) {
                appLog(LOG_TAG, "Ad was loaded")

                appOpenAd = ad
                loadTime = Date().time

                super.onAdLoaded(ad)
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                appLog(LOG_TAG, "Ad failed to load: ${loadAdError.message}")

                appOpenAd = null
                loadTime = 0

                super.onAdFailedToLoad(loadAdError)
            }
        }

        val request: AdRequest = AdRequest.Builder().build()

        appLog(LOG_TAG, "Ad unit id: $adUnitId")

        AppOpenAd.load(application, adUnitId, request, loadCallback)
    }

    private fun isAdAvailable(): Boolean {
        if (!havePlan() && appOpenAd != null) {
            val validateInHours = 4
            val dateDifference = (Date()).time - this.loadTime
            val numMilliSecondsPerHour: Long = 3600000
            return (dateDifference < (numMilliSecondsPerHour * validateInHours))
        }

        return false
    }

    companion object {
        private const val LOG_TAG = "AppOpenManager"
    }

}