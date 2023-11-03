package com.renobile.carrinho.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.github.kittinunf.fuel.httpGet
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.orhanobut.hawk.Hawk
import com.renobile.carrinho.R
import com.renobile.carrinho.application.CustomApplication
import com.renobile.carrinho.databinding.ActivityStartBinding
import com.renobile.carrinho.util.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.intentFor

class StartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding

    companion object {
        const val TAG = "SplashActivity"
        const val MY_REQUEST_CODE = 1
    }

    private var appUpdateManager: AppUpdateManager? = null

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.w(TAG, "Token FCM: " + Hawk.get(PREF_FCM_TOKEN, ""))

        if (Hawk.get(PREF_DEVICE_ID, "").isEmpty()) {
            Hawk.put(PREF_DEVICE_ID, Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID))
            CustomApplication().updateFuelParams()
        }

        identifyApp()
    }

    private fun identifyApp() {
        if (Hawk.get(PREF_ADMOB_ID, "").isEmpty()) {
            val token = Hawk.get(PREF_FCM_TOKEN, "")
            val params = listOf(API_TOKEN to token)

            API_ROUTE_IDENTIFY.httpGet(params).responseString { request, response, result ->
                printFuelLog(request, response, result)

                saveAppData(result)

                checkAppVersion()
            }
        } else {
            checkAppVersion()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                initApp()
            } else {
                binding.llLoading.hide()

                alert(R.string.error_on_update_message, R.string.error_on_update) {
                    positiveButton(R.string.try_again) { checkAppVersion() }
                    negativeButton(R.string.update_later) { initApp() }
                }.show()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        appUpdateManager
                ?.appUpdateInfo
                ?.addOnSuccessListener { appUpdateInfo ->
                    if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                        appUpdateManager?.startUpdateFlowForResult(
                                appUpdateInfo,
                                AppUpdateType.IMMEDIATE,
                                this,
                                MY_REQUEST_CODE
                        )
                    }
                }
    }

    private fun checkAppVersion() {
        binding.llLoading.show()

        appUpdateManager = AppUpdateManagerFactory.create(this)

        val appUpdateInfoTask = appUpdateManager?.appUpdateInfo

        appUpdateInfoTask
                ?.addOnFailureListener {
                    initApp()
                }
                ?.addOnSuccessListener { appUpdateInfo ->
                    val updateAvailable = appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    val isImmediate = appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)

                    if (updateAvailable && isImmediate) {

                        appUpdateManager?.startUpdateFlowForResult(
                                appUpdateInfo,
                                AppUpdateType.IMMEDIATE,
                                this,
                                MY_REQUEST_CODE)
                    } else {

                        initApp()

                    }
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

}
