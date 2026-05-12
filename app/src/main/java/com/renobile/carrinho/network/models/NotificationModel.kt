package com.renobile.carrinho.network.models

import com.google.gson.annotations.SerializedName

data class NotificationResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("notifications") val notifications: List<NotificationModel>?
)

data class NotificationModel(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("body") val body: String,
    @SerializedName("date") val date: String,
    @SerializedName("image") val image: String?
)
