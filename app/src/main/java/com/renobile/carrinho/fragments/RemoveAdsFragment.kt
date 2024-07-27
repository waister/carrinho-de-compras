package com.renobile.carrinho.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.orhanobut.hawk.Hawk
import com.renobile.carrinho.R
import com.renobile.carrinho.activity.StartActivity
import com.renobile.carrinho.databinding.FragmentRemoveAdsBinding
import com.renobile.carrinho.util.ONE_DAY
import com.renobile.carrinho.util.PREF_ADMOB_REMOVE_ADS_ID
import com.renobile.carrinho.util.PREF_PLAN_VIDEO_DURATION
import com.renobile.carrinho.util.PREF_PLAN_VIDEO_MILLIS
import com.renobile.carrinho.util.appLog
import com.renobile.carrinho.util.havePlan
import com.renobile.carrinho.util.haveVideoPlan
import com.renobile.carrinho.util.hide
import com.renobile.carrinho.util.isDebug
import com.renobile.carrinho.util.show
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton

class RemoveAdsFragment : Fragment(), OnUserEarnedRewardListener {

    private var _binding: FragmentRemoveAdsBinding? = null
    private val binding get() = _binding!!

    private var adMobRemoveAds: String = ""
    private var planCount: Int = 0
    private var isRewardedAlertShown: Boolean = false
    private var rewardedAd: RewardedAd? = null
    private var _alertDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRemoveAdsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
    }

    override fun onUserEarnedReward(rewardedAd: RewardItem) {
        val amount = rewardedAd.amount
        val type = rewardedAd.type
        appLog(TAG, "User earned the reward amount: $amount")
        appLog(TAG, "User earned the reward type: $type")

        Hawk.put(PREF_PLAN_VIDEO_MILLIS, System.currentTimeMillis())
    }

    override fun onDestroy() {
        super.onDestroy()
        _alertDialog?.dismiss()
    }

    private fun initViews() = with(binding) {
        tvThanks.hide()
        cvWatch.hide()

        adMobRemoveAds = Hawk.get(PREF_ADMOB_REMOVE_ADS_ID, "")

        if (isDebug()) adMobRemoveAds = "ca-app-pub-3940256099942544/5224354917"

        appLog(TAG, "adMobRemoveAds: $adMobRemoveAds")

        loadWatchToBy()
    }

    private fun loadWatchToBy() = with(binding) {
        appLog(TAG, "loadWatchToBy() - havePlan(): ${havePlan()}")
        appLog(TAG, "loadWatchToBy() - haveVideoPlan(): ${haveVideoPlan()}")

        if (haveVideoPlan()) {
            if (planCount == 0)
                requireActivity().runOnUiThread {
                    tvThanks.show()
                }

            return
        }

        appLog(TAG, "loadWatchToBy() - adMobRemoveAds: $adMobRemoveAds")

        if (havePlan() || adMobRemoveAds.isEmpty()) return

        requireActivity().runOnUiThread {
            btWatchStart.alpha = 0.5f
            pbWatchLoading.show()

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
                                    pbWatchLoading.hide()
                                    btWatchStart.alpha = 0.5f
                                }
                            }
                        }

                        override fun onAdLoaded(ad: RewardedAd) {
                            appLog(TAG, "Ad was loaded")

                            pbWatchLoading.hide()
                            btWatchStart.alpha = 1f

                            rewardedAd = ad

                            rewardedAd?.fullScreenContentCallback =
                                object : FullScreenContentCallback() {
                                    override fun onAdDismissedFullScreenContent() {
                                        appLog(TAG, "Ad was dismissed")

                                        alertRestartApp()
                                    }

                                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                        appLog(TAG, "Ad failed to show: ${adError.message}")

                                        alertErrorLoad()
                                    }

                                    override fun onAdShowedFullScreenContent() {
                                        appLog(TAG, "Ad showed fullscreen content")

                                        rewardedAd = null
                                    }
                                }
                        }
                    })
            }

        }

        val panVideoDuration = Hawk.get(PREF_PLAN_VIDEO_DURATION, ONE_DAY)
        val panVideoDurationDays = panVideoDuration / ONE_DAY
        val description = getString(R.string.watch_to_by_body, panVideoDurationDays)

        btWatchStart.setOnClickListener {
            val canShow = activity != null && rewardedAd != null && isVisible
            appLog(TAG, "Activity is null: ${activity == null}")
            appLog(TAG, "Rewarded ad is null: ${rewardedAd == null}")
            appLog(TAG, "Fragment is visible: $isVisible")
            appLog(TAG, "Can show rewarded ad: $canShow")

            if (canShow) {
                rewardedAd?.show(requireActivity(), this@RemoveAdsFragment)
            } else {
                appLog(TAG, "The rewarded ad wasn't ready yet")
            }
        }

        requireActivity().runOnUiThread {
            tvWatchDescription.text = description

            cvWatch.show()
        }
    }

    private fun alertRestartApp() {
        if (activity != null && !isRewardedAlertShown && haveVideoPlan()) {
            isRewardedAlertShown = true

            if (_alertDialog == null)
                _alertDialog = AlertDialog.Builder(requireActivity())
                    .setCancelable(false)
                    .setTitle(R.string.plan_success_title)
                    .setMessage(R.string.plan_success_body)
                    .setPositiveButton(R.string.restart_app) { _, _ ->
                        restartApp()
                    }
                    .create()

            _alertDialog?.show()
        }
    }

    private fun restartApp() {
        val intent = Intent(activity, StartActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)

        activity?.finish()
    }

    private fun alertErrorLoad() {
        activity?.alert(R.string.error_load_video, R.string.ops) { okButton {} }?.show()
    }

    companion object {
        const val TAG = "RemoveAdsFragment"
    }

}
