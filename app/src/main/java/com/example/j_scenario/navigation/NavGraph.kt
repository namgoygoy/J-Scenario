package com.example.j_scenario.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.j_scenario.ui.screens.FeedbackScreen
import com.example.j_scenario.ui.screens.HomeScreen
import com.example.j_scenario.ui.screens.LoadingScreen
import com.example.j_scenario.ui.screens.ScenarioScreen
import com.example.j_scenario.ui.viewmodel.FeedbackViewModel
import com.example.j_scenario.ui.viewmodel.ScenarioViewModel

@Composable
fun NavGraph(navController: NavHostController) {
    // Shared ViewModels across navigation
    val scenarioViewModel: ScenarioViewModel = viewModel()
    val feedbackViewModel: FeedbackViewModel = viewModel()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen(
                onStartScenario = { scenario ->
                    scenarioViewModel.setScenario(scenario)
                    navController.navigate(Screen.Scenario.route)
                }
            )
        }
        composable(route = Screen.Scenario.route) {
            ScenarioScreen(
                viewModel = scenarioViewModel,
                onBack = { navController.popBackStack() },
                onSubmitSuccess = {
                    navController.navigate(Screen.Loading.route)
                }
            )
        }
        composable(route = Screen.Loading.route) {
            LoadingScreen(
                viewModel = scenarioViewModel,
                feedbackViewModel = feedbackViewModel,
                onLoadingComplete = {
                    navController.navigate(Screen.Feedback.route) {
                        popUpTo(Screen.Scenario.route) { inclusive = false }
                    }
                }
            )
        }
        composable(route = Screen.Feedback.route) {
            FeedbackScreen(
                viewModel = feedbackViewModel,
                onContinue = {
                    scenarioViewModel.resetInteractionState()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}

