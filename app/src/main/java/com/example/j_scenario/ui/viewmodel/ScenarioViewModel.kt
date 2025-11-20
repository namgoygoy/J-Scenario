package com.example.j_scenario.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.j_scenario.data.api.NetworkModule
import com.example.j_scenario.data.model.InteractionResponse
import com.example.j_scenario.data.model.NetworkResult
import com.example.j_scenario.data.model.Scenario
import com.example.j_scenario.data.repository.InteractionRepository
import com.example.j_scenario.data.repository.ScenarioRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

/**
 * ScenarioScreen ViewModel
 * 
 * 시나리오 진행 및 음성 녹음/전송을 관리합니다.
 */
class ScenarioViewModel : ViewModel() {
    
    private val scenarioRepository = ScenarioRepository(NetworkModule.apiService)
    private val interactionRepository = InteractionRepository(NetworkModule.apiService)
    
    // 현재 시나리오
    private val _currentScenario = MutableStateFlow<Scenario?>(null)
    val currentScenario: StateFlow<Scenario?> = _currentScenario.asStateFlow()
    
    // 녹음 상태
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()
    
    // 녹음 시간 (초 단위)
    private val _recordingDuration = MutableStateFlow(0)
    val recordingDuration: StateFlow<Int> = _recordingDuration.asStateFlow()
    
    // 녹음된 오디오 파일 (재생용)
    private val _recordedAudioFile = MutableStateFlow<File?>(null)
    val recordedAudioFile: StateFlow<File?> = _recordedAudioFile.asStateFlow()
    
    // 녹음 타이머 Job
    private var recordingTimerJob: Job? = null
    
    // 인터랙션 처리 상태
    private val _interactionState = MutableStateFlow<NetworkResult<InteractionResponse>?>(null)
    val interactionState: StateFlow<NetworkResult<InteractionResponse>?> = _interactionState.asStateFlow()
    
    /**
     * 시나리오 설정
     */
    fun setScenario(scenario: Scenario) {
        _currentScenario.value = scenario
        // 새 시나리오 시작 시 녹음 파일 초기화
        clearRecordedAudio()
        resetInteractionState()
    }
    
    /**
     * 시나리오 ID로 로드
     */
    fun loadScenario(scenarioId: String) {
        viewModelScope.launch {
            scenarioRepository.getScenarioById(scenarioId)
                .collect { result ->
                    if (result is NetworkResult.Success) {
                        _currentScenario.value = result.data
                    }
                }
        }
    }
    
    /**
     * 녹음 시작
     */
    fun startRecording() {
        _isRecording.value = true
        _recordingDuration.value = 0
        startRecordingTimer()
    }
    
    /**
     * 녹음 중지 (저장만 하고 전송하지 않음)
     */
    fun stopRecording(audioFile: File) {
        _isRecording.value = false
        stopRecordingTimer()
        
        // 녹음된 파일 저장 (재생용)
        _recordedAudioFile.value = audioFile
    }
    
    /**
     * 녹음된 파일 제출
     */
    fun submitRecording() {
        val audioFile = _recordedAudioFile.value
        val scenario = _currentScenario.value
        
        if (audioFile == null || scenario == null) {
            return
        }
        
        viewModelScope.launch {
            interactionRepository.processAudioInteraction(
                scenarioId = scenario.id,
                userId = null,
                audioFile = audioFile
            ).collect { result ->
                _interactionState.value = result
            }
        }
    }
    
    /**
     * 녹음 취소
     */
    fun cancelRecording() {
        _isRecording.value = false
        _recordingDuration.value = 0
        stopRecordingTimer()
    }
    
    /**
     * 녹음 타이머 시작
     */
    private fun startRecordingTimer() {
        recordingTimerJob?.cancel()
        recordingTimerJob = viewModelScope.launch {
            while (_isRecording.value) {
                delay(1000)
                _recordingDuration.value += 1
            }
        }
    }
    
    /**
     * 녹음 타이머 중지
     */
    private fun stopRecordingTimer() {
        recordingTimerJob?.cancel()
        recordingTimerJob = null
    }
    
    /**
     * 상태 초기화
     */
    fun resetInteractionState() {
        _interactionState.value = null
    }
    
    /**
     * 녹음된 오디오 파일 초기화
     */
    fun clearRecordedAudio() {
        _recordedAudioFile.value = null
    }
}

