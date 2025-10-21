package com.rfcoding.core.domain.auth

import kotlinx.coroutines.flow.Flow

interface SessionStorage {
    fun observeAuthenticatedUser(): Flow<AuthenticatedUser?>
    suspend fun set(authenticatedUser: AuthenticatedUser?)
}