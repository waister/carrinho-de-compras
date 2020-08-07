package com.renobile.carrinho.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.eightbitlab.bottomnavigationbar.BottomBarItem
import com.github.kittinunf.fuel.httpGet
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.orhanobut.hawk.Hawk
import com.renobile.carrinho.BuildConfig
import com.renobile.carrinho.R
import com.renobile.carrinho.fragments.*
import com.renobile.carrinho.util.*
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.browse

class MainActivity : AppCompatActivity() {

    private val fragments = mutableListOf<Fragment>()
    private var selectedTabName = FRAGMENT_MAIN
    private var billingClient: BillingClient? = null
    private var adBannerLoaded = false
    private var interstitialAd: InterstitialAd? = null

    companion object {
        private const val POSITION_CART: Int = 0
        private const val POSITION_LIST: Int = 1
        private const val POSITION_COMPARATOR: Int = 2
        private const val POSITION_REMOVE_ADS: Int = 3
        private const val POSITION_MORE: Int = 4
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MobileAds.initialize(this) {}

        bn_navigation.addTab(BottomBarItem(R.drawable.ic_cart_outline, R.string.cart))
        bn_navigation.addTab(BottomBarItem(R.drawable.ic_format_list_checks, R.string.list))
        bn_navigation.addTab(BottomBarItem(R.drawable.ic_select_compare, R.string.compare))
        bn_navigation.addTab(BottomBarItem(R.drawable.ic_crown, R.string.premium))
        bn_navigation.addTab(BottomBarItem(R.drawable.ic_dots_horizontal, R.string.more))

        fragments.add(CartFragment())
        fragments.add(ListFragment())
        fragments.add(ComparatorFragment())
        fragments.add(RemoveAdsFragment())
        fragments.add(MoreFragment())

        bn_navigation.setOnSelectListener { position ->
            val tabName = when (position) {
                POSITION_LIST -> FRAGMENT_LIST
                POSITION_COMPARATOR -> FRAGMENT_COMPARATOR
                POSITION_REMOVE_ADS -> FRAGMENT_REMOVE_ADS
                POSITION_MORE -> FRAGMENT_MORE
                else -> FRAGMENT_MAIN
            }

            viewFragment(fragments[position], tabName)
        }

        when (intent.getStringExtra(PARAM_TYPE)) {
            API_LIST -> forceSelectTab(POSITION_LIST)
            API_COMPARATOR -> forceSelectTab(POSITION_COMPARATOR)
            API_PREMIUM -> forceSelectTab(POSITION_REMOVE_ADS)
            else -> forceSelectTab(POSITION_CART)
        }

        checkPurchase()
        checkVersion()
        initAdMob()
    }

    private fun initAdMob() {
        MobileAds.initialize(this) {}

        val deviceId = listOf(AdRequest.DEVICE_ID_EMULATOR)
        val configuration = RequestConfiguration.Builder().setTestDeviceIds(deviceId).build()
        MobileAds.setRequestConfiguration(configuration)

        loadAdBanner(ll_banner, Hawk.get(PREF_ADMOB_AD_MAIN_ID, ""))

        interstitialAd = createInterstitialAd()
        interstitialAd?.loadAd(AdRequest.Builder().build())
    }

    private fun viewFragment(fragment: Fragment, name: String) {
        selectedTabName = name

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fl_fragments, fragment, name)
        fragmentTransaction.commit()
    }

    private fun forceSelectTab(position: Int) {
        var fragment: Fragment = CartFragment()

        when (position) {
            POSITION_LIST -> fragment = ListFragment()
            POSITION_COMPARATOR -> fragment = ComparatorFragment()
            POSITION_REMOVE_ADS -> fragment = RemoveAdsFragment()
            POSITION_MORE -> fragment = MoreFragment()
        }

        viewFragment(fragment, FRAGMENT_MAIN)
        bn_navigation.selectTab(position, true)
    }

    override fun onBackPressed() {
        if (selectedTabName != FRAGMENT_MAIN) {
            forceSelectTab(POSITION_CART)
        } else {
            showInterstitialAd()

            super.onBackPressed()
        }
    }

    fun showInterstitialAd() {
        if (interstitialAd != null && interstitialAd!!.isLoaded)
            interstitialAd!!.show()
    }

    private fun checkPurchase() {
        if (billingClient == null) {

            billingClient = BillingClient
                    .newBuilder(this)
                    .setListener { _, _ -> checkPurchase() }
                    .enablePendingPurchases()
                    .build()

            billingClient?.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK)
                        checkPurchase()
                }

                override fun onBillingServiceDisconnected() {}
            })

        } else {

            var havePlan = false
            val result = billingClient!!.queryPurchases(BillingClient.SkuType.SUBS)
            val list = result.purchasesList

            if (list != null) {
                for (purchase in list) {
                    if (!havePlan && purchase.purchaseState == Purchase.PurchaseState.PURCHASED)
                        havePlan = true
                }
            }

            Hawk.put(PREF_HAVE_PLAN, havePlan)

            if (havePlan && adBannerLoaded) {
                val intent = Intent(this, SplashActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }
        }
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
                            alert(getString(R.string.update_needed), getString(R.string.updated_title)) {
                                positiveButton(R.string.updated_positive) {
                                    browse(storeAppLink())
                                }
                                negativeButton(R.string.updated_logout) { finish() }
                                onCancelled { finish() }
                            }.show()
                        } else if (BuildConfig.VERSION_CODE < versionLast) {
                            alert(getString(R.string.update_available), getString(R.string.updated_title)) {
                                positiveButton(R.string.updated_positive) {
                                    browse(storeAppLink())
                                }
                                negativeButton(R.string.updated_negative) {}
                            }.show()
                        }
                    }
                }
            }

        }
    }

}
