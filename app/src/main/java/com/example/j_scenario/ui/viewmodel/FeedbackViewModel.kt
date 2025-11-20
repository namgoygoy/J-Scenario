package com.example.j_scenario.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.j_scenario.data.model.InteractionResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * FeedbackScreen ViewModel
 * 
 * 평가 결과를 관리하고 표시합니다.
 */
class FeedbackViewModel : ViewModel() {
    
    // 평가 결과
    private val _interactionResponse = MutableStateFlow<InteractionResponse?>(null)
    val interactionResponse: StateFlow<InteractionResponse?> = _interactionResponse.asStateFlow()
    
    /**
     * 평가 결과 설정
     */
    fun setInteractionResponse(response: InteractionResponse) {
        _interactionResponse.value = response
    }
    
    /**
     * 다음 시나리오로 이동 (Navigation 처리는 UI에서)
     */
    fun onNextScenario() {
        // 다음 시나리오 준비 로깅 등
        _interactionResponse.value = null
    }
    
    /**
     * 홈으로 돌아가기
     */
    fun onBackToHome() {
        // 홈 복귀 로깅 등
        _interactionResponse.value = null
    }
}

