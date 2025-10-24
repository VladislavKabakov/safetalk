package com.example.messengerchat.data.api

import android.util.Log
import com.example.messengerchat.constants.WebSocketConstants.AUTH_HEADER
import com.example.messengerchat.constants.WebSocketConstants.CONNECT_TIMEOUT
import com.example.messengerchat.constants.WebSocketConstants.WS_URL
import com.example.messengerchat.data.models.WebSocketMessage
import com.google.gson.Gson
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class WebSocketManager(
    private val token: String,
    private val onMessageReceived: (WebSocketMessage) -> Unit,
    private val onConnectionOpened: () -> Unit = {},
    private val onConnectionClosed: () -> Unit = {},
    private val onError: (Exception) -> Unit = {}
) {
    private var webSocket: WebSocketClient? = null
    private val gson = Gson()

    fun connect() {
        val uri = URI(WS_URL)

        val headers = HashMap<String, String>()
        headers[AUTH_HEADER] = "Bearer $token"

        webSocket = object : WebSocketClient(uri, headers) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                Log.d("WebSocket", "Connected with headers")
                onConnectionOpened()
            }

            override fun onMessage(message: String?) {
                message?.let {
                    try {
                        val wsMessage = gson.fromJson(it, WebSocketMessage::class.java)
                        onMessageReceived(wsMessage)
                    } catch (e: Exception) {
                        Log.e("WebSocket", "Error parsing message: ${e.message}")
                    }
                }
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.d("WebSocket", "Disconnected: $reason (Code: $code)")
                onConnectionClosed()
            }

            override fun onError(ex: Exception?) {
                // just pass
            }
        }

        webSocket?.connectionLostTimeout = CONNECT_TIMEOUT
        webSocket?.connect()
    }

    fun sendMessage(message: WebSocketMessage) {
        val json = gson.toJson(message)
        webSocket?.send(json)
    }

    fun disconnect() {
        webSocket?.close()
    }
}

