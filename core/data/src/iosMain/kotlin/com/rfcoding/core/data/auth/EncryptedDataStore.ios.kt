package com.rfcoding.core.data.auth

import com.rfcoding.core.data.auth.dto.AuthenticatedUserDto
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
                    json.decodeFromString<AuthenticatedUserDto>(it)
                } catch (_: Exception) {
                    null
                }
            }
            _state.update { value }
        }
    }

    actual fun read(): Flow<AuthenticatedUserDto?> = _state

    actual suspend fun write(value: AuthenticatedUserDto?) {
        val json = value?.let {
            json.encodeToString(it)
        }
        IOSKeychain.write(json)

        _state.update { value }
    }
}