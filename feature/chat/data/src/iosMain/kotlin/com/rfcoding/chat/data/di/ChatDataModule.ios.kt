package com.rfcoding.chat.data.di

import com.rfcoding.chat.data.lifecycle.AppLifecycleObserver
import com.rfcoding.chat.database.DatabaseFactory
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual val platformChatDataModule = module {
    single { DatabaseFactory() }
    singleOf(::AppLifecycleObserver)
}