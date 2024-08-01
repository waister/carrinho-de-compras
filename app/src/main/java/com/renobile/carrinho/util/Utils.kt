package com.renobile.carrinho.util

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.URLUtil
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.Fragment
import com.facebook.shimmer.ShimmerFrameLayout
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.result.Result
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.material.snackbar.Snackbar
import com.orhanobut.hawk.Hawk
import com.renobile.carrinho.BuildConfig
import com.renobile.carrinho.R
import com.renobile.carrinho.domain.Product
import io.realm.RealmResults
import org.jetbrains.anko.find
import org.jetbrains.anko.share
import org.jetbrains.anko.toast
import java.text.DateFormat
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

fun AppCompatEditText.maskMoney() {
    this.addTextChangedListener(MaskMoney(this))
}

fun storeAppLink(): String =
    "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"

fun Activity?.sendCart(products: RealmResults<Product>? = null, cartName: String) {
    if (this == null) return

    if (products != null && products.size > 0) {
        var text = ""

        var volumes = 0.0
        var total = 0.0

        products.forEach {
            val price = it.price * it.quantity

            volumes += it.quantity
            total += price

            text += "${it.quantity} - ${it.name} - ${price.formatPrice()}\n"
        }

        text += getString(
            R.string.share_cart_text,
            products.size.addPluralCharacter(),
            products.size,
            volumes.addPluralCharacter(),
            volumes.formatQuantity(),
            total.formatPrice(),
            storeAppLink()
        )

        share(text, getString(R.string.send_list_label, cartName))
    } else {
        toast(R.string.error_empty)
    }
}

fun Activity?.sendList(products: RealmResults<Product>? = null, cartName: String) {
    if (this == null) return

    if (products != null && products.size > 0) {
        var text = ""

        var volumes = 0.0
        var total = 0.0

        products.forEach {
            val price = it.price * it.quantity

            volumes += it.quantity
            total += price

            text += "${it.quantity} - ${it.name} - ${price.formatPrice()}\n"
        }

        text += getString(
            R.string.share_cart_text,
            products.size.addPluralCharacter(),
            products.size,
            volumes.addPluralCharacter(),
            volumes.formatQuantity(),
            total.formatPrice(),
            storeAppLink()
        )

        share(text, getString(R.string.send_list_label, cartName))
    } else {
        toast(R.string.error_empty)
    }
}

fun String?.getStringValid(): String {
    if (!this.isNullOrEmpty() && this != "null" && this != "[null]") {
        return this
    }
    return ""
}

fun printFuelLog(request: Request, response: Response, result: Result<String, FuelError>) {
    Log.w("FUEL_API_CALL", "API was called to route: ${request.url}")

    if (BuildConfig.DEBUG) {
        val url = request.url

        println("\n--- FUEL_REQUEST_START - $url\n")
        println(request)
        println("\n--- FUEL_REQUEST_END - $url\n")

        println("\n--- FUEL_RESPONSE_START - $url\n")
        println(response)
        println("\n--- FUEL_RESPONSE_END - $url\n")

        println("\n--- FUEL_RESULT_START - $url\n")
        println(result)
        println("\n--- FUEL_RESULT_END - $url\n")
    }
}

fun haveVideoPlan(): Boolean {
    val planVideoMillis = Hawk.get(PREF_PLAN_VIDEO_MILLIS, 0L)
    if (planVideoMillis != 0L) {
        val panVideoDuration = Hawk.get(PREF_PLAN_VIDEO_DURATION, FIVE_DAYS)
        val expiration = Hawk.get(PREF_PLAN_VIDEO_MILLIS, 0L) + panVideoDuration
        return expiration > System.currentTimeMillis()
    }
    return false
}

fun haveBillingPlan(): Boolean = Hawk.get(PREF_HAVE_PLAN, !BuildConfig.DEBUG)

fun havePlan(): Boolean = haveBillingPlan() || haveVideoPlan()

