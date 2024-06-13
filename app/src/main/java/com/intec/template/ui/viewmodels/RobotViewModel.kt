package com.intec.template.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ainirobot.coreservice.client.Definition
import com.ainirobot.coreservice.client.RobotApi
import com.ainirobot.coreservice.client.listener.CommandListener
import com.ainirobot.coreservice.client.listener.Person
import com.intec.t2o.preferences.PreferencesRepository
import com.intec.template.data.Face
import com.intec.template.data.InteractionState
import com.intec.template.robot.RobotManager
import com.intec.template.robot.SkillApiService
import com.intec.template.robot.data.Place
import com.intec.template.robot.listeners.SpeechRecognitionListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@HiltViewModel
class RobotViewModel @Inject constructor(
    val robotManager: RobotManager,
    private val skillApiService: SkillApiService,
    private val preferencesRepository: PreferencesRepository
) : AndroidViewModel(application = Application()), SpeechRecognitionListener {

    private val _recognizedText = MutableLiveData<String>()
    private val _speechText = MutableStateFlow("")
    val speechText = _speechText.asStateFlow()

    val destinationsList: LiveData<List<String>> = robotManager.getPlaceList()

    // Caras
    val faceTypeState: StateFlow<Face> = robotManager.faceType
    var interactionState = MutableStateFlow(InteractionState.NONE)
    var focusJob: Job? = null

    private val _places = MutableStateFlow<List<Place>>(emptyList())
    val places: StateFlow<List<Place>> get() = _places.asStateFlow()

    var apiKey: String = preferencesRepository.getApiKey()
    var tokenGPT: String = preferencesRepository.getTokenGPT()
    val userId = "Wedge_Antilles"
    var tokenProvider: String = ""
    var callId: String = ""

    enum class NavigationState {
        EyesScreen,
    }



    init {
        Log.d("RobotViewModel", "RobotViewModel Init")
        robotManager.setSpeechRecognitionListener(this)
        configurePersonDetection()

        skillApiService.partialSpeechResult.observeForever { speechResult ->
            Log.d("SkillApiService", "Observed partial speech result: $speechResult")
            viewModelScope.launch {
                _speechText.value = speechResult
            }
        }

        apiKey = preferencesRepository.getApiKey()
        tokenGPT = preferencesRepository.getTokenGPT()
    }

    fun detenerFocus() {
        robotManager.stopFocusFollow()
    }

    fun getPlaces() {
        val places = robotManager.getPlaceList()
        Log.d("mainScreenPlaces", places.toString())
    }

    fun updateTokenGPT(newTokenGPT: String) {
        tokenGPT = newTokenGPT
        preferencesRepository.setTokenGPT(newTokenGPT)
    }



    fun openVideoCall(onSuccess: () -> Unit, onError: (String) -> Unit) {
        Log.d("RobotViewModel", "Estableciendo videollamada")
        if (apiKey.isEmpty()) {
            onError("API key is empty")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                tokenProvider = fetchToken(apiKey, userId)
                if (tokenProvider.isNotEmpty()) {
                    callId = "holajetse"
                    withContext(Dispatchers.Main) {
                        Log.d("RobotViewModel", "Token obtenido exitosamente, iniciando navigateToVideoCallScreen")
                        onSuccess()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Log.e("RobotViewModel", "Error: Failed to fetch token")
                        onError("Failed to fetch token")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("RobotViewModel", "Error: ${e.message ?: "Unknown error"}")
                    onError(e.message ?: "Unknown error")
                }
            }
        }
    }

    suspend fun fetchToken(apiKey: String, userId: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(
                    "https://pronto.getstream.io/api/auth/create-token?" +
                            "api_key=$apiKey&user_id=$userId"
                )
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.inputStream.use { it ->
                    val reader = BufferedReader(InputStreamReader(it))
                    val response = reader.readText()
                    val token = JSONObject(response).getString("token")
                    token
                }
            } catch (e: Exception) {
                Log.e("RobotViewModel", "Error fetching token: ${e.message}")
                ""
            }
        }
    }



    fun registrarPersonListener() {
        focusJob?.cancel()
        focusJob = viewModelScope.launch {
            Log.d("isFollowing", "registrar persona")
            robotManager.registerPersonListener(true)
        }
    }





    private fun configurePersonDetection() {
        Log.d("RobotViewModel", "Configurar detección de personas")
        robotManager.onPersonDetected = { personList ->
            Log.d("RobotViewModel", "Person List: $personList")
            if (personList != null && personList.isNotEmpty()) {
                handlePersonDetection(personList)
            } else {
                Log.d("RobotViewModel", "No person detected")
                startPersonDetection()
            }
        }
    }

    private fun startPersonDetection() {
        Log.d("RobotViewModel", "Iniciar detección de personas")
        val detectedPerson: List<Person>? = robotManager.detectPerson(0)
        Log.d("RobotViewModel", "Detected person: $detectedPerson")
    }

    private fun handlePersonDetection(personList: List<Any>) {
        if (personList.isNotEmpty()) {
            Log.d("RobotViewModel", "Person detected: $personList")
            cambiarFace(Face.HAPPY)
        }
    }

    fun cambiarFace(face: Face) {
        robotManager.faceType.value = face
    }



    override fun onSpeechPartialResult(result: String) {
        _recognizedText.postValue(result)
    }

    override fun onSpeechFinalResult(result: String) {
        _recognizedText.postValue(result)
        if (result.contains("ey peter despierta", true)) {
            registrarPersonListener()
        }
    }

    fun speak(text: String, appendNewLine: Boolean, onSpeakComplete: () -> Unit) {
        robotManager.speak(text, appendNewLine, onSpeakComplete)
    }

    fun irA(destino: String){
        robotManager.goTo(destino)
    }
}

