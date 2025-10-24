package com.example.messengerchat.data.api

import com.example.messengerchat.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @GET("ping")
    suspend fun ping(): Response<Map<String, String>>

    @POST("sign-up")
    suspend fun signUp(@Body authData: AuthData): Response<SimpleResponse>

    @POST("sign-in")
    suspend fun signIn(@Body signInData: SignInData): Response<AuthResponse>

    @POST("reset-password")
    suspend fun resetPassword(@Body authData: AuthData): Response<SimpleResponse>

    @GET("user")
    suspend fun getUserByLogin(@Query("login") login: String): Response<List<User>>

    @POST("chat")
    suspend fun createChat(@Body request: CreateChatRequest): Response<CreateChatResponse>

    @GET("chat-history")
    suspend fun getChatHistory(@Query("chat_id") chatId: String): Response<List<Message>>

    @GET("user-chats")
    suspend fun getUserChats(): Response<List<Chat>>
}