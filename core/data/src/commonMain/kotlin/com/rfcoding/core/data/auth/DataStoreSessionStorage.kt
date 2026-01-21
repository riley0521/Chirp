package com.rfcoding.core.data.auth

import com.rfcoding.core.data.mappers.toAuthenticatedUser
import com.rfcoding.core.data.mappers.toSerializable
import com.rfcoding.core.domain.auth.AuthenticatedUser
import com.rfcoding.core.domain.auth.SessionStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreSessionStorage(
    private val dataStore: EncryptedDataStore
): SessionStorage {

    override fun observeAuthenticatedUser(): Flow<AuthenticatedUser?> {
        return dataStore.read().map {
            it?.toAuthenticatedUser()
        }
    }

    override suspend fun set(authenticatedUser: AuthenticatedUser?) {
        dataStore.write(authenticatedUser?.toSerializable())
    }
}