package com.rfcoding.chat.data.di

import com.rfcoding.chat.database.DatabaseFactory
import org.koin.dsl.module

actual val platformChatDataModule = module {
    single { DatabaseFactory() }
}