package com.rfcoding.core.data.crypto

import diglol.crypto.AesCbc
import diglol.crypto.random.nextBytes

object CryptoUtils {

    private val SECRET_KEY = "secret_chirp1234".encodeToByteArray()
    private const val IV_SIZE = 16

    suspend fun encrypt(value: ByteArray): ByteArray {
        val iv = nextBytes(IV_SIZE)
        val cipher = AesCbc(SECRET_KEY, iv)
        return cipher.encrypt(value)
    }

    suspend fun decrypt(value: ByteArray): ByteArray {
        val iv = value.copyOf(IV_SIZE)
        val encryptedValue = value.copyOfRange(IV_SIZE, value.size)

        val cipher = AesCbc(SECRET_KEY, iv)
        return cipher.decrypt(iv + encryptedValue)
    }
}