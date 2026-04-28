package com.renobile.carrinho.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

data class ConfigResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("configs") val configs: AppConfigs?
)

data class AppConfigs(
    @SerializedName("store_link") val storeLink: String?,
    @SerializedName("app_name") val appName: String?,
    @SerializedName("admob_id") val admobId: String?,
    @SerializedName("admob_ad_main_id") val admobAdMainId: String?,
    @SerializedName("admob_interstitial_id") val admobInterstitialId: String?,
    @SerializedName("admob_remove_ads_id") val admobRemoveAdsId: String?,
    @SerializedName("admob_open_app_id") val admobOpenAppId: String?,
    @SerializedName("plan_video_duration") val planVideoDuration: Long?
)

interface ConfigApiService {
    @GET("identify")
    suspend fun identify(
        @Query("token") token: String
    ): ConfigResponse
}
