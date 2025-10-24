package com.example.messengerchat.ui.chat

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.messengerchat.R
import com.example.messengerchat.data.api.RetrofitClient
import com.example.messengerchat.data.preferences.PreferencesManager
import com.example.messengerchat.ui.auth.LoginActivity
import com.example.messengerchat.ui.search.UserSearchActivity
import com.example.messengerchat.ui.viewmodels.ChatViewModel

class ChatListActivity : AppCompatActivity() {

    private lateinit var viewModel: ChatViewModel
    private lateinit var prefsManager: PreferencesManager
    private lateinit var chatAdapter: ChatAdapter

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabNewChat: FloatingActionButton
    private lateinit var tvEmptyState: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list_fixed)

        prefsManager = PreferencesManager(this)

        val token = prefsManager.getToken()
        if (!token.isNullOrEmpty()) {
            RetrofitClient.setAuthToken(token)
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]

        initViews()
        observeViewModel()

        viewModel.loadChats()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.rvChats)
        fabNewChat = findViewById(R.id.fabNewChat)
        tvEmptyState = findViewById(R.id.tvEmptyState)

        chatAdapter = ChatAdapter { chat ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("chat_id", chat.id)
            intent.putExtra("user_nick", chat.userNick)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = chatAdapter

        fabNewChat.setOnClickListener {
            startActivity(Intent(this, UserSearchActivity::class.java))
        }
    }

    private fun observeViewModel() {
        viewModel.chats.observe(this) { chats ->
            if (chats.isEmpty()) {
                tvEmptyState.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                tvEmptyState.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                chatAdapter.submitList(chats)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_chat_list, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                prefsManager.clearAll()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadChats()
    }
}