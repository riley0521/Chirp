package com.rfcoding.core.data.crypto

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class CryptoUtilsTest {

    @Test
    fun testEncryptAndDecrypt() = runBlocking {
        val sample = "Sample-Chirp"

        val encrypted = CryptoUtils.encrypt(sample.encodeToByteArray())
        val decrypted = CryptoUtils.decrypt(encrypted)

        assertEquals(sample, decrypted.decodeToString())
    }
}