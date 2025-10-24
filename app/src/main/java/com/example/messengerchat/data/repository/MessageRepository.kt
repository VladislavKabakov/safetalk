package com.example.messengerchat.data.repository

import com.example.messengerchat.data.api.RetrofitClient

class MessageRepository {
    private val apiService = RetrofitClient.apiService

    suspend fun getChatHistory(chatId: String) = apiService.getChatHistory(chatId)
}