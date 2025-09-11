package com.zerointrusion

import android.content.Context
import android.util.Log
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException


object NetworkHelper {
    fun sendEncryptedDataToServer(
        qrRegistrationData: JSONObject,
        type: String,
        callback: Callback
    ) {
        Log.d("type", "type: $type")
        val url = when (type) {
            "registration-domain" -> BuildConfig.API_REGISTRATION
            "registration-application" -> BuildConfig.API_REGISTRATION
            "domain-login" -> BuildConfig.API_LOGIN
            "recovery" -> BuildConfig.API_RECOVERY_SETTINGS
            "applications" -> BuildConfig.API_ALLOW_APPLICATION_LIST
            "delete-domain" -> BuildConfig.API_ALLOW_DELETE_DOMAIN
            "delete-applications"-> BuildConfig.API_ALLOW_DELETE_APPLICATIONS
            "update-applications"-> BuildConfig.API_ALLOW_EDIT_APPLICATIONS
            "system_hub_registration" -> BuildConfig.API_REGISTRATION
            "system_hub_login" -> BuildConfig.API_LOGIN
            else -> null
        }
        Log.d("qrRegistrationData", "qrRegistrationData: $qrRegistrationData")
        var xExtensionAuth = "";
        if(type != "recovery"){
            xExtensionAuth = qrRegistrationData.getString("xExtensionAuth")
            qrRegistrationData.remove("xExtensionAuth")
        }


        Log.d("URL", "url: $url")
        if (url == null) return

        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body = qrRegistrationData.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("X-Extension-Auth", "HMAC $xExtensionAuth")
            .post(body)
            .build()
        Log.d("HTTP", "Send data: $qrRegistrationData");



           client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                Log.e("HTTP", "Failed to send data: ${e.message}")
                callback.onError(e.message ?: "Unknown error")
            }
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val respStr = response.body?.string()
                Log.d("HTTP", "Server response: $respStr")
                callback.onResult(respStr)
            }
        })
    }

    fun getDeviceRegistrationData(context: Context, callback: Callback) {
        if (PasswordManager.hasAllRegistrationKeys()) {
            callback.onResult("cached")
            Log.d("KEY_EXIST", "KEY_EXIST !!!: ")
            return
        }

        val url = BuildConfig.API_DEVICE_REGISTRATION
        val client = OkHttpClient()

        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError(e.message ?: "Unknown error")
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                callback.onResult(response.body?.string())
            }
        })
    }

}
