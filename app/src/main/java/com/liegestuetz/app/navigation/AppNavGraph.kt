package com.liegestuetz.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.liegestuetz.auth.navigation.AuthRoutes
import com.liegestuetz.auth.navigation.authGraph
import com.liegestuetz.challenge.navigation.ChallengeRoutes
import com.liegestuetz.challenge.navigation.challengeGraph
import com.liegestuetz.home.navigation.HomeRoutes
import com.liegestuetz.home.navigation.homeGraph
import com.liegestuetz.profile.navigation.ProfileRoutes
import com.liegestuetz.profile.navigation.profileGraph

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = AuthRoutes.SPLASH,
    ) {
        // ── Auth ──────────────────────────────────────────────────────────────
        authGraph(
            navController = navController,
            onNavigateToHome = {
                navController.navigate(HomeRoutes.HOME) {
                    popUpTo(AuthRoutes.SPLASH) { inclusive = true }
                }
            },
        )

        // ── Home ──────────────────────────────────────────────────────────────
        homeGraph(
            navController = navController,
            onNavigateToChallenge = { id ->
                navController.navigate(ChallengeRoutes.detail(id))
            },
            onNavigateToCreate = {
                navController.navigate(ChallengeRoutes.CREATE)
            },
            onNavigateToJoin = {
                navController.navigate(ChallengeRoutes.JOIN)
            },
            onNavigateToProfile = {
                navController.navigate(ProfileRoutes.PROFILE)
            },
        )

        // ── Challenge ─────────────────────────────────────────────────────────
        challengeGraph(navController)

        // ── Profile ───────────────────────────────────────────────────────────
        profileGraph(
            navController = navController,
            onSignedOut = {
                navController.navigate(AuthRoutes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            },
        )
    }
}
