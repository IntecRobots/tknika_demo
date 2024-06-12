package com.intec.template.robot

import android.content.Context
import android.util.Log
import com.ainirobot.coreservice.client.ApiListener
import com.ainirobot.coreservice.client.RobotApi
import com.ainirobot.coreservice.client.speech.SkillApi
import com.ainirobot.coreservice.client.speech.SkillCallback
import com.intec.template.robot.callbacks.ModuleCallback
import com.intec.template.robot.listeners.SpeechRecognitionListener
import javax.inject.Inject

class RobotConnectionService @Inject constructor(
    private val applicationContext: Context,
    val skillApi: SkillApi
) {
    //Variable que inicializa el listener de reconocimiento de voz
    private var speechRecognitionListener: SpeechRecognitionListener? = null

    //Variable que informa cuando la API del robot se ha conectado
    var onRobotApiConnected: (() -> Unit)? = null

    fun connectToRobotApi() {
        RobotApi.getInstance().connectServer(applicationContext, object : ApiListener {
            override fun handleApiDisabled() {
                // Manejar API deshabilitada
            }

            override fun handleApiConnected() {
                Log.d("CONNECT SERVER", "Robot api connected!")
                onRobotApiConnected?.invoke()
                // Conexión exitosa, configurar callbacks
                RobotApi.getInstance().setCallback(ModuleCallback())
            }

            override fun handleApiDisconnected() {
                // Manejar desconexión
            }
        })
    }

    // Definir mSkillCallback aquí
    private val mSkillCallback = object : SkillCallback() {
        override fun onSpeechParResult(s: String) {
            // Implementar lógica aquí
            speechRecognitionListener?.onSpeechPartialResult(s)
        }

        override fun onStart() {
            // Implementar lógica aquí
        }

        override fun onStop() {
            // Implementar lógica aquí
        }

        override fun onVolumeChange(volume: Int) {
            // Implementar lógica aquí
        }

        override fun onQueryEnded(status: Int) {
            // Implementar lógica aquí
        }

        override fun onQueryAsrResult(asrResult: String) {
            // Implementar lógica aquí
            speechRecognitionListener?.onSpeechFinalResult(asrResult)
        }
    }

    fun setSpeechRecognitionListener(listener: SpeechRecognitionListener?) {
        this.speechRecognitionListener = listener
    }

    fun connectToSkillApi() {
        skillApi.connectApi(applicationContext, object : ApiListener {
            override fun handleApiDisabled() {
                // Manejar API deshabilitada
            }

            override fun handleApiConnected() {
                Log.d("SKILLAPI", "Skill api connected!")
                skillApi.registerCallBack(mSkillCallback)
                // Aquí puedes inicializar y configurar RobotManager si es necesario
            }

            override fun handleApiDisconnected() {
                // Manejar desconexión
            }
        })
    }
}
