package com.harsh.fileselector.model


import com.google.gson.annotations.SerializedName

data class UploadImage(
    @SerializedName("expiry")
    val expiry: String,
    @SerializedName("key")
    val key: String,
    @SerializedName("link")
    val link: String,
    @SerializedName("success")
    val success: Boolean
)