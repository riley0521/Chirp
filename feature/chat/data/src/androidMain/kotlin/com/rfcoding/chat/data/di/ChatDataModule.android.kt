package com.rfcoding.chat.data.di

import com.rfcoding.chat.data.lifecycle.AppLifecycleObserver
import com.rfcoding.chat.data.network.ConnectionErrorHandler
import com.rfcoding.chat.data.network.ConnectivityObserver
import com.rfcoding.chat.data.notification.FirebasePushNotificationService
import com.rfcoding.chat.database.DatabaseFactory
import com.rfcoding.chat.domain.notification.PushNotificationService
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformChatDataModule = module {
    single { DatabaseFactory(androidContext()) }
    singleOf(::AppLifecycleObserver)
    singleOf(::ConnectivityObserver)
    singleOf(::ConnectionErrorHandler)
    singleOf(::FirebasePushNotificationService).bind<PushNotificationService>()
}