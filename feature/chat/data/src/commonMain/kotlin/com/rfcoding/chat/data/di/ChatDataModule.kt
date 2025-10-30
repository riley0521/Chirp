package com.rfcoding.chat.data.di

import com.rfcoding.chat.data.chat.KtorChatService
import com.rfcoding.chat.domain.chat.ChatService
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val chatDataModule = module {
    singleOf(::KtorChatService).bind<ChatService>()
}