package com.rfcoding.auth.presentation.di

import com.rfcoding.auth.presentation.email_verification.EmailVerificationViewModel
import com.rfcoding.auth.presentation.forgot_password.ForgotPasswordViewModel
import com.rfcoding.auth.presentation.login.LoginViewModel
import com.rfcoding.auth.presentation.register.RegisterViewModel
import com.rfcoding.auth.presentation.register_success.RegisterSuccessViewModel
import com.rfcoding.auth.presentation.reset_password.ResetPasswordViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authPresentationModule = module {
    viewModelOf(::RegisterViewModel)
    viewModelOf(::RegisterSuccessViewModel)
    viewModelOf(::EmailVerificationViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::ForgotPasswordViewModel)
    viewModelOf(::ResetPasswordViewModel)
}