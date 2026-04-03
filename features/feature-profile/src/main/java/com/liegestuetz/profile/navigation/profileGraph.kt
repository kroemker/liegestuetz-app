package com.liegestuetz.profile.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.liegestuetz.profile.presentation.ProfileScreen

object ProfileRoutes {
    const val PROFILE = "profile"
}

fun NavGraphBuilder.profileGraph(
    navController: NavHostController,
    onSignedOut: () -> Unit,
) {
    composable(ProfileRoutes.PROFILE) {
        ProfileScreen(
            onNavigateBack = { navController.popBackStack() },
            onSignedOut = onSignedOut,
        )
    }
}
