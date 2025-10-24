package com.example.messengerchat.data.models

import com.google.gson.annotations.SerializedName

data class Chat(
    @SerializedName("Id")
    val id: String,
    @SerializedName("UserNick")
    val userNick: String,
    @SerializedName("LastMessage")
    val lastMessage: String?
)

data class CreateChatRequest(
    @SerializedName("user_ids")
    val userIds: List<String>
)

data class CreateChatResponse(
    @SerializedName("ChatId")
    val chatId: String
)