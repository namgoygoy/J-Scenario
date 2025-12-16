package com.example.j_scenario.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.j_scenario.data.model.NetworkResult
import com.example.j_scenario.data.model.Scenario
import com.example.j_scenario.data.model.ScenarioCategory
import com.example.j_scenario.ui.components.CustomProgressBar
import com.example.j_scenario.ui.theme.*
import com.example.j_scenario.ui.viewmodel.HomeViewModel
import com.example.j_scenario.utils.UrlUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onStartScenario: (Scenario) -> Unit,
    viewModel: HomeViewModel
) {
    val scenarioState by viewModel.scenarioState.collectAsState()
    val userStats by viewModel.userStats.collectAsState()
    val dailyProgress by viewModel.dailyProgress.collectAsState()
    val completedScenariosToday by viewModel.completedScenariosToday.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Survival Japanese", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.loadRandomScenario() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "새로고침")
                    }
                    IconButton(onClick = { /* TODO: Settings */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "설정")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkGreenBg,
                    titleContentColor = TextPrimary,
                    actionIconContentColor = TextPrimary
                )
            )
        },
        containerColor = DarkGreenBg
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
            // 일일 진행도
            ProgressSection(
                progress = dailyProgress,
                completedCount = completedScenariosToday,
                totalGoal = 3
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 일일 시나리오 카드 (네트워크 상태 처리)
            when (val state = scenarioState) {
                is NetworkResult.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                }
                is NetworkResult.Success -> {
                    DailyScenarioCard(
                        scenario = state.data,
                        onStartScenario = { onStartScenario(state.data) }
                    )
                }
                is NetworkResult.Error -> {
                    ErrorCard(
                        message = state.message,
                        onRetry = { viewModel.loadRandomScenario() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 통계
            Text(
                text = "나의 통계",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            StatsGrid(userStats = userStats)
            
            // 하단 여유 공간 (네비게이션 바 높이 + 추가 여유 공간)
            Spacer(modifier = Modifier.height(112.dp))
            }
        }
    }
}

@Composable
fun ProgressSection(
    progress: Float,
    completedCount: Int,
    totalGoal: Int
) {
    // 애니메이션 적용 (800ms duration)
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800),
        label = "daily_progress_animation"
    )
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
        Text(
            text = "일일 진행도",
            fontSize = 12.sp,
            color = TextSecondary,
            letterSpacing = 0.5.sp
        )
            Text(
                text = "$completedCount / $totalGoal",
                fontSize = 12.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        CustomProgressBar(
            progress = animatedProgress,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun DailyScenarioCard(
    scenario: Scenario,
    onStartScenario: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column {
            // 백엔드 URL이 상대 경로인 경우 절대 경로로 변환
            val fullImageUrl = UrlUtils.toAbsoluteUrl(scenario.imageUrl)
            
            AsyncImage(
                model = fullImageUrl,
                contentDescription = scenario.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = scenario.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    // 난이도 표시
                    Text(
                        text = "★".repeat(scenario.difficultyLevel),
                        fontSize = 14.sp,
                        color = PrimaryGreen
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = scenario.description,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "미션: ${scenario.mission}",
                    fontSize = 13.sp,
                    color = LightGreen,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onStartScenario,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen,
                        contentColor = DarkBg
                    )
                ) {
                    Text(
                        text = "시나리오 시작",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorCard(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "⚠️",
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen,
                    contentColor = DarkBg
                )
            ) {
                Text(
                    text = "다시 시도",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun StatsGrid(userStats: com.example.j_scenario.ui.viewmodel.UserStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatBox(
            label = "생존 일수",
            value = userStats.weekStreak.toString(),
            modifier = Modifier.weight(1f)
        )
        StatBox(
            label = "완료한 퀘스트",
            value = userStats.totalScenarios.toString(),
            modifier = Modifier.weight(1f)
        )
        StatBox(
            label = "평균 점수",
            value = userStats.averageScore.toString(),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatBox(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextSecondary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F251A)
@Composable
fun ProgressSectionPreview() {
    JScenarioTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            ProgressSection(
                progress = 0.66f,
                completedCount = 2,
                totalGoal = 3
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F251A)
@Composable
fun DailyScenarioCardPreview() {
    JScenarioTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            DailyScenarioCard(
                scenario = Scenario(
                    id = "scenario_001",
                    category = ScenarioCategory.EMERGENCY,
                    title = "잃어버린 지갑",
                    description = "당신은 지갑을 잃어버렸습니다. 경찰서에서 분실 신고를 해야 합니다.",
                    mission = "경찰관에게 지갑을 잃어버린 경위와 지갑의 특징을 설명하세요.",
                    imageUrl = "https://i.imgur.com/8Nf9w7C.jpeg",
                    difficultyLevel = 3
                ),
                onStartScenario = {}
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F251A)
@Composable
fun StatsGridPreview() {
    JScenarioTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            StatsGrid(
                userStats = com.example.j_scenario.ui.viewmodel.UserStats(
                    weekStreak = 14,
                    totalScenarios = 28,
                    averageScore = 85
                )
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F251A)
@Composable
fun ErrorCardPreview() {
    JScenarioTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            ErrorCard(
                message = "네트워크 연결을 확인해주세요",
                onRetry = {}
            )
        }
    }
}

