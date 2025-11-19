package com.rfcoding.chirp.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

actual val platformAppModule = module {
    // TODO
    single { CoroutineScope(SupervisorJob()) }
}