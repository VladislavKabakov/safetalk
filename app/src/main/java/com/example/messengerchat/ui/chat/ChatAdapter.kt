package com.example.messengerchat.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.messengerchat.R
import com.example.messengerchat.data.models.Chat

class ChatAdapter(
    private val onChatClick: (Chat) -> Unit
) : ListAdapter<Chat, ChatAdapter.ChatViewHolder>(ChatDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        private val tvLastMessage: TextView = itemView.findViewById(R.id.tvLastMessage)

        fun bind(chat: Chat) {
            val userName = when {
                chat.userNick.isNotEmpty() -> chat.userNick
                else -> "Unknown User"
            }
            tvUserName.text = userName

            val lastMessage = when {
                chat.lastMessage.isNullOrEmpty() -> "Tap to start chatting"
                chat.lastMessage == "No messages yet" -> "Tap to start chatting"
                else -> chat.lastMessage
            }
            tvLastMessage.text = lastMessage

            tvLastMessage.alpha = if (chat.lastMessage.isNullOrEmpty() ||
                chat.lastMessage == "No messages yet"
            ) 0.6f else 1.0f

            itemView.setOnClickListener {
                onChatClick(chat)
            }
        }
    }

    class ChatDiffCallback : DiffUtil.ItemCallback<Chat>() {
        override fun areItemsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            return oldItem == newItem
        }
    }
}