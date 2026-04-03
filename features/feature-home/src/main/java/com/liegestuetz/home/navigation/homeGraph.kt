package com.liegestuetz.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.liegestuetz.home.presentation.HomeScreen

object HomeRoutes {
    const val HOME = "home"
}

fun NavGraphBuilder.homeGraph(
    navController: NavHostController,
    onNavigateToChallenge: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToJoin: () -> Unit,
    onNavigateToProfile: () -> Unit,
) {
    composable(HomeRoutes.HOME) {
        HomeScreen(
            onNavigateToChallenge = onNavigateToChallenge,
            onNavigateToCreate = onNavigateToCreate,
            onNavigateToJoin = onNavigateToJoin,
            onNavigateToProfile = onNavigateToProfile,
        )
    }
}
