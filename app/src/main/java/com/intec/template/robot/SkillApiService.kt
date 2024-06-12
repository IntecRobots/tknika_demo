package com.intec.template.robot

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.ainirobot.coreservice.client.ApiListener
import com.ainirobot.coreservice.client.speech.SkillApi
import com.ainirobot.coreservice.client.speech.SkillCallback
import javax.inject.Inject

class SkillApiService @Inject constructor(
    private val context: Context
) {
    private var skillApi: SkillApi = SkillApi()

    // LiveData para observar los resultados parciales del reconocimiento de voz
    val partialSpeechResult = MutableLiveData<String>()

    private val apiListener = object : ApiListener {
        override fun handleApiDisabled() {
            Log.d("SkillApiService", "API Disabled")
        }

        override fun handleApiConnected() {
            Log.d("SkillApiService", "API Connected")
            skillApi.registerCallBack(mSkillCallback)
        }

        override fun handleApiDisconnected() {
            Log.d("SkillApiService", "API Disconnected")
        }
    }


    private val mSkillCallback = object : SkillCallback() {
        override fun onSpeechParResult(text: String?) {
            text?.let {
                Log.d("SkillApiService", "Partial Speech Result: $it")
                partialSpeechResult.postValue(it)
            }
        }

        override fun onStart() {
            Log.d("SkillApiService", "Listening Started")
        }

        override fun onStop() {
            Log.d("SkillApiService", "Listening Stopped")
        }

        override fun onVolumeChange(volume: Int) {
            Log.d("SkillApiService", "Volume Changed: $volume")
        }

        override fun onQueryEnded(queryEndStatus: Int) {
            Log.d("SkillApiService", "Query Ended: $queryEndStatus")
        }
    }


    init {
        Log.d("SkillApiService", "Initializing SkillApiService")
        connectApi()
    }

    private fun connectApi() {
        Log.d("SkillApiService", "Connecting to Skill API")
        skillApi.connectApi(context, apiListener)
    }

    // Métodos adicionales según sea necesario...
}