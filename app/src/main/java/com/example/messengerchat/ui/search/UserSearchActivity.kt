package com.example.messengerchat.ui.search

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.messengerchat.R
import com.example.messengerchat.data.api.RetrofitClient
import com.example.messengerchat.data.preferences.PreferencesManager
import com.example.messengerchat.ui.chat.ChatActivity
import com.example.messengerchat.ui.chat.UserAdapter
import com.example.messengerchat.ui.viewmodels.ChatViewModel
import kotlinx.coroutines.*

class UserSearchActivity : AppCompatActivity() {

    private lateinit var viewModel: ChatViewModel
    private lateinit var userAdapter: UserAdapter
    private lateinit var prefsManager: PreferencesManager

    private lateinit var etSearch: EditText
    private lateinit var rvUsers: RecyclerView
    private lateinit var btnBack: ImageButton

    private var searchJob: Job? = null
    private var pendingChatUserLogin: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_search_fixed)

        prefsManager = PreferencesManager(this)

        val token = prefsManager.getToken()

        if (!token.isNullOrEmpty()) {
            RetrofitClient.setAuthToken(token)
        }

        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]

        val currentUserId = prefsManager.getUserId()

        if (currentUserId.isNullOrEmpty()) {
            Toast.makeText(this, "User session error. Please login again.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        initViews()
        observeViewModel()
    }

    private fun initViews() {
        etSearch = findViewById(R.id.etSearch)
        rvUsers = findViewById(R.id.rvUsers)
        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener {
            finish()
        }

        userAdapter = UserAdapter { user ->
            pendingChatUserLogin = user.login
            createChatWithUser(user.id)
        }

        rvUsers.layoutManager = LinearLayoutManager(this)
        rvUsers.adapter = userAdapter

        etSearch.addTextChangedListener { text ->
            searchJob?.cancel()
            val searchText = text?.toString()?.trim() ?: ""

            if (searchText.length >= 2) {
                searchJob = CoroutineScope(Dispatchers.Main).launch {
                    delay(1000)
                    viewModel.searchUsers(searchText)
                }
            } else {
                userAdapter.submitList(emptyList())
            }
        }
    }

    private fun observeViewModel() {
        viewModel.users.observe(this) { users ->
            val currentUserId = prefsManager.getUserId()
            val filteredUsers = if (!currentUserId.isNullOrEmpty()) {
                users.filter { it.id != currentUserId }
            } else {
                users
            }

            if (filteredUsers.isEmpty()) {
                Toast.makeText(this, "No users found", Toast.LENGTH_LONG).show()
            }

            userAdapter.submitList(filteredUsers)
        }

        viewModel.createChatResult.observe(this) { result ->
            result.fold(
                onSuccess = { chatId ->
                    if (chatId.isNotEmpty()) {
                        val intent = Intent(this, ChatActivity::class.java).apply {
                            putExtra("chat_id", chatId)
                            putExtra("user_nick", pendingChatUserLogin ?: "User")
                        }
                        startActivity(intent)
                        finish()
                    }
                },
                onFailure = { error ->
                    Toast.makeText(this, "Chat with user already exists", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    private fun createChatWithUser(userId: String) {
        val currentUserId = prefsManager.getUserId()

        if (currentUserId.isNullOrEmpty() || userId.isEmpty()) {
            Toast.makeText(this, "Invalid user data", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.createChat(listOf(currentUserId, userId))
    }
}