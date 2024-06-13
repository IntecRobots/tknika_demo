package com.intec.template.robot

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.ainirobot.coreservice.client.Definition
import com.ainirobot.coreservice.client.RobotApi
import com.ainirobot.coreservice.client.actionbean.Pose
import com.ainirobot.coreservice.client.listener.ActionListener
import com.ainirobot.coreservice.client.listener.CommandListener
import com.ainirobot.coreservice.client.listener.Person
import com.ainirobot.coreservice.client.listener.TextListener
import com.ainirobot.coreservice.client.person.PersonApi
import com.ainirobot.coreservice.client.person.PersonListener
import com.ainirobot.coreservice.client.speech.entity.TTSEntity
import com.intec.template.data.Face
import com.intec.template.robot.data.Place
import com.intec.template.robot.listeners.NavigationListener
import com.intec.template.robot.listeners.SpeechRecognitionListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RobotManager @Inject constructor(
    private val robotConnectionService: RobotConnectionService
) {
    private var speechRecognitionListener: SpeechRecognitionListener? = null

    private val navigationListeners = mutableListOf<NavigationListener>()

    private lateinit var commandListener: CommandListener
    private lateinit var personListener: PersonListener
    private lateinit var actionListener: ActionListener
    private lateinit var placesListener: CommandListener
    private lateinit var headListener: CommandListener
    private lateinit var textListener: TextListener

    val placesList: MutableList<Place> = mutableListOf()
    private val _destinationsList = MutableLiveData(listOf<String>())
    var onPersonDetected: ((List<Person>?) -> Unit)? = null

    private val personApi = PersonApi.getInstance()
    var faceType = MutableStateFlow(Face.NEUTRAL)

    init {
        setupActionListener()
        setupCommandListener()
        setupPersonListener()
        setupPlacesListener()
        setupHeadListener()
        setupTextListener()
        robotConnectionService.onRobotApiConnected = {
            getPlaceList()
        }
        robotConnectionService.connectToRobotApi()
    }

    fun addNavigationListener(listener: NavigationListener) {
        navigationListeners.add(listener)
    }

    fun removeNavigationListener(listener: NavigationListener) {
        navigationListeners.remove(listener)
    }

    fun unregisterPersonListener() {
        Log.d("RobotMan PersonListener", "Unregistering Person")
        personApi.unregisterPersonListener(personListener)
    }

    var personList : List<Person>? = null
    var personListFilter : List<Person>? = null
    private val _isFollowing = MutableStateFlow(false)
    val isFollowing: StateFlow<Boolean> = _isFollowing.asStateFlow()

    suspend fun registerPersonListener(primeraVez: Boolean) {
        Log.d("RobotMan PersonListener", "Registering Person")
        if (primeraVez){

            val creandoLista = personApi.registerPersonListener(personListener)
            Log.d("isFollowing", "$creandoLista")
        }
        personList = personApi.getAllPersons()
        Log.d("isFollowing", personList.toString())
        personListFilter = personList?.filter { it.isWithFace }
        Log.d("isFollowingFilter", personListFilter.toString())

        if (personListFilter != null) {
            if( personListFilter!!.isNotEmpty()) {

                Log.d("isFolowingFiltrado", "id ${personListFilter!!.last().id}")



                if (!_isFollowing.value){
                    startFocusFollow(personListFilter!!.last().id) {

                    }
                }
                Log.d("isFollowing", "Person registered true")

                delay(500)
                registerPersonListener(false)

            }else{
                Log.d("isFollowing", "Error registering Person false")
                delay(500)
                registerPersonListener(true)
            }
        }else{
            Log.d("isFollowing", "Error registering Person false")
            delay(500)
            registerPersonListener(true)
        }
    }

    fun detectPerson(faceId: Int): List<Person>? {

        return personApi.allPersons
    }

    fun stopDetection() {
        stopFocusFollow()
        unregisterPersonListener()
    }

    fun listening(listen: Boolean) {
        if (listen){
            robotConnectionService.skillApi.setRecognizeMode(listen)
            robotConnectionService.skillApi.setRecognizable(listen)
        }else{
            robotConnectionService.skillApi.setRecognizable(listen)
        }
    }

    private fun setupActionListener() {
        actionListener = object : ActionListener() {
            @Deprecated("Deprecated in Java")
            override fun onStatusUpdate(status: Int, data: String) {
                when (status) {
                    Definition.STATUS_NAVI_AVOID -> navigationListeners.forEach { it.onRouteBlocked() }
                    Definition.STATUS_NAVI_AVOID_END -> navigationListeners.forEach { it.onObstacleDisappeared() }
                    Definition.STATUS_START_NAVIGATION -> navigationListeners.forEach { it.onNavigationStarted() }
                    Definition.STATUS_TRACK_TARGET_SUCCEED -> Log.d("RobotManager", "Target tracking succeeded")
                    Definition.STATUS_GUEST_APPEAR -> Log.d("RobotManager", "Guest appeared")
                    Definition.STATUS_GUEST_LOST -> Log.d("RobotManager", "Guest lost")
                    Definition.STATUS_GUEST_FARAWAY -> Log.d("RobotManager", "Guest faraway")
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(errorCode: Int, errorString: String?) {
                Log.e("RobotManager", "Error en el seguimiento: $errorString")
            }

            @Deprecated("Deprecated in Java")
            override fun onResult(status: Int, responseString: String?) {
                Log.d("RobotManager", "Respuesta del seguimiento: $responseString")
            }
        }
    }

    private fun setupCommandListener() {
        commandListener = object : CommandListener() {
            override fun onResult(result: Int, message: String, extraData: String?) {
                if ("succeed" == message) {
                    Log.d("RobotManager", "Command succeeded")
                } else {
                    Log.d("RobotManager", "Command failed")
                }
            }

            override fun onStatusUpdate(status: Int, data: String?, extraData: String?) {
                super.onStatusUpdate(status, data, extraData)
                Log.d("RobotManager", "Command status update: $status, $data, $extraData")
            }
        }
    }

    private fun setupHeadListener() {
        headListener = object : CommandListener() {
            override fun onResult(result: Int, message: String) {
                try {
                    val json = JSONObject(message)
                    val status = json.getString("status")
                    if (Definition.CMD_STATUS_OK == status) {
                        // success
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun setupPlacesListener() {
        placesListener = object : CommandListener() {
            @Deprecated("Deprecated in Java")
            override fun onResult(result: Int, message: String) {
                try {
                    val jsonArray = JSONArray(message)
                    val newPlaces = mutableListOf<Place>()
                    val newDestinations = mutableListOf<String>()
                    val newPoses = mutableListOf<Pose>()

                    for (i in 0 until jsonArray.length()) {
                        val json = jsonArray.getJSONObject(i)
                        val x = json.getDouble("x")
                        val y = json.getDouble("y")
                        val theta = json.getDouble("theta")
                        val name = json.getString("name")
                        val ignoreDistance = false
                        val safedistance = 1
                        val pose = Pose(
                            x.toFloat(),
                            y.toFloat(),
                            theta.toFloat(),
                            name,
                            ignoreDistance,
                            safedistance
                        )
                        val place = Place(x, y, theta, name)

                        newPlaces.add(place)
                        newDestinations.add(name)
                        newPoses.add(pose)
                    }

                    placesList.addAll(newPlaces)
                    _destinationsList.value = placesList.map { it.name }
                    Log.d("RobotManager PLACES", placesList.toString())
                } catch (e: JSONException) {
                    Log.e("ERROR", "Error parsing JSON", e)
                } catch (e: NullPointerException) {
                    Log.e("ERROR", "Null pointer exception", e)
                }
            }
        }
    }

    private fun setupPersonListener() {
        personListener = object : PersonListener() {
            override fun personChanged() {
                val personList = PersonApi.getInstance().allPersons
                onPersonDetected?.invoke(personList)
                Log.d("RobotMan PersonListener", "Person changed: $personList")
            }
        }
    }

    private fun setupTextListener() {
        textListener = object : TextListener() {
            override fun onStart() {
                // Handle TTS start
            }

            override fun onStop() {
                // Handle TTS stop
            }

            override fun onError() {
                // Handle TTS error
            }

            override fun onComplete() {
                // Handle TTS complete
            }
        }
    }

    fun getPlaceList(): MutableLiveData<List<String>> {
        Log.d("RobotManager", "Getting place list")
        RobotApi.getInstance().getPlaceList(1, placesListener)
        return _destinationsList
    }

    fun startFocusFollow(faceId: Int, onUpdate: (MutableStateFlow<Boolean>) -> Unit) {
        // Define los parámetros necesarios para el método startFocusFollow
        val reqId = 1 // Define o calcula un ID de solicitud adecuado
        val lostTimeout =
            10 // Define el tiempo en segundos antes de reportar la pérdida del objetivo
        val maxDistance = 2.5F //Define la distancia máxima en metros para el seguimiento
        Log.d("startFocusFollow", "Iniciando seguimiento")
        // Inicia el seguimiento de la persona con el ID de cara dado
        RobotApi.getInstance().startFocusFollow(reqId, faceId,
            lostTimeout.toLong(), maxDistance, object : ActionListener() {
                @Deprecated("Deprecated in Java")
                override fun onStatusUpdate(status: Int, data: String?) {
                    Log.d("startFocusFollow", "Estado del seguimiento: $status")
                    when (status) {
                        Definition.STATUS_TRACK_TARGET_SUCCEED -> {
                            // El seguimiento del objetivo ha tenido éxito
                            _isFollowing.value = true
                            onUpdate(_isFollowing)
                            Log.d(
                                "isFollowingstartFocusFolow",
                                "Seguimiento del objetivo exitoso ${isFollowing.value}"
                            )
                        }

                        Definition.STATUS_GUEST_LOST -> {
                            // El objetivo se ha perdido
                            _isFollowing.value = false
                            stopFocusFollow()
                            Log.d("isFollowingstartFocusfollow", "Objetivo perdido : ${isFollowing.value}")
                        }

                        Definition.STATUS_GUEST_FARAWAY -> {
                            _isFollowing.value = true

                            // El objetivo está fuera de rango
                            Log.d("isFollowingstartFocusFollow", "Objetivo fuera de rango")
                        }

                        Definition.STATUS_GUEST_APPEAR -> {
                            // El objetivo está en rango nuevamente

                            _isFollowing.value = true
                            onUpdate(_isFollowing)
                            Log.d(
                                "isFollowingstartFocusFollow",
                                "Objetivo detectado nuevamente: ${isFollowing.value}"
                            )
                        }
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(errorCode: Int, errorString: String?) {
                    // Maneja los errores aquí
                    Log.e("startFocusFollow", "Error en el seguimiento: $errorString , $errorCode")
                    when(errorCode) {
                        Definition.ERROR_SET_TRACK_FAILED -> {
                            //no se puede establecer el seguimiento
                            Log.d("isFollowingstartFocusFollow", "No se puede establecer el seguimiento")
                            //startFocusFollow(0){onUpdate(_isFollowing)}
                        }

                        Definition.ERROR_TARGET_NOT_FOUND -> {
                            //no se puede encontrar el objetivo
                            Log.d("isFollowingstartFocusFollow", "No se puede encontrar el objetivo")
                            //startFocusFollow(0){onUpdate(_isFollowing)}
                        }
                        Definition.ACTION_RESPONSE_ALREADY_RUN -> {
                            Log.d("isFollowingstartFocusFollow", "Ya se está ejecutando el seguimiento")
                        }
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onResult(status: Int, responseString: String?) {
                    // Maneja el resultado aquíPerson
                    Log.d("startFocusFollow", "Respuesta del seguimiento: $responseString")
                }
            })
    }

    fun stopFocusFollow() {
        Log.d("stopFocusFollow", "Deteniendo seguimiento")
        unregisterPersonListener()
        RobotApi.getInstance().stopFocusFollow(0)
    }

    fun moveForward() {
        RobotApi.getInstance().goForward(0, 0.3f, 1f, true, commandListener)
    }

    fun moveBackward() {
        RobotApi.getInstance().goBackward(0, 0.3f, commandListener)
    }

    fun moveLeft() {
        RobotApi.getInstance().turnLeft(0, 0.3f, commandListener)
    }

    fun moveRight() {
        RobotApi.getInstance().turnRight(0, 1.5f, commandListener)
    }

    fun moveHeadUp() {
        RobotApi.getInstance().moveHead(0, "absolute", "absolute", 50, 80, headListener)
    }

    fun moveHeadDown() {
        RobotApi.getInstance().moveHead(0, "absolute", "absolute", 50, 10, headListener)
    }

    fun resetHead() {
        RobotApi.getInstance().resetHead(0, headListener)
    }

    fun stopForward() {
        RobotApi.getInstance().stopMove(1, commandListener)
    }

    fun setSpeechRecognitionListener(listener: SpeechRecognitionListener) {
        this.speechRecognitionListener = listener
    }

    private fun notifySpeechPartialResult(result: String) {
        speechRecognitionListener?.onSpeechPartialResult(result)
    }

    private fun notifySpeechFinalResult(result: String) {
        speechRecognitionListener?.onSpeechFinalResult(result)
    }
    interface NavigationCallback {
        fun onNavigationCompleted()
        fun onNavigationStarted()
        fun onSpeakFinished()
    }
    private var navigationCallback: NavigationCallback? = null

    fun speak(
        text: String,
        listen: Boolean,
        onSpeakComplete: () -> Unit
    ) {
        Log.d("RobotMan speak", "Speaking: $text, Is going to listen: $listen")
        if (listen) {
            robotConnectionService.skillApi.setRecognizeMode(listen)
            robotConnectionService.skillApi.setRecognizable(listen)
        }else{
            robotConnectionService.skillApi.setRecognizable(listen)
        }
        robotConnectionService.skillApi.playText(
            TTSEntity("sid-012345", text),
            object : TextListener() {
                override fun onStart() {
                    // Iniciar reproducción
                }

                override fun onStop() {
                    // Detener reproducción
                }

                override fun onError() {
                    // Manejar error
                }

                override fun onComplete() {
                    // Reproducción completada
                    Log.d("SPEAK", "Speak finished")
                    navigationCallback?.onSpeakFinished()
                    onSpeakComplete()
                }
            })
    }


    fun goTo(destinyGoal: String){
        RobotApi.getInstance().startNavigation(0, destinyGoal,0.12345, 100000, actionListener)
    }
}
