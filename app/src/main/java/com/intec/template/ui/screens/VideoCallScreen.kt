package com.intec.t2o.screens

import android.media.MediaPlayer
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.intec.t2o.network.SocketClient
import com.intec.t2o.network.WebSocketMessageListener
import com.intec.template.R
import com.intec.template.navigation.AppScreens
import io.getstream.video.android.compose.theme.VideoTheme
import io.getstream.video.android.compose.ui.components.call.activecall.CallContent
import io.getstream.video.android.compose.ui.components.call.controls.ControlActions
import io.getstream.video.android.core.GEO
import io.getstream.video.android.core.StreamVideo
import io.getstream.video.android.core.StreamVideoBuilder
import io.getstream.video.android.model.User

import com.intec.template.ui.viewmodels.RobotViewModel
import io.getstream.video.android.compose.ui.components.call.controls.actions.FlipCameraAction
import io.getstream.video.android.compose.ui.components.call.controls.actions.ToggleCameraAction
import io.getstream.video.android.compose.ui.components.call.controls.actions.ToggleMicrophoneAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


val userToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiV2VkZ2VfQW50aWxsZXMiLCJpc3MiOiJodHRwczovL3Byb250by5nZXRzdHJlYW0uaW8iLCJzdWIiOiJ1c2VyL1dlZGdlX0FudGlsbGVzIiwiaWF0IjoxNzEzMjU5MDEwLCJleHAiOjE3MTM4NjM4MTV9.slvig4X0I3r8Nm2LHqWFn9rsJ3xokcNSxjPkw5O26S8"
val userId = "Wedge_Antilles"
val callId = "TKNIKA"

@Composable
fun VideoCallScreen(
    robotViewModel: RobotViewModel,
    navController: NavController
) {
    val coroutineScope = rememberCoroutineScope()
    val socketClient = remember { SocketClient("username", robotViewModel.robotManager) }

    LaunchedEffect(socketClient) {
        socketClient.connect()
        socketClient.webSocketListener = object : WebSocketMessageListener {
            override fun onWebSocketMessageReceived(message: String) {
                Log.d("WebSocket Message", message)
                coroutineScope.launch {
                    com.intec.template.ui.screens.handleWebSocketMessage(
                        message,
                        robotViewModel,
                        navController
                    )
                }
            }

            override fun onWebSocketEventReceived(event: String, message: String) {
                Log.d("WebSocket Event", "Event: $event, Message: $message")
            }
        }
    }
    val context = LocalContext.current
    var showLoading by remember { mutableStateOf(true) }

    // Paso 1 - Crear un usuario.
    val user = User(id = userId, name = "TKNIKA")
    StreamVideo.removeClient()

    val apiKey = robotViewModel.apiKey

    Log.d("SN", robotViewModel.callId)

    // val token = mqttViewModel.fetchToken(apiKey, userId)

    // Paso 2 - Inicializar StreamVideo.
    val client = StreamVideoBuilder(
        context = context,
        apiKey,  // Reemplaza con tu API key real
        geo = GEO.GlobalEdgeNetwork,
        user = user,
        token = robotViewModel.tokenProvider
    ).build()

    // Paso 3 - Unirse a una llamada.
    val call = client.call("default", robotViewModel.callId)

    LaunchedEffect(call) {
        val result = call.join(create = true)
        result.onSuccess {
            showLoading = false  // Oculta el spinner cuando la llamada se ha unido exitosamente
            call.camera.flip()  // Voltea la cámara
        }.onError {
            showLoading = false  // Oculta el spinner en caso de error
            Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
        }
    }


    VideoTheme {
        if (showLoading) {
            val mediaPlayer = MediaPlayer.create(context, R.raw.call_sound)

            DisposableEffect(Unit) {
                mediaPlayer.start()  // Inicia el sonido
                onDispose {
                    mediaPlayer.stop()  // Detiene el sonido cuando el composable se elimina de la composición
                    mediaPlayer.release()  // Libera recursos
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(100.dp))


            }
        } else {
            val isCameraEnabled by call.camera.isEnabled.collectAsState()
            val isMicrophoneEnabled by call.microphone.isEnabled.collectAsState()

            CallContent(
                modifier = Modifier.fillMaxSize(),
                call = call,
                onBackPressed = {},
                controlsContent = {
                    ControlActions(
                        call = call,
                        actions = listOf(
                            {
                                ToggleCameraAction(
                                    modifier = Modifier.size(52.dp),
                                    isCameraEnabled = isCameraEnabled,
                                    onCallAction = { call.camera.setEnabled(it.isEnabled) }
                                )
                            },
                            {
                                ToggleMicrophoneAction(
                                    modifier = Modifier.size(52.dp),
                                    isMicrophoneEnabled = isMicrophoneEnabled,
                                    onCallAction = { call.microphone.setEnabled(it.isEnabled) }
                                )
                            },
                            {
                                FlipCameraAction(
                                    modifier = Modifier.size(52.dp),
                                    onCallAction = { call.camera.flip() }
                                )
                            }
                        )
                    )
                }
            )
        }
    }
}

suspend fun handleWebSocketMessage(message: String, robotViewModel: RobotViewModel, navController: NavController) {
    withContext(Dispatchers.Main) {
        when (message) {
            "endCall" -> {
                Log.d("webSocket", "me voy a eyeScreen")
                navController.navigate(AppScreens.EyesScreen.route)
            }
        }
    }
}





