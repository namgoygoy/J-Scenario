package com.example.j_scenario.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Scenario : Screen("scenario")
    data object Loading : Screen("loading")
    data object Feedback : Screen("feedback")
}

