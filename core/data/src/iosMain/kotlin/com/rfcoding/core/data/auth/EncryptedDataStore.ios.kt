package com.rfcoding.core.data.auth

import com.rfcoding.core.data.auth.dto.AuthenticatedUserDto
import com.rfcoding.core.data.crypto.CryptoUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

actual class EncryptedDataStore(
    private val applicationScope: CoroutineScope
) {

    private val json = Json {
        ignoreUnknownKeys = true
    }
    private val _state = MutableStateFlow<AuthenticatedUserDto?>(null)

    init {
        runBlocking {
            val value = IOSKeychain.read()?.let {
                try {
                    val decryptedJson = CryptoUtils.decrypt(it).decodeToString()
                    json.decodeFromString<AuthenticatedUserDto>(decryptedJson)
                } catch (_: Exception) {
                    null
                }
            }
            _state.update { value }
        }
    }

    actual fun read(): Flow<AuthenticatedUserDto?> = _state

    actual suspend fun write(value: AuthenticatedUserDto?) {
        val jsonBytes = value?.let {
            json.encodeToString(it)
        }?.encodeToByteArray() ?: ByteArray(0)
        IOSKeychain.write(CryptoUtils.encrypt(jsonBytes))

        _state.update { value }
    }
}