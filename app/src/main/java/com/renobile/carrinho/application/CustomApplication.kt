package com.renobile.carrinho.application

import android.app.Application
import com.github.kittinunf.fuel.core.FuelManager
import com.orhanobut.hawk.Hawk
import com.renobile.carrinho.BuildConfig
import com.renobile.carrinho.util.*
import io.realm.Realm
import io.realm.RealmConfiguration

class CustomApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Hawk.init(this).build()

        Realm.init(this)
        Realm.setDefaultConfiguration(RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build())

        FuelManager.instance.basePath = "${APP_HOST}api/${BuildConfig.API_APP_NAME}"

        updateFuelParams()
    }

    fun updateFuelParams() {
        FuelManager.instance.baseParams = listOf(
                API_IDENTIFIER to Hawk.get(PREF_DEVICE_ID, ""),
                API_VERSION to BuildConfig.VERSION_CODE,
                API_PLATFORM to API_ANDROID,
                API_DEBUG to (if (BuildConfig.DEBUG) "1" else "0"),
                API_V to 5
        )
    }

}
