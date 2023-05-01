package com.neowise.tracko.data

import android.content.Context
import com.google.gson.Gson
import com.neowise.tracko.constant.Urls
import com.neowise.tracko.data.model.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.util.*

class RemoteApi private constructor(context: Context) {

    companion object {
        private var instance: RemoteApi? = null

        fun getInstance(context: Context): RemoteApi {
            if (instance == null) {
                instance = RemoteApi(context)
            }
            return instance!!
        }
    }

    private val client = OkHttpClient()

    private var nextSessionId = 0

    fun login(params: LoginParams): AccessToken {
//        return AccessToken("abc", "acx")
        val response = request(Urls.LOGIN_URL, params)
        return Gson().fromJson(response, AccessToken::class.java)
    }

    fun registration(params: RegisterParams): AccessToken {
//        return AccessToken("abc", "acx")
        val response = request(Urls.REGISTER_URL, params)
        return Gson().fromJson(response, AccessToken::class.java)
    }

    fun startSession(token: String, params: SessionParams) : GpsSession {
//        return GpsSession(id = System.currentTimeMillis().toString(), name = Date().toString())
        val response = request(Urls.SESSIONS_URL, params, token)
        return Gson().fromJson(response, GpsSession::class.java)
    }

    fun sendLocation(token: String, location: GpsLocation): GpsLocation? {
        val response = request(Urls.LOCATIONS_URL, location, token)
        return Gson().fromJson(response, GpsLocation::class.java)
    }

    private fun request(url: String, body: Any, auth: String? = null): String {
        val builder = Request.Builder()
            .url(url)
            .post(RequestBody.create("application/json".toMediaTypeOrNull(), Gson().toJson(body)))

        if (auth != null) {
            builder.addHeader("Authorization", "Bearer $auth")
        }

        return client.newCall(builder.build()).execute().body!!.string()
    }
}
