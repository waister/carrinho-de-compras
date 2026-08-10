package com.renobile.carrinho.network

import com.renobile.carrinho.network.models.NotificationResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface NotificationApiService {
    @GET("notifications")
    suspend fun getNotifications(): NotificationResponse

    @GET("notification/{id}")
    suspend fun getNotificationDetail(@Path("id") id: String): NotificationResponse
}
