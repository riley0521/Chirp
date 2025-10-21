package com.rfcoding.core.data.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.rfcoding.core.data.auth.dto.AuthenticatedUserDto
import com.rfcoding.core.data.mappers.toAuthenticatedUser
import com.rfcoding.core.data.mappers.toSerializable
import com.rfcoding.core.domain.auth.AuthenticatedUser
import com.rfcoding.core.domain.auth.SessionStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class DataStoreSessionStorage(
    private val dataStore: DataStore<Preferences>
): SessionStorage {

    private val authenticatedUserKey = stringPreferencesKey("KEY_AUTHENTICATED_USER")

    private val json = Json {
        ignoreUnknownKeys = true
    }

    override fun observeAuthenticatedUser(): Flow<AuthenticatedUser?> {
        return dataStore
            .data
            .map { pref ->
                val serializedJson = pref[authenticatedUserKey] ?: return@map null
                json.decodeFromString<AuthenticatedUserDto>(serializedJson).toAuthenticatedUser()
            }
    }

    override suspend fun set(authenticatedUser: AuthenticatedUser?) {
        if (authenticatedUser == null) {
            dataStore.edit { it.remove(authenticatedUserKey) }
            return
        }

        val serialized = json.encodeToString(authenticatedUser.toSerializable())
        dataStore.edit { pref ->
            pref[authenticatedUserKey] = serialized
        }
    }
}