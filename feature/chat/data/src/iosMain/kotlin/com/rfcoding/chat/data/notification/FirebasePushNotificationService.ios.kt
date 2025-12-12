package com.rfcoding.chat.data.notification

import com.rfcoding.chat.domain.notification.PushNotificationService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

actual class FirebasePushNotificationService : PushNotificationService {

    actual override fun observeDeviceToken(): Flow<String?> = flow {

    }
}