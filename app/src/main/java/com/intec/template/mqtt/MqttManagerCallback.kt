package com.intec.template.mqtt

import android.util.Log
import androidx.compose.runtime.MutableState
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttMessage


interface MqttMessageListener {
    fun onMessageReceived(topic: String, message: String)
}

class MqttManagerCallback(
    private val connectionState: MutableState<String>,
    private val onMessageArrived: (String) -> Unit,
    private val listener: MqttMessageListener
) : MqttCallbackExtended {

    override fun messageArrived(topic: String, message: MqttMessage) {
        onMessageArrived("Topic: $topic, Message: $message")
        Log.d("MQTT Message Arrived", "$topic: $message")
        val receivedMessage = message?.toString() ?: return
        listener.onMessageReceived(topic, receivedMessage)
        // Aquí puedes manejar la lógica cuando llega un mensaje
    }

    override fun connectComplete(reconnect: Boolean, serverURI: String) {
        connectionState.value = "Connected"
    }

    override fun connectionLost(cause: Throwable?) {
        connectionState.value = "Connection Lost"
        listener.onMessageReceived("Connection Lost", cause?.message ?: "Unknown")
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {
        // Manejar lógica cuando un mensaje ha sido entregado
    }
}