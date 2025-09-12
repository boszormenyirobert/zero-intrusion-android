package com.zerointrusion

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import org.json.JSONObject


class AllowDisallowApplicationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val qrJsonString = intent.getStringExtra("qrString")
        val qrJson = JSONObject(qrJsonString)

        val userData = getLogin(this, qrJson)
        val type = "domain-login"

        super.onCreate(savedInstanceState)
        setContentView(R.layout.allow_disallow_application)

        val message = intent.getStringExtra("message")
        findViewById<TextView>(R.id.tvMessage).text = message

        findViewById<Button>(R.id.btnAllow).setOnClickListener {
            userData?.let {
                NetworkHelper.sendEncryptedDataToServer(
                    it,
                    type.toString(),
                    object : Callback {
                        override fun onResult(response: String?) {
                            Log.d("HTTP", "Success: $response")
                        }

                        override fun onError(error: String) {
                            Log.e("HTTP", "Error: $error")
                        }
                    })
            }
            finish()
        }

        findViewById<Button>(R.id.btnClose).setOnClickListener {
            finish()
        }
    }
}
