package com.rfcoding.core.data.auth

import com.rfcoding.core.data.auth.dto.AuthenticatedUserDto

actual class EncryptedDataStore {

    actual suspend fun read(): AuthenticatedUserDto? {
        TODO("Not yet implemented")
    }

    actual suspend fun write(value: AuthenticatedUserDto?) {
    }
}