package com.neowise.tracko.data

import android.content.Context
import android.preference.PreferenceManager
import com.neowise.tracko.data.model.AccessToken
import com.neowise.tracko.data.model.LoginParams

class Preferences private constructor(private val context: Context) {

    companion object {

        private var instance: Preferences? = null

        @Synchronized
        fun getInstance(context: Context): Preferences {
            if (instance == null) instance = Preferences(context)
            return instance!!
        }
    }

    fun getLogin(): LoginParams {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return LoginParams(
            email = prefs.getString("email", "")!!,
            password = prefs.getString("password", "")!!
        )
    }

    fun saveLogin(login: LoginParams, token: AccessToken) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPreferences.edit()
            .putString("email", login.email)
            .putString("password", login.password)
            .putString("token", token.token)
            .apply()
    }
}