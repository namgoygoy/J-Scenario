package com.example.j_scenario.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.j_scenario.ui.screens.FeedbackScreen
import com.example.j_scenario.ui.screens.HomeScreen
import com.example.j_scenario.ui.screens.LoadingScreen
import com.example.j_scenario.ui.screens.ScenarioScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen(
                onStartScenario = { navController.navigate(Screen.Scenario.route) }
            )
        }
        composable(route = Screen.Scenario.route) {
            ScenarioScreen(
                onBack = { navController.popBackStack() },
                onRecord = { navController.navigate(Screen.Loading.route) }
            )
        }
        composable(route = Screen.Loading.route) {
            LoadingScreen(
                onLoadingComplete = {
                    navController.navigate(Screen.Feedback.route) {
                        popUpTo(Screen.Scenario.route) { inclusive = false }
                    }
                }
            )
        }
        composable(route = Screen.Feedback.route) {
            FeedbackScreen(
                onContinue = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}

