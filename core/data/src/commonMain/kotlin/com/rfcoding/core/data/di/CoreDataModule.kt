package com.rfcoding.core.data.di

import com.rfcoding.core.data.auth.DataStoreSessionStorage
import com.rfcoding.core.data.auth.KtorAuthService
import com.rfcoding.core.data.logging.KermitLogger
import com.rfcoding.core.data.networking.HttpClientFactory
import com.rfcoding.core.domain.auth.AuthService
import com.rfcoding.core.domain.auth.SessionStorage
import com.rfcoding.core.domain.logging.ChirpLogger
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

expect val platformCoreDataModule: Module

val coreDataModule = module {
    includes(platformCoreDataModule)
    single<ChirpLogger> { KermitLogger }
    single {
        HttpClientFactory(get()).create(get())
    }
    singleOf(::KtorAuthService).bind<AuthService>()
    singleOf(::DataStoreSessionStorage).bind<SessionStorage>()
}