package com.renobile.carrinho.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.renobile.carrinho.CustomApplication
import org.json.JSONObject

const val PREF_DEVICE_ID = "PrefDeviceId"
const val PREF_FCM_TOKEN = "PrefFcmToken"
const val PREF_SHARE_LINK = "PrefStoreLink"
const val PREF_HAVE_PLAN = "PrefHavePlan"
const val PREF_APP_NAME = "PrefAppName"
const val PREF_NOTIFICATION_JSON = "PrefNotificationJson"
const val PREF_PRICE_FIRST = "PrefPriceFirst"
const val PREF_SIZE_FIRST = "PrefSizeFirst"
const val PREF_PRICE_SECOND = "PrefPriceSecond"
const val PREF_SIZE_SECOND = "PrefSizeSecond"
const val PREF_PLAN_VIDEO_DURATION = "PrefPlanVideoDuration"
const val PREF_PLAN_VIDEO_MILLIS = "PrefPlanVideoMillis"

const val PREF_ADMOB_ID = "PrefAdMobId"
const val PREF_ADMOB_AD_MAIN_ID = "PrefAdMobAdMainId"
const val PREF_ADMOB_INTERSTITIAL_ID = "PrefAdMobInterstitialId"
const val PREF_ADMOB_REMOVE_ADS_ID = "PrefAdMobRemoveAds"
const val PREF_ADMOB_OPEN_APP_ID = "PrefAdMobOpenAppId"
const val PREF_PUSH_NOTIFICATION = "PrefPushNotification"

@Suppress("unused")
object Prefs {
    private const val PREFS_NAME = "carrinho_prefs"

    private val preferences: SharedPreferences by lazy {
        CustomApplication.instance.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun <T> putValue(key: String, value: T) {
        preferences.edit {
            when (value) {
                is String -> putString(key, value)
                is Int -> putInt(key, value)
                is Boolean -> putBoolean(key, value)
                is Long -> putLong(key, value)
                is Float -> putFloat(key, value)
                is JSONObject -> putString(key, value.toString())
                else -> throw IllegalArgumentException("Unsupported preference type")
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getValue(key: String, defaultValue: T): T {
        return when (defaultValue) {
            is String -> preferences.getString(key, defaultValue) as T
            is Int -> preferences.getInt(key, defaultValue) as T
            is Boolean -> preferences.getBoolean(key, defaultValue) as T
            is Long -> preferences.getLong(key, defaultValue) as T
            is Float -> preferences.getFloat(key, defaultValue) as T
            else -> throw IllegalArgumentException("Unsupported preference type")
        }
    }

    fun removeKey(key: String) {
        preferences.edit { remove(key) }
    }

    fun clear() {
        preferences.edit { clear() }
    }

    fun getJSONObject(key: String): JSONObject? {
        val jsonString = preferences.getString(key, null)
        return jsonString?.let { JSONObject(it) }
    }
}
