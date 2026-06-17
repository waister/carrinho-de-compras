package com.renobile.carrinho.util

import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.json.JSONException
import org.json.JSONObject

const val APP_HOST = "https://maggapps.com/"

const val API_ANDROID = "android"
const val API_IDENTIFIER = "identifier"
const val API_VERSION = "version"
const val API_PLATFORM = "platform"
const val API_DEBUG = "debug"

const val API_V = "api_v"
const val API_NOTIFICATIONS = "notifications"
const val API_LIST = "list"
const val API_PREMIUM = "premium"
const val API_FEEDBACK = "feedback"
const val API_COMPARATOR = "comparator"
const val API_ABOUT_APP = "about_app"
const val API_WAKEUP = "wakeup"

fun JSONObject?.getStringVal(tag: String, default: String = ""): String {
    if (this != null && has(tag)) {
        try {
            return getString(tag).getStringValid()
        } catch (e: JSONException) {
            if (isDebug()) e.printStackTrace() else FirebaseCrashlytics.getInstance().recordException(e)
        }
    }
    return default
}

fun String?.getValidJSONObject(): JSONObject? {
    if (!this.isNullOrEmpty() && this != "null") {
        try {
            return JSONObject(this)
        } catch (e: JSONException) {
            if (isDebug()) e.printStackTrace() else FirebaseCrashlytics.getInstance().recordException(e)
        }
    }
    return null
}
