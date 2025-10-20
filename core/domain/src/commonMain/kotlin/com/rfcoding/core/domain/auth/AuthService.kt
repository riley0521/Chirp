package com.rfcoding.core.domain.auth

import com.rfcoding.core.domain.util.DataError
import com.rfcoding.core.domain.util.EmptyResult

interface AuthService {

    suspend fun register(
        email: String,
        username: String,
        password: String
    ): EmptyResult<DataError.Remote>

    suspend fun resendEmailVerification(
        email: String
    ): EmptyResult<DataError.Remote>

    suspend fun verifyEmail(
        token: String
    ): EmptyResult<DataError.Remote>
}