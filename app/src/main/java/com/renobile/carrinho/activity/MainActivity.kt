package com.renobile.carrinho.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.github.kittinunf.fuel.httpGet
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.orhanobut.hawk.Hawk
import com.renobile.carrinho.BuildConfig
import com.renobile.carrinho.R
import com.renobile.carrinho.databinding.ActivityMainBinding
import com.renobile.carrinho.fragments.CartFragment
import com.renobile.carrinho.fragments.ComparatorFragment
import com.renobile.carrinho.fragments.ListFragment
import com.renobile.carrinho.fragments.MoreFragment
import com.renobile.carrinho.fragments.RemoveAdsFragment
import com.renobile.carrinho.util.API_COMPARATOR
import com.renobile.carrinho.util.API_LIST
import com.renobile.carrinho.util.API_PREMIUM
import com.renobile.carrinho.util.API_ROUTE_IDENTIFY
import com.renobile.carrinho.util.API_SUCCESS
import com.renobile.carrinho.util.API_TOKEN
import com.renobile.carrinho.util.API_VERSION_LAST
import com.renobile.carrinho.util.API_VERSION_MIN
import com.renobile.carrinho.util.FRAGMENT_COMPARATOR
import com.renobile.carrinho.util.FRAGMENT_LIST
import com.renobile.carrinho.util.FRAGMENT_MAIN
import com.renobile.carrinho.util.FRAGMENT_MORE
import com.renobile.carrinho.util.FRAGMENT_REMOVE_ADS
import com.renobile.carrinho.util.InAppUpdate
import com.renobile.carrinho.util.PARAM_TYPE
import com.renobile.carrinho.util.PREF_ADMOB_AD_MAIN_ID
import com.renobile.carrinho.util.PREF_ADMOB_INTERSTITIAL_ID
import com.renobile.carrinho.util.PREF_FCM_TOKEN
import com.renobile.carrinho.util.PREF_PUSH_NOTIFICATION
import com.renobile.carrinho.util.getBooleanVal
import com.renobile.carrinho.util.getIntVal
import com.renobile.carrinho.util.getValidJSONObject
import com.renobile.carrinho.util.havePlan
import com.renobile.carrinho.util.isDebug
import com.renobile.carrinho.util.loadBannerAd
import com.renobile.carrinho.util.printFuelLog
import com.renobile.carrinho.util.saveAppData
import com.renobile.carrinho.util.storeAppLink
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var selectedTabName = FRAGMENT_MAIN
    private var interstitialAd: InterstitialAd? = null
    private var _alertDialog: AlertDialog? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val permissionTag = Manifest.permission.POST_NOTIFICATIONS
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted)
                checkTokenFcm()
        }

    companion object {
        private const val TIMES_TO_APPEAR = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.rlRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                left = systemBars.left,
                top = systemBars.top,
                right = systemBars.right,
                bottom = systemBars.bottom
            )
            insets
        }

        binding.bnNavigation.setOnItemSelectedListener { item ->
            val (position, tabName) = when (item.itemId) {
                R.id.action_list -> 1 to FRAGMENT_LIST
                R.id.action_compare -> 2 to FRAGMENT_COMPARATOR
                R.id.action_premium -> 3 to FRAGMENT_REMOVE_ADS
                R.id.action_more -> 4 to FRAGMENT_MORE
                else -> 0 to FRAGMENT_MAIN
            }

            viewFragment(position, tabName)
            true
        }

        when (intent.getStringExtra(PARAM_TYPE)) {
            API_LIST -> forceSelectTab(R.id.action_list)
            API_COMPARATOR -> forceSelectTab(R.id.action_compare)
            API_PREMIUM -> forceSelectTab(R.id.action_premium)
            else -> forceSelectTab(R.id.action_cart)
        }

        if (!isDebug())
            InAppUpdate(this)

        checkVersion()
        initAdMob()

        checkTokenFcm()
        requestNotificationPermission()
    }

    override fun onDestroy() {
        super.onDestroy()
        _alertDialog?.dismiss()
    }

    fun showInterstitialAd() {
        interstitialAd?.show(this)
    }

    private fun initAdMob() {
        if (havePlan()) return

        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(this@MainActivity) {}
            runOnUiThread {
                val deviceId = listOf(AdRequest.DEVICE_ID_EMULATOR, "7242AA08F80EC72727EE3DECA8262032")
                val configuration = RequestConfiguration.Builder().setTestDeviceIds(deviceId).build()
                MobileAds.setRequestConfiguration(configuration)

                loadBannerAd(
                    adViewContainer = binding.llBanner,
                    adUnitId = Hawk.get(PREF_ADMOB_AD_MAIN_ID, ""),
                    adSize = null,
                    collapsible = false,
                    shimmer = binding.shimmerBanner
                )

                createInterstitialAd()
            }
        }
    }

    private fun createInterstitialAd() {
        var adUnitId = Hawk.get(PREF_ADMOB_INTERSTITIAL_ID, "")

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

    private fun viewFragment(index: Int, name: String) {
        val fragment = when (index) {
            1 -> ListFragment()
            2 -> ComparatorFragment()
            3 -> RemoveAdsFragment()
            4 -> MoreFragment()
            else -> CartFragment()
        }

        selectedTabName = name

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fl_fragments, fragment, name)
        fragmentTransaction.commit()
    }

    private fun forceSelectTab(itemId: Int) {
        val (position, tabName) = when (itemId) {
            R.id.action_list -> 1 to FRAGMENT_LIST
            R.id.action_compare -> 2 to FRAGMENT_COMPARATOR
            R.id.action_premium -> 3 to FRAGMENT_REMOVE_ADS
            R.id.action_more -> 4 to FRAGMENT_MORE
            else -> 0 to FRAGMENT_MAIN
        }
        
        viewFragment(position, tabName)
        binding.bnNavigation.selectedItemId = itemId
    }

    private fun checkVersion() {
        val token = Hawk.get(PREF_FCM_TOKEN, "")

        if (token.isNotEmpty()) {
            val params = listOf(API_TOKEN to token)

            API_ROUTE_IDENTIFY.httpGet(params).responseString { request, response, result ->
                printFuelLog(request, response, result)

                val (data, error) = result

                if (error == null) {
                    val apiObj = data.getValidJSONObject()

                    saveAppData(result)

                    if (apiObj.getBooleanVal(API_SUCCESS)) {
                        val versionLast = apiObj.getIntVal(API_VERSION_LAST)
                        val versionMin = apiObj.getIntVal(API_VERSION_MIN)

                        if (BuildConfig.VERSION_CODE < versionMin) {
                            AlertDialog.Builder(this)
                                .setTitle(R.string.updated_title)
                                .setMessage(R.string.update_needed)
                                .setPositiveButton(R.string.updated_positive) { _, _ ->
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(storeAppLink()))
                                    startActivity(intent)
                                }
                                .setNegativeButton(R.string.updated_logout) { _, _ -> finish() }
                                .setOnCancelListener { finish() }
                                .show()
                        } else if (BuildConfig.VERSION_CODE < versionLast) {
                            AlertDialog.Builder(this)
                                .setTitle(R.string.updated_title)
                                .setMessage(R.string.update_available)
                                .setPositiveButton(R.string.updated_positive) { _, _ ->
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(storeAppLink()))
                                    startActivity(intent)
                                }
                                .setNegativeButton(R.string.updated_negative, null)
                                .show()
                        }
                    }
                }
            }

        }
    }

    private fun checkTokenFcm() {
        val lastToken = Hawk.get(PREF_FCM_TOKEN, "")

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            } else {
                val token = task.result

                try {
                    if (token != lastToken) {
                        Hawk.put(PREF_FCM_TOKEN, token)

                        checkVersion()
                    }
                } catch (e: Exception) {
                    if (isDebug()) e.printStackTrace()
                }
            }
        })
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, permissionTag) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowPushNotificationQuestionDialog())
                    alertNotificationsIsImportant()
                else
                    permissionLauncher.launch(permissionTag)
            }
        }
    }

    private fun shouldShowPushNotificationQuestionDialog(): Boolean {
        val appOpened = Hawk.get(PREF_PUSH_NOTIFICATION, 1)
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
        Hawk.put(PREF_PUSH_NOTIFICATION, TIMES_TO_APPEAR + 1)
    }

    private fun increaseAppOpened() {
        Hawk.put(PREF_PUSH_NOTIFICATION, Hawk.get(PREF_PUSH_NOTIFICATION, 1) + 1)
    }

    private fun showMessage() {
        Toast.makeText(applicationContext, R.string.notification_change_idea, Toast.LENGTH_LONG).show()
    }

}
