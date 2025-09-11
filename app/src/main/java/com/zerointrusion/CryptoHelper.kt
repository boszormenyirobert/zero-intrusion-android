import java.security.SecureRandom
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import android.util.Base64
import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.interfaces.SecretBox
import com.goterl.lazysodium.utils.Key
import java.nio.charset.StandardCharsets

object CryptoHelper {

    private lateinit var secretKey: SecretKey
    private lateinit var ivSpec: IvParameterSpec

    private var keyGenerated = false

    fun isKeyInitialized() = keyGenerated

    fun generateKey() {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256) // AES-256 kulcs
        secretKey = keyGen.generateKey()

        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)
        ivSpec = IvParameterSpec(iv)

        keyGenerated = true
    }

    private val sodium = SodiumAndroid()
    private val lazySodium = LazySodiumAndroid(sodium, StandardCharsets.UTF_8)

    fun encryptToBase64(message: String, secret: String): String {
        val input = secret.toByteArray()
        val keyBytes = ByteArray(SecretBox.KEYBYTES)

        // Hash kulcs generálása (32 bájtos kulcs)
        lazySodium.cryptoGenericHash(
            keyBytes,
            SecretBox.KEYBYTES,
            input,
            input.size.toLong(),
            null,
            0
        )
        val key = Key.fromBytes(keyBytes)

        // Nonce generálása
        val nonce = lazySodium.randomBytesBuf(SecretBox.NONCEBYTES)

        // Titkosítás (RAW output, nem base64!)
        val messageBytes = message.toByteArray(Charsets.UTF_8)
        val cipherBytes = ByteArray(messageBytes.size + SecretBox.MACBYTES)
        lazySodium.cryptoSecretBoxEasy(
            cipherBytes,
            messageBytes,
            messageBytes.size.toLong(),
            nonce,
            key.asBytes
        )

        // Nonce + cipher összefűzése
        val combined = nonce + cipherBytes

        // Visszakódolás base64-re
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }


}
