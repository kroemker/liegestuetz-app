package com.liegestuetz.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

/**
 * Root navigation graph.
 *
 * Each feature registers its own nested graph via an extension function on NavGraphBuilder.
 * Screens are added progressively through phases:
 *
 *  Phase 2: authGraph()   → splash, login, register
 *  Phase 3: homeGraph()   → home, create-challenge, join-challenge
 *  Phase 4: challengeGraph() → challenge-detail, challenge-settings
 *  Phase 6: profileGraph() → profile
 */
@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.SPLASH,
    ) {
        // Phase 2: authGraph(navController)
        // Phase 3: homeGraph(navController)
        // Phase 4: challengeGraph(navController)
        // Phase 6: profileGraph(navController)
    }
}

object NavRoutes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val CREATE_CHALLENGE = "create_challenge"
    const val JOIN_CHALLENGE = "join_challenge"
    const val CHALLENGE_DETAIL = "challenge_detail/{challengeId}"
    const val CHALLENGE_SETTINGS = "challenge_settings/{challengeId}"
    const val PROFILE = "profile"

    fun challengeDetail(challengeId: String) = "challenge_detail/$challengeId"
    fun challengeSettings(challengeId: String) = "challenge_settings/$challengeId"
}
