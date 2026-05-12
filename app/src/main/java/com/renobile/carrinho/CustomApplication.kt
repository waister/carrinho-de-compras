package com.renobile.carrinho

import androidx.multidex.MultiDexApplication
import com.google.android.gms.ads.MobileAds
import com.renobile.carrinho.di.appModules
import com.renobile.carrinho.util.AppOpenManager
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
    }

}
