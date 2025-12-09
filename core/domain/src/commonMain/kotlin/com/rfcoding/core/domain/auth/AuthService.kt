package com.rfcoding.core.domain.auth

import com.rfcoding.core.domain.util.DataError
import com.rfcoding.core.domain.util.EmptyResult
import com.rfcoding.core.domain.util.Result

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

    suspend fun login(
        email: String,
        password: String
    ): Result<AuthenticatedUser, DataError.Remote>

    suspend fun logout(
        refreshToken: String
    ): EmptyResult<DataError.Remote>

    suspend fun forgotPassword(
        email: String
    ): EmptyResult<DataError.Remote>

    suspend fun resetPassword(
        token: String,
        newPassword: String
    ): EmptyResult<DataError.Remote>

    suspend fun changePassword(
        oldPassword: String,
        newPassword: String
    ): EmptyResult<DataError.Remote>
}