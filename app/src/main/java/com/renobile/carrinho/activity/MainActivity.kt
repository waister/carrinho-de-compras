package com.renobile.carrinho.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.eightbitlab.bottomnavigationbar.BottomBarItem
import com.github.kittinunf.fuel.httpGet
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
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
import com.renobile.carrinho.util.PARAM_TYPE
import com.renobile.carrinho.util.PREF_FCM_TOKEN
import com.renobile.carrinho.util.getBooleanVal
import com.renobile.carrinho.util.getIntVal
import com.renobile.carrinho.util.getValidJSONObject
import com.renobile.carrinho.util.havePlan
import com.renobile.carrinho.util.loadAdBanner
import com.renobile.carrinho.util.printFuelLog
import com.renobile.carrinho.util.saveAppData
import com.renobile.carrinho.util.storeAppLink
import org.jetbrains.anko.alert
import org.jetbrains.anko.browse


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var selectedTabName = FRAGMENT_MAIN
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

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            bnNavigation.addTab(BottomBarItem(R.drawable.ic_cart_outline, R.string.cart))
            bnNavigation.addTab(BottomBarItem(R.drawable.ic_format_list_checks, R.string.list))
            bnNavigation.addTab(BottomBarItem(R.drawable.ic_select_compare, R.string.compare))
            bnNavigation.addTab(BottomBarItem(R.drawable.ic_crown, R.string.premium))
            bnNavigation.addTab(BottomBarItem(R.drawable.ic_dots_horizontal, R.string.more))

            bnNavigation.setOnSelectListener { position ->
                val tabName = when (position) {
                    POSITION_LIST -> FRAGMENT_LIST
                    POSITION_COMPARATOR -> FRAGMENT_COMPARATOR
                    POSITION_REMOVE_ADS -> FRAGMENT_REMOVE_ADS
                    POSITION_MORE -> FRAGMENT_MORE
                    else -> FRAGMENT_MAIN
                }

                viewFragment(position, tabName)
            }
        }

        when (intent.getStringExtra(PARAM_TYPE)) {
            API_LIST -> forceSelectTab(POSITION_LIST)
            API_COMPARATOR -> forceSelectTab(POSITION_COMPARATOR)
            API_PREMIUM -> forceSelectTab(POSITION_REMOVE_ADS)
            else -> forceSelectTab(POSITION_CART)
        }

        checkVersion()
        initAdMob()
    }

    private fun initAdMob() {
        if (havePlan()) return

        MobileAds.initialize(this) {
            val deviceId = listOf(AdRequest.DEVICE_ID_EMULATOR, "7242AA08F80EC72727EE3DECA8262032")
            val configuration = RequestConfiguration.Builder().setTestDeviceIds(deviceId).build()
            MobileAds.setRequestConfiguration(configuration)

            loadAdBanner(binding.llBanner, "ca-app-pub-6521704558504566/7944661753")

            createInterstitialAd()
        }
    }

    private fun createInterstitialAd() {
        if (havePlan()) return

        val id = "ca-app-pub-6521704558504566/4051651496"
        val request = AdRequest.Builder().build()

        InterstitialAd.load(this, id, request, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                interstitialAd = null
            }

            override fun onAdLoaded(loadedAd: InterstitialAd) {
                interstitialAd = loadedAd
            }
        })
    }

    fun showInterstitialAd() {
        interstitialAd?.show(this)
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

    private fun forceSelectTab(position: Int) {
        viewFragment(position, FRAGMENT_MAIN)

        binding.bnNavigation.selectTab(position, true)
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
                            alert(
                                getString(R.string.update_needed),
                                getString(R.string.updated_title)
                            ) {
                                positiveButton(R.string.updated_positive) {
                                    browse(storeAppLink())
                                }
                                negativeButton(R.string.updated_logout) { finish() }
                                onCancelled { finish() }
                            }.show()
                        } else if (BuildConfig.VERSION_CODE < versionLast) {
                            alert(
                                getString(R.string.update_available),
                                getString(R.string.updated_title)
                            ) {
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
