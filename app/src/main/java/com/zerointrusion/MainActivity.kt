package com.zerointrusion

import PasswordManager.deleteRegistrationKeys
import PasswordManager.registerDevice
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.journeyapps.barcodescanner.CaptureActivity
import com.zerointrusion.NetworkHelper.getDeviceRegistrationData
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.zerointrusion.MessageBus
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    private lateinit var qrLauncher: ActivityResultLauncher<ScanOptions>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        qrLauncher = registerForActivityResult(ScanContract()) { result ->
            if (result.contents != null) {
                handleQrResult(this, result.contents)
            } else {
                Log.d("QR_CODE", "Scan cancelled or failed")
            }
        }

        if (!PasswordManager.hasAllRegistrationKeys()) {
            handleRegistration()
        } else {
            handleCards()
       }

        MessageBus.lastMessage.observe(this) { msg ->
            myAutoUpdateFunction(msg)
        }
    }

    private fun handleRegistration(){
        if (!CryptoHelper.isKeyInitialized()) {
            CryptoHelper.generateKey()
        }

        getDeviceRegistrationData(this, object : Callback {
            override fun onResult(response: String?) {
                response?.let {
                    runOnUiThread {
                        // Save device identity
                        registerDevice(this@MainActivity, response, true)

                        // Start activity
                        val intent = Intent(
                            this@MainActivity,
                            DeviceRegistrationActivity::class.java
                        )
                        startActivity(intent)
                    }
                }
            }

            override fun onError(error: String) {
                Log.e("DEBUG", "Error: $error")
            }
        })
    }

    private fun handleCards(){
        val scanCard = findViewById<MaterialCardView>(R.id.scan_card)
        scanCard.setOnClickListener {
            startQrScan() }

        val registerCard = findViewById<MaterialCardView>(R.id.register_card)
        registerDevice(this, registerCard);

        val prepayCard = findViewById<MaterialCardView>(R.id.prepay_card)
        prepayCard.setOnClickListener {
            Toast.makeText(this, "Button clicked", Toast.LENGTH_SHORT).show()
        }

        val resetCard = findViewById<MaterialCardView>(R.id.reset_card)
        resetCard.setOnClickListener {
            Toast.makeText(this, "Development mode", Toast.LENGTH_SHORT).show()
            deleteRegistrationKeys(this)
        }
    }

    private fun startQrScan() {
        val options = ScanOptions()
        options.setPrompt("Scan a QR code")
        options.setBeepEnabled(false)
        options.setOrientationLocked(false)

        options.setCaptureActivity(CaptureActivity::class.java)
        options.setBarcodeImageEnabled(false)
        options.setCameraId(0)
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)


        qrLauncher.launch(options)
    }

    private fun myAutoUpdateFunction(message: String) {
        val qrJson = JSONObject(message)
        Log.d("MAIN", "Function triggered with: $qrJson")
    }
}
