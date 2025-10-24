package com.example.messengerchat.ui.chat

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.messengerchat.R
import com.example.messengerchat.data.models.Message
import com.example.messengerchat.utils.Constants.DATE_FORMAT
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(
    private val currentUserId: String,
    private val onMessageLongClick: (Message) -> Unit,
    private val onFileClick: (Message) -> Unit
) : ListAdapter<Message, MessageAdapter.MessageViewHolder>(MessageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message_extended, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val llMessage: LinearLayout = itemView.findViewById(R.id.llMessage)
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val tvEdited: TextView = itemView.findViewById(R.id.tvEdited)
        private val ivFileIcon: ImageView = itemView.findViewById(R.id.ivFileIcon)
        private val tvFileName: TextView = itemView.findViewById(R.id.tvFileName)
        private val llFileAttachment: LinearLayout = itemView.findViewById(R.id.llFileAttachment)


        fun bind(message: Message) {
            if (message.isDeleted) {
                tvMessage.text = "This message was deleted"
                tvMessage.setTextColor(Color.GRAY)
                tvMessage.setTypeface(null, Typeface.ITALIC)
                tvEdited.visibility = View.GONE
                llFileAttachment.visibility = View.GONE
            } else {
                if (message.type == 2) {
                    tvMessage.visibility = View.GONE
                    llFileAttachment.visibility = View.VISIBLE

                    tvFileName.text = message.text.take(8)

                    ivFileIcon.setImageResource(android.R.drawable.ic_menu_save)

                    llFileAttachment.setOnClickListener {
                        onFileClick(message)
                    }
                } else {
                    tvMessage.text = message.text
                    tvMessage.setTextColor(Color.BLACK)
                    tvMessage.setTypeface(null, Typeface.NORMAL)

                    if (message.isEdited) {
                        tvEdited.visibility = View.VISIBLE
                        tvEdited.text = "edited"
                    } else {
                        tvEdited.visibility = View.GONE
                    }
                }
            }

            try {
                val date = when {
                    message.createdAt.contains(".") -> {
                        val dotIndex = message.createdAt.indexOf(".")
                        val zIndex = message.createdAt.indexOf("Z")
                        val beforeDot = message.createdAt.take(dotIndex)
                        val afterDot = message.createdAt.substring(dotIndex + 1, zIndex)
                        val millis = afterDot.take(3).padEnd(3, '0')
                        val cleanDateStr = "$beforeDot.${millis}Z"

                        val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                        fmt.timeZone = TimeZone.getTimeZone("UTC")
                        fmt.parse(cleanDateStr)
                    }

                    else -> {
                        val fmt = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
                        fmt.timeZone = TimeZone.getTimeZone("UTC")
                        fmt.parse(message.createdAt)
                    }
                }

                if (date != null) {
                    val outputFormat = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
                    outputFormat.timeZone = TimeZone.getDefault()
                    tvTime.text = outputFormat.format(date)
                } else {
                    tvTime.text = "Invalid date"
                }
            } catch (e: Exception) {
                tvTime.text = "Invalid date"
            }

            val params = llMessage.layoutParams as FrameLayout.LayoutParams
            if (message.fromUser == currentUserId) {
                params.gravity = Gravity.END
                llMessage.setBackgroundResource(R.drawable.bg_message_sent)
            } else {
                params.gravity = Gravity.START
                llMessage.setBackgroundResource(R.drawable.bg_message_received)
            }
            llMessage.layoutParams = params

            if (!message.isDeleted || message.fromUser == currentUserId) {
                itemView.setOnLongClickListener {
                    onMessageLongClick(message)
                    true
                }
            } else {
                itemView.setOnLongClickListener(null)
            }
        }
    }

    class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }
}