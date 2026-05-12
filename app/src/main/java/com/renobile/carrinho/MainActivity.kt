package com.renobile.carrinho

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.renobile.carrinho.ui.theme.MyAppTheme
import com.renobile.carrinho.util.InAppUpdate
import com.renobile.carrinho.util.PREF_ADMOB_INTERSTITIAL_ID
import com.renobile.carrinho.util.PREF_PUSH_NOTIFICATION
import com.renobile.carrinho.util.Prefs
import com.renobile.carrinho.util.havePlan
import com.renobile.carrinho.util.isDebug
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModel()
    private var interstitialAd: InterstitialAd? = null
    private var _alertDialog: AlertDialog? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val permissionTag = Manifest.permission.POST_NOTIFICATIONS
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted)
                viewModel.checkTokenFcm()
        }

    companion object {
        private const val TIMES_TO_APPEAR = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (!isDebug())
            InAppUpdate(this)

        initAdMob()
        viewModel.checkTokenFcm()
        requestNotificationPermission()

        setContent {
            MyAppTheme {
                MainScreen(
                    mainViewModel = viewModel,
                    onShowInterstitialAd = { showInterstitialAd() }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _alertDialog?.dismiss()
    }

    fun showInterstitialAd() {
        interstitialAd?.show(this)
        createInterstitialAd() // Load next one
    }

    private fun initAdMob() {
        if (havePlan()) return

        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(this@MainActivity) {}
            runOnUiThread {
                createInterstitialAd()
            }
        }
    }

    private fun createInterstitialAd() {
        var adUnitId = Prefs.getValue(PREF_ADMOB_INTERSTITIAL_ID, "")

        if (havePlan() || adUnitId.isEmpty()) return

        if (isDebug()) adUnitId = "ca-app-pub-3940256099942544/1033173712"

        val request = AdRequest.Builder().build()

        InterstitialAd.load(this, adUnitId, request, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                interstitialAd = null
            }

            override fun onAdLoaded(loadedAd: InterstitialAd) {
                interstitialAd = loadedAd
            }
        })
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(
                this,
                permissionTag
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (shouldShowPushNotificationQuestionDialog())
                alertNotificationsIsImportant()
            else
                permissionLauncher.launch(permissionTag)
        }
    }

    private fun shouldShowPushNotificationQuestionDialog(): Boolean {
        val appOpened = Prefs.getValue(PREF_PUSH_NOTIFICATION, 1)
        return if (appOpened < TIMES_TO_APPEAR) {
            increaseAppOpened()
            false
        } else if (appOpened == TIMES_TO_APPEAR) {
            increaseAppOpened()
            true
        } else
            false
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun alertNotificationsIsImportant() {
        if (_alertDialog == null)
            _alertDialog = AlertDialog.Builder(this)
                .setTitle(R.string.notification_question_title)
                .setMessage(R.string.notifications_important)
                .setCancelable(false)
                .setPositiveButton(R.string.allow_notifications) { dialog, _ ->
                    permissionLauncher.launch(permissionTag)
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.ignore) { dialog, _ ->
                    dialog.dismiss()
                    blockPushNotificationQuestion()
                    showMessage()
                }
                .create()

        _alertDialog?.show()
    }

    private fun blockPushNotificationQuestion() {
        Prefs.putValue(PREF_PUSH_NOTIFICATION, TIMES_TO_APPEAR + 1)
    }

    private fun increaseAppOpened() {
        Prefs.putValue(PREF_PUSH_NOTIFICATION, Prefs.getValue(PREF_PUSH_NOTIFICATION, 1) + 1)
    }

    private fun showMessage() {
        Toast.makeText(applicationContext, R.string.notification_change_idea, Toast.LENGTH_LONG).show()
    }
}
