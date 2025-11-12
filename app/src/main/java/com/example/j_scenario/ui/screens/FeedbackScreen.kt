package com.example.j_scenario.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.j_scenario.ui.components.CustomProgressBar
import com.example.j_scenario.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(onContinue: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("피드백", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onContinue) {
                        Icon(Icons.Default.Close, contentDescription = "닫기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkGreenBg,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 메인 타이틀
            Text(
                text = "잘했어요, 생존했습니다!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 서브 텍스트
            Text(
                text = "훌륭한 응답이었습니다. 하지만 언제나 개선의 여지는 있죠. 당신의 성과는 다음과 같습니다:",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 전체 점수 박스
            Card(
                modifier = Modifier.padding(horizontal = 32.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "전체 점수",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "92%",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // 상세 피드백
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                FeedbackCategory(
                    title = "발음",
                    score = 95,
                    description = "명확하고 자연스러움"
                )
                FeedbackCategory(
                    title = "문법",
                    score = 88,
                    description = "사소한 오류"
                )
                FeedbackCategory(
                    title = "적절성 (TPO)",
                    score = 93,
                    description = "상황에 잘 맞음"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 계속하기 버튼
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen,
                    contentColor = DarkBg
                )
            ) {
                Text(
                    text = "계속하기",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun FeedbackCategory(
    title: String,
    score: Int,
    description: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "$score%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        CustomProgressBar(
            progress = score / 100f,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = description,
            fontSize = 11.sp,
            color = TextSecondary,
            letterSpacing = 0.5.sp
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F251A)
@Composable
fun FeedbackScreenPreview() {
    JScenarioTheme {
        FeedbackScreen(onContinue = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F251A)
@Composable
fun FeedbackCategoryPreview() {
    JScenarioTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            FeedbackCategory(
                title = "발음",
                score = 95,
                description = "명확하고 자연스러움"
            )
            FeedbackCategory(
                title = "문법",
                score = 88,
                description = "사소한 오류"
            )
            FeedbackCategory(
                title = "적절성 (TPO)",
                score = 93,
                description = "상황에 잘 맞음"
            )
        }
    }
}

