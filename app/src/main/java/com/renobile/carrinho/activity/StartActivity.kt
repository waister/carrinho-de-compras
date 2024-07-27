package com.renobile.carrinho.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.github.kittinunf.fuel.httpGet
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.orhanobut.hawk.Hawk
import com.renobile.carrinho.R
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
import com.renobile.carrinho.util.hide
import com.renobile.carrinho.util.isNotNumeric
import com.renobile.carrinho.util.printFuelLog
import com.renobile.carrinho.util.saveAppData
import com.renobile.carrinho.util.show
import org.jetbrains.anko.intentFor
import java.util.Calendar
import kotlin.random.Random

class StartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding

    private var _alertDialog: AlertDialog? = null
    private var appUpdateManager: AppUpdateManager? = null
    private var updateFlowResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            appLog(
                "IN_APP_UPDATE",
                "Launcher result code: ${result.resultCode} |  ${result.resultCode == RESULT_OK}"
            )

            if (result.resultCode == RESULT_OK) {
                initApp()
            } else {
                binding.pbLoading.hide()

                if (_alertDialog == null)
                    _alertDialog = AlertDialog.Builder(this)
                        .setTitle(R.string.error_on_update)
                        .setMessage(R.string.error_on_update_message)
                        .setCancelable(false)
                        .setPositiveButton(R.string.try_again) { dialog, _ ->
                            checkAppVersion()

                            dialog.dismiss()
                        }
                        .setNegativeButton(R.string.update_later) { dialog, _ ->
                            initApp()

                            dialog.dismiss()
                        }
                        .create()

                _alertDialog?.show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.w(TAG, "Token FCM: " + Hawk.get(PREF_FCM_TOKEN, ""))

        createDeviceID()

        if (Hawk.get(PREF_ADMOB_ID, "").isEmpty()) {
            identifyApp()
        } else {
            if ((application as CustomApplication).isCheckUpdatesNeeded) {
                (application as CustomApplication).isCheckUpdatesNeeded = false

                checkAppVersion()
            } else
                initApp()
        }
    }

    override fun onResume() {
        super.onResume()

        appUpdateManager
            ?.appUpdateInfo
            ?.addOnFailureListener {
                appLog("IN_APP_UPDATE", "On resume error: ${it.message}")

                it.printStackTrace()
            }
            ?.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    appUpdateManager?.startUpdateFlowForResult(
                        appUpdateInfo,
                        updateFlowResultLauncher,
                        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                    )
                }
            }
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

    private fun checkAppVersion() {
        binding.pbLoading.show()

        appUpdateManager = AppUpdateManagerFactory.create(this)

        appUpdateManager
            ?.appUpdateInfo
            ?.addOnFailureListener {
                initApp()

                appLog("IN_APP_UPDATE", "Error message: ${it.message}")

                it.printStackTrace()
            }
            ?.addOnSuccessListener { appUpdateInfo ->
                appLog("IN_APP_UPDATE", "Success - appUpdateInfo: $appUpdateInfo")

                val updateAvailable =
                    appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE

                val appUpdateType = when {
                    appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) -> AppUpdateType.FLEXIBLE
                    appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) -> AppUpdateType.IMMEDIATE
                    else -> null
                }

                if (updateAvailable && appUpdateType != null) {
                    appUpdateManager?.startUpdateFlowForResult(
                        appUpdateInfo,
                        updateFlowResultLauncher,
                        AppUpdateOptions.newBuilder(appUpdateType).build()
                    )
                } else
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
