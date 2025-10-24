package com.example.messengerchat.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.messengerchat.data.models.Chat
import com.example.messengerchat.data.models.User
import com.example.messengerchat.data.repository.ChatRepository
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val repository = ChatRepository()

    private val _chats = MutableLiveData<List<Chat>>()
    val chats: LiveData<List<Chat>> = _chats

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users

    private val _createChatResult = MutableLiveData<Result<String>>()
    val createChatResult: LiveData<Result<String>> = _createChatResult

    fun loadChats() {
        viewModelScope.launch {
            try {
                val response = repository.getUserChats()
                if (response.isSuccessful) {
                    _chats.value = response.body() ?: emptyList()
                } else {
                    _chats.value = emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _chats.value = emptyList()
            }
        }
    }

    fun searchUsers(query: String) {
        viewModelScope.launch {
            try {
                val response = repository.searchUsers(query)
                if (response.isSuccessful) {
                    _users.value = response.body() ?: emptyList()
                } else {
                    _users.value = emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _users.value = emptyList()
            }
        }
    }

    fun createChat(userIds: List<String>) {
        viewModelScope.launch {
            try {
                val response = repository.createChat(userIds)
                if (response.isSuccessful) {
                    _createChatResult.value = Result.success(response.body()?.chatId ?: "")
                } else {
                    val errorBody = response.errorBody()?.string()
                    _createChatResult.value = Result.failure(Exception("Failed to create chat: $errorBody"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _createChatResult.value = Result.failure(e)
            }
        }
    }
}