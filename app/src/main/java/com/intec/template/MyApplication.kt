package com.intec.template

import android.app.Application
import com.intec.template.robot.RobotConnectionService
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MyApplication: Application() {

    @Inject
    lateinit var robotConnectionService: RobotConnectionService

    override fun onCreate() {
        super.onCreate()
        //Inicialización específica de la aplicación
        robotConnectionService.connectToRobotApi()
        robotConnectionService.connectToSkillApi()
    }
}