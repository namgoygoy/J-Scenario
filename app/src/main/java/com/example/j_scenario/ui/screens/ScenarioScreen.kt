package com.example.j_scenario.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.j_scenario.data.model.Scenario
import com.example.j_scenario.data.model.ScenarioCategory
import com.example.j_scenario.ui.components.AudioPlayer
import com.example.j_scenario.ui.theme.*
import com.example.j_scenario.ui.viewmodel.ScenarioViewModel
import com.example.j_scenario.utils.AudioRecorder
import com.example.j_scenario.utils.UrlUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScenarioScreen(
    viewModel: ScenarioViewModel,
    onBack: () -> Unit,
    onSubmitSuccess: () -> Unit
) {
    val context = LocalContext.current
    val currentScenario by viewModel.currentScenario.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val recordingDuration by viewModel.recordingDuration.collectAsState()
    val recordedAudioFile by viewModel.recordedAudioFile.collectAsState()
    
    // AudioRecorder 인스턴스
    val audioRecorder = remember { AudioRecorder(context) }
    
    // 권한 요청 Launcher
    var hasRecordPermission by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasRecordPermission = isGranted
        if (isGranted) {
            startRecordingAudio(audioRecorder, viewModel)
        }
    }
    
    // 시나리오가 없으면 로딩 표시
    if (currentScenario == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PrimaryGreen)
        }
        return
    }
    
    val scenario = currentScenario!!
    
    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            if (audioRecorder.isRecording()) {
                audioRecorder.cancelRecording()
                viewModel.cancelRecording()
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(scenario.title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
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
            // 시나리오 이미지
            // 백엔드 URL이 상대 경로인 경우 절대 경로로 변환
            val fullImageUrl = remember(scenario.imageUrl) {
                UrlUtils.toAbsoluteUrl(scenario.imageUrl)
            }
            
            AsyncImage(
                model = fullImageUrl,
                contentDescription = scenario.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .aspectRatio(16f / 9f),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 캐릭터 음성 재생 (자동 재생)
            scenario.characterAudioUrl?.let { audioUrl ->
                // 백엔드 URL이 상대 경로인 경우 절대 경로로 변환
                val fullAudioUrl = remember(audioUrl) {
                    UrlUtils.toAbsoluteUrl(audioUrl)
                }
                
                LaunchedEffect(scenario.id) {
                    android.util.Log.d("ScenarioScreen", "Character audio URL: $audioUrl -> $fullAudioUrl")
                }
                
                AudioPlayer(
                    audioUrl = fullAudioUrl,
                    autoPlay = true,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } ?: run {
                LaunchedEffect(scenario.id) {
                android.util.Log.w("ScenarioScreen", "No character audio URL for scenario: ${scenario.id}")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 시나리오 설명
            Text(
                text = scenario.description,
                fontSize = 16.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 미션 카드
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "🎯 미션",
                        fontSize = 14.sp,
                        color = PrimaryGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = scenario.mission,
                        fontSize = 15.sp,
                        color = TextPrimary,
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // 녹음 버튼 (녹음된 파일이 없을 때만 표시)
            if (recordedAudioFile == null) {
                RecordingButton(
                    isRecording = isRecording,
                    recordingDuration = recordingDuration,
                    onRecordClick = {
                        if (isRecording) {
                            stopRecordingAudio(audioRecorder, viewModel)
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            if (isRecording) {
                Text(
                    text = "* 녹음을 마치려면 버튼을 다시 누르세요",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
            
            // 녹음된 오디오 재생 및 제출 섹션
            recordedAudioFile?.let { audioFile ->
                Spacer(modifier = Modifier.height(24.dp))
                
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
                            text = "내 녹음 확인",
                            fontSize = 16.sp,
                            color = PrimaryGreen,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "녹음된 음성을 들어보고 제출하세요",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // AudioPlayer로 로컬 파일 재생
                        AudioPlayer(
                            audioUrl = "file://${audioFile.absolutePath}",
                            autoPlay = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // 제출 및 재녹음 버튼
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // 재녹음 버튼
                            Button(
                                onClick = {
                                    audioFile.delete()
                                    viewModel.clearRecordedAudio()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CardBg,
                                    contentColor = TextPrimary
                                )
                            ) {
                                Text(
                                    text = "재녹음",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            
                            // 제출 버튼
                            Button(
                                onClick = {
                                    viewModel.submitRecording()
                                    onSubmitSuccess()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryGreen,
                                    contentColor = DarkBg
                                ),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 6.dp,
                                    pressedElevation = 10.dp
                                )
                            ) {
                                Text(
                                    text = "✓ 제출하기",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
            
            // 하단 여유 공간 (네비게이션 바 높이 + 추가 여유 공간)
            Spacer(modifier = Modifier.height(112.dp))
            }
        }
    }
}

/**
 * 녹음 버튼 컴포저블
 */
@Composable
fun RecordingButton(
    isRecording: Boolean,
    recordingDuration: Int,
    onRecordClick: () -> Unit
) {
    // 녹음 중일 때 펄스 애니메이션
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
            Button(
            onClick = onRecordClick,
                modifier = Modifier
                    .width(300.dp)
                .height(56.dp)
                .scale(if (isRecording) scale else 1f),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRecording) MaterialTheme.colorScheme.error else PrimaryGreen,
                    contentColor = DarkBg
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                )
            ) {
                Icon(
                if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                text = if (isRecording) "녹음 중지 ${formatDuration(recordingDuration)}" else "녹음하기",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
    }
}

/**
 * 녹음 시작 헬퍼 함수
 */
private fun startRecordingAudio(
    audioRecorder: AudioRecorder,
    viewModel: ScenarioViewModel
) {
    val file = audioRecorder.startRecording()
    if (file != null) {
        viewModel.startRecording()
    }
}

/**
 * 녹음 중지 헬퍼 함수 (전송하지 않음)
 */
private fun stopRecordingAudio(
    audioRecorder: AudioRecorder,
    viewModel: ScenarioViewModel
) {
    val audioFile = audioRecorder.stopRecording()
    if (audioFile != null && audioFile.exists()) {
        viewModel.stopRecording(audioFile)
    }
}

/**
 * 시간 포맷 헬퍼 함수
 */
private fun formatDuration(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("(%02d:%02d)", mins, secs)
}

// Preview는 ViewModel 의존성 때문에 제거

