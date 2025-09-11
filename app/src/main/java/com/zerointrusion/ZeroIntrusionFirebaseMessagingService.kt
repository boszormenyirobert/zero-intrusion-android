package com.zerointrusion

import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.util.Log
import org.json.JSONObject
import android.content.Context

class ZeroIntrusionFirebaseMessagingService : FirebaseMessagingService() {
    override fun onCreate() {
        super.onCreate()
        Log.d("FCM_TEST", "FirebaseMessagingService elindult")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FCM", "Message received: ${message.data}")

        val content = message.data["qrData"]
        val qrJson = JSONObject(content)
        Log.d("QR", qrJson.toString())

        message.data["action"]?.let { action ->
            if (action == "show_allow_close") {
                val intent = Intent(this, AllowDisallowApplicationActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("message", message.data["message"])
                    putExtra("qrString", content)
                }
                startActivity(intent)
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")

        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("fcm_token", token).apply()
    }
}