package com.rfcoding.auth.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import com.rfcoding.auth.presentation.email_verification.EmailVerificationRoot
import com.rfcoding.auth.presentation.forgot_password.ForgotPasswordRoot
import com.rfcoding.auth.presentation.login.LoginRoot
import com.rfcoding.auth.presentation.register.RegisterRoot
import com.rfcoding.auth.presentation.register_success.RegisterSuccessRoot
import com.rfcoding.auth.presentation.reset_password.ResetPasswordRoot

fun NavGraphBuilder.authGraph(
    navController: NavController,
    onLoginSuccess: () -> Unit
) {
    navigation<AuthGraphRoutes.Graph>(
        startDestination = AuthGraphRoutes.Login
    ) {
        composable<AuthGraphRoutes.Login> {
            LoginRoot(
                onForgotPassword = {
                    navController.navigate(AuthGraphRoutes.ForgotPassword)
                },
                onLoginSuccess = onLoginSuccess,
                onRegister = {
                    navController.navigate(AuthGraphRoutes.Register) {
                        restoreState = true
                    }
                }
            )
        }
        composable<AuthGraphRoutes.Register> {
            RegisterRoot(
                onRegisterSuccess = { registeredEmail ->
                    navController.navigate(AuthGraphRoutes.RegisterSuccess(registeredEmail))
                },
                onLogin = {
                    navController.navigate(AuthGraphRoutes.Login) {
                        popUpTo(AuthGraphRoutes.Register) {
                            inclusive = true
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        composable<AuthGraphRoutes.RegisterSuccess> {
            RegisterSuccessRoot(
                onLogin = {
                    navController.navigate(AuthGraphRoutes.Login)
                }
            )
        }
        composable<AuthGraphRoutes.ForgotPassword> {
            ForgotPasswordRoot()
        }
        composable<AuthGraphRoutes.ResetPassword>(
            deepLinks = listOf(
                navDeepLink {
                    this.uriPattern = "chirp://www.bluesky.io/api/auth/reset-password?token={token}"
                }
            )
        ) {
            ResetPasswordRoot(
                onBack = {
                    navController.navigate(AuthGraphRoutes.Login) {
                        popUpTo(AuthGraphRoutes.ResetPassword) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable<AuthGraphRoutes.EmailVerification>(
            deepLinks = listOf(
                navDeepLink {
                    this.uriPattern = "chirp://www.bluesky.io/api/auth/verify?token={token}"
                }
            )
        ) {
            EmailVerificationRoot(
                onLogin = {
                    navController.navigate(AuthGraphRoutes.Login) {
                        popUpTo(AuthGraphRoutes.EmailVerification) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}