package com.zerointrusion

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import android.content.Context
import android.util.Log

class DeviceRegistrationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.device_registration_input)

        val email = findViewById<EditText>(R.id.email)
        val phoneNumber = findViewById<EditText>(R.id.phone)
        val submitButton = findViewById<Button>(R.id.submit_button)
        val privacyCheckBox = findViewById<CheckBox>(R.id.privacy_policy_checkbox)

        submitButton.setOnClickListener {
            if (!privacyCheckBox.isChecked) {
                Toast.makeText(this, "Please agree to the Privacy Policy", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val fcm_token = prefs.getString("fcm_token", null)

            val userData = JSONObject()
            userData.put("email", email.text.toString())
            userData.put("phone", phoneNumber.text.toString())
            userData.put("privacyPolicy", privacyCheckBox.isChecked)
            userData.put("fcmToken", fcm_token)

            val secureUserData = getRecoverySettings(this, userData);
            Log.d("secureUserData", "secureUserData: $secureUserData")

            val callback = object : Callback {
                override fun onResult(response: String?) {
                    runOnUiThread {
                        Toast.makeText(this@DeviceRegistrationActivity, "Device registered successfully", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@DeviceRegistrationActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
                override fun onError(error: String) {
                    runOnUiThread {
                        Toast.makeText(this@DeviceRegistrationActivity, "Registration failed: $error", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // Send as Post request
            NetworkHelper.sendEncryptedDataToServer(secureUserData, "recovery", callback)

            Toast.makeText(this, "Device registered successfully", Toast.LENGTH_SHORT).show()

            // Redirect to the main-view
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

            setResult(Activity.RESULT_OK)
            finish()
        }
    }
}
