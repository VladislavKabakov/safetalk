package com.example.messengerchat.ui.chat

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.messengerchat.R
import com.example.messengerchat.constants.HttpClientConstants.BASE_URL
import com.example.messengerchat.data.api.ChatApiService
import com.example.messengerchat.data.api.RetrofitClient
import com.example.messengerchat.data.api.WebSocketManager
import com.example.messengerchat.data.models.*
import com.example.messengerchat.data.preferences.PreferencesManager
import com.example.messengerchat.data.repository.ChatRepository
import com.example.messengerchat.ui.viewmodels.MessageViewModel
import com.example.messengerchat.utils.Constants.DATE_FORMAT
import com.example.messengerchat.utils.MessageCrypto
import com.example.messengerchat.utils.NotificationHelper
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {
    private val repository = ChatRepository()

    private lateinit var viewModel: MessageViewModel
    private lateinit var prefsManager: PreferencesManager
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var webSocketManager: WebSocketManager
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var chatApiService: ChatApiService

    private lateinit var recyclerView: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var btnAttach: ImageButton
    private lateinit var tvUserName: TextView
    private lateinit var llEditMessage: LinearLayout
    private lateinit var tvEditingMessage: TextView
    private lateinit var btnCancelEdit: ImageButton
    private lateinit var llFileTransfers: LinearLayout
    private lateinit var btnBack: ImageButton

    private var chatId: String = ""
    private var userNick: String = ""
    private var currentUserId: String = ""
    private var currentLogin: String = ""
    private var otherUserId: String = ""
    private var editingMessage: Message? = null

    companion object {
        private const val CHAT_ACTIVITY = "ChatActivity"
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(this, "Notifications disabled", Toast.LENGTH_SHORT).show()
        }
    }

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            uploadFile(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_complete)

        prefsManager = PreferencesManager(this)
        notificationHelper = NotificationHelper(this)

        chatId = intent.getStringExtra("chat_id") ?: ""
        userNick = intent.getStringExtra("user_nick") ?: "Unknown User"

        lifecycleScope.launch {
            otherUserId = repository.searchUsers(userNick).body()
                ?.find { it.login == userNick }
                ?.id.orEmpty()
        }

        currentUserId = prefsManager.getUserId() ?: ""
        currentLogin = prefsManager.getUserLogin() ?: ""

        if (chatId.isBlank()) {
            Toast.makeText(this, "Invalid chat", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        if (currentUserId.isBlank()) {
            Toast.makeText(this, "Session error. Please login again.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Log.d(CHAT_ACTIVITY, "Chat opened - ChatID: $chatId, UserNick: $userNick, CurrentUserID: $currentUserId")

        viewModel = ViewModelProvider(this)[MessageViewModel::class.java]

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(RetrofitClient.httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        chatApiService = retrofit.create(ChatApiService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        initViews()
        observeViewModel()
        initWebSocket()

        viewModel.loadChatHistory(chatId)
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.rvMessages)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        btnAttach = findViewById(R.id.btnAttach)
        tvUserName = findViewById(R.id.tvUserName)
        llEditMessage = findViewById(R.id.llEditMessage)
        tvEditingMessage = findViewById(R.id.tvEditingMessage)
        btnCancelEdit = findViewById(R.id.btnCancelEdit)
        llFileTransfers = findViewById(R.id.llFileTransfers)
        btnBack = findViewById(R.id.btnBack)

        tvUserName.text = userNick

        messageAdapter = MessageAdapter(
            currentUserId = currentUserId,
            onMessageLongClick = { message ->
                showMessageOptions(message)
            },
            onFileClick = { message ->
                handleFileDownload(message)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        recyclerView.adapter = messageAdapter

        btnBack.setOnClickListener {
            finish()
        }

        btnSend.setOnClickListener {
            val messageText = etMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                if (editingMessage != null) {
                    editMessage(editingMessage!!, messageText)
                } else {
                    sendMessage(messageText)
                }
                etMessage.text.clear()
            }
        }

        btnAttach.setOnClickListener {
            filePickerLauncher.launch("*/*")
        }

        btnCancelEdit.setOnClickListener {
            cancelEditing()
        }
    }

    private fun uploadFile(uri: Uri) {
        lifecycleScope.launch {
            try {
                val progressDialog = AlertDialog.Builder(this@ChatActivity)
                    .setMessage("Uploading file...")
                    .setCancelable(false)
                    .create()
                progressDialog.show()

                val file = getFileFromUri(uri)
                if (file == null) {
                    progressDialog.dismiss()
                    Toast.makeText(this@ChatActivity, "Failed to get file", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val requestFile = file.asRequestBody(getContentType(uri))
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                val response = chatApiService.uploadFile(
                    userId = otherUserId,
                    chatId = chatId,
                    file = body
                )

                progressDialog.dismiss()

                if (response.isSuccessful) {
                    val fileUploadResponse = response.body()
                    Log.d(CHAT_ACTIVITY, "File uploaded successfully: ${fileUploadResponse?.filePath}")

                    val fileMessage = Message(
                        id = System.currentTimeMillis().toInt(),
                        text = file.name,
                        fromUser = currentUserId,
                        toUser = otherUserId,
                        chatId = chatId,
                        createdAt = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date()),
                        type = 2,
                    )

                    viewModel.addMessage(fileMessage)

                    Toast.makeText(this@ChatActivity, "File sent successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e(CHAT_ACTIVITY, "File upload failed: ${response.code()}")
                    Toast.makeText(this@ChatActivity, "Failed to upload file", Toast.LENGTH_SHORT).show()
                }

                file.delete()

            } catch (e: Exception) {
                Log.e(CHAT_ACTIVITY, "File upload error", e)
                Toast.makeText(this@ChatActivity, "Error uploading file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val fileName = getFileName(uri)
            val tempFile = File(cacheDir, fileName)

            FileOutputStream(tempFile).use { outputStream ->
                inputStream.use { input ->
                    input.copyTo(outputStream)
                }
            }

            tempFile
        } catch (e: Exception) {
            Log.e(CHAT_ACTIVITY, "Error getting file from URI", e)
            null
        }
    }

    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (columnIndex != -1) {
                        result = it.getString(columnIndex)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1) {
                result = result?.substring(cut!! + 1)
            }
        }
        return result ?: "file_${System.currentTimeMillis()}"
    }

    private fun getContentType(uri: Uri): okhttp3.MediaType? {
        val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
        return mimeType.toMediaTypeOrNull()
    }

    private fun handleFileDownload(message: Message) {
        var fileId = message.text

        if (fileId.isBlank()) {
            Toast.makeText(this, "Invalid file ID", Toast.LENGTH_SHORT).show()
            return
        }

        fileId = if (message.fromUser == currentUserId) {
            MessageCrypto.decrypt(currentLogin, currentUserId, fileId)
        } else {
            MessageCrypto.decrypt(userNick, otherUserId, fileId)
        }

        AlertDialog.Builder(this)
            .setTitle("Download File")
            .setMessage("Do you want to download this file?")
            .setPositiveButton("Download") { _, _ ->
                downloadFile(fileId, message)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun downloadFile(fileId: String, message: Message) {
        lifecycleScope.launch {
            try {
                val progressDialog = AlertDialog.Builder(this@ChatActivity)
                    .setMessage("Downloading file...")
                    .setCancelable(false)
                    .create()
                progressDialog.show()

                val response = chatApiService.downloadFile(fileId)

                if (response.isSuccessful) {
                    response.body()?.let { responseBody ->
                        val fileName = message.text

                        saveFileToDownloads(responseBody, fileName)

                        progressDialog.dismiss()
                        Toast.makeText(
                            this@ChatActivity,
                            "File downloaded: $fileName",
                            Toast.LENGTH_SHORT
                        ).show()
                    } ?: run {
                        progressDialog.dismiss()
                        Toast.makeText(
                            this@ChatActivity,
                            "Empty response",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    progressDialog.dismiss()
                    Toast.makeText(
                        this@ChatActivity,
                        "Download failed: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(CHAT_ACTIVITY, "Download error", e)
                Toast.makeText(
                    this@ChatActivity,
                    "Download error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun saveFileToDownloads(
        responseBody: okhttp3.ResponseBody,
        fileName: String
    ) {
        try {
            val downloadsDir = getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)

            file.outputStream().use { outputStream ->
                responseBody.byteStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            Log.d(CHAT_ACTIVITY, "File saved to: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e(CHAT_ACTIVITY, "Error saving file", e)
            throw e
        }
    }

    private fun observeViewModel() {
        viewModel.messages.observe(this) { messages ->
            Log.d(CHAT_ACTIVITY, "Messages updated: ${messages.size} messages")

            val sortedMessages = messages.sortedBy { message ->
                parseMessageDate(message.createdAt)
            }.map { message ->
                if (message.fromUser == currentUserId) {
                    message.copy(
                        text = MessageCrypto.decrypt(currentLogin, currentUserId, message.text)
                    )
                } else {
                    message.copy(
                        text = MessageCrypto.decrypt(userNick, otherUserId, message.text)
                    )
                }
            }

            messageAdapter.submitList(sortedMessages)
            if (sortedMessages.isNotEmpty()) {
                recyclerView.scrollToPosition(sortedMessages.size - 1)
            }
        }
    }

    private fun initWebSocket() {
        val token = prefsManager.getToken()
        if (token.isNullOrBlank()) {
            Log.e(CHAT_ACTIVITY, "Token is null or blank")
            Toast.makeText(this, "Authentication error", Toast.LENGTH_SHORT).show()
            return
        }

        webSocketManager = WebSocketManager(
            token = token,
            onMessageReceived = { wsMessage ->
                Log.d(CHAT_ACTIVITY, "WebSocket message received: $wsMessage")
                runOnUiThread {
                    when (wsMessage.type) {
                        1, null -> handleNewMessage(wsMessage) // null for backward compatibility
                        else -> Log.w(CHAT_ACTIVITY, "Unknown message type: ${wsMessage.type}")
                    }
                }
            },
            onConnectionOpened = {
                runOnUiThread {
                    Log.d(CHAT_ACTIVITY, "WebSocket connected")
                    Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show()
                }
            },
            onConnectionClosed = {
                runOnUiThread {
                    Log.d(CHAT_ACTIVITY, "WebSocket disconnected")
                }
            },
            onError = { error ->
                runOnUiThread {
                    Log.e(CHAT_ACTIVITY, "WebSocket error", error)
                    Toast.makeText(this, "Connection error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )

        webSocketManager.connect()
    }

    private fun handleNewMessage(wsMessage: WebSocketMessage) {
        if (wsMessage.chatId != chatId) {
            Log.d(CHAT_ACTIVITY, "Message for different chat: ${wsMessage.chatId}")
            return
        }

        val message = Message(
            id = wsMessage.messageId ?: System.currentTimeMillis().toInt(),
            text = MessageCrypto.decrypt(userNick, otherUserId, wsMessage.message ?: ""),
            fromUser = wsMessage.fromUser ?: "",
            toUser = if (wsMessage.fromUser == currentUserId) "" else currentUserId,
            chatId = chatId,
            createdAt = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date())
        )

        viewModel.addMessage(message)

        if (wsMessage.fromUser != currentUserId && !wsMessage.fromUser.isNullOrBlank()) {
            notificationHelper.showMessageNotification(
                userNick,
                wsMessage.message ?: "",
                chatId
            )
        }
    }

    private fun sendMessage(text: String) {
        Log.d(CHAT_ACTIVITY, "Sending message: $text to chat: $chatId")

        if (chatId.isBlank() || currentUserId.isBlank()) {
            Toast.makeText(this, "Cannot send message: Invalid chat data", Toast.LENGTH_SHORT).show()
            return
        }

        val wsMessage = WebSocketMessage(
            chatId = chatId,
            message = MessageCrypto.encrypt(currentLogin, currentUserId, text),
            fromUser = currentUserId
        )

        try {
            webSocketManager.sendMessage(wsMessage)

            val message = Message(
                id = System.currentTimeMillis().toInt(),
                text = text,
                fromUser = currentUserId,
                toUser = "", // will be set by server
                chatId = chatId,
                createdAt = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date())
            )
            viewModel.addMessage(message)

            Log.d(CHAT_ACTIVITY, "Message sent successfully")
        } catch (e: Exception) {
            Log.e(CHAT_ACTIVITY, "Failed to send message", e)
        }
    }

    private fun parseMessageDate(dateStr: String): Long {
        return try {
            val date = when {
                dateStr.contains(".") -> {
                    val dotIndex = dateStr.indexOf(".")
                    val zIndex = dateStr.indexOf("Z")
                    if (dotIndex in 1..<zIndex) {
                        val beforeDot = dateStr.take(dotIndex)
                        val afterDot = dateStr.substring(dotIndex + 1, zIndex)
                        val millis = afterDot.take(3).padEnd(3, '0')
                        val cleanDateStr = "$beforeDot.${millis}Z"

                        val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                        fmt.timeZone = TimeZone.getTimeZone("UTC")
                        fmt.parse(cleanDateStr)
                    } else {
                        null
                    }
                }

                else -> {
                    val fmt = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
                    fmt.timeZone = TimeZone.getTimeZone("UTC")
                    fmt.parse(dateStr)
                }
            }

            date?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            Log.e(CHAT_ACTIVITY, "Error parsing date: $dateStr, error: ${e.message}")
            System.currentTimeMillis()
        }
    }

    private fun showMessageOptions(message: Message) {
        val options = mutableListOf<String>()

        if (message.fromUser == currentUserId && !message.isDeleted) {
            options.add("Edit")
            options.add("Delete for everyone")
        }

        options.add("Copy")

        if (options.isEmpty()) return

        AlertDialog.Builder(this)
            .setTitle("Message options")
            .setItems(options.toTypedArray()) { _, which ->
                when (options[which]) {
                    "Edit" -> startEditingMessage(message)
                    "Delete for everyone" -> deleteMessage(message)
                    "Copy" -> copyMessage(message)
                }
            }
            .show()
    }

    private fun startEditingMessage(message: Message) {
        editingMessage = message
        llEditMessage.visibility = View.VISIBLE
        tvEditingMessage.text = message.text
        etMessage.setText(message.text)
        etMessage.setSelection(message.text.length)
        etMessage.requestFocus()
    }

    private fun cancelEditing() {
        editingMessage = null
        llEditMessage.visibility = View.GONE
        etMessage.text.clear()
    }

    private fun editMessage(message: Message, newText: String) {
        lifecycleScope.launch {
            try {
                val request =
                    EditMessageRequest(message.id, MessageCrypto.encrypt(currentLogin, currentUserId, newText))
                val response = chatApiService.editMessage(request)

                if (response.isSuccessful) {
                    val editResponse = response.body()
                    viewModel.updateMessage(message.id, newText, editResponse?.editedAt ?: "")

                    cancelEditing()
                } else {
                    Toast.makeText(this@ChatActivity, "Failed to edit message", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(CHAT_ACTIVITY, "Edit message error", e)
            }
        }
    }

    private fun deleteMessage(message: Message) {
        val dialogMessage = "Delete this message for everyone?"

        AlertDialog.Builder(this)
            .setTitle("Delete Message")
            .setMessage(dialogMessage)
            .setPositiveButton("Delete") { _, _ ->
                performDeleteMessage(message)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performDeleteMessage(message: Message) {
        lifecycleScope.launch {
            try {
                val response = chatApiService.deleteMessage(messageId = message.id)

                if (response.isSuccessful) {
                    viewModel.deleteMessage(message.id)

                    Snackbar.make(recyclerView, "Message deleted", Snackbar.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ChatActivity, "Failed to delete message", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(CHAT_ACTIVITY, "Delete message error", e)
            }
        }
    }

    private fun copyMessage(message: Message) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("Message", message.text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Message copied", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            webSocketManager.disconnect()
        } catch (e: Exception) {
            Log.e(CHAT_ACTIVITY, "Error disconnecting WebSocket", e)
        }
    }
}