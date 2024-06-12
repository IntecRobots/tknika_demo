package com.intec.template.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.intec.t2o.screens.VideoCallScreen
import com.intec.template.ui.screens.EyesScreen
import com.intec.template.ui.screens.MainScreen

import com.intec.template.ui.viewmodels.RobotViewModel

@Composable
fun AppNavigation(robotViewModel: RobotViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppScreens.EyesScreen.route
    ){
        composable(AppScreens.MainScreen.route){
            MainScreen(navController = navController, robotViewModel = robotViewModel)
        }
        composable(AppScreens.VideoCallScreen.route){
            VideoCallScreen(robotViewModel = robotViewModel, navController = navController)
        }
        composable(AppScreens.EyesScreen.route) {
            EyesScreen(navController = navController, robotViewModel = robotViewModel)
        }
    }

}