package com.rfcoding.chat.data.di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.rfcoding.chat.data.chat.KtorChatService
import com.rfcoding.chat.data.chat.OfflineFirstChatRepository
import com.rfcoding.chat.data.chat.WebSocketChatConnectionClient
import com.rfcoding.chat.data.message.KtorChatMessageService
import com.rfcoding.chat.data.message.OfflineFirstMessageRepository
import com.rfcoding.chat.data.network.ConnectionRetryHandler
import com.rfcoding.chat.data.network.KtorWebSocketConnector
import com.rfcoding.chat.data.notification.KtorDeviceTokenService
import com.rfcoding.chat.database.ChirpChatDatabase
import com.rfcoding.chat.database.DatabaseFactory
import com.rfcoding.chat.domain.chat.ChatConnectionClient
import com.rfcoding.chat.domain.chat.ChatRepository
import com.rfcoding.chat.domain.chat.ChatService
import com.rfcoding.chat.domain.message.ChatMessageService
import com.rfcoding.chat.domain.message.MessageRepository
import com.rfcoding.chat.domain.notification.DeviceTokenService
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

expect val platformChatDataModule: Module

val chatDataModule = module {
    includes(platformChatDataModule)
    singleOf(::KtorChatService).bind<ChatService>()
    singleOf(::KtorChatMessageService).bind<ChatMessageService>()
    singleOf(::KtorDeviceTokenService).bind<DeviceTokenService>()
    single<ChirpChatDatabase> {
        get<DatabaseFactory>()
            .create()
            .setDriver(BundledSQLiteDriver())
            .build()
    }
    singleOf(::OfflineFirstChatRepository).bind<ChatRepository>()
    singleOf(::OfflineFirstMessageRepository).bind<MessageRepository>()
    singleOf(::KtorWebSocketConnector)
    singleOf(::ConnectionRetryHandler)
    singleOf(::WebSocketChatConnectionClient).bind<ChatConnectionClient>()
    single {
        Json {
            ignoreUnknownKeys = true
        }
    }
}