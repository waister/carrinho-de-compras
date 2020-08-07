package com.renobile.carrinho.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.github.kittinunf.fuel.httpGet
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.orhanobut.hawk.Hawk
import com.renobile.carrinho.BuildConfig
import com.renobile.carrinho.R
import com.renobile.carrinho.activity.SplashActivity
import com.renobile.carrinho.util.*
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

        Hawk.put(PREF_FCM_TOKEN, token)

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

        var notifyIntent = Intent(applicationContext, SplashActivity::class.java)

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

            notifyIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))

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
                e.printStackTrace()
            } catch (e: ConnectException) {
                e.printStackTrace()
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
            val channel = NotificationChannel(channelId, getString(name), NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
            builder.setChannelId(channelId)
        }

        manager.notify(NOTIFICATION_DEFAULT_ID, builder.build())

        Log.i(TAG, "Push notification displayed - vibrate: $vibrate")

        if (vibrate.isNotEmpty()) {
            val pattern = longArrayOf(0, 100, 0, 100)
            val vibrator = applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, -1)
            }
        }
    }

}
