package com.example.messengerchat.data.models

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("ID")
    val id: String,
    @SerializedName("Login")
    val login: String
)