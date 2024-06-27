package com.example.testar

import android.content.Context
import android.util.Log
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence
import javax.inject.Inject

class MqttManager @Inject constructor(
    private val context: Context,
    private val serverUri: String,
    private val defaultTopic: String,
    private val onMessageReceived: (String, String) -> Unit
) {
    private lateinit var mqttClient: MqttAndroidClient
    private val persistence = MqttDefaultFilePersistence(context.filesDir.path)
    private val topicsToSubscribe = mutableListOf<String>()
    private var messageHandler: ((String, String) -> Unit)? = null

    init {
        connect()
        Log.d("mqtt", "Me he conectado a MQTTMANAGER!!")
    }

    private fun connect() {
        try {
            mqttClient = MqttAndroidClient(context, serverUri, MqttClient.generateClientId(), persistence)
            val options = MqttConnectOptions().apply {
                isAutomaticReconnect = true
                isCleanSession = false
                userName = "intecfull"
                password = "intecfullpassword".toCharArray()
            }

            mqttClient.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                    Log.d("MQTT", "Connection lost")
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    val receivedMessage = message?.toString()
                    if (!receivedMessage.isNullOrEmpty() && topic != null) {
                        messageHandler?.invoke(topic, receivedMessage)
                        Log.d("MQTT", "Message received from $topic: $receivedMessage")
                    }
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    Log.d("MQTT", "Delivery complete")
                }
            })

            mqttClient.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d("MQTT", "Connected successfully")
                    mqttClient.subscribe(defaultTopic, 0)
                    subscribePendingTopics()
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d("MQTT", "Connection failed: ${exception?.message}")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    private fun subscribePendingTopics() {
        for (topic in topicsToSubscribe) {
            try {
                mqttClient.subscribe(topic, 0)
                Log.d("MQTT", "Subscribed to topic: $topic")
            } catch (e: MqttException) {
                e.printStackTrace()
                Log.d("MQTT", "Failed to subscribe to topic: $topic")
            }
        }
        topicsToSubscribe.clear()
    }

    fun subscribeToTopics(topics: List<String>) {
        topicsToSubscribe.addAll(topics)
        if (mqttClient.isConnected) {
            subscribePendingTopics()
        } else {
            Log.d("MQTT", "Client not connected, topics will be subscribed once connected")
        }
    }

    fun publish(topic: String, message: String) {
        try {
            if (mqttClient.isConnected) {
                val mqttMessage = MqttMessage(message.toByteArray())
                mqttClient.publish(topic, mqttMessage)
                Log.d("MQTT", "Message published to $topic: $message")
            } else {
                Log.d("MQTT", "Client not connected, cannot publish message")
            }
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun setOnMessageReceived(handler: (String, String) -> Unit) {
        messageHandler = handler
    }

    fun disconnect() {
        try {
            mqttClient.disconnect()
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

}
