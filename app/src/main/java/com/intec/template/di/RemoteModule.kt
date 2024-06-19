package com.intec.template.di

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.ainirobot.coreservice.client.speech.SkillApi
import com.example.testar.MqttManager
import com.intec.t2o.network.SocketClient
import com.intec.t2o.preferences.PreferencesRepository
import com.intec.template.robot.RobotConnectionService
import com.intec.template.robot.RobotManager
import com.intec.template.robot.SkillApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RemoteModule {

    @Singleton
    @Provides
    fun provideRobotManager(robotConnectionService: RobotConnectionService): RobotManager {
        return RobotManager(robotConnectionService) // Asumiendo que RobotManager no tiene dependencias en su constructor
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext appContext: Context): SharedPreferences {
        return appContext.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun providePreferencesRepository(sharedPreferences: SharedPreferences): PreferencesRepository {
        return PreferencesRepository(sharedPreferences)
    }

    @Singleton
    @Provides
    fun provideRobotConnectionService(@ApplicationContext context: Context): RobotConnectionService {
        return RobotConnectionService(context, skillApi = SkillApi())
    }

    @Singleton
    @Provides
    fun provideSkillApi(@ApplicationContext context: Context): SkillApiService {
        // Suponiendo que SkillApi tiene un método estático `getInstance()` y requiere inicialización
        return SkillApiService(context)
    }

    @Provides
    @Singleton
    fun provideSocketClient(@ApplicationContext context: Context, robotManager: RobotManager): SocketClient {
        val username = "AndroidUser" // Este valor podría ser dinámico y venir de alguna configuración o preferencia.
        return SocketClient(username, robotManager)
    }

    @Singleton
    @Provides
    fun provideMqttManager(
        @ApplicationContext context: Context
    ): MqttManager {
        return MqttManager(
            context,
            "tcp://10.14.0.182:1883", // Cambia esto por tu URI del servidor
            "/test/topic", // Tema por defecto para suscripción
            { topic, message -> Log.d("MQTT", "Message received from $topic: $message") }
        )
    }



}