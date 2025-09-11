package com.zerointrusion

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.widget.Toast
import com.google.android.material.card.MaterialCardView
import com.zerointrusion.NetworkHelper.getDeviceRegistrationData
import org.json.JSONObject

fun getRegistration(context: android.content.Context, qrJson:JSONObject): JSONObject{
    val publicId = PasswordManager.getCredentials(context, "publicId")
    val privateId = PasswordManager.getCredentials(context, "privateId")
    val secret = PasswordManager.getCredentials(context, "secret")

    // Encrypt privateId
    val encryptedUserPrivateId = CryptoHelper.encryptToBase64(privateId, secret)

    Log.d("SECRET", "Send data: $secret")

    val type = qrJson.takeIf { it.has("type") && !it.isNull("type") }?.getString("type")

    val userName = qrJson.takeIf { it.has("userName") && !it.isNull("userName") }?.getString("userName")
    val userPassword = qrJson.takeIf { it.has("userPassword") && !it.isNull("userPassword") }?.getString("userPassword")

    var encryptedUserCredential: String? = null
    if (!userName.isNullOrEmpty() && !userPassword.isNullOrEmpty()) {
        // Encrypt credentials
        val secretMessage = JSONObject()
        secretMessage.put("userName", qrJson.getString("userName"))
        secretMessage.put("userPassword", qrJson.getString("userPassword"))
        val secretMessageString = secretMessage.toString()
        encryptedUserCredential = CryptoHelper.encryptToBase64(secretMessageString, secret)
    }

    val source = qrJson.takeIf { it.has("source") && !it.isNull("source") }?.getString("source")
    val isNew = qrJson.takeIf { it.has("isNew") && !it.isNull("isNew") }?.getString("isNew")
    val registrationProcessId = qrJson.takeIf { it.has("registrationProcessId") && !it.isNull("registrationProcessId") }?.getString("registrationProcessId")
    val xExtensionAuth = qrJson.takeIf { it.has("xExtensionAuthOne") && !it.isNull("xExtensionAuthOne") }?.getString("xExtensionAuthOne")

    val corporateId = qrJson.takeIf { it.has("corporateId") && !it.isNull("corporateId") }?.getString("corporateId")
    val corporateAuthentication = qrJson.takeIf { it.has("corporateAuthentication") && !it.isNull("corporateAuthentication") }?.getString("corporateAuthentication")


    val userData = JSONObject()
    userData.put("publicId", publicId)
    userData.put("privateId", encryptedUserPrivateId)
    userData.put("email", "boszormenyirobert@yahoo.com")
    userData.put("userCredential", encryptedUserCredential)
    userData.put("corporateId", corporateId)
    userData.put("corporateAuthentication", corporateAuthentication)
    userData.put("update", isNew)
    userData.put("source", source)
    userData.put("type", type)
    userData.put("registrationProcessId", registrationProcessId)
    userData.put("xExtensionAuth", xExtensionAuth) // Add to the header

    if(type == "registration-domain" || type == "system_hub_registration"){
        val domain = qrJson.takeIf { it.has("domain") && !it.isNull("domain") }?.getString("domain")
        val description = qrJson.takeIf { it.has("description") && !it.isNull("description") }?.getString("description")

        userData.put("domain", domain)
        userData.put("description", description)
    } else if(type == "registration-application") {
        val application = qrJson.getString("application")
        val description = qrJson.getString("description")

        // Add to the payload
        userData.put("application", application)
        userData.put("description", description)
    }

    if(type == "system_hub_registration"){
        val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#\$%^&*()-_=+[]{}<>?."
        val secretMessageString = (1..14)
            .map { charset.random() }
            .joinToString("")

        encryptedUserCredential = CryptoHelper.encryptToBase64(secretMessageString, secret)
        userData.put("userCredential", encryptedUserCredential)
    }

    return userData;
}

fun getLogin(context: android.content.Context, qrJson:JSONObject): JSONObject {
    Log.e("qrJson", "qrJson: $qrJson")

    val publicId = PasswordManager.getCredentials(context, "publicId")
    val privateId = PasswordManager.getCredentials(context, "privateId")
    val secret = PasswordManager.getCredentials(context, "secret")

    val domainProcessId = qrJson.takeIf { it.has("domainProcessId") && !it.isNull("domainProcessId") }?.getString("domainProcessId")

    val domain = qrJson.takeIf { it.has("domain") && !it.isNull("domain") }?.getString("domain")
    val type = qrJson.takeIf { it.has("type") && !it.isNull("type") }?.getString("type")
    val source = qrJson.takeIf { it.has("source") && !it.isNull("source") }?.getString("source")
    val xExtensionAuth = qrJson.takeIf { it.has("xExtensionAuthOne") && !it.isNull("xExtensionAuthOne") }?.getString("xExtensionAuthOne")

    val encryptedUserPrivateId = CryptoHelper.encryptToBase64(privateId, secret)

    val userData = JSONObject()
    userData.put("publicId", publicId)
    userData.put("privateId", encryptedUserPrivateId)
    userData.put("domain", domain)
    userData.put("domainProcessId", domainProcessId)
    userData.put("type", type)
    userData.put("source", source)
    userData.put("xExtensionAuth", xExtensionAuth) // Add to the header

    if(type == "system_hub_login") {
        val corporateId = qrJson.takeIf { it.has("corporateId") && !it.isNull("corporateId") }?.getString("corporateId")
        val corporateAuthentication = qrJson.takeIf { it.has("corporateAuthentication") && !it.isNull("corporateAuthentication") }?.getString("corporateAuthentication")

        userData.put("corporateId", corporateId)
        userData.put("corporateAuthentication", corporateAuthentication)
        userData.put("email", "boszormenyirobert@yahoo.com")
    }
    Log.e("LOGIN", "userData: $userData")
    return userData
}

fun allowApplicationList(context: android.content.Context, qrJson:JSONObject): JSONObject {
    val publicId = PasswordManager.getCredentials(context, "publicId")
    val privateId = PasswordManager.getCredentials(context, "privateId")
    val secret = PasswordManager.getCredentials(context, "secret")

    val xExtensionAuth = qrJson.getString("xExtensionAuthOne")
    val applicationProcessId = qrJson.takeIf { it.has("applicationProcessId") && !it.isNull("applicationProcessId") }?.getString("applicationProcessId")

    val type = qrJson.takeIf { it.has("type") && !it.isNull("type") }?.getString("type")
    val source = qrJson.takeIf { it.has("source") && !it.isNull("source") }?.getString("source")

    val encryptedUserPrivateId = CryptoHelper.encryptToBase64(privateId, secret)

    val userData = JSONObject()
    userData.put("publicId", publicId)
    userData.put("privateId", encryptedUserPrivateId)
    userData.put("applicationProcessId", applicationProcessId)
    userData.put("type", type)
    userData.put("source", source)
    userData.put("xExtensionAuth", xExtensionAuth) // Add to the header

    return userData
}

fun getRecoverySettings(context: android.content.Context, userData:JSONObject): JSONObject {
    val publicId = PasswordManager.getCredentials(context, "publicId")
    val privateId = PasswordManager.getCredentials(context, "privateId")
    val secret = PasswordManager.getCredentials(context, "secret")

    val encryptedUserPrivateId = CryptoHelper.encryptToBase64(privateId, secret)

    userData.put("publicId", publicId)
    userData.put("privateId", encryptedUserPrivateId)

    return userData
}

fun allowDeleteDomain(context: android.content.Context, qrJson:JSONObject): JSONObject {
    val publicId = PasswordManager.getCredentials(context, "publicId")
    val privateId = PasswordManager.getCredentials(context, "privateId")
    val secret = PasswordManager.getCredentials(context, "secret")

    val domain = qrJson.takeIf { it.has("domain") && !it.isNull("domain") }?.getString("domain")
    val type = qrJson.takeIf { it.has("type") && !it.isNull("type") }?.getString("type")
    val source = qrJson.takeIf { it.has("source") && !it.isNull("source") }?.getString("source")

    val xExtensionAuth = qrJson.getString("xExtensionAuthOne")
    val removeProcessId = qrJson.takeIf { it.has("removeProcessId") && !it.isNull("removeProcessId") }?.getString("removeProcessId")

    val encryptedUserPrivateId = CryptoHelper.encryptToBase64(privateId, secret)

    val userData = JSONObject()
    userData.put("publicId", publicId)
    userData.put("privateId", encryptedUserPrivateId)
    userData.put("domain", domain)
    userData.put("type", type)
    userData.put("source", source)
    userData.put("removeProcessId", removeProcessId)
    userData.put("xExtensionAuth", xExtensionAuth) // Add to the header

    return userData
}

fun allowDeleteApplications(context: android.content.Context, qrJson:JSONObject): JSONObject {
    val publicId = PasswordManager.getCredentials(context, "publicId")
    val privateId = PasswordManager.getCredentials(context, "privateId")
    val secret = PasswordManager.getCredentials(context, "secret")

    val type = qrJson.takeIf { it.has("type") && !it.isNull("type") }?.getString("type")
    val source = qrJson.takeIf { it.has("source") && !it.isNull("source") }?.getString("source")

    val xExtensionAuth = qrJson.getString("xExtensionAuthOne")
    val removeProcessId = qrJson.takeIf { it.has("removeProcessId") && !it.isNull("removeProcessId") }?.getString("removeProcessId")
    val targetId = qrJson.takeIf { it.has("targetId") && !it.isNull("targetId") }?.getString("targetId")

    val encryptedUserPrivateId = CryptoHelper.encryptToBase64(privateId, secret)

    val userData = JSONObject()
    userData.put("publicId", publicId)
    userData.put("privateId", encryptedUserPrivateId)
    userData.put("type", type)
    userData.put("source", source)
    userData.put("removeProcessId", removeProcessId)
    userData.put("targetId", targetId)
    userData.put("xExtensionAuth", xExtensionAuth) // Add to the header

    return userData
}

fun allowEditApplications(context: android.content.Context, qrJson:JSONObject): JSONObject {
    val publicId = PasswordManager.getCredentials(context, "publicId")
    val privateId = PasswordManager.getCredentials(context, "privateId")
    val secret = PasswordManager.getCredentials(context, "secret")

    val type = qrJson.takeIf { it.has("type") && !it.isNull("type") }?.getString("type")
    val source = qrJson.takeIf { it.has("source") && !it.isNull("source") }?.getString("source")

    val xExtensionAuth = qrJson.getString("xExtensionAuthOne")
    val registrationProcessId = qrJson.takeIf { it.has("registrationProcessId") && !it.isNull("registrationProcessId") }?.getString("registrationProcessId")
    val targetId = qrJson.takeIf { it.has("targetId") && !it.isNull("targetId") }?.getString("targetId")

    // Encrypt credentials
    val secretMessage = JSONObject()
    secretMessage.put("userName", qrJson.getString("userName"))
    secretMessage.put("userPassword", qrJson.getString("userPassword"))
    val secretMessageString = secretMessage.toString()
    val encryptedUserCredential = CryptoHelper.encryptToBase64(secretMessageString, secret)

    val description = qrJson.takeIf { it.has("description") && !it.isNull("description") }?.getString("description")

    val application = qrJson.getString("application")

    Log.e("DATA", "type: $type")
    Log.e("DATA", "source: $source")

    val encryptedUserPrivateId = CryptoHelper.encryptToBase64(privateId, secret)

    val userData = JSONObject()
    userData.put("publicId", publicId)
    userData.put("privateId", encryptedUserPrivateId)
    userData.put("type", type)
    userData.put("source", source)
    userData.put("registrationProcessId", registrationProcessId)
    userData.put("targetId", targetId)
    userData.put("userCredential", encryptedUserCredential)
    userData.put("application", application)
    userData.put("description", description)
    userData.put("xExtensionAuth", xExtensionAuth) // Add to the header

    return userData
}

fun allowAccessApplicationList(context: android.content.Context, qrJson:JSONObject): JSONObject {
    Log.e("HTTP", "Failed to send data: ${qrJson}")

    val publicId = PasswordManager.getCredentials(context, "publicId")
    val privateId = PasswordManager.getCredentials(context, "privateId")
    val secret = PasswordManager.getCredentials(context, "secret")

    val applicationName = qrJson.getString("applicationName")
    val description = qrJson.getString("description")
    val registrationProcessId = qrJson.getString("registrationProcessId")
    val type = qrJson.takeIf { it.has("type") && !it.isNull("type") }?.getString("type")
    val source = qrJson.takeIf { it.has("source") && !it.isNull("source") }?.getString("source")
    val isNew = qrJson.getString("isNew")

    val encryptedUserPrivateId = CryptoHelper.encryptToBase64(privateId, secret)

    // Encrypt credentials
    val secretMessage = JSONObject()
    secretMessage.put("applicationUserName", qrJson.getString("applicationUserName"))
    secretMessage.put("applicationPassword", qrJson.getString("applicationPassword"))

    val secretMessageString = secretMessage.toString()
    val encryptedUserCredential = CryptoHelper.encryptToBase64(secretMessageString, secret)

    val newApplication = JSONObject()
    newApplication.put("publicId", publicId)
    newApplication.put("privateId", encryptedUserPrivateId)
    newApplication.put("application", applicationName)
    newApplication.put("userCredential", encryptedUserCredential)
    newApplication.put("description", description)
    newApplication.put("registrationProcessId", registrationProcessId)
    newApplication.put("isNew", isNew)
    newApplication.put("type", type)
    newApplication.put("source", source)

    Log.e("URL", "url: $newApplication")

    return newApplication
}

fun registerDevice(activity: MainActivity, registerCard: MaterialCardView) {
    registerCard.isEnabled = PasswordManager.hasAllRegistrationKeys()

    if(registerCard.isEnabled) {
        registerCard.setCardBackgroundColor(Color.parseColor("#C8C8C8"))
    }
    registerCard.setOnClickListener {
        if(!registerCard.isEnabled) {
            Log.e("DEBUG", "deviceRegistration setOnClickListener")
            if (!PasswordManager.hasAllRegistrationKeys()) {
                Log.e("DEBUG", "Error: hasAllRegistrationKeys")
                Toast.makeText(activity, "Keys are not ready yet, please wait.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            getDeviceRegistrationData(activity, object : Callback {
                override fun onResult(response: String?) {
                    Log.e("DEBUG", response.toString())

                    response?.let {
                        (activity as Activity).runOnUiThread {
                            Log.e("DEBUG", "getDeviceRegistrationData")
                            PasswordManager.registerDevice(activity, response, true)
                            val intent = Intent(activity as Activity, DeviceRegistrationActivity::class.java)
                            (activity as Activity).startActivity(intent)
                        }
                    }
                }

                override fun onError(error: String) {
                    Log.e("DEBUG", "Error: $error")
                }
            })
        } else {
            Toast.makeText(activity, "Device is already registrated.", Toast.LENGTH_SHORT).show()
        }
    }
}

fun handleQrResult(context: Context, content: String) {
    val qrJson = JSONObject(content)
    val type = qrJson.takeIf { it.has("type") && !it.isNull("type") }?.getString("type")
    Log.d("QR", qrJson.toString())
    Log.e("TYPE", "TYPE: $type")

    val userData = when (type) {
        "delete-domain" -> allowDeleteDomain(context, qrJson)
        "registration-domain" -> getRegistration(context, qrJson)
        "recovery" -> getRecoverySettings(context, qrJson)
        "domain-login" -> getLogin(context, qrJson)
        "registration-application" -> getRegistration(context, qrJson)
        "applications" -> allowApplicationList(context, qrJson)
        "delete-applications" -> allowDeleteApplications(context, qrJson)
        "update-applications" -> allowEditApplications(context, qrJson)
        "system_hub_registration" -> getRegistration(context, qrJson)
        "system_hub_login" -> getLogin(context, qrJson)
        else -> {
            Log.e("userData ", "Unknown type '$type'")
            null
        }
    }

    userData?.let {
        NetworkHelper.sendEncryptedDataToServer(it, type.toString(), object : Callback {
            override fun onResult(response: String?) {
                Log.d("HTTP", "Success: $response")
            }

            override fun onError(error: String) {
                Log.e("HTTP", "Error: $error")
            }
        })
    }
}