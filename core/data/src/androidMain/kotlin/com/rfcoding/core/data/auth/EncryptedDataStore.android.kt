package com.rfcoding.core.data.auth

import android.content.Context
import androidx.datastore.dataStore
import com.rfcoding.core.data.auth.dto.AuthenticatedUserDto
import kotlinx.coroutines.flow.Flow

private val Context.dataStore by dataStore(
    fileName = "user-preferences",
    serializer = AuthenticatedUserSerializer
)

actual class EncryptedDataStore(
    private val context: Context
) {

    actual suspend fun read(): Flow<AuthenticatedUserDto?> {
        return context.dataStore.data
    }

    actual suspend fun write(value: AuthenticatedUserDto?) {
        context.dataStore.updateData { value }
    }
}