package com.rfcoding.core.data.auth

import com.rfcoding.core.data.auth.dto.AuthenticatedUserDto

expect class EncryptedDataStore {

    suspend fun read(): AuthenticatedUserDto?

    suspend fun write(value: AuthenticatedUserDto?)
}