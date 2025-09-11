import android.content.Context
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import android.app.Activity
import android.os.Handler
import android.os.Looper
import kotlin.system.exitProcess

object PasswordManager {

    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val PREF_NAME = "secret_prefs"

    private fun getKeyAlias(key: String) = "password_key_$key"

    fun initializeKey(key: String) {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val alias = getKeyAlias(key)
        if (!keyStore.containsAlias(alias)) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            val keySpec = KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
            keyGenerator.init(keySpec)
            keyGenerator.generateKey()
        }
    }

    private fun getSecretKey(key: String): SecretKey? {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val alias = getKeyAlias(key)

        return if (keyStore.containsAlias(alias)) {
            val entry = keyStore.getEntry(alias, null) as? KeyStore.SecretKeyEntry
            entry?.secretKey
        } else {
            Log.w("KeyStore", "Key not found for alias: $alias")
            null
        }
    }

    fun storePassword(context: Context, key: String, password: String) {
        val secretKey = getSecretKey(key)
        if (secretKey == null) {
            Log.e("PasswordManager", "SecretKey is null, cannot store password for key: $key")
            return
        }

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val iv = cipher.iv
        val encrypted = cipher.doFinal(password.toByteArray())

        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString("${key}_password", Base64.encodeToString(encrypted, Base64.DEFAULT))
            .putString("${key}_iv", Base64.encodeToString(iv, Base64.DEFAULT))
            .apply()
    }


    fun getPassword(context: Context, key: String): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val encryptedBase64 = prefs.getString("${key}_password", null) ?: return null
        val ivBase64 = prefs.getString("${key}_iv", null) ?: return null

        val encrypted = Base64.decode(encryptedBase64, Base64.DEFAULT)
        val iv = Base64.decode(ivBase64, Base64.DEFAULT)

        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(key), spec)
            String(cipher.doFinal(encrypted))
        } catch (e: Exception) {
            Log.e("Decryption", "Failed to decrypt for key $key", e)
            null
        }
    }

    fun getCredentials(context: Context, key: String): String {

        initializeKey(key)

        val existing = getPassword(context, key)
        if (existing != null) return existing

        return "This device is not registered"
    }

    fun hasAllRegistrationKeys(): Boolean {
        val keys = listOf("publicId", "privateId", "secret")
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        return keys.all { keyStore.containsAlias(getKeyAlias(it)) }
    }

    fun deleteRegistrationKeys(activity: Activity) {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val keysToDelete = listOf("publicId", "privateId", "secret")

        for (key in keysToDelete) {
            val alias = PasswordManager.getKeyAlias(key)
            if (keyStore.containsAlias(alias)) {
                keyStore.deleteEntry(alias)
                Log.d("Keystore", "Deleted key: $alias")
            }
        }
        activity.finishAffinity()
        Handler(Looper.getMainLooper()).postDelayed({
            exitProcess(0)
        }, 3000)
    }

    fun createSecretKey(key: String) {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val keyGenSpec = KeyGenParameterSpec.Builder(
            getKeyAlias(key),
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        keyGenerator.init(keyGenSpec)
        keyGenerator.generateKey()
    }
    fun createSecretKeyIfNeeded(alias: String) {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        if (keyStore.containsAlias(alias)) return

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val spec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()

        keyGenerator.init(spec)
        keyGenerator.generateKey()
    }

    fun registerDevice(context: Context, responseString: String, reset: Boolean): Boolean {
        Log.d("DEBUG", "Response JSON: $responseString")

        val fullJson = JSONObject(responseString)
        val contentJsonString = fullJson.getString("content")


        val jsonObject = try {
            JSONObject(contentJsonString)
        } catch (e: JSONException) {
            Log.e("registerDevice", "Invalid JSON response: $responseString")
            return false
        }

        val privateSecret = try {
            jsonObject.getJSONObject("privateSecret")
        } catch (e: JSONException) {
            return false
        }

        val publicId = privateSecret.optString("publicId", "")
        val privateId = privateSecret.optString("privateId", "")
        val secret = privateSecret.optString("secret", "")

        val deviceRegistrationData = mapOf(
            "publicId" to publicId,
            "privateId" to privateId,
            "secret" to secret
        )

        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

        for ((key, value) in deviceRegistrationData) {
            val alias = getKeyAlias(key)
            if (!reset && keyStore.containsAlias(alias)) {
                Log.d("HTTP", "Key already exists and reset is false: $key → skipping")
                continue
            }
            createSecretKeyIfNeeded(alias)
            storePassword(context, key, value)
            Log.d("HTTP", "Stored key: $key")
        }

        return true
    }

}
