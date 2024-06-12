package com.intec.template.mqtt

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence


class MqttManager(
    private val context: Context,
    private val callback: MqttManagerCallback,
    private var mqttConfig: MQTTConfig,
    application: Application
) : AndroidViewModel(application) {

    private var mqttAndroidClient: MqttAndroidClient
    private val app = getApplication<Application>()
    private val persistence = MqttDefaultFilePersistence(app.filesDir.path)

        init {
            mqttAndroidClient =
                MqttAndroidClient(context, mqttConfig.SERVER_URI, mqttConfig.client_id, persistence)
            mqttAndroidClient.setCallback(callback)
        }

        // State para mensajes entrantes
        private val _incomingMessages = mutableStateOf(emptyList<String>())
        val incomingMessages: State<List<String>> get() = _incomingMessages

        // Añadir un mensaje a la lista de mensajes recibidos
        private fun addIncomingMessage(message: String) {
            Log.d("MqttViewModel", "Nuevo mensaje recibido: $message")
            val currentMessages = _incomingMessages.value
            _incomingMessages.value = currentMessages + message
        }

        fun connect(
            onSuccess: (() -> Unit)? = null,
            onFailure: ((Throwable?) -> Unit)? = null
        ) {

            val mqttConnectOptions = MqttConnectOptions()
            mqttConnectOptions.userName = mqttConfig.user
            Log.d("MQTTManager connect", "User: ${mqttConfig.user}")
            mqttConnectOptions.password = mqttConfig.password.toCharArray()
            Log.d("MQTTManager connect", "Pwd: ${mqttConfig.password}")
            mqttConnectOptions.isAutomaticReconnect = true
            Log.d(
                "MQTTManager connect",
                "AutomaticReconnect: ${mqttConnectOptions.isAutomaticReconnect}"
            )
            mqttConnectOptions.isCleanSession = false
            Log.d("MQTTManager connect", "CleanSession: ${mqttConnectOptions.isCleanSession}")

            try {
                Log.d("Connect", "Trying to connect")
                Log.d("MQTTManager", "Connecting to broker: ${mqttConfig.SERVER_URI}")
                Log.d("MQTTManager", "Client id: ${mqttConfig.client_id}")
                Log.d("MQTTManager", "User: ${mqttConfig.user}")
                Log.d("MQTTManager", "Pwd: ${mqttConfig.password}")
                mqttAndroidClient.connect(mqttConnectOptions, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        // We are connected
                        Log.d("MQTT CONNECTION", "onSuccess")
                        val disconnectedBufferOptions = DisconnectedBufferOptions()
                        disconnectedBufferOptions.isBufferEnabled = true
                        disconnectedBufferOptions.bufferSize = 100
                        disconnectedBufferOptions.isPersistBuffer = false
                        disconnectedBufferOptions.isDeleteOldestMessages = false
                        mqttAndroidClient.setBufferOpts(disconnectedBufferOptions)

                        onSuccess?.invoke()
                        //addToHistory("Connected to $serverUri")

                        subscribeToAllTopics(getTopics())
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable) {
                        // Something went wrong e.g. connection timeout or firewall problems
                        Log.d("MQTT CONNECTION", "onFailure: ${exception.message}")
                        onFailure?.invoke(exception)
                        //addToHistory("Failed to connect: $serverUri")
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("MQTT CONNECTION", "ERROR: ${e.message}")
            }
        }

        fun actualizarConfiguracion(nuevaConfig: MQTTConfig) {
            Log.d("MQTTManager", "Actualizando configuración: $nuevaConfig")

            if (isConnected()) {
                disconnect()
            }

            mqttConfig = nuevaConfig
            mqttAndroidClient.unregisterResources()
            mqttAndroidClient.close()
            mqttAndroidClient =
                MqttAndroidClient(context, mqttConfig.SERVER_URI, mqttConfig.client_id, persistence)
            mqttAndroidClient.setCallback(callback)
            mqttAndroidClient.registerResources(context)

            // Reconectar con la nueva configuración
            connect()
        }

        fun disconnect() {
            try {
                mqttAndroidClient.disconnect(null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        Log.d("MQTT DISCONNECTION", "onSuccess")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.d("MQTT DISCONNECTION", "onFailure")
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("MQTT DISCONNECTION", "ERROR: ${e.message}")
            }
        }

        private fun isConnected(): Boolean {
            // Verificar si actualmente está conectado
            return mqttAndroidClient?.isConnected ?: false
        }

        fun getTopics(): MutableList<String> {
            return resumeTopics()
        }

        fun resumeTopics(): MutableList<String> {
            var listaTopics: MutableList<String> = mutableListOf()

            listaTopics.add(0, "robot/nav_cmds/go_charger")
            listaTopics.add(1, "robot/status/battery")
            listaTopics.add(2, "robot/nav_pub/current_pose")
            listaTopics.add(3, "robot/nav_pub/status")
            listaTopics.add(4, "robot/nav_pub/nav_error")
            listaTopics.add(5, "robot/simulation/nav_pub/nav_error")
            listaTopics.add(6, "robot/simulation/events/person_pushing")
            listaTopics.add(7, "robot/nav_pub/current_config")
            listaTopics.add(8, "robot/simulation/nav_pub/current_config")
            listaTopics.add(9, "robot/simulation/nav_pub/stored_locations")
            listaTopics.add(10, "robot/simulation/events/bumper")
            listaTopics.add(11, "robot/simulation/status/error")
            listaTopics.add(12, "robot/voice_cmds/text_to_speech")
            listaTopics.add(13, "robot/voice_recog/speech_to_text")
            listaTopics.add(14, "robot/voice_recog/response")
            listaTopics.add(15, "robot/voice_info/sound_record")
            listaTopics.add(16, "robot/human_cmd/cmd")
            listaTopics.add(17, "robot/simulation/ai_vision/new_person")
            listaTopics.add(18, "robot/nav_cmds/go_to")
            listaTopics.add(19, "robot/nav_cmds/stop_navigation")
            listaTopics.add(20, "robot/voice_cmds/question")
            listaTopics.add(21, "robot/welcome_cmd")
            listaTopics.add(22, "robot/focus")
            listaTopics.add(23, "robot/unfocus")
            listaTopics.add(24, "robot/move_forward")
            listaTopics.add(25, "robot/nav_cmds/pause_navigation")
            listaTopics.add(26, "robot/nav_cmds/resume_navigation")
            listaTopics.add(27, "robot/stop_stt")
            listaTopics.add(28, "robot/voice_cmds/question_si_no")
            listaTopics.add(29, "robot/faceType")
            listaTopics.add(30, "robot/wake_up")
            listaTopics.add(31, "robot/interactionState")
            listaTopics.add(32, "robot/nav_cmds/driving_finished")
            listaTopics.add(33, "robot/voice_cmds/remove_question")
            listaTopics.add(34, "robot/open_homescreen")
            listaTopics.add(35, "zigbee2mqtt/Cerradura/right/set")
            listaTopics.add(36, "zigbee2mqtt/Cerradura/left/set")
            listaTopics.add(37, "zigbee2mqtt/Pulsador/action")
            listaTopics.add(38, "/api/robot/control")
            return listaTopics
        }

        fun subscribeToAllTopics(topicList: MutableList<String>) {
            while (topicList.isNotEmpty()) {
                var currentTopic = topicList[0]
                //addToHistory("Subscribing to $currentTopic")
                subscribeToTopic(currentTopic)
                topicList.removeAt(0)
            }
        }

        fun subscribeToTopic(currentTopic: String) {
            Log.d("SUBSCRIPTION", "SUBSCRIBING TO TOPIC!")

            try {
                mqttAndroidClient.subscribe(currentTopic, 0, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        Log.d("SUBSCRIPTION", "SUBSCRIPTION to $currentTopic SUCCESS!")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.d("SUBSCRIPTION", "SUBSCRIPTION to $currentTopic FAILURE!")
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("SUBSCRIPTION", "$currentTopic ERROR: ${e.message}")
            }
        }

        fun publishMessage(topic: String, mensage: String, qos: Int = 1) {

            try {
                val message = MqttMessage()
                message.payload = mensage.toByteArray()
                //Log.d("PUBLISH", (message.payload).toString())
                if (mqttAndroidClient.isConnected) {
                    Log.d("PUBLISH", "SENDING MESSAGE")
                    mqttAndroidClient.publish(topic, message)
                    if (!mqttAndroidClient.isConnected) {
                        Log.d(
                            "PUBLISH",
                            mqttAndroidClient.bufferedMessageCount.toString() + " messages in buffer."
                        )
                        //addToHistory(mqttAndroidClient.bufferedMessageCount.toString() + " messages in buffer.")
                    }
                } else {
                    //Snackbar.make(findViewById(android.R.id.content), "Not connected", Snackbar.LENGTH_SHORT).setAction("Action", null).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("PUBLISH", "ERROR: ${e.message}")
            }
        }


    }