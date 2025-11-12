package com.example.j_scenario.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.j_scenario.ui.components.CustomProgressBar
import com.example.j_scenario.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onStartScenario: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Survival Japanese", fontWeight = FontWeight.Bold) },
                actions = {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 일일 진행도
            ProgressSection()

            Spacer(modifier = Modifier.height(24.dp))

            // 일일 시나리오 카드
            DailyScenarioCard(onStartScenario)

            Spacer(modifier = Modifier.height(24.dp))

            // 통계
            Text(
                text = "나의 통계",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            StatsGrid()
        }
    }
}

@Composable
fun ProgressSection() {
    Column {
        Text(
            text = "일일 진행도",
            fontSize = 12.sp,
            color = TextSecondary,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        CustomProgressBar(
            progress = 0.6f,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun DailyScenarioCard(onStartScenario: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column {
            AsyncImage(
                model = "https://i.imgur.com/8Nf9w7C.jpeg",
                contentDescription = "Hidden Village",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "잃어버린 마을",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "당신은 숨겨진 마을에 우연히 도착했습니다. 하지만 주민들이 경계하고 있네요. 그들의 신뢰를 얻고 쉴 곳을 찾을 수 있을까요?",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    lineHeight = 20.sp
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
fun StatsGrid() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatBox(
            label = "생존 일수",
            value = "14",
            modifier = Modifier.weight(1f)
        )
        StatBox(
            label = "완료한 퀘스트",
            value = "28",
            modifier = Modifier.weight(1f)
        )
        StatBox(
            label = "학습한 단어",
            value = "150",
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
fun HomeScreenPreview() {
    JScenarioTheme {
        HomeScreen(onStartScenario = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F251A)
@Composable
fun ProgressSectionPreview() {
    JScenarioTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            ProgressSection()
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F251A)
@Composable
fun DailyScenarioCardPreview() {
    JScenarioTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            DailyScenarioCard(onStartScenario = {})
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F251A)
@Composable
fun StatsGridPreview() {
    JScenarioTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            StatsGrid()
        }
    }
}

