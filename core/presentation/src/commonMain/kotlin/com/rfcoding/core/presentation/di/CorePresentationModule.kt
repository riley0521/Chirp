package com.rfcoding.core.presentation.di

import com.rfcoding.core.presentation.util.ScopedStoreRegistryViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

expect val platformCorePresentationModule: Module

val corePresentationModule = module {
    includes(platformCorePresentationModule)
    viewModelOf(::ScopedStoreRegistryViewModel)
}