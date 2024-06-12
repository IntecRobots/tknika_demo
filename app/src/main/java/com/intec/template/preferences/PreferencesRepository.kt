package com.intec.t2o.preferences

import android.content.SharedPreferences
import javax.inject.Inject

class PreferencesRepository @Inject constructor(private val sharedPreferences: SharedPreferences) {

    fun getTokenGPT(): String = sharedPreferences.getString("token_gpt", "") ?: ""
    fun setTokenGPT(token: String) = sharedPreferences.edit().putString("token_gpt", token).apply()

    fun getApiKey(): String = sharedPreferences.getString("api_key", "") ?: ""
    fun setApiKey(ip: String) = sharedPreferences.edit().putString("api_key", ip).apply()


}