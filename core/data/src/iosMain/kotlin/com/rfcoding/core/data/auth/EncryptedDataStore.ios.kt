package com.rfcoding.core.data.auth

import com.rfcoding.core.data.auth.dto.AuthenticatedUserDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

actual class EncryptedDataStore(
    private val applicationScope: CoroutineScope
) {

    private val _state = MutableStateFlow<AuthenticatedUserDto?>(null)

    init {
        applicationScope.launch {
            val value = IOSKeychain.read()?.let {
                try {
                    Json.decodeFromString<AuthenticatedUserDto>(it)
                } catch (_: Exception) {
                    null
                }
            }
            _state.update { value }
        }
    }

    actual suspend fun read(): Flow<AuthenticatedUserDto?> = _state

    actual suspend fun write(value: AuthenticatedUserDto?) {
        val json = Json.encodeToString(value)
        IOSKeychain.write(json)

        _state.update { value }
    }
}