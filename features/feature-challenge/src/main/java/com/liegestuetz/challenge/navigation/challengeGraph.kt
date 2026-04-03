package com.liegestuetz.challenge.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.liegestuetz.challenge.presentation.create.CreateChallengeScreen
import com.liegestuetz.challenge.presentation.detail.ChallengeDetailScreen
import com.liegestuetz.challenge.presentation.detail.ChallengeSettingsScreen
import com.liegestuetz.challenge.presentation.join.JoinChallengeScreen

object ChallengeRoutes {
    const val CREATE = "create_challenge"
    const val JOIN = "join_challenge"
    const val DETAIL = "challenge_detail/{challengeId}"
    const val SETTINGS = "challenge_settings/{challengeId}"

    fun detail(challengeId: String) = "challenge_detail/$challengeId"
    fun settings(challengeId: String) = "challenge_settings/$challengeId"
}

fun NavGraphBuilder.challengeGraph(navController: NavHostController) {
    composable(ChallengeRoutes.CREATE) {
        CreateChallengeScreen(
            onNavigateBack = { navController.popBackStack() },
            onChallengeCreated = { id ->
                navController.navigate(ChallengeRoutes.detail(id)) {
                    popUpTo(ChallengeRoutes.CREATE) { inclusive = true }
                }
            },
        )
    }

    composable(ChallengeRoutes.JOIN) {
        JoinChallengeScreen(
            onNavigateBack = { navController.popBackStack() },
            onChallengeJoined = { id ->
                navController.navigate(ChallengeRoutes.detail(id)) {
                    popUpTo(ChallengeRoutes.JOIN) { inclusive = true }
                }
            },
        )
    }

    composable(
        route = ChallengeRoutes.DETAIL,
        arguments = listOf(navArgument("challengeId") { type = NavType.StringType }),
    ) {
        ChallengeDetailScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToSettings = { id -> navController.navigate(ChallengeRoutes.settings(id)) },
        )
    }

    composable(
        route = ChallengeRoutes.SETTINGS,
        arguments = listOf(navArgument("challengeId") { type = NavType.StringType }),
    ) {
        ChallengeSettingsScreen(onNavigateBack = { navController.popBackStack() })
    }
}
