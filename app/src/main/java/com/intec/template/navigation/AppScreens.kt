package com.intec.template.navigation

sealed class AppScreens (val route: String){
    object MainScreen: AppScreens("main_screen")
    object VideoCallScreen: AppScreens("video_call_screen")
    object EyesScreen : AppScreens("eyes_screen")
}