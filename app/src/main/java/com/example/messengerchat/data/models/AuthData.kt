package com.example.messengerchat.data.models

import com.google.gson.annotations.SerializedName

data class AuthData(
    val login: String,
    val password: String,
    val keyword: String? = null
)

data class SignInData(
    val login: String,
    val password: String
)

data class AuthResponse(
    val message: MessageResponse
)

data class MessageResponse(
    @SerializedName("Token")
    val token: String,
    @SerializedName("UserId")
    val userId: String
)

data class SimpleResponse(
    val message: String
)