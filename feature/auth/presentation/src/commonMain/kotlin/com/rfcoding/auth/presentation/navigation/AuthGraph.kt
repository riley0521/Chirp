package com.rfcoding.auth.presentation.navigation

import androidx.compose.material3.Text
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import com.rfcoding.auth.presentation.email_verification.EmailVerificationRoot
import com.rfcoding.auth.presentation.register.RegisterRoot
import com.rfcoding.auth.presentation.register_success.RegisterSuccessRoot

fun NavGraphBuilder.authGraph(
    navController: NavController,
    onLoginSuccess: () -> Unit
) {
    navigation<AuthGraphRoutes.Graph>(
        startDestination = AuthGraphRoutes.Register
    ) {
        composable<AuthGraphRoutes.Login> {
            Text("Ang sarap mo pia")
        }
        composable<AuthGraphRoutes.Register> {
            RegisterRoot(
                onRegisterSuccess = { registeredEmail ->
                    navController.navigate(AuthGraphRoutes.RegisterSuccess(registeredEmail))
                },
                onLogin = {
                    navController.navigate(AuthGraphRoutes.Login)
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
        composable<AuthGraphRoutes.ForgotPassword> {  }
        composable<AuthGraphRoutes.ResetPassword> {  }
        composable<AuthGraphRoutes.EmailVerification>(
            deepLinks = listOf(
                navDeepLink {
                    this.uriPattern = "chirp://www.bluesky.io/api/auth/verify?token={token}"
                }
            )
        ) {
            EmailVerificationRoot(
                onLogin = {
                    navController.navigateUp()
                    navController.navigate(AuthGraphRoutes.Login)
                }
            )
        }
    }
}