package com.intec.t2o.network

import android.util.Log
import com.intec.template.robot.RobotManager
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONException
import org.json.JSONObject
import java.net.URISyntaxException

interface WebSocketMessageListener {
    fun onWebSocketMessageReceived(message: String)
    fun onWebSocketEventReceived(event: String, message: String)
}

class SocketClient(private val username: String, private val robotManager: RobotManager) {
    private var socket: Socket
    var webSocketListener: WebSocketMessageListener? = null

    init {
        try {
            socket = IO.socket("https://serverbotintec.onrender.com")
        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }
    }

    fun connect() {
        socket.connect()
        setupEventListeners()
    }

    private fun setupEventListeners() {
        socket.on(Socket.EVENT_CONNECT) {
            Log.d("SocketClient", "Connected to server")
            socket.emit("event-message", "$username joined the chat")
        }

        socket.on("event-message") { args ->
            val message = args[0] as String
            webSocketListener?.onWebSocketMessageReceived(message)
        }

        socket.on("object-event") { args ->
            if (args.isNotEmpty()) {
                // Así es como deberías acceder al JSONObject
                val jsonData = args[0] as JSONObject
                try {
                    // Extraer 'event' y 'message' del JSONObject
                    val event = jsonData.getString("event")
                    val message = jsonData.getString("message")
                    webSocketListener?.onWebSocketEventReceived(event, message)
                } catch (e: JSONException) {
                    Log.e("SocketClient", "Error parsing JSON data", e)
                }
            } else {
                Log.e("SocketClient", "Received empty data on 'object-event'")
            }
        }

        socket.on(Socket.EVENT_DISCONNECT) {
            Log.d("SocketClient", "Disconnected from server")
        }
    }

    fun disconnect() {
        socket.disconnect()
    }
}
