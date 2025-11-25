package com.rfcoding.chat.data.di

import com.rfcoding.chat.data.lifecycle.AppLifecycleObserver
import com.rfcoding.chat.data.network.ConnectionErrorHandler
import com.rfcoding.chat.data.network.ConnectivityObserver
import com.rfcoding.chat.database.DatabaseFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual val platformChatDataModule = module {
    single { DatabaseFactory(androidContext()) }
    singleOf(::AppLifecycleObserver)
    singleOf(::ConnectivityObserver)
    singleOf(::ConnectionErrorHandler)
}