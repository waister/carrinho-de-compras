package com.renobile.carrinho.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.github.kittinunf.fuel.httpGet
import com.renobile.carrinho.application.CustomApplication
import com.renobile.carrinho.database.RealmToRoomMigration
import com.renobile.carrinho.databinding.ActivityStartBinding
import com.renobile.carrinho.util.API_ABOUT_APP
import com.renobile.carrinho.util.API_NOTIFICATIONS
import com.renobile.carrinho.util.API_ROUTE_IDENTIFY
import com.renobile.carrinho.util.API_TOKEN
import com.renobile.carrinho.util.PARAM_ITEM_ID
import com.renobile.carrinho.util.PARAM_TYPE
import com.renobile.carrinho.util.PREF_ADMOB_ID
import com.renobile.carrinho.util.PREF_DEVICE_ID
import com.renobile.carrinho.util.PREF_FCM_TOKEN
import com.renobile.carrinho.util.Prefs
import com.renobile.carrinho.util.appLog
import com.renobile.carrinho.util.isDebug
import com.renobile.carrinho.util.printFuelLog
import com.renobile.carrinho.util.saveAppData
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

class StartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding

    private var _alertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.w(TAG, "Token FCM: " + Prefs.getValue(PREF_FCM_TOKEN, ""))

        createDeviceID()

        lifecycleScope.launch {
            // TODO: realiza a migração do Realm para o Room, remover no próximo release
            RealmToRoomMigration(this@StartActivity).migrate()

            if (Prefs.getValue(PREF_ADMOB_ID, "").isEmpty())
                identifyApp()
            else
                initApp()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _alertDialog?.dismiss()
    }

    private fun identifyApp() {
        if (Prefs.getValue(PREF_ADMOB_ID, "").isEmpty()) {
            val token = Prefs.getValue(PREF_FCM_TOKEN, "")
            val params = listOf(API_TOKEN to token)

            API_ROUTE_IDENTIFY.httpGet(params).responseString { request, response, result ->
                printFuelLog(request, response, result)

                saveAppData(result)

                initApp()
            }
        } else {
            initApp()
        }
    }

    private fun initApp() {
        val type = intent.getStringExtra(PARAM_TYPE)
        val itemId = intent.getStringExtra(PARAM_ITEM_ID)
        Log.i(TAG, "Received type from notification: $type")
        Log.i(TAG, "Received itemId from notification: $itemId")

        val mainIntent = Intent(this, MainActivity::class.java)
        mainIntent.putExtra(PARAM_TYPE, type)
        startActivity(mainIntent)

        when (type) {
            API_NOTIFICATIONS -> {
                if (itemId != null && itemId.isEmpty()) {
                    startActivity(Intent(this, NotificationsActivity::class.java))
                } else {
                    val intent = Intent(this, NotificationDetailsActivity::class.java)
                    intent.putExtra(PARAM_ITEM_ID, itemId)
                    startActivity(intent)
                }
            }

            API_ABOUT_APP -> startActivity(Intent(this, AboutActivity::class.java))
        }

        finish()
    }

    private fun createDeviceID() {
        val currentDeviceID = Prefs.getValue(PREF_DEVICE_ID, "")
        val isIdentifierV3 = currentDeviceID.contains(IDENTIFIER_VERSION)

        if (currentDeviceID.isEmpty() || !isIdentifierV3) {
            val newDeviceId = generateDeviceIdentifier()

            Prefs.putValue(PREF_DEVICE_ID, newDeviceId)
            CustomApplication().updateFuelParams()

            appLog("GENERATE_DEVICE_ID", "New device ID: $newDeviceId")
        } else {
            appLog("GENERATE_DEVICE_ID", "Ignored, current ID: $currentDeviceID")
        }
    }

    private fun generateDeviceIdentifier(): String {
        val deviceID = try {
            val uniqueDevicePseudoID =
                "35" + Build.BOARD.length % 10 + Build.BRAND.length % 10 + Build.DEVICE.length % 10 +
                        Build.DISPLAY.length % 10 + Build.HOST.length % 10 + Build.ID.length % 10 +
                        Build.MANUFACTURER.length % 10 + Build.MODEL.length % 10 + Build.PRODUCT.length % 10 +
                        Build.TAGS.length % 10 + Build.TYPE.length % 10 + Build.USER.length % 10
            val serial = Build.getRadioVersion()

            UUID(uniqueDevicePseudoID.hashCode().toLong(), serial.hashCode().toLong()).toString()
        } catch (e: Exception) {
            if (isDebug()) e.printStackTrace()
            UUID(System.currentTimeMillis(), Random.nextLong(1000000, 9999999)).toString()
        }

        return "$deviceID${IDENTIFIER_VERSION}"
    }

    companion object {
        const val TAG = "SplashActivity"
        const val IDENTIFIER_VERSION: String = "-v3"
    }

}
