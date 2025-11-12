package com.example.j_scenario.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.j_scenario.ui.theme.JScenarioTheme
import com.example.j_scenario.ui.theme.PrimaryGreen
import com.example.j_scenario.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun LoadingScreen(onLoadingComplete: () -> Unit) {
    // 2.5초 후 자동으로 피드백 화면으로 이동
    LaunchedEffect(Unit) {
        delay(2500)
        onLoadingComplete()
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
                text = "응답을 분석 중입니다...",
                fontSize = 16.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F251A)
@Composable
fun LoadingScreenPreview() {
    JScenarioTheme {
        LoadingScreen(onLoadingComplete = {})
    }
}

