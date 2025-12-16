package com.example.j_scenario.ui.screens

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.lerp
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.example.j_scenario.R
import com.example.j_scenario.data.model.InteractionResponse
import com.example.j_scenario.ui.components.AudioPlayer
import com.example.j_scenario.ui.components.CustomProgressBar
import com.example.j_scenario.ui.theme.*
import com.example.j_scenario.ui.viewmodel.FeedbackViewModel
import com.example.j_scenario.utils.ScoreUtils
import com.example.j_scenario.utils.UrlUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
    viewModel: FeedbackViewModel,
    onContinue: () -> Unit,
    onNextChapter: () -> Unit
) {
    val interactionResponse by viewModel.interactionResponse.collectAsState()
    
    // 다음 챕터가 있는지 확인
    val hasNextChapter = viewModel.hasNextChapter()
    
    // 응답이 없으면 기본 데이터 표시 (데모용)
    val response = interactionResponse
    val evaluation = response?.evaluation
    
    // ============================================================
    // 애니메이션 상태 (최상위 레벨)
    // ============================================================
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val targetScore = evaluation?.overallScore ?: 92
    
    // 고유 interaction ID를 key로 사용하여 중복 재생 방지
    val interactionId = response?.interactionId
    
    val scoreAnimatable = remember(interactionId) { Animatable(0f) }
    var isAnimationComplete by remember(interactionId) { mutableStateOf(false) }
    var showCelebration by remember(interactionId) { mutableStateOf(false) }
    
    // SoundPool 초기화
    val soundPool = remember {
        SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()
    }
    
    val risingSoundId = remember {
        try {
            soundPool.load(context, R.raw.rising_sound, 1)
        } catch (e: Exception) {
            -1
        }
    }
    
    // SoundPool 정리
    DisposableEffect(Unit) {
        onDispose {
            soundPool.release()
        }
    }
    
    // 점수 애니메이션 + 효과음 (interactionId가 있을 때만 실행)
    LaunchedEffect(interactionId) {
        // interactionId가 null이면 애니메이션 실행하지 않음
        if (interactionId == null) return@LaunchedEffect
        
        delay(300)
        var streamId = -1
        
        if (risingSoundId != -1) {
            streamId = soundPool.play(risingSoundId, 1.0f, 1.0f, 1, -1, 1.0f)
        }
        
        scoreAnimatable.animateTo(
            targetValue = targetScore.toFloat(),
            animationSpec = tween(durationMillis = 1500)
        ) {
            val progress = if (targetScore > 0) value / targetScore.toFloat() else 0f
            val pitch = 1.0f + (progress * 1.0f)
            if (streamId != -1 && risingSoundId != -1) {
                soundPool.setRate(streamId, pitch)
            }
        }
        
        if (streamId != -1) {
            soundPool.stop(streamId)
        }
        
        isAnimationComplete = true
        
        if (targetScore >= 80) {
            showCelebration = true
        }
    }
    
    val animatedScore = scoreAnimatable.value.toInt()
    val scoreColor = ScoreUtils.getScoreColor(animatedScore)
    
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

            Spacer(modifier = Modifier.height(8.dp))

            // AI 코칭 조언 (한국어)
            evaluation?.let { eval ->
                if (eval.coachingAdvice.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBg)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "🎓 코칭 조언",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = eval.coachingAdvice,
                                fontSize = 13.sp,
                                color = TextPrimary,
                                lineHeight = 20.sp
                            )
                        }
                    }
                } else {
                    // Fallback: 기존 로직
                    Text(
                        text = buildString {
                            when {
                                eval.overallScore >= 85 -> {
                                    append("✨ 훌륭한 응답입니다!\n")
                                    if (eval.grammar.description.isNotEmpty()) {
                                        append("${eval.grammar.description}\n")
                                    }
                                    if (eval.appropriateness.description.isNotEmpty()) {
                                        append("${eval.appropriateness.description}")
                                    }
                                }
                                eval.overallScore >= 70 -> {
                                    append("👍 좋은 시도입니다!\n")
                                    val improvements = mutableListOf<String>()
                                    if (eval.grammar.score < 80) {
                                        improvements.add(eval.grammar.description)
                                    }
                                    if (eval.appropriateness.score < 80) {
                                        improvements.add(eval.appropriateness.description)
                                    }
                                    if (improvements.isNotEmpty()) {
                                        append("개선 포인트: ${improvements.joinToString(" / ")}")
                                    }
                                }
                                eval.overallScore >= 50 -> {
                                    append("💪 연습이 필요합니다!\n")
                                    append("${eval.grammar.description}\n")
                                    append("${eval.appropriateness.description}")
                                }
                                else -> {
                                    append("📚 기초부터 다시 점검해봅시다!\n")
                                    append("${eval.grammar.description}\n")
                                    append("${eval.appropriateness.description}")
                                }
                            }
                        },
                        fontSize = 14.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ============================================================
            // 전체 점수 박스
            // ============================================================
            // 전체 점수 카드
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
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
                        text = "${animatedScore}%",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = scoreColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = ScoreUtils.getScoreMessage(animatedScore) + " " + ScoreUtils.getScoreEmoji(animatedScore),
                        fontSize = 14.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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

            // 버튼: 다음 챕터가 있으면 "다음 챕터로", 없으면 "홈으로"
            if (hasNextChapter) {
                // 다음 챕터 버튼
                Button(
                    onClick = {
                        viewModel.onNextScenario()
                        onNextChapter()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen,
                        contentColor = DarkBg
                    )
                ) {
                    Text(
                        text = "▶ 다음 챕터로",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 홈으로 돌아가기 버튼 (보조)
                Button(
                    onClick = {
                        viewModel.onBackToHome()
                        onContinue()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CardBg,
                        contentColor = TextPrimary
                    )
                ) {
                    Text(
                        text = "홈으로",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            } else {
                // 홈으로 돌아가기 버튼 (메인)
                Button(
                    onClick = {
                        viewModel.onBackToHome()
                        onContinue()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen,
                        contentColor = DarkBg
                    )
                ) {
                    Text(
                        text = "홈으로 돌아가기",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
            
            // 하단 여유 공간 (네비게이션 바 높이 + 추가 여유 공간)
            Spacer(modifier = Modifier.height(112.dp))
            }
            
            // ============================================================
            // 화면 전체 폭죽 애니메이션 (80점 이상, 최상위 레이어)
            // ============================================================
            if (showCelebration) {
                // Lottie 컴포지션 로드
                val composition by rememberLottieComposition(
                    LottieCompositionSpec.RawRes(R.raw.celebration)
                )
                val progress by animateLottieCompositionAsState(
                    composition = composition,
                    iterations = 1, // 1회 재생
                    isPlaying = true
                )
                
                // 화면 전체에 폭죽 애니메이션 표시
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.fillMaxSize()
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
    // 점수 애니메이션 (정수)
    var displayScore by remember { mutableIntStateOf(0) }
    
    val animatedScore by animateIntAsState(
        targetValue = displayScore,
        animationSpec = tween(
            durationMillis = 1500,
            delayMillis = 400
        ),
        label = "category_score_animation"
    )
    
    // 색상 애니메이션을 위한 Float 값
    var displayScoreFloat by remember { mutableFloatStateOf(0f) }
    
    val animatedScoreFloat by animateFloatAsState(
        targetValue = displayScoreFloat,
        animationSpec = tween(
            durationMillis = 1500,
            delayMillis = 400
        ),
        label = "category_score_color_animation"
    )
    
    // 화면 진입 시 애니메이션 시작
    LaunchedEffect(score) {
        displayScore = score
        displayScoreFloat = score.toFloat()
    }
    
    // 실시간 색상 그라데이션
    val scoreColor = ScoreUtils.getScoreColor(animatedScoreFloat.toInt())
    
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
                text = "$animatedScore%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = scoreColor
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        CustomProgressBar(
            progress = animatedScore / 100f,
            modifier = Modifier.fillMaxWidth(),
            score = animatedScore,
            animated = true
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

