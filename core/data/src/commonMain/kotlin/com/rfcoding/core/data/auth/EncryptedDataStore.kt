package com.rfcoding.core.data.auth

import com.rfcoding.core.data.auth.dto.AuthenticatedUserDto
import kotlinx.coroutines.flow.Flow

expect class EncryptedDataStore {

    suspend fun read(): Flow<AuthenticatedUserDto?>

    suspend fun write(value: AuthenticatedUserDto?)
}