package com.example.j_scenario.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.j_scenario.data.model.InteractionResponse
import com.example.j_scenario.ui.components.AudioPlayer
import com.example.j_scenario.ui.components.CustomProgressBar
import com.example.j_scenario.ui.theme.*
import com.example.j_scenario.ui.viewmodel.FeedbackViewModel
import com.example.j_scenario.utils.UrlUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
    viewModel: FeedbackViewModel,
    onContinue: () -> Unit
) {
    val interactionResponse by viewModel.interactionResponse.collectAsState()
    
    // 응답이 없으면 기본 데이터 표시 (데모용)
    val response = interactionResponse
    val evaluation = response?.evaluation
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            // 메인 타이틀
            Text(
                text = if (evaluation != null && evaluation.overallScore >= 70) {
                    "잘했어요, 생존했습니다!"
                } else {
                    "다시 도전해 보세요!"
                },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 서브 텍스트
            Text(
                text = evaluation?.let {
                    "당신이 말한 내용: \"${it.transcription}\"\n${if (it.correctedText != null) "교정: \"${it.correctedText}\"" else ""}"
                } ?: "응답을 분석했습니다. 당신의 성과는 다음과 같습니다:",
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
                        text = "${evaluation?.overallScore ?: 92}%",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // AI 응답 음성 재생
            response?.aiResponseAudioUrl?.let { audioUrl ->
                // 백엔드 URL이 상대 경로인 경우 절대 경로로 변환
                val fullAudioUrl = UrlUtils.toAbsoluteUrl(audioUrl)
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "AI 응답 듣기",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = response.aiResponseText,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        AudioPlayer(
                            audioUrl = fullAudioUrl,
                            autoPlay = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
            }

            // 상세 피드백
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                evaluation?.let { eval ->
                    FeedbackCategory(
                        title = eval.pronunciation.name,
                        score = eval.pronunciation.score,
                        description = eval.pronunciation.description
                    )
                    FeedbackCategory(
                        title = eval.grammar.name,
                        score = eval.grammar.score,
                        description = eval.grammar.description
                    )
                    FeedbackCategory(
                        title = eval.appropriateness.name,
                        score = eval.appropriateness.score,
                        description = eval.appropriateness.description
                    )
                } ?: run {
                    // 기본 데이터 (데모용)
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
            
            // 하단 여유 공간 (네비게이션 바 높이 + 추가 여유 공간)
            Spacer(modifier = Modifier.height(112.dp))
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

// Preview는 ViewModel 의존성 때문에 제거
// 필요시 mock ViewModel을 사용하여 Preview 구현 가능

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

