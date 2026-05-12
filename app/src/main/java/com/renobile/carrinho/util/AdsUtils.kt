package com.renobile.carrinho.util

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import java.util.UUID

fun haveVideoPlan(): Boolean {
    val planVideoMillis = Prefs.getValue(PREF_PLAN_VIDEO_MILLIS, 0L)
    if (planVideoMillis != 0L) {
        val panVideoDuration = Prefs.getValue(PREF_PLAN_VIDEO_DURATION, FIVE_DAYS)
        val expiration = Prefs.getValue(PREF_PLAN_VIDEO_MILLIS, 0L) + panVideoDuration
        return expiration > System.currentTimeMillis()
    }
    return false
}

fun haveBillingPlan(): Boolean = Prefs.getValue(PREF_HAVE_PLAN, false)

fun havePlan(): Boolean = haveBillingPlan() || haveVideoPlan()

fun Context?.loadBannerAd(
    adViewContainer: LinearLayout?,
    adUnitId: String,
    adSize: AdSize? = null,
    collapsible: Boolean = false,
    shimmer: ShimmerFrameLayout? = null,
) {
    val logTag = "LOAD_ADMOB_BANNER"

    if (this == null || adUnitId.isEmpty() || adViewContainer == null || havePlan()) {
        shimmer?.hide()
        appLog(logTag, "loadAdMobBanner() falied | $this | $adUnitId | ${havePlan()}")
        return
    }

    shimmer?.show()

    appLog(logTag, "adUnitId: $adUnitId")

    val adView = AdView(this)
    adViewContainer.addView(adView)

    adView.adUnitId = if (isDebug()) "ca-app-pub-3940256099942544/6300978111" else adUnitId

    adView.setAdSize(adSize ?: getAdSize(adViewContainer))

    val extras = Bundle()
    if (collapsible) {
        extras.putString("collapsible", "bottom")
        extras.putString("collapsible_request_id", UUID.randomUUID().toString())
    }

    val adRequest = AdRequest.Builder()
        .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
        .build()

    adView.loadAd(adRequest)

    adView.adListener = object : AdListener() {
        override fun onAdLoaded() {
            super.onAdLoaded()
            shimmer?.hide()
            appLog(logTag, "onAdLoaded()")
        }

        override fun onAdFailedToLoad(error: LoadAdError) {
            super.onAdFailedToLoad(error)
            shimmer?.hide()
            appLog(logTag, "onAdFailedToLoad(): ${error.message}")
        }

        override fun onAdOpened() {
            super.onAdOpened()
            appLog(logTag, "onAdOpened()")
        }

        override fun onAdClosed() {
            super.onAdClosed()
            appLog(logTag, "onAdClosed()")
        }
    }

    appLog(logTag, "ENDS")
}

fun Context.getAdSize(adViewContainer: LinearLayout): AdSize {
    var adWidthPixels = adViewContainer.width.toFloat()
    if (adWidthPixels == 0f)
        adWidthPixels = displayWidth().toFloat()

    val density = resources.displayMetrics.density
    val adWidth = (adWidthPixels / density).toInt()
    return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
}

fun Context?.displayWidth() = if (this != null) resources.displayMetrics.widthPixels else 0


fun View.hide() {
    visibility = View.GONE
}

fun View.show() {
    visibility = View.VISIBLE
}
