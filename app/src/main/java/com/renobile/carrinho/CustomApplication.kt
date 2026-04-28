package com.renobile.carrinho

import androidx.multidex.MultiDexApplication
import com.github.kittinunf.fuel.core.FuelManager
import com.google.android.gms.ads.MobileAds
import com.renobile.carrinho.di.appModules
import com.renobile.carrinho.util.API_ANDROID
import com.renobile.carrinho.util.API_DEBUG
import com.renobile.carrinho.util.API_IDENTIFIER
import com.renobile.carrinho.util.API_PLATFORM
import com.renobile.carrinho.util.API_V
import com.renobile.carrinho.util.API_VERSION
import com.renobile.carrinho.util.APP_HOST
import com.renobile.carrinho.util.AppOpenManager
import com.renobile.carrinho.util.PREF_DEVICE_ID
import com.renobile.carrinho.util.Prefs
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class CustomApplication : MultiDexApplication() {

    companion object {
        lateinit var instance: CustomApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        startKoin {
            androidLogger()
            androidContext(this@CustomApplication)
            modules(appModules)
        }

        Thread {
            MobileAds.initialize(this) {}
        }.start()
        AppOpenManager(this)

        FuelManager.instance.basePath = "${APP_HOST}api/${BuildConfig.API_APP_NAME}"

        updateFuelParams()
    }

    fun updateFuelParams() {
        FuelManager.instance.baseParams = listOf(
            API_IDENTIFIER to Prefs.getValue(PREF_DEVICE_ID, ""),
            API_VERSION to BuildConfig.VERSION_CODE,
            API_PLATFORM to API_ANDROID,
            API_DEBUG to (if (BuildConfig.DEBUG) "1" else "0"),
            API_V to 8
        )
    }

}