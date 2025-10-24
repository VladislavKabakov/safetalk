package com.example.messengerchat.data.repository

import com.example.messengerchat.data.api.RetrofitClient
import com.example.messengerchat.data.models.CreateChatRequest

class ChatRepository {
    private val apiService = RetrofitClient.apiService

    suspend fun getUserChats() = apiService.getUserChats()

    suspend fun createChat(userIds: List<String>) =
        apiService.createChat(CreateChatRequest(userIds))

    suspend fun searchUsers(login: String) = apiService.getUserByLogin(login)
}