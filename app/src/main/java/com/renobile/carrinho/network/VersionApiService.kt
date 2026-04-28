package com.renobile.carrinho.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

data class VersionResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("version_last") val versionLast: Int,
    @SerializedName("version_min") val versionMin: Int,
)

interface VersionApiService {
    @GET("identify")
    suspend fun checkVersion(
        @Query("token") token: String,
    ): VersionResponse
}
