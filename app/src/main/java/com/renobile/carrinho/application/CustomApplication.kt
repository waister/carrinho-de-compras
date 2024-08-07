package com.renobile.carrinho.application

import androidx.multidex.MultiDexApplication
import com.github.kittinunf.fuel.core.FuelManager
import com.google.android.gms.ads.MobileAds
import com.orhanobut.hawk.Hawk
import com.renobile.carrinho.BuildConfig
import com.renobile.carrinho.domain.RealmMigration
import com.renobile.carrinho.util.API_ANDROID
import com.renobile.carrinho.util.API_DEBUG
import com.renobile.carrinho.util.API_IDENTIFIER
import com.renobile.carrinho.util.API_IDENTIFIER_OLD
import com.renobile.carrinho.util.API_PLATFORM
import com.renobile.carrinho.util.API_V
import com.renobile.carrinho.util.API_VERSION
import com.renobile.carrinho.util.APP_HOST
import com.renobile.carrinho.util.AppOpenManager
import com.renobile.carrinho.util.PREF_DEVICE_ID
import com.renobile.carrinho.util.PREF_DEVICE_ID_OLD
import io.realm.Realm
import io.realm.RealmConfiguration


class CustomApplication : MultiDexApplication() {

    var isCheckUpdatesNeeded: Boolean = true

    override fun onCreate() {
        super.onCreate()

        Hawk.init(this).build()

        Thread {
            MobileAds.initialize(this) {}
        }.start()
        AppOpenManager(this)

        Realm.init(this)
        Realm.setDefaultConfiguration(
            RealmConfiguration.Builder()
                .allowWritesOnUiThread(true)
                .schemaVersion(2)
                .migration(RealmMigration())
                .build()
        )

        FuelManager.instance.basePath = "${APP_HOST}api/${BuildConfig.API_APP_NAME}"

        updateFuelParams()
    }

    fun updateFuelParams() {
        FuelManager.instance.baseParams = listOf(
            API_IDENTIFIER to Hawk.get(PREF_DEVICE_ID, ""),
            API_IDENTIFIER_OLD to Hawk.get(PREF_DEVICE_ID_OLD, ""),
            API_VERSION to BuildConfig.VERSION_CODE,
            API_PLATFORM to API_ANDROID,
            API_DEBUG to (if (BuildConfig.DEBUG) "1" else "0"),
            API_V to 8
        )
    }

}
