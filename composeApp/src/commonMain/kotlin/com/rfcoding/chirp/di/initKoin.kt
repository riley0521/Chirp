package com.rfcoding.chirp.di

import com.rfcoding.auth.presentation.di.authPresentationModule
import com.rfcoding.chat.presentation.di.chatPresentationModule
import com.rfcoding.core.data.di.coreDataModule
import com.rfcoding.core.presentation.di.corePresentationModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(
            appModule,
            coreDataModule,
            corePresentationModule,
            authPresentationModule,
            chatPresentationModule
        )
    }
}