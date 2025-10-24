package com.example.messengerchat.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.messengerchat.data.models.Message
import com.example.messengerchat.data.repository.MessageRepository
import kotlinx.coroutines.launch

class MessageViewModel : ViewModel() {
    private val repository = MessageRepository()

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    fun loadChatHistory(chatId: String) {
        viewModelScope.launch {
            try {
                val response = repository.getChatHistory(chatId)
                if (response.isSuccessful) {
                    _messages.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                _messages.value = emptyList()
            }
        }
    }

    fun addMessage(message: Message) {
        val currentMessages = _messages.value?.toMutableList() ?: mutableListOf()
        currentMessages.add(message)
        _messages.value = currentMessages
    }

    fun updateMessage(messageId: Int, newText: String, editedAt: String) {
        val currentMessages = _messages.value?.toMutableList() ?: return
        val index = currentMessages.indexOfFirst { it.id == messageId }
        if (index != -1) {
            currentMessages[index] = currentMessages[index].copy(
                text = newText,
                editedAt = editedAt,
                isEdited = true
            )
            _messages.value = currentMessages
        }
    }

    fun deleteMessage(messageId: Int) {
        val currentMessages = _messages.value?.toMutableList() ?: return
        val index = currentMessages.indexOfFirst { it.id == messageId }
        if (index != -1) {
            currentMessages[index] = currentMessages[index].copy(
                text = "This message was deleted",
                isDeleted = true
            )
            _messages.value = currentMessages
        }
    }
}