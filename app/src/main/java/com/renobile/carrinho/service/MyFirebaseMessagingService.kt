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
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.renobile.carrinho.BuildConfig
import com.renobile.carrinho.MainActivity
import com.renobile.carrinho.R
import com.renobile.carrinho.repositories.ConfigRepository
import com.renobile.carrinho.util.API_ABOUT_APP
import com.renobile.carrinho.util.API_FEEDBACK
import com.renobile.carrinho.util.API_NOTIFICATIONS
import com.renobile.carrinho.util.API_PREMIUM
import com.renobile.carrinho.util.API_WAKEUP
import com.renobile.carrinho.util.PARAM_ITEM_ID
import com.renobile.carrinho.util.PARAM_TYPE
import com.renobile.carrinho.util.PREF_FCM_TOKEN
import com.renobile.carrinho.util.Prefs
import com.renobile.carrinho.util.appLog
import com.renobile.carrinho.util.getCircleCroppedBitmap
import com.renobile.carrinho.util.getThumbUrl
import com.renobile.carrinho.util.isDebug
import com.renobile.carrinho.util.isValidUrl
import com.renobile.carrinho.util.storeAppLink
import com.renobile.carrinho.util.stringToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.IOException
import java.net.URL

class MyFirebaseMessagingService : FirebaseMessagingService(), KoinComponent {

    private val configRepository: ConfigRepository by inject()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        const val TAG = "MyFCM"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        appLog(TAG, "New token: $token")

        Prefs.putValue(PREF_FCM_TOKEN, token)

        serviceScope.launch {
            configRepository.identify(token).onSuccess {
                configRepository.saveConfig(it)
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        appLog(TAG, "Firebase Cloud Messaging new message received!")

        val pushData = parsePushData(remoteMessage.data)
        appLog(TAG, "Push data: $pushData")

        if (pushData.title.isEmpty() || pushData.type == API_WAKEUP) return

        val finalLink = handleVersionCheck(pushData.version, pushData.link) ?: return
        val notifyIntent = createNotifyIntent(pushData, finalLink)

        showNotification(pushData, notifyIntent)

        if (pushData.vibrate.isNotEmpty()) {
            handleVibration()
        }
    }

    private fun parsePushData(data: Map<String, String>) = PushData(
        type = data["type"] ?: "",
        title = data["title"] ?: "",
        body = data["body"] ?: "",
        link = data["link"] ?: "",
        image = data["image"] ?: "",
        version = data["version"] ?: "",
        itemId = data["item_id"] ?: "",
        vibrate = data["vibrate"] ?: ""
    )

    private fun handleVersionCheck(version: String, currentLink: String): String? {
        if (version.isEmpty()) return currentLink

        val versionCode = version.stringToInt()
        if (versionCode <= 0) return currentLink

        return if (BuildConfig.VERSION_CODE < versionCode) {
            storeAppLink()
        } else {
            null
        }
    }

    private fun createNotifyIntent(pushData: PushData, link: String): Intent {
        return if (link.isValidUrl()) {
            Intent(Intent.ACTION_VIEW, link.toUri())
        } else {
            Intent(applicationContext, MainActivity::class.java).apply {
                putExtra(PARAM_TYPE, pushData.type)
                putExtra(PARAM_ITEM_ID, pushData.itemId)
            }
        }
    }

    private fun showNotification(pushData: PushData, notifyIntent: Intent) {
        val channelId = "${pushData.type}_channel"
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        setupNotificationChannel(manager, pushData.type, channelId)

        val builder = NotificationCompat.Builder(applicationContext, channelId).apply {
            setAutoCancel(true)
            setContentIntent(createPendingIntent(notifyIntent))
            setDefaults(NotificationCompat.DEFAULT_ALL)
            setSmallIcon(R.drawable.ic_notification)
            setContentTitle(pushData.title)
            setContentText(pushData.body)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            color = ContextCompat.getColor(this@MyFirebaseMessagingService, R.color.colorPrimaryDark)

            if (pushData.body.length > 40) {
                setStyle(NotificationCompat.BigTextStyle().bigText(pushData.body))
            }
        }

        if (pushData.image.isNotEmpty()) {
            loadNotificationIcon(builder, pushData.image)
        }

        manager.notify(1, builder.build())
        appLog(TAG, "Push notification displayed - vibrate: ${pushData.vibrate}")
    }

    private fun createPendingIntent(notifyIntent: Intent): PendingIntent? {
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        return TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(notifyIntent)
            getPendingIntent(0, flags)
        }
    }

    private fun loadNotificationIcon(builder: NotificationCompat.Builder, image: String) {
        val thumbUrl = getThumbUrl(image, 100, 100)
        appLog(TAG, "Push thumb url: $thumbUrl")

        try {
            val url = URL(thumbUrl)
            val icon = BitmapFactory.decodeStream(url.openConnection().getInputStream())
            icon?.let {
                builder.setLargeIcon(it.getCircleCroppedBitmap())
            }
        } catch (e: IOException) {
            if (isDebug()) e.printStackTrace() else FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    private fun setupNotificationChannel(manager: NotificationManager, type: String, channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nameRes = when (type) {
                API_PREMIUM -> R.string.remove_ads
                API_FEEDBACK -> R.string.feedback
                API_NOTIFICATIONS -> R.string.notifications
                API_ABOUT_APP -> R.string.about_app
                else -> R.string.channel_updates
            }
            val channel = NotificationChannel(channelId, getString(nameRes), NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
        }
    }

    private fun handleVibration() {
        val pattern = longArrayOf(0, 100, 0, 100)
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
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

    private data class PushData(
        val type: String,
        val title: String,
        val body: String,
        val link: String,
        val image: String,
        val version: String,
        val itemId: String,
        val vibrate: String,
    )
}
