package com.rfcoding.auth.presentation.di

import com.rfcoding.auth.presentation.register.RegisterViewModel
import com.rfcoding.auth.presentation.register_success.RegisterSuccessViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authPresentationModule = module {
    viewModelOf(::RegisterViewModel)
    viewModelOf(::RegisterSuccessViewModel)
}