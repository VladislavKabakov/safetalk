package com.example.messengerchat.data.models

import com.google.gson.annotations.SerializedName

data class Message(
    @SerializedName("Id")
    val id: Int,
    @SerializedName("Text")
    val text: String,
    @SerializedName("FromUser")
    val fromUser: String,
    @SerializedName("ToUser")
    val toUser: String,
    @SerializedName("ChatId")
    val chatId: String,
    @SerializedName("CreatedAt")
    val createdAt: String,
    @SerializedName("Type")
    val type: Int? = 1, // 2 for files

    val editedAt: String? = null,
    val isEdited: Boolean = false,
    val isDeleted: Boolean = false,
)

data class WebSocketMessage(
    @SerializedName("type")
    val type: Int? = 1, // 2 for files
    @SerializedName("chat_id")
    val chatId: String,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("from_user")
    val fromUser: String? = null,
    @SerializedName("message_id")
    val messageId: Int? = null,
    val newText: String? = null,
    val editedAt: String? = null,
    val deletedForAll: Boolean? = null
)

data class EditMessageRequest(
    @SerializedName("Id")
    val messageId: Int,
    @SerializedName("NText")
    val newText: String
)

data class EditMessageResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("edited_at")
    val editedAt: String
)