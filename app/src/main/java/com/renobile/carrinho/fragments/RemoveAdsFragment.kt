package com.renobile.carrinho.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import com.android.billingclient.api.*
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.orhanobut.hawk.Hawk
import com.renobile.carrinho.BuildConfig
import com.renobile.carrinho.R
import com.renobile.carrinho.activity.SplashActivity
import com.renobile.carrinho.util.*
import kotlinx.android.synthetic.main.fragment_remove_ads.*
import kotlinx.android.synthetic.main.inc_progress_light.*
import org.jetbrains.anko.*

class RemoveAdsFragment : Fragment(), PurchasesUpdatedListener, BillingClientStateListener {

    companion object {
        const val TAG = "RemoveAdsActivity"
    }

    private var billingClient: BillingClient? = null
    private var purchaseToken: String? = null
    private var adMobRemoveAds: String = ""
    private var planVideoDuration: Long = 0
    private var showBackButton: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        showBackButton = arguments?.getBoolean(PARAM_SHOW_BACK, false) ?: false

        return inflater.inflate(R.layout.fragment_remove_ads, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
    }

    private fun initViews() {
        if (showBackButton) {
            toolbar?.setNavigationIcon(R.drawable.ic_arrow_left)
            toolbar?.setNavigationOnClickListener {
                activity?.onBackPressed()
            }
        }

        rl_progress_light?.visibility = View.VISIBLE
        tv_thanks?.visibility = View.GONE

        adMobRemoveAds = if (BuildConfig.DEBUG)
            "ca-app-pub-3940256099942544/5224354917"
        else
            Hawk.get(PREF_ADMOB_REMOVE_ADS_ID, "")

        checkPurchase()
    }

    private fun checkPurchase() {
        if (activity == null) return


//        ll_plans?.post {
        println("----------------------- REMOVE ALL 1")
//            ll_plans?.removeAllViews()
            ll_plans?.removeAllViewsInLayout()
        println("----------------------- REMOVE ALL 2")

            if (billingClient == null) {

                billingClient = BillingClient
                    .newBuilder(requireContext())
                    .setListener(this)
                    .enablePendingPurchases()
                    .build()

                billingClient!!.startConnection(this)

            } else {

                billingClient!!.queryPurchasesAsync(BillingClient.SkuType.SUBS) { _, list ->
                    var planSelected = ""
                    var planTime = 0L
                    var havePlan = false

                    for (purchase in list) {
                        if (!havePlan && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                            havePlan = true
                            planSelected = purchase.skus[0]
                            planTime = purchase.purchaseTime
                            purchaseToken = purchase.purchaseToken
                        }
                    }

                    if (havePlan)
                        rl_progress_light?.visibility = View.GONE

                    val skuList = arrayListOf<String>()
                    skuList.add(Hawk.get(PREF_BILL_PLAN_YEAR, ""))

                    val params = SkuDetailsParams.newBuilder()
                        .setSkusList(skuList)
                        .setType(BillingClient.SkuType.SUBS)
                        .build()

                    Log.w("RemoveAdsFragment", "Request billing params: $params")

                    billingClient!!.querySkuDetailsAsync(params) { _, skuDetailsList ->
                        if (skuDetailsList != null && activity != null) {
                            var selectedSkuDetails: SkuDetails? = null

                            for (skuDetails in skuDetailsList) {
                                if (planSelected == skuDetails.sku) {
                                    selectedSkuDetails = skuDetails
                                }
                            }

                            if (selectedSkuDetails == null) {
                                for (skuDetails in skuDetailsList) {
                                    addItemView(skuDetails)
                                }
                            } else {
                                tv_thanks?.visibility = View.VISIBLE

                                addItemView(selectedSkuDetails, planTime)

                            }

                            rl_progress_light?.visibility = View.GONE

                            addWatchToBy()
                        }
                    }
                }
            }
//        }
    }

    private fun addItemView(skuDetails: SkuDetails?, planTime: Long = 0L) {
        if (skuDetails == null || activity == null) return

        val inflater = LayoutInflater.from(activity) ?: return

        @SuppressLint("InflateParams")
        val viewItem = inflater.inflate(R.layout.item_remove_ads, null) ?: return

        val tvTitle = viewItem.find<TextView>(R.id.tv_title)
        val tvPrice = viewItem.find<TextView>(R.id.tv_price)
        val tvDescription = viewItem.find<TextView>(R.id.tv_description)
        val tvDate = viewItem.find<TextView>(R.id.tv_date)
        val btSubscribe = viewItem.find<AppCompatButton>(R.id.bt_subscribe)
        val btManage = viewItem.find<AppCompatButton>(R.id.bt_manage)

        tvTitle.text = skuDetails.title.formatPlanTitle()
        tvPrice.text = skuDetails.price
        tvDescription.text = skuDetails.description

        if (planTime != 0L) {

            tvDate.text = getString(R.string.subscribed_in, planTime.formatDate())

            btSubscribe.visibility = View.GONE
            btManage.visibility = View.VISIBLE

            btManage.setOnClickListener {
                activity?.browse(
                    "https://play.google.com/store/account/subscriptions" +
                            "?sku=${skuDetails.sku}&package=${requireActivity().packageName}"
                )
            }

        } else {

            tvDate.visibility = View.GONE

            btSubscribe.setOnClickListener {
                val flowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(skuDetails)
                    .build()

                billingClient!!.launchBillingFlow(requireActivity(), flowParams)
            }

        }

        ll_plans?.addView(viewItem)
//        ll_plans?.post { ll_plans?.addView(viewItem) }
    }

    override fun onBillingServiceDisconnected() {
        context?.alert(R.string.error_unknown_send_message, R.string.ops) { okButton {} }?.show()
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            checkPurchase()
        }
    }

    override fun onPurchasesUpdated(p0: BillingResult, p1: MutableList<Purchase>?) {
        checkPurchase()
    }

    private fun addWatchToBy() {
        if (activity == null && adMobRemoveAds.isNotEmpty()) return

        val inflater = LayoutInflater.from(activity)

        @SuppressLint("InflateParams")
        val viewItem = inflater.inflate(R.layout.item_remove_ads, null)

        val tvTitle = viewItem.find<TextView>(R.id.tv_title)
        val tvPrice = viewItem.find<TextView>(R.id.tv_price)
        val tvDescription = viewItem.find<TextView>(R.id.tv_description)
        val tvDate = viewItem.find<TextView>(R.id.tv_date)
        val btSubscribe = viewItem.find<AppCompatButton>(R.id.bt_subscribe)

        val panVideoDuration = Hawk.get(PREF_PLAN_VIDEO_DURATION, FIVE_DAYS)
        val panVideoDurationDays = panVideoDuration / ONE_DAY
        var description = getString(R.string.watch_to_by_body, panVideoDurationDays)
        var buttonText = R.string.watch_to_by_button

        if (haveVideoPlan()) {
            val expiration = Hawk.get(PREF_PLAN_VIDEO_MILLIS, 0L) + panVideoDuration
            val remaining = ((expiration - System.currentTimeMillis()) / ONE_DAY) + 1
            description = getString(R.string.watch_to_by_body_paid, remaining)
            buttonText = R.string.watch_to_by_button_again
        }

        tvTitle.setText(R.string.watch_to_by_title)
        tvPrice.setText(R.string.watch_to_by_price)
        tvDescription.text = description
        btSubscribe.setText(buttonText)

        tvDate.visibility = View.GONE

        btSubscribe.setOnClickListener {
            if (activity != null) {
                planVideoDuration = Hawk.get(PREF_PLAN_VIDEO_DURATION, FIVE_DAYS)

                rl_progress_light?.visibility = View.VISIBLE

                val request = AdRequest.Builder().build()
                val ctx = requireContext()

                RewardedAd.load(ctx, adMobRemoveAds, request, object : RewardedAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.w(TAG, "RewardedAd : onAdFailedToLoad(): ${adError.message}")

                        rl_progress_light?.visibility = View.GONE

                        activity?.alert(R.string.error_load_video, R.string.ops) { okButton {} }?.show()
                    }

                    override fun onAdLoaded(loadedAd: RewardedAd) {
                        rl_progress_light?.visibility = View.GONE

                        loadedAd.show(requireActivity()) {
                            Log.w(TAG, "RewardedAd : onRewarded() reward item amount: ${it.amount}")
                            Log.w(TAG, "RewardedAd : onRewarded() reward item type: ${it.type}")

                            Hawk.put(PREF_PLAN_VIDEO_MILLIS, System.currentTimeMillis())

                            activity?.longToast(R.string.watch_to_by_success)

                            val intent = Intent(activity, SplashActivity::class.java)
                            intent.putExtra(PARAM_TYPE, API_PREMIUM)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(intent)
                        }
                    }
                })


                /*mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(activity)
                mRewardedVideoAd.rewardedVideoAdListener = object : RewardedVideoAdListener {
                    override fun onRewardedVideoAdClosed() {
                        Log.w(TAG, "onRewardedVideoAdClosed()")
                    }

                    override fun onRewardedVideoAdLeftApplication() {
                        Log.w(TAG, "onRewardedVideoAdLeftApplication()")
                    }

                    override fun onRewardedVideoAdLoaded() {
                        Log.w(TAG, "onRewardedVideoAdLoaded()")

                        mRewardedVideoAd.show()

                        rl_progress_light?.visibility = View.GONE
                    }

                    override fun onRewardedVideoAdOpened() {
                        Log.w(TAG, "onRewardedVideoAdOpened()")
                    }

                    override fun onRewardedVideoCompleted() {
                        Log.w(TAG, "onRewardedVideoCompleted()")
                    }

                    override fun onRewarded(reward: RewardItem?) {
                        Log.w(TAG, "onRewarded() reward item amount: ${reward?.amount}")
                        Log.w(TAG, "onRewarded() reward item type: ${reward?.type}")

                        Hawk.put(PREF_PLAN_VIDEO_MILLIS, System.currentTimeMillis())

                        activity?.longToast(R.string.watch_to_by_success)

                        val intent = Intent(activity, SplashActivity::class.java)
                        intent.putExtra(PARAM_TYPE, API_PREMIUM)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                    }

                    override fun onRewardedVideoStarted() {
                        Log.w(TAG, "onRewardedVideoStarted()")
                    }

                    override fun onRewardedVideoAdFailedToLoad(errorCode: Int) {
                        Log.w(TAG, "onRewardedVideoAdFailedToLoad() error code: $errorCode")

                        rl_progress_light?.visibility = View.GONE

                        activity?.alert(R.string.error_load_video, R.string.ops) { okButton {} }?.show()
                    }
                }*/

//                mRewardedVideoAd.loadAd(adMobRemoveAds, AdRequest.Builder().build())
            }
        }

        println("---------------- addWatchToBy 6 :: viewItem: $viewItem")

        ll_plans?.addView(viewItem)
//        ll_plans?.post { ll_plans?.addView(viewItem) }
        println("---------------- addWatchToBy 7")
    }

}
