package com.intec.template.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.intec.template.navigation.AppScreens
import com.intec.template.ui.viewmodels.RobotViewModel
import com.intec.t2o.network.SocketClient
import com.intec.t2o.network.WebSocketMessageListener
import com.intec.template.robot.RobotManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MainScreen(navController: NavController, robotViewModel: RobotViewModel) {
    val focusManager = LocalFocusManager.current
    var getChatGptToken by remember { mutableStateOf(robotViewModel.tokenGPT) }
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    val places by robotViewModel.places.collectAsState()

    // Estado para controlar la última vez que hubo interacción del usuario
    var lastInteractionTime by remember { mutableStateOf(System.currentTimeMillis()) }

    // Initialize WebSocket client and listener
    val socketClient = remember { SocketClient("username", robotViewModel.robotManager) }

    LaunchedEffect(socketClient) {
        socketClient.connect()
        socketClient.webSocketListener = object : WebSocketMessageListener {
            override fun onWebSocketMessageReceived(message: String) {
                Log.d("WebSocket Message", message)
                coroutineScope.launch {
                    handleWebSocketMessage(message, robotViewModel, navController)
                }
            }

            override fun onWebSocketEventReceived(event: String, message: String) {
                Log.d("WebSocket Event", "Event: $event, Message: $message")
            }
        }
    }

    // Effect to detect inactivity
    LaunchedEffect(lastInteractionTime) {
        while (true) {
            delay(1000)
            if (System.currentTimeMillis() - lastInteractionTime > 15000) {
                navController.navigate(AppScreens.EyesScreen.route)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = {
                focusManager.clearFocus()
                lastInteractionTime = System.currentTimeMillis() // Reset the interaction timer
            })
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        navController.navigate(AppScreens.EyesScreen.route)
                        lastInteractionTime = System.currentTimeMillis() // Reset the interaction timer
                    },
                    modifier = Modifier.size(width = 80.dp, height = 40.dp)
                ) {
                    Text("Ojos")
                }
                Spacer(modifier = Modifier.width(16.dp))
                TitleComponent(title = "TOKENS", modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(80.dp))  // Espaciador para centrar el título
            }

            TextField(
                value = getChatGptToken,
                onValueChange = {
                    getChatGptToken = it
                    robotViewModel.updateTokenGPT(it)
                    lastInteractionTime = System.currentTimeMillis() // Reset the interaction timer
                },
                label = { Text("Token ChatGPT") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                        robotViewModel.updateTokenGPT(getChatGptToken)
                        lastInteractionTime = System.currentTimeMillis() // Reset the interaction timer
                    }),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    robotViewModel.updateTokenGPT(getChatGptToken)
                    lastInteractionTime = System.currentTimeMillis() // Reset the interaction timer
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Añadir")
            }
            Spacer(modifier = Modifier.weight(1f)) // Empuja todo lo que sigue hacia abajo
            LazyRowUbicaciones(robotViewModel = robotViewModel, modifier = Modifier.fillMaxWidth())
        }
    }
}

suspend fun handleWebSocketMessage(message: String, robotViewModel: RobotViewModel, navController: NavController) {
    withContext(Dispatchers.Main) {
        when (message) {
            "videollamada" -> {
                Log.d("webSocket", "entrando a la videollamada")
                robotViewModel.openVideoCall(
                    onSuccess = {
                        Log.d("webSocket", "navigateToVideoCallScreen llamado desde openVideoCall")
                        navController.navigate(AppScreens.VideoCallScreen.route)
                    },
                    onError = { errorMessage ->
                        Log.e("webSocket", "Error al obtener el token: $errorMessage")
                    }
                )
            }
            "endCall" -> {
                Log.d("webSocket", "me voy a eyeScreen")
                navController.navigate(AppScreens.EyesScreen.route)
            }
            "adelante" -> {
               robotViewModel.adelante()
            }
            "derecha" -> {
                robotViewModel.derecha()

            }
            "izquierda" -> {
                robotViewModel.izquierda()
            }
            "detener" -> {
                robotViewModel.parar()
            }
            "abajo" -> {
                robotViewModel.abajo()
            }
            "arriba" -> {
                robotViewModel.arriba()
            }
        }
    }
}

@Composable
fun LazyRowUbicaciones(
    robotViewModel: RobotViewModel,
    modifier: Modifier = Modifier,
) {

    // Observa los cambios en el LiveData desde el ViewModel
    val destinations by robotViewModel.destinationsList.observeAsState(initial = emptyList())

    LazyRow(
        modifier = modifier
            .background(Color.Transparent)
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        items(items = destinations) { destinationName ->
            Button(
                modifier = Modifier
                    .height(30.dp) // Ajusta la altura del botón
                    .width(100.dp), // Ajusta el ancho del botón,
                onClick = { robotViewModel.irA(destinationName) },
                contentPadding =
                PaddingValues( all = 4.dp )
            ) {
                Text(
                    text = destinationName,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.size(4.dp))
        }
    }
}

@Composable
fun TitleComponent(title: String, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            color = Color.Black,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}
