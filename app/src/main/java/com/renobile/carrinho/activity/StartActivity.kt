package com.renobile.carrinho.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.github.kittinunf.fuel.httpGet
import com.orhanobut.hawk.Hawk
import com.renobile.carrinho.application.CustomApplication
import com.renobile.carrinho.databinding.ActivityStartBinding
import com.renobile.carrinho.util.API_ABOUT_APP
import com.renobile.carrinho.util.API_NOTIFICATIONS
import com.renobile.carrinho.util.API_ROUTE_IDENTIFY
import com.renobile.carrinho.util.API_TOKEN
import com.renobile.carrinho.util.PARAM_ITEM_ID
import com.renobile.carrinho.util.PARAM_TYPE
import com.renobile.carrinho.util.PREF_ADMOB_ID
import com.renobile.carrinho.util.PREF_DEVICE_ID
import com.renobile.carrinho.util.PREF_DEVICE_ID_OLD
import com.renobile.carrinho.util.PREF_FCM_TOKEN
import com.renobile.carrinho.util.appLog
import com.renobile.carrinho.util.isNotNumeric
import com.renobile.carrinho.util.printFuelLog
import com.renobile.carrinho.util.saveAppData
import org.jetbrains.anko.intentFor
import java.util.Calendar
import kotlin.random.Random

class StartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding

    private var _alertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.w(TAG, "Token FCM: " + Hawk.get(PREF_FCM_TOKEN, ""))

        createDeviceID()

        if (Hawk.get(PREF_ADMOB_ID, "").isEmpty())
            identifyApp()
        else
            initApp()
    }

    override fun onDestroy() {
        super.onDestroy()
        _alertDialog?.dismiss()
    }

    private fun identifyApp() {
        if (Hawk.get(PREF_ADMOB_ID, "").isEmpty()) {
            val token = Hawk.get(PREF_FCM_TOKEN, "")
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

        startActivity(intentFor<MainActivity>(PARAM_TYPE to type))

        when (type) {
            API_NOTIFICATIONS -> {
                if (itemId != null && itemId.isEmpty())
                    startActivity(intentFor<NotificationsActivity>())
                else
                    startActivity(intentFor<NotificationDetailsActivity>(PARAM_ITEM_ID to itemId))
            }

            API_ABOUT_APP -> startActivity(intentFor<AboutActivity>())
        }

        finish()
    }

    private fun createDeviceID() {
        val currentDeviceID = Hawk.get(PREF_DEVICE_ID, "")
        val isNotNumeric = currentDeviceID.isNotNumeric()

        if (currentDeviceID.isEmpty() || isNotNumeric) {
            if (isNotNumeric) Hawk.put(PREF_DEVICE_ID_OLD, currentDeviceID)

            val milliseconds = Calendar.getInstance().timeInMillis.toString()
            val random = Random.nextInt(10000, 99999)
            var stringID = "$milliseconds$random"

            if (stringID.length > 18) {
                stringID = stringID.substring(0, 18)
            } else if (stringID.length < 18) {
                stringID = stringID.padEnd(18, '9')
            }

            Hawk.put(PREF_DEVICE_ID, stringID)
            CustomApplication().updateFuelParams()

            appLog("GENERATE_DEVICE_ID", "New device ID: $stringID")
        } else {
            appLog("GENERATE_DEVICE_ID", "Ignored, current ID: $currentDeviceID")
        }
    }

    companion object {
        const val TAG = "SplashActivity"
    }

}