fun Context?.loadBannerAd(
    adViewContainer: LinearLayout?,
    adUnitId: String,
    adSize: AdSize? = null,
    collapsible: Boolean = false,
    shimmer: ShimmerFrameLayout? = null
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

fun Context?.shareApp() {
    if (this == null) return

    val app = Hawk.get(PREF_APP_NAME, "")
    val default = "${APP_HOST}app/link/${BuildConfig.API_APP_NAME}"
    val link = Hawk.get(PREF_SHARE_LINK, default)

    share(getString(R.string.share_text, link), getString(R.string.share_subject, app))
}

fun Long.formatDate(): String = DateFormat.getDateInstance(DateFormat.SHORT).format(this)

fun String?.formatDate(): String {
    try {
        if (!this.isNullOrEmpty()) {
            val locale = Locale.getDefault()
            val parsed = SimpleDateFormat(FORMAT_DATETIME_API, locale).parse(this)

            if (parsed != null)
                DateFormat.getDateInstance(DateFormat.SHORT).format(parsed.time)
        }
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return ""
}

fun String?.formatDatetime(): String {
    try {
        if (!this.isNullOrEmpty()) {
            val locale = Locale.getDefault()
            val parsed = SimpleDateFormat(FORMAT_DATETIME_API, locale).parse(this)

            if (parsed != null)
                return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                    .format(parsed.time)
        }
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return ""
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun String?.isValidUrl(): Boolean = !this.isNullOrEmpty() && URLUtil.isValidUrl(this)

fun String?.stringToInt(): Int {
    if (this != null && this != "null") {
        val number = this.replace("\\D".toRegex(), "")
        if (number.isNotEmpty())
            return number.toInt()
    }
    return 0
}

fun String?.getApiImage(): String {
    if (this != null) {
        if (!contains("http") && contains("/uploads/")) {
            val path = APP_HOST.removeSuffix("/") + this

            if (path.isValidUrl()) {
                return path
            }
        }

        return this
    }

    return ""
}

fun Context?.getThumbUrl(image: String?, width: Int = 220, height: Int = 0, quality: Int = 85): String {
    if (this != null && image != null && !image.contains("http") && image.contains("/uploads/")) {
        return APP_HOST + "thumb?src=$image&w=$width&h=$height&q=$quality"
    }

    return image.getApiImage()
}

fun Bitmap?.getCircleCroppedBitmap(): Bitmap? {
    var output: Bitmap? = null
    val bitmap = this

    if (bitmap != null) {
        try {
            output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)

            val color = -0xbdbdbe
            val paint = Paint()
            val rect = Rect(0, 0, bitmap.width, bitmap.height)

            paint.isAntiAlias = true
            canvas.drawARGB(0, 0, 0, 0)
            paint.color = color
            if (bitmap.width < bitmap.height) {
                canvas.drawCircle(
                    (bitmap.width / 2).toFloat(), (bitmap.height / 2).toFloat(),
                    (bitmap.width / 2).toFloat(), paint
                )
            } else {
                canvas.drawCircle(
                    (bitmap.width / 2).toFloat(), (bitmap.height / 2).toFloat(),
                    (bitmap.height / 2).toFloat(), paint
                )
            }
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(bitmap, rect, rect, paint)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    return output
}

fun View?.longSnackbar(resourceId: Int) {
    this?.longSnackbar(this.context!!.getString(resourceId))
}

fun View?.longSnackbar(text: String) {
    this?.customSnackbar(text, Snackbar.LENGTH_LONG)
}

fun View?.customSnackbar(text: String, length: Int) {
    if (this != null) {
        val snackbar = Snackbar.make(this, text, length)
        val textView = snackbar.view.find<TextView>(R.id.snackbar_text)
        textView.setTextColor(Color.WHITE)
        snackbar.show()
    }
}

fun String.fromHtml(): Spanned {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
    } else {
        @Suppress("DEPRECATION")
        Html.fromHtml(this)
    }
}

fun Double.formatPercent(): String {
    val percentFormat = NumberFormat.getPercentInstance()
    percentFormat.maximumFractionDigits = 2
    return percentFormat.format(this)
}

fun Double.formatPrice(): String = NumberFormat.getCurrencyInstance().format(this)
fun Double.formatQuantity(): String = NumberFormat.getNumberInstance().format(this)

fun EditText?.getNumber(): Double {
    if (this == null) return 0.0
    val value = this.text.toString()
    return if (value.isEmpty()) 0.0 else java.lang.Double.parseDouble(value)
}

fun EditText?.getDouble(): Double {
    if (this == null) return 0.0

    var value = this.text.toString()
        .replace(Regex("[^0-9,.]"), "")
        .replace(",", ".")

    if (value.isEmpty())
        value = "0"

    return value.toDouble()
}

fun Activity?.hideKeyboard() {
    val view = this?.currentFocus
    val inputManager = this?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    if (view != null)
        inputManager.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
}

fun saveAppData(result: Result<String, FuelError>) {
    val (data, error) = result

    if (error == null) {
        val apiObj = data.getValidJSONObject()

        if (apiObj.getBooleanVal(API_SUCCESS)) {
            Hawk.put(PREF_SHARE_LINK, apiObj.getStringVal(API_SHARE_LINK))
            Hawk.put(PREF_APP_NAME, apiObj.getStringVal(API_APP_NAME))
            Hawk.put(PREF_ADMOB_ID, apiObj.getStringVal(API_ADMOB_ID))
            Hawk.put(PREF_ADMOB_AD_MAIN_ID, apiObj.getStringVal(API_ADMOB_AD_MAIN_ID))
            Hawk.put(PREF_ADMOB_INTERSTITIAL_ID, apiObj.getStringVal(API_ADMOB_INTERSTITIAL_ID))
            Hawk.put(PREF_ADMOB_REMOVE_ADS_ID, apiObj.getStringVal(API_ADMOB_REMOVE_ADS_ID))
            Hawk.put(PREF_ADMOB_OPEN_APP_ID, apiObj.getStringVal(API_ADMOB_OPEN_APP_ID))
            Hawk.put(PREF_BILL_PLAN_YEAR, apiObj.getStringVal(API_BILL_PLAN_YEAR))
            Hawk.put(PREF_PLAN_VIDEO_DURATION, apiObj.getLongVal(API_PLAN_VIDEO_DURATION))
        }
    }
}

fun appLog(tag: String, msg: String) {
    if (BuildConfig.DEBUG)
        Log.i("MAGGAPPS_LOG", "➡➡➡ $tag: $msg")
}

fun View.hide() {
    visibility = View.GONE
}

fun View.show() {
    visibility = View.VISIBLE
}

fun View.isVisible(isVisible: Boolean) {
    if (isVisible) show() else hide()
}

fun String?.isNumeric(): Boolean {
    if (this == null) return false
    val regex = "-?[0-9]+(\\.[0-9]+)?".toRegex()
    return this.matches(regex)
}

fun String?.isNotNumeric(): Boolean = !this.isNumeric()

fun isDebug() = BuildConfig.DEBUG

fun AutoCompleteTextView.setEmpty() = this.text?.clear()

fun AppCompatEditText.setEmpty() = this.text?.clear()

fun TextView.setEmpty() = this.setText(R.string.empty)

fun Fragment.createCartListName(): String {
    val currentMillis = System.currentTimeMillis()

    val dateFormatDay: DateFormat = SimpleDateFormat("dd", Locale.getDefault())
    val day = dateFormatDay.format(currentMillis)

    val dateFormatMonth: DateFormat = SimpleDateFormat("MM", Locale.getDefault())
    val month = dateFormatMonth.format(currentMillis)

    return getString(R.string.cart_name_default, day, month)
}

fun Double.isSingular() = this > 0.0 && this < 2.00

fun Double.addPluralCharacter() = if (this.isSingular()) "" else "s"

fun Int.addPluralCharacter() = if (this == 1) "" else "s"

fun Double.isEmpty() = this == 0.0
