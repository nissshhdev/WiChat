package com.nissshh.wichat.core.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom

object SecurityManager {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS_IDENTITY = "wichat_identity_key"

    /**
     * Get or create stable RSA-2048 identity keypair backed by Android Keystore.
     */
    fun getOrCreateIdentityKeyPair(): KeyPair {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (keyStore.containsAlias(KEY_ALIAS_IDENTITY)) {
            val entry = keyStore.getEntry(KEY_ALIAS_IDENTITY, null) as KeyStore.PrivateKeyEntry
            return KeyPair(entry.certificate.publicKey, entry.privateKey)
        }

        val keyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_RSA,
            ANDROID_KEYSTORE
        )
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS_IDENTITY,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY or KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setDigests(KeyProperties.DIGEST_SHA256)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
            .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
            .setKeySize(2048)
            .build()

        keyPairGenerator.initialize(spec)
        return keyPairGenerator.generateKeyPair()
    }

    fun getPublicKeyBase64(keyPair: KeyPair): String {
        return Base64.encodeToString(keyPair.public.encoded, Base64.NO_WRAP)
    }

    /**
     * Hash passcode using SHA-256 with a unique random salt.
     * Never stores raw plaintext passcode.
     */
    fun hashPasscode(passcode: String, salt: ByteArray = generateSalt()): Pair<String, String> {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(salt)
        val hash = md.digest(passcode.toByteArray(Charsets.UTF_8))
        val hashString = Base64.encodeToString(hash, Base64.NO_WRAP)
        val saltString = Base64.encodeToString(salt, Base64.NO_WRAP)
        return Pair(hashString, saltString)
    }

    fun verifyPasscode(passcode: String, storedHash: String, storedSalt: String): Boolean {
        val salt = Base64.decode(storedSalt, Base64.NO_WRAP)
        val md = MessageDigest.getInstance("SHA-256")
        md.update(salt)
        val hash = md.digest(passcode.toByteArray(Charsets.UTF_8))
        val currentHash = Base64.encodeToString(hash, Base64.NO_WRAP)
        return currentHash == storedHash
    }

    private fun generateSalt(): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return salt
    }
}
