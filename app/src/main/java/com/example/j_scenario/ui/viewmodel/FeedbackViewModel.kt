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
    
    // 현재 시나리오 ID (다음 챕터 확인용)
    private val _currentScenarioId = MutableStateFlow<String?>(null)
    val currentScenarioId: StateFlow<String?> = _currentScenarioId.asStateFlow()
    
    /**
     * 평가 결과 설정
     */
    fun setInteractionResponse(response: InteractionResponse) {
        _interactionResponse.value = response
        _currentScenarioId.value = response.scenarioId
    }
    
    // 챕터별 최대 챕터 수 매핑 (하드코딩)
    private val maxChapters = mapOf(
        "scenario_001" to 3,  // 잃어버린 지갑: 3개 챕터
        // 향후 추가되는 멀티 챕터 시나리오는 여기에 추가
    )
    
    /**
     * 다음 챕터 ID 계산
     * 
     * 예: scenario_001_1 → scenario_001_2
     *     scenario_001_3 → null (마지막 챕터)
     *     scenario_002 → null (단일 챕터)
     */
    fun getNextChapterId(): String? {
        val currentId = _currentScenarioId.value ?: return null
        
        // ID 패턴: scenario_XXX_Y (Y는 챕터 번호)
        val regex = """(.+)_(\d+)$""".toRegex()
        val matchResult = regex.find(currentId)
        
        return if (matchResult != null) {
            val baseId = matchResult.groupValues[1]  // scenario_001
            val currentChapter = matchResult.groupValues[2].toInt()  // 1
            
            // 최대 챕터 수 확인
            val maxChapter = maxChapters[baseId] ?: return null
            
            // 현재 챕터가 마지막이면 null 반환
            if (currentChapter >= maxChapter) {
                android.util.Log.d("FeedbackViewModel", "Last chapter reached: $currentId (max: $maxChapter)")
                return null
            }
            
            val nextChapter = currentChapter + 1
            val nextId = "${baseId}_${nextChapter}"
            android.util.Log.d("FeedbackViewModel", "Next chapter: $currentId → $nextId")
            nextId
        } else {
            // 단일 챕터 시나리오 (예: scenario_002)
            android.util.Log.d("FeedbackViewModel", "Single chapter scenario: $currentId")
            null
        }
    }
    
    /**
     * 다음 챕터가 있는지 확인
     */
    fun hasNextChapter(): Boolean {
        val hasNext = getNextChapterId() != null
        android.util.Log.d("FeedbackViewModel", "Has next chapter: $hasNext (current: ${_currentScenarioId.value})")
        return hasNext
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
        _currentScenarioId.value = null
    }
}
