package com.example.messengerchat.data.repository

import com.example.messengerchat.data.api.RetrofitClient
import com.example.messengerchat.data.models.AuthData
import com.example.messengerchat.data.models.SignInData

class AuthRepository {
    private val apiService = RetrofitClient.apiService

    suspend fun signUp(login: String, password: String, keyword: String) =
        apiService.signUp(AuthData(login, password, keyword))

    suspend fun signIn(login: String, password: String) =
        apiService.signIn(SignInData(login, password))

    suspend fun resetPassword(login: String, password: String, keyword: String) =
        apiService.resetPassword(AuthData(login, password, keyword))
}