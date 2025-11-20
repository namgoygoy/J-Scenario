package com.example.j_scenario.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.j_scenario.data.api.NetworkModule
import com.example.j_scenario.data.model.NetworkResult
import com.example.j_scenario.data.model.Scenario
import com.example.j_scenario.data.repository.ScenarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * HomeScreen ViewModel
 * 
 * 홈 화면에서 일일 시나리오를 관리합니다.
 */
class HomeViewModel : ViewModel() {
    
    private val repository = ScenarioRepository(NetworkModule.apiService)
    
    // 시나리오 로딩 상태
    private val _scenarioState = MutableStateFlow<NetworkResult<Scenario>>(NetworkResult.Loading)
    val scenarioState: StateFlow<NetworkResult<Scenario>> = _scenarioState.asStateFlow()
    
    // 사용자 통계 (임시 데이터)
    private val _userStats = MutableStateFlow(UserStats())
    val userStats: StateFlow<UserStats> = _userStats.asStateFlow()
    
    init {
        loadRandomScenario()
    }
    
    /**
     * 랜덤 시나리오 로드
     */
    fun loadRandomScenario() {
        viewModelScope.launch {
            repository.getRandomScenario()
                .collect { result ->
                    _scenarioState.value = result
                }
        }
    }
    
    /**
     * 시나리오 시작 (Navigation 처리는 UI에서)
     */
    fun onStartScenario() {
        // 시나리오 시작 로깅 등 추가 작업
    }
}

/**
 * 사용자 통계 데이터 클래스
 */
data class UserStats(
    val todayStudyTime: Int = 0,  // 분 단위
    val weekStreak: Int = 0,       // 연속 학습 일수
    val totalScenarios: Int = 0,   // 완료한 시나리오 수
    val averageScore: Int = 0      // 평균 점수
)

