package com.example.j_scenario.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.j_scenario.data.model.NetworkResult
import com.example.j_scenario.ui.screens.FeedbackScreen
import com.example.j_scenario.ui.screens.HomeScreen
import com.example.j_scenario.ui.screens.LoadingScreen
import com.example.j_scenario.ui.screens.ScenarioScreen
import com.example.j_scenario.ui.viewmodel.FeedbackViewModel
import com.example.j_scenario.ui.viewmodel.HomeViewModel
import com.example.j_scenario.ui.viewmodel.ScenarioViewModel

@Composable
fun NavGraph(navController: NavHostController) {
    val context = LocalContext.current
    
    // Shared ViewModels across navigation
    val scenarioViewModel: ScenarioViewModel = viewModel()
    val feedbackViewModel: FeedbackViewModel = viewModel()
    val homeViewModel: HomeViewModel = viewModel { HomeViewModel(context) }
    
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen(
                onStartScenario = { scenario ->
                    scenarioViewModel.setScenario(scenario)
                    navController.navigate(Screen.Scenario.route)
                },
                viewModel = homeViewModel
            )
        }
        composable(route = Screen.Scenario.route) {
            ScenarioScreen(
                viewModel = scenarioViewModel,
                onBack = {
                    // 뒤로가기 시 녹음 파일 초기화
                    scenarioViewModel.clearRecordedAudio()
                    navController.popBackStack()
                },
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
                    // 시나리오 완료 처리 (일일 진행도 증가 + 통계 업데이트)
                    // 단, 다음 챕터가 없을 때만 완료로 간주
                    if (!feedbackViewModel.hasNextChapter()) {
                        val score = feedbackViewModel.interactionResponse.value?.evaluation?.overallScore ?: 0
                        homeViewModel.onScenarioCompleted(score)
                    }
                    
                    // 홈으로 돌아가기 (상태 초기화)
                    scenarioViewModel.clearRecordedAudio()
                    scenarioViewModel.resetInteractionState()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNextChapter = {
                    // 다음 챕터로 이동 (완료로 간주하지 않음)
                    val nextChapterId = feedbackViewModel.getNextChapterId()
                    if (nextChapterId != null) {
                        // 다음 챕터 시나리오 로드 (내부에서 자동으로 초기화됨)
                        scenarioViewModel.loadScenario(nextChapterId)
                        
                        // Scenario 화면으로 이동
                        navController.navigate(Screen.Scenario.route) {
                            // Feedback 화면 제거
                            popUpTo(Screen.Feedback.route) { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}
