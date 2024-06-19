package com.intec.template.ui.screens

import android.annotation.SuppressLint
import android.os.Build.VERSION.SDK_INT
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.intec.t2o.chatGPT.ChatGPTManager
import com.intec.t2o.network.SocketClient
import com.intec.t2o.network.WebSocketMessageListener
import com.intec.template.R
import com.intec.template.data.Face
import com.intec.template.data.InteractionState
import com.intec.template.navigation.AppScreens
import com.intec.template.ui.viewmodels.RobotViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun EyesScreen(
    navController: NavController,
    robotViewModel: RobotViewModel
) {
    Log.d("Current Screen", "Eyes Screen")

    val faceType by robotViewModel.faceTypeState.collectAsState()
    val interactionState by robotViewModel.interactionState.collectAsState()
    val speechText by robotViewModel.speechText.collectAsState()
    val chatGPTManager = ChatGPTManager(robotViewModel.tokenGPT)
    val coroutineScope = rememberCoroutineScope()

    var hasSpoken by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

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

    LaunchedEffect(speechText) {
        Log.d("speech", speechText)
        delay(1000)
        if (speechText.contains("kaixo", ignoreCase = true))  {
            hasSpoken = true
            robotViewModel.registrarPersonListener()
            robotViewModel.speak("kaixo", true) {

            }
        } else if (speechText.contains("peter", ignoreCase = true)) {
            isLoading = true
            chatGPTManager.fetchGPT3ChatResponse(speechText) { response ->
                isLoading = false
                robotViewModel.speak(response, true) {
                    Log.d("ChatGPT", "Response spoken: $response")
                }
            }
        } else if (speechText.contains("enciender luces", ignoreCase = true)) {
            robotViewModel.encenderLuces(true);
        } else if (speechText.contains("apagar luces", ignoreCase = true)) {
            robotViewModel.apagarLuces(false);
        }
    }

    Box(
        Modifier
            .clickable {
                robotViewModel.detenerFocus()
                navController.navigate(AppScreens.MainScreen.route)
            }
            .background(Color.Black)
            .fillMaxSize()
    ) {
        ImageExample(faceType, interactionState, speechText, isLoading)
    }
}

@Composable
fun ImageExample(
    faceType: Face,
    interactionState: InteractionState,
    speechText: String,
    isLoading: Boolean
) {
    Log.d("InteractionState", "$interactionState")
    val imageEmotionsLoader = ImageLoader.Builder(LocalContext.current)
        .components {
            if (SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()

    val imageInteractionLoader = ImageLoader.Builder(LocalContext.current)
        .components {
            if (SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()

    FuturisticGradientBackground {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when (faceType) {
                Face.NEUTRAL -> {
                    Image(
                        painter = rememberAsyncImagePainter(
                            R.drawable.blue_neutral,
                            imageEmotionsLoader
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center)
                    )
                }
                Face.HAPPY -> {
                    Image(
                        painter = rememberAsyncImagePainter(
                            R.drawable.blue_happy,
                            imageEmotionsLoader
                        ),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Face.BORED -> {
                    Image(
                        painter = rememberAsyncImagePainter(
                            R.drawable.blue_bored,
                            imageEmotionsLoader
                        ),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Face.MAD -> {
                    Image(
                        painter = rememberAsyncImagePainter(
                            R.drawable.blue_mad,
                            imageEmotionsLoader
                        ),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Face.SAD -> {
                    Image(
                        painter = rememberAsyncImagePainter(
                            R.drawable.blue_sad,
                            imageEmotionsLoader
                        ),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Face.LOVE -> {
                    Image(
                        painter = rememberAsyncImagePainter(
                            R.drawable.blue_love,
                            imageEmotionsLoader
                        ),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            when (interactionState) {
                InteractionState.NONE -> {
                    // Nothing to show for now
                }
                InteractionState.THINKING -> {
                    Image(
                        painter = rememberAsyncImagePainter(
                            R.drawable.dots,
                            imageEmotionsLoader
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 1.dp)
                            .width(70.dp)
                            .height(70.dp)
                    )
                }
                InteractionState.LISTENING -> {
                    Image(
                        painter = rememberAsyncImagePainter(
                            R.drawable.microphone,
                            imageEmotionsLoader
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 1.dp)
                            .width(70.dp)
                            .height(70.dp)
                    )
                }
                InteractionState.SPEAKING -> {
                    Image(
                        painter = rememberAsyncImagePainter(
                            R.drawable.speaking,
                            imageInteractionLoader
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 1.dp)
                            .width(70.dp)
                            .height(70.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .padding(10.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.White
                    )
                } else {
                    Text(
                        text = speechText,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}



@Composable
fun FuturisticGradientBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        content()
    }
}


