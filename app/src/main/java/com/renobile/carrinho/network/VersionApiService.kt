package com.renobile.carrinho.network

import retrofit2.http.GET
import retrofit2.http.Query

interface VersionApiService {
    @GET("identify")
    suspend fun checkVersion(
        @Query("token") token: String,
    ): ConfigResponse
}
