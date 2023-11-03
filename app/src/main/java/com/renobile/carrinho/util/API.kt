package com.renobile.carrinho.util

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

const val APP_HOST = "https://maggapps.com/"

const val API_ROUTE_IDENTIFY = "/identify"
const val API_ROUTE_NOTIFICATIONS = "/notifications"
const val API_ROUTE_NOTIFICATION = "/notification/"

const val API_SUCCESS = "success"
const val API_VERSION_LAST = "version_last"
const val API_VERSION_MIN = "version_min"
const val API_SHARE_LINK = "store_link"
const val API_MESSAGE = "message"
const val API_ID = "id"
const val API_ANDROID = "android"
const val API_IDENTIFIER = "identifier"
const val API_VERSION = "version"
const val API_PLATFORM = "platform"
const val API_DEBUG = "debug"
const val API_TITLE = "title"
const val API_LINK = "link"
const val API_BODY = "body"
const val API_DATE = "date"
const val API_IMAGE = "image"
const val API_TOKEN = "token"
const val API_V = "api_v"
const val API_NOTIFICATIONS = "notifications"
const val API_NOTIFICATION = "notification"
const val API_LIST = "list"
const val API_PREMIUM = "premium"
const val API_FEEDBACK = "feedback"
const val API_COMPARATOR = "comparator"
const val API_ABOUT_APP = "about_app"
const val API_WAKEUP = "wakeup"
const val API_APP_NAME = "app_name"
const val API_PLAN_VIDEO_DURATION = "plan_video_duration"
const val API_BILL_PLAN_YEAR = "bill_plan_year"

const val API_ADMOB_ID = "admob_id"
const val API_ADMOB_AD_MAIN_ID = "admob_ad_main_id"
const val API_ADMOB_INTERSTITIAL_ID = "admob_interstitial_id"
const val API_ADMOB_REMOVE_ADS_ID = "admob_remove_ads_id"
const val API_ADMOB_OPEN_APP_ID = "admob_open_app_id"


fun JSONObject?.getStringVal(tag: String, default: String = ""): String {
    if (this != null && has(tag)) {
        try {
            return getString(tag).getStringValid()
        } catch (_: JSONException) {
        }
    }
    return default
}

fun JSONObject?.getIntVal(tag: String, default: Int = 0): Int {
    if (this != null && has(tag)) {
        try {
            return getInt(tag)
        } catch (_: JSONException) {
        }
    }
    return default
}

fun JSONObject?.getLongVal(tag: String, default: Long = 0): Long {
    if (this != null && has(tag)) {
        try {
            return getLong(tag)
        } catch (_: JSONException) {
        }
    }
    return default
}

fun JSONObject?.getBooleanVal(tag: String, default: Boolean = false): Boolean {
    if (this != null && has(tag)) {
        try {
            return getBoolean(tag)
        } catch (_: JSONException) {
        }
    }
    return default
}

fun String?.getValidJSONObject(): JSONObject? {
    if (!this.isNullOrEmpty() && this != "null") {
        try {
            return JSONObject(this)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
    return null
}

fun JSONObject?.getJSONObjectVal(tag: String): JSONObject? {
    if (this != null && this.has(tag) && !this.isNull(tag)) {
        try {
            return this.getJSONObject(tag)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
    return null
}

fun JSONObject?.getJSONArrayVal(tag: String): JSONArray? {
    if (this != null && this.has(tag) && !this.isNull(tag)) {
        try {
            return this.getJSONArray(tag)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
    return null
}