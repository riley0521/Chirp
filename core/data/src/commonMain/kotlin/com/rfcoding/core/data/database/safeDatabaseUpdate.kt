package com.rfcoding.core.data.database

import androidx.sqlite.SQLiteException
import com.rfcoding.core.domain.exceptions.ChirpException
import com.rfcoding.core.domain.util.DataError
import com.rfcoding.core.domain.util.Result
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

suspend inline fun <T> safeDatabaseUpdate(update: suspend () -> T): Result<T, DataError> {
    return try {
        Result.Success(update())
    } catch (_: SQLiteException) {
        Result.Failure(DataError.Local.DISK_FULL)
    } catch (e: ChirpException) {
        currentCoroutineContext().ensureActive()
        e.error
    }
}