package com.liegestuetz.auth.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.liegestuetz.auth.presentation.LoginScreen
import com.liegestuetz.auth.presentation.RegisterScreen
import com.liegestuetz.auth.presentation.SplashScreen

object AuthRoutes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
}

fun NavGraphBuilder.authGraph(
    navController: NavHostController,
    onNavigateToHome: () -> Unit,
) {
    composable(AuthRoutes.SPLASH) {
        SplashScreen(
            onNavigateToHome = onNavigateToHome,
            onNavigateToLogin = {
                navController.navigate(AuthRoutes.LOGIN) {
                    popUpTo(AuthRoutes.SPLASH) { inclusive = true }
                }
            },
        )
    }

    composable(AuthRoutes.LOGIN) {
        LoginScreen(
            onNavigateToHome = onNavigateToHome,
            onNavigateToRegister = { navController.navigate(AuthRoutes.REGISTER) },
        )
    }

    composable(AuthRoutes.REGISTER) {
        RegisterScreen(
            onNavigateToHome = onNavigateToHome,
            onNavigateToLogin = { navController.popBackStack() },
        )
    }
}
