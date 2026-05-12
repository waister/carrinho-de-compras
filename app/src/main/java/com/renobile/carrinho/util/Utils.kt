package com.renobile.carrinho.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.text.Html
import android.text.Spanned
import android.util.Log
import android.view.View
import android.webkit.URLUtil
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.createBitmap
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.renobile.carrinho.BuildConfig
import com.renobile.carrinho.MainActivity
import com.renobile.carrinho.R
import com.renobile.carrinho.database.entities.ProductEntity
import java.text.DateFormat
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

fun Fragment.findNavControllerSafely(): NavController? {
    return if (isAdded) {
        findNavController()
    } else {
        null
    }
}

fun storeAppLink(): String =
    "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"

fun Activity?.sendCart(products: List<ProductEntity>? = null, cartName: String) {
    if (this == null) return

    if (!products.isNullOrEmpty()) {
        var text = getString(R.string.label_my_cart)

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
        toast(R.string.error_empty_cart)
    }
}

fun Activity?.sendList(products: List<ProductEntity>? = null, listName: String) {
    if (this == null) return

    if (!products.isNullOrEmpty()) {
        var text = getString(R.string.label_my_list)

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

        share(text, getString(R.string.send_list_label, listName))
    } else {
        toast(R.string.error_empty_list)
    }
}

fun Activity.share(text: String, subject: String = "") {
    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "text/plain"
    intent.putExtra(Intent.EXTRA_SUBJECT, subject)
    intent.putExtra(Intent.EXTRA_TEXT, text)
    startActivity(Intent.createChooser(intent, null))
}

fun Context.share(text: String, subject: String = "") {
    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "text/plain"
    intent.putExtra(Intent.EXTRA_SUBJECT, subject)
    intent.putExtra(Intent.EXTRA_TEXT, text)
    startActivity(Intent.createChooser(intent, null))
}

fun Context.toast(message: Int) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun String?.getStringValid(): String {
    if (!this.isNullOrEmpty() && this != "null" && this != "[null]") {
        return this
    }
    return ""
}

fun Context?.shareApp() {
    if (this == null) return

    val app = Prefs.getValue(PREF_APP_NAME, "")
    val default = "${APP_HOST}app/link/${BuildConfig.API_APP_NAME}"
    val link = Prefs.getValue(PREF_SHARE_LINK, default)

    share(getString(R.string.share_text, link), getString(R.string.share_subject, app))
}

fun Long.formatDate(): String = DateFormat.getDateInstance(DateFormat.SHORT).format(this)

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
        if (isDebug()) e.printStackTrace() else FirebaseCrashlytics.getInstance().recordException(e)
    }
    return ""
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
            output = createBitmap(bitmap.width, bitmap.height)
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
            if (isDebug()) e.printStackTrace() else FirebaseCrashlytics.getInstance().recordException(e)
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
        val textView = snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
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

fun appLog(tag: String, msg: String) {
    if (BuildConfig.DEBUG)
        Log.i("MAGGAPPS_LOG", "➡➡➡ $tag: $msg")
}


fun isDebug() = BuildConfig.DEBUG

fun createCartListNameGeneric(): String {
    val currentMillis = System.currentTimeMillis()
    val date = SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(currentMillis)
    return "Compras $date"
}

fun Double.isSingular() = this > 0.0 && this < 2.00

fun Double.addPluralCharacter() = if (this.isSingular()) "" else "s"

fun Int.addPluralCharacter() = if (this == 1) "" else "s"

fun Double.isEmpty() = this == 0.0

fun Activity.restartApp() {
    val intent = Intent(this, MainActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    startActivity(intent)

    finish()
}