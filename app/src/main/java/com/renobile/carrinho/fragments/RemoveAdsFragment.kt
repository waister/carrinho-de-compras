package com.renobile.carrinho.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.android.billingclient.api.*
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.orhanobut.hawk.Hawk
import com.renobile.carrinho.R
import com.renobile.carrinho.activity.SplashActivity
import com.renobile.carrinho.util.*
import kotlinx.android.synthetic.main.fragment_remove_ads.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.browse
import org.jetbrains.anko.okButton

class RemoveAdsFragment : Fragment(), PurchasesUpdatedListener, BillingClientStateListener,
    OnUserEarnedRewardListener {

    companion object {
        const val TAG = "RemoveAdsFragment"
    }

    private var billingClient: BillingClient? = null
    private var purchaseToken: String? = null
    private var adMobRemoveAds: String = ""
    private var planCount: Int = 0
    private var isRewardedAlertShown: Boolean = false
    private var rewardedAd: RewardedAd? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_remove_ads, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
    }

    private fun initViews() {
        rl_progress?.visibility = View.VISIBLE
        tv_thanks?.visibility = View.GONE
        cv_watch?.visibility = View.GONE
        cv_billing?.visibility = View.GONE

        adMobRemoveAds = Hawk.get(PREF_ADMOB_REMOVE_ADS_ID, "")

        appLog(TAG, "adMobRemoveAds: $adMobRemoveAds")

        checkPurchase()
    }

    private fun checkPurchase(relaunch: Boolean = false) {
        if (activity == null) return

        if (billingClient == null) {

            billingClient = BillingClient
                .newBuilder(requireContext())
                .setListener(this)
                .enablePendingPurchases()
                .build()

            billingClient!!.startConnection(this)

        } else {

            var planSelected = ""
            var planTime = 0L
            var havePlan = false
            val purchasesResult = billingClient!!.queryPurchases(BillingClient.SkuType.SUBS)
            val list = purchasesResult.purchasesList

            appLog(TAG, "purchasesList: $list")

            if (list != null) {
                for (purchase in list) {
                    if (!havePlan && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        havePlan = true
                        planSelected = purchase.skus[0]
                        planTime = purchase.purchaseTime
                        purchaseToken = purchase.purchaseToken
                    }
                }
            } else {
                rl_progress?.visibility = View.GONE

                loadWatchToBy()
            }

            appLog(TAG, "havePlan: $havePlan")
            appLog(TAG, "haveVideoPlan(): ${haveVideoPlan()}")
            appLog(TAG, "haveBillingPlan(): ${haveBillingPlan()}")
            appLog(TAG, "havePlan(): ${havePlan()}")

            if (havePlan) {
                requireActivity().runOnUiThread {
                    tv_thanks?.visibility = View.VISIBLE
                    rl_progress?.visibility = View.GONE
                }
                appLog(TAG, "relaunch: $relaunch")

                if (relaunch) {
                    restartApp()
                    return
                }
            }

            val params = SkuDetailsParams.newBuilder()
                .setSkusList(getSkuList())
                .setType(BillingClient.SkuType.SUBS)
                .build()

            appLog(TAG, "Request billing params: $params")

            billingClient!!.querySkuDetailsAsync(params) { _, skuDetailsList ->
                appLog(TAG, "skuDetailsList: $skuDetailsList")
                appLog(TAG, "activity: $activity")

                if (skuDetailsList != null && activity != null) {

                    var selectedSkuDetails: SkuDetails? = null

                    for (skuDetails in skuDetailsList) {
                        if (planSelected == skuDetails.sku) {
                            selectedSkuDetails = skuDetails
                        }
                    }

//                    ll_plans?.removeAllViews()

                    if (selectedSkuDetails == null) {
                        for (skuDetails in skuDetailsList) {
                            addItemView(skuDetails)

                            planCount++
                        }

                        loadWatchToBy()

                    } else {
                        requireActivity().runOnUiThread {
                            tv_thanks?.visibility = View.VISIBLE
                        }

                        addItemView(selectedSkuDetails, planTime)
                    }

                    requireActivity().runOnUiThread {
                        rl_progress?.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun getSkuList(): ArrayList<String> {
        val skuList = arrayListOf<String>()

        val year = Hawk.get(PREF_BILL_PLAN_YEAR, "")

        if (year.isNotEmpty()) skuList.add(year)

        return skuList
    }

    private fun addItemView(skuDetails: SkuDetails?, planTime: Long = 0L) {
        if (skuDetails == null || activity == null) return

        requireActivity().runOnUiThread {
            tv_billing_title.text = skuDetails.title.formatPlanTitle()
            tv_billing_price.text = skuDetails.price
            tv_billing_description.text = skuDetails.description
        }

        if (planTime != 0L) {

            requireActivity().runOnUiThread {
                tv_billing_date.text = getString(R.string.subscribed_in, planTime.formatDate())

                bt_billing_subscribe.visibility = View.GONE
                bt_billing_manage.visibility = View.VISIBLE
            }

            bt_billing_manage.setOnClickListener {
                activity?.browse(
                    "https://play.google.com/store/account/subscriptions" +
                            "?sku=${skuDetails.sku}&package=${requireActivity().packageName}"
                )
            }

        } else {

            requireActivity().runOnUiThread {
                tv_billing_date.visibility = View.GONE
            }

            bt_billing_subscribe.setOnClickListener {
                appLog(TAG, "skuDetails: $skuDetails")

                val flowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(skuDetails)
                    .build()

                billingClient!!.launchBillingFlow(requireActivity(), flowParams)
            }

        }

        requireActivity().runOnUiThread {
            cv_billing.visibility = View.VISIBLE
        }
    }

    override fun onBillingServiceDisconnected() {
        appLog(TAG, "onBillingServiceDisconnected()")
        context?.alert(R.string.error_unknown_send_message, R.string.ops) { okButton {} }?.show()
    }

    override fun onBillingSetupFinished(result: BillingResult) {
        appLog(TAG, "onBillingSetupFinished()")
        checkPurchase()
    }

    override fun onPurchasesUpdated(result: BillingResult, list: MutableList<Purchase>?) {
        val relaunch = result.responseCode == BillingClient.BillingResponseCode.OK
        appLog(TAG, "onBillingSetupFinished() - relaunch: $relaunch")
        checkPurchase(relaunch)
    }

    private fun loadWatchToBy() {
        appLog(TAG, "loadWatchToBy() - havePlan(): ${havePlan()}")
        appLog(TAG, "loadWatchToBy() - haveVideoPlan(): ${haveVideoPlan()}")

        if (haveVideoPlan()) {
            if (planCount == 0)
                requireActivity().runOnUiThread {
                    tv_thanks?.visibility = View.VISIBLE
                }

            return
        }

        appLog(TAG, "loadWatchToBy() - adMobRemoveAds: $adMobRemoveAds")

        if (havePlan() || adMobRemoveAds.isEmpty()) return

        requireActivity().runOnUiThread {
            bt_watch_start.alpha = 0.5f
            pb_watch_loading.visibility = View.VISIBLE

            MobileAds.initialize(requireContext()) {
                appLog(TAG, "Mobile ads initialized")

                val deviceId = listOf(AdRequest.DEVICE_ID_EMULATOR)
                val configuration =
                    RequestConfiguration.Builder().setTestDeviceIds(deviceId).build()
                MobileAds.setRequestConfiguration(configuration)

                val request = AdRequest.Builder().build()

                RewardedAd.load(
                    requireContext(),
                    adMobRemoveAds,
                    request,
                    object : RewardedAdLoadCallback() {
                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            appLog(TAG, "Filed to load ad: ${adError.message}")

                            alertErrorLoad()

                            rewardedAd = null

                            if (isAdded) {
                                requireActivity().runOnUiThread {
                                    pb_watch_loading.visibility = View.GONE
                                    bt_watch_start.alpha = 0.5f
                                }
                            }
                        }

                        override fun onAdLoaded(ad: RewardedAd) {
                            appLog(TAG, "Ad was loaded")

                            pb_watch_loading.visibility = View.GONE
                            bt_watch_start.alpha = 1f

                            rewardedAd = ad

                            rewardedAd?.fullScreenContentCallback =
                                object : FullScreenContentCallback() {
                                    override fun onAdDismissedFullScreenContent() {
                                        appLog(TAG, "Ad was dismissed")

                                        alertRestartApp()
                                    }

                                    override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                                        appLog(TAG, "Ad failed to show: ${adError?.message}")

                                        alertErrorLoad()
                                    }

                                    override fun onAdShowedFullScreenContent() {
                                        appLog(TAG, "Ad showed fullscreen content")

                                        rewardedAd = null

                                        rl_progress?.visibility = View.GONE
                                    }
                                }
                        }
                    })
            }

        }

        val panVideoDuration = Hawk.get(PREF_PLAN_VIDEO_DURATION, ONE_DAY)
        val panVideoDurationDays = panVideoDuration / ONE_DAY
        val description = getString(R.string.watch_to_by_body, panVideoDurationDays)

        bt_watch_start.setOnClickListener {
            val canShow = activity != null && rewardedAd != null
            appLog(TAG, "Subscribe button clicked - can show: $canShow")

            if (canShow) {
                rewardedAd?.show(requireActivity(), this)
            } else {
                appLog(TAG, "The rewarded ad wasn't ready yet")
            }
        }

        requireActivity().runOnUiThread {
            tv_watch_description.text = description

            cv_watch.visibility = View.VISIBLE
        }
    }

    override fun onUserEarnedReward(rewardedAd: RewardItem) {
        val amount = rewardedAd.amount
        val type = rewardedAd.type
        appLog(TAG, "User earned the reward amount: $amount")
        appLog(TAG, "User earned the reward type: $type")

        Hawk.put(PREF_PLAN_VIDEO_MILLIS, System.currentTimeMillis())
    }

    private fun alertRestartApp() {
        if (activity != null && !isRewardedAlertShown && haveVideoPlan()) {
            isRewardedAlertShown = true

            AlertDialog.Builder(requireActivity())
                .setCancelable(false)
                .setTitle(R.string.plan_success_title)
                .setMessage(R.string.plan_success_body)
                .setPositiveButton(R.string.restart_app) { _, _ ->
                    restartApp()
                }
                .create()
                .show()
        }
    }

    private fun restartApp() {
        val intent = Intent(activity, SplashActivity::class.java)
        intent.putExtra(PARAM_TYPE, API_PREMIUM)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)

        activity?.finish()
    }

    private fun alertErrorLoad() {
        activity?.alert(R.string.error_load_video, R.string.ops) { okButton {} }?.show()
    }

}
