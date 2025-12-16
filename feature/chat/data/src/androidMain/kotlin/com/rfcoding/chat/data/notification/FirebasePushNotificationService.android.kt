package com.rfcoding.chat.data.notification

import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import com.rfcoding.chat.domain.notification.PushNotificationService
import com.rfcoding.core.domain.logging.ChirpLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.coroutineContext

actual class FirebasePushNotificationService(
    private val logger: ChirpLogger,
    private val applicationScope: CoroutineScope
) : PushNotificationService {

    private val deviceToken = flow {
        try {
            val fcmToken = Firebase.messaging.token.await()
            logger.info("Token received: $fcmToken")
            emit(fcmToken)
        } catch (e: Exception) {
            coroutineContext.ensureActive()
            logger.error("Failed to get FCM token", e)
            emit(null)
        }
    }.stateIn(
        applicationScope,
        SharingStarted.Eagerly,
        null
    )

    actual override fun observeDeviceToken(): Flow<String?> = deviceToken
}