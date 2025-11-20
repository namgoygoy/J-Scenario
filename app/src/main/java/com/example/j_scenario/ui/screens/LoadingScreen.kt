package com.example.j_scenario.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.j_scenario.data.model.NetworkResult
import com.example.j_scenario.ui.theme.JScenarioTheme
import com.example.j_scenario.ui.theme.PrimaryGreen
import com.example.j_scenario.ui.theme.TextSecondary
import com.example.j_scenario.ui.viewmodel.FeedbackViewModel
import com.example.j_scenario.ui.viewmodel.ScenarioViewModel
import kotlinx.coroutines.delay

@Composable
fun LoadingScreen(
    viewModel: ScenarioViewModel,
    feedbackViewModel: FeedbackViewModel,
    onLoadingComplete: () -> Unit
) {
    val interactionState by viewModel.interactionState.collectAsState()
    
    // 인터랙션 상태 관찰 및 처리
    LaunchedEffect(interactionState) {
        when (val state = interactionState) {
            is NetworkResult.Success -> {
                // 평가 결과를 FeedbackViewModel에 전달
                feedbackViewModel.setInteractionResponse(state.data)
                // 짧은 딜레이 후 피드백 화면으로 이동
                delay(500)
                onLoadingComplete()
            }
            is NetworkResult.Error -> {
                // 에러 발생 시에도 짧은 딜레이 후 이동 (에러 처리는 나중에)
                delay(1000)
                onLoadingComplete()
            }
            else -> {
                // Loading 상태는 계속 표시
            }
        }
    }
    
    // TODO: 실제 인터랙션 API 호출이 없으므로, 임시로 딜레이 후 이동
    LaunchedEffect(Unit) {
        if (interactionState == null) {
            delay(2500)
            onLoadingComplete()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(50.dp),
                color = PrimaryGreen,
                strokeWidth = 4.dp
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = when (interactionState) {
                    is NetworkResult.Loading -> "응답을 분석 중입니다..."
                    is NetworkResult.Error -> "분석 중 오류가 발생했습니다..."
                    else -> "응답을 분석 중입니다..."
                },
                fontSize = 16.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Preview는 ViewModel 의존성 때문에 제거
// 필요시 mock ViewModel을 사용하여 Preview 구현 가능

