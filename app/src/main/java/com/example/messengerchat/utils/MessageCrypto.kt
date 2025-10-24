package com.example.messengerchat.utils

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom
import java.util.Base64

object MessageCrypto {

    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val KEY_ALGORITHM = "AES"
    private const val KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 10000
    private const val KEY_LENGTH = 256
    private const val GCM_TAG_LENGTH = 128
    private const val GCM_IV_LENGTH = 12
    private const val SALT_LENGTH = 16

    fun encrypt(login: String, userId: String, message: String): String {
        try {
            val salt = ByteArray(SALT_LENGTH)
            SecureRandom().nextBytes(salt)

            val iv = ByteArray(GCM_IV_LENGTH)
            SecureRandom().nextBytes(iv)

            val key = deriveKey(login, userId, salt)

            val cipher = Cipher.getInstance(ALGORITHM)
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec)

            val aad = "$login:$userId".toByteArray(Charsets.UTF_8)
            cipher.updateAAD(aad)

            val encryptedBytes = cipher.doFinal(message.toByteArray(Charsets.UTF_8))

            val combined = ByteArray(salt.size + iv.size + encryptedBytes.size)
            System.arraycopy(salt, 0, combined, 0, salt.size)
            System.arraycopy(iv, 0, combined, salt.size, iv.size)
            System.arraycopy(encryptedBytes, 0, combined, salt.size + iv.size, encryptedBytes.size)

            return Base64.getEncoder().encodeToString(combined)

        } catch (_: Exception) {
            // for backward compatibility
            return message
        }
    }

    fun decrypt(login: String, userId: String, encryptedMessage: String): String {
        try {
            val combined = Base64.getDecoder().decode(encryptedMessage)

            val salt = ByteArray(SALT_LENGTH)
            val iv = ByteArray(GCM_IV_LENGTH)
            val encryptedBytesWithTag = ByteArray(combined.size - SALT_LENGTH - GCM_IV_LENGTH)

            System.arraycopy(combined, 0, salt, 0, SALT_LENGTH)
            System.arraycopy(combined, SALT_LENGTH, iv, 0, GCM_IV_LENGTH)

            System.arraycopy(
                combined,
                SALT_LENGTH + GCM_IV_LENGTH,
                encryptedBytesWithTag,
                0,
                encryptedBytesWithTag.size
            )

            val key = deriveKey(login, userId, salt)

            val cipher = Cipher.getInstance(ALGORITHM)
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec)

            val aad = "$login:$userId".toByteArray(Charsets.UTF_8)
            cipher.updateAAD(aad)

            val decryptedBytes = cipher.doFinal(encryptedBytesWithTag)

            return String(decryptedBytes, Charsets.UTF_8)
        } catch (_: Exception) {
            // for backward compatibility
            return encryptedMessage
        }
    }

    private fun deriveKey(login: String, userId: String, salt: ByteArray): SecretKey {
        val password = "$login:$userId".toCharArray()

        val keySpec = PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH)
        val keyFactory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM)
        val keyBytes = keyFactory.generateSecret(keySpec).encoded

        return SecretKeySpec(keyBytes, KEY_ALGORITHM)
    }
}