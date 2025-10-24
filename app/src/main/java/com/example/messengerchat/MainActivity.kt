package com.example.messengerchat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.messengerchat.data.api.RetrofitClient
import com.example.messengerchat.data.preferences.PreferencesManager
import com.example.messengerchat.ui.auth.LoginActivity
import com.example.messengerchat.ui.chat.ChatListActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefsManager = PreferencesManager(this)

        val token = prefsManager.getToken()
        val userId = prefsManager.getUserId()

        val intent = if (!token.isNullOrEmpty() && !userId.isNullOrEmpty()) {
            RetrofitClient.setAuthToken(token)
            Intent(this, ChatListActivity::class.java)
        } else {
            Intent(this, LoginActivity::class.java)
        }

        startActivity(intent)
        finish()
    }
}