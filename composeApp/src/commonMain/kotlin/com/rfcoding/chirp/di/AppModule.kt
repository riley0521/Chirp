package com.rfcoding.chirp.di

import com.rfcoding.chirp.MainViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

expect val platformAppModule: Module

val appModule = module {
    includes(platformAppModule)
    viewModelOf(::MainViewModel)
}