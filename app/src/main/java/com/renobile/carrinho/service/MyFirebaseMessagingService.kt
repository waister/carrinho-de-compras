package com.renobile.carrinho.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.github.kittinunf.fuel.httpGet
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.renobile.carrinho.BuildConfig
import com.renobile.carrinho.R
import com.renobile.carrinho.activity.StartActivity
import com.renobile.carrinho.util.API_ABOUT_APP
import com.renobile.carrinho.util.API_FEEDBACK
import com.renobile.carrinho.util.API_NOTIFICATIONS
import com.renobile.carrinho.util.API_PREMIUM
import com.renobile.carrinho.util.API_ROUTE_IDENTIFY
import com.renobile.carrinho.util.API_TOKEN
import com.renobile.carrinho.util.API_WAKEUP
import com.renobile.carrinho.util.PARAM_ITEM_ID
import com.renobile.carrinho.util.PARAM_TYPE
import com.renobile.carrinho.util.PREF_FCM_TOKEN
import com.renobile.carrinho.util.Prefs
import com.renobile.carrinho.util.getCircleCroppedBitmap
import com.renobile.carrinho.util.getThumbUrl
import com.renobile.carrinho.util.isDebug
import com.renobile.carrinho.util.isValidUrl
import com.renobile.carrinho.util.printFuelLog
import com.renobile.carrinho.util.storeAppLink
import com.renobile.carrinho.util.stringToInt
import java.io.IOException
import java.net.ConnectException
import java.net.URL

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        const val TYPE = "type"
        const val TITLE = "title"
        const val BODY = "body"
        const val LINK = "link"
        const val IMAGE = "image"
        const val VERSION = "version"
        const val ITEM_ID = "item_id"
        const val VIBRATE = "vibrate"

        const val TAG = "MyFCM"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Log.i(TAG, "New token: $token")

        Prefs.putValue(PREF_FCM_TOKEN, token)

        val params = listOf(API_TOKEN to token)
        API_ROUTE_IDENTIFY.httpGet(params).responseString { request, response, result ->
            printFuelLog(request, response, result)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.i(TAG, "Firebase Cloud Messaging new message received!")

        val data = remoteMessage.data

        Log.i(TAG, "Push message data: $data")

        var type = ""
        var title = ""
        var body = ""
        var link = ""
        var image = ""
        var version = ""
        var itemId = ""
        var vibrate = ""

        for (entry in data.entries) {
            val value = entry.value

            when (entry.key) {
                TYPE -> type = value
                TITLE -> title = value
                BODY -> body = value
                LINK -> link = value
                IMAGE -> image = value
                VERSION -> version = value
                ITEM_ID -> itemId = value
                VIBRATE -> vibrate = value
            }
        }

        Log.i(TAG, "Push type: $type")
        Log.i(TAG, "Push title: $title")
        Log.i(TAG, "Push body: $body")
        Log.i(TAG, "Push link: $link")
        Log.i(TAG, "Push image: $image")
        Log.i(TAG, "Push version: $version")
        Log.i(TAG, "Push itemId: $itemId")

        if (title.isEmpty() || type == API_WAKEUP)
            return

        val channelId = "${type}_channel"

        var notifyIntent = Intent(applicationContext, StartActivity::class.java)

        if (version.isNotEmpty()) {
            val versionCode = version.stringToInt()

            if (versionCode > 0) {
                if (BuildConfig.VERSION_CODE < versionCode) {
                    link = storeAppLink()
                } else {
                    return
                }
            }
        }

        if (link.isValidUrl()) {

            notifyIntent = Intent(Intent.ACTION_VIEW, link.toUri())

        } else {

            notifyIntent.putExtra(PARAM_TYPE, type)
            notifyIntent.putExtra(PARAM_ITEM_ID, itemId)

        }

        val builder = NotificationCompat.Builder(applicationContext, channelId)

        val pendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(notifyIntent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        builder.setAutoCancel(true)
        builder.setContentIntent(pendingIntent)
        builder.setDefaults(NotificationCompat.DEFAULT_ALL)
        builder.setSmallIcon(R.drawable.ic_notification)
        builder.setContentTitle(title)
        builder.setContentText(body)
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        builder.color = ContextCompat.getColor(this, R.color.colorPrimaryDark)

        if (body.length > 40) {
            builder.setStyle(NotificationCompat.BigTextStyle().bigText(body))
        }

        if (image.isNotEmpty()) {
            val thumbUrl = getThumbUrl(image, 100, 100)

            Log.i(TAG, "Push thumb url: $thumbUrl")

            try {
                val url = URL(thumbUrl)
                val icon = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                if (icon != null) {
                    builder.setLargeIcon(icon.getCircleCroppedBitmap())
                }
            } catch (e: IOException) {
                if (isDebug()) e.printStackTrace() else FirebaseCrashlytics.getInstance().recordException(e)
            } catch (e: ConnectException) {
                if (isDebug()) e.printStackTrace() else FirebaseCrashlytics.getInstance().recordException(e)
            }
        }

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = when (type) {
                API_PREMIUM -> R.string.remove_ads
                API_FEEDBACK -> R.string.feedback
                API_NOTIFICATIONS -> R.string.notifications
                API_ABOUT_APP -> R.string.about_app
                else -> R.string.channel_updates
            }
            val channel =
                NotificationChannel(channelId, getString(name), NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
            builder.setChannelId(channelId)
        }

        manager.notify(1, builder.build())

        Log.i(TAG, "Push notification displayed - vibrate: $vibrate")

        if (vibrate.isNotEmpty()) {
            val pattern = longArrayOf(0, 100, 0, 100)
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vm.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(VIBRATOR_SERVICE) as Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createWaveform(
                        pattern,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, -1)
            }
        }
    }

}
