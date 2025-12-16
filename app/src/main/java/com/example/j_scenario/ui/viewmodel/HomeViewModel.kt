package com.example.j_scenario.ui.viewmodel

import android.content.Context
import android.content.SharedPreferences
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
import java.text.SimpleDateFormat
import java.util.*

/**
 * HomeScreen ViewModel
 * 
 * 홈 화면에서 일일 시나리오를 관리합니다.
 */
class HomeViewModel(private val context: Context? = null) : ViewModel() {
    
    private val repository = ScenarioRepository(NetworkModule.apiService)
    
    // SharedPreferences 키
    private val PREFS_NAME = "j_scenario_prefs"
    private val KEY_LAST_DATE = "last_date"
    private val KEY_COMPLETED_SCENARIOS = "completed_scenarios_today"
    
    // 통계 관련 키
    private val KEY_TOTAL_SCENARIOS = "total_scenarios"
    private val KEY_TOTAL_SCORE_SUM = "total_score_sum"
    private val KEY_LAST_STUDY_DATE = "last_study_date"
    private val KEY_CURRENT_STREAK = "current_streak"
    private val KEY_STREAK_START_DATE = "streak_start_date"
    
    // 일일 목표 시나리오 수
    private val DAILY_GOAL = 3
    
    // SharedPreferences 인스턴스
    private val prefs: SharedPreferences? = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    // 시나리오 로딩 상태
    private val _scenarioState = MutableStateFlow<NetworkResult<Scenario>>(NetworkResult.Loading)
    val scenarioState: StateFlow<NetworkResult<Scenario>> = _scenarioState.asStateFlow()
    
    // 사용자 통계 (임시 데이터)
    private val _userStats = MutableStateFlow(UserStats())
    val userStats: StateFlow<UserStats> = _userStats.asStateFlow()
    
    // 일일 진행도 (0.0 ~ 1.0)
    private val _dailyProgress = MutableStateFlow(0f)
    val dailyProgress: StateFlow<Float> = _dailyProgress.asStateFlow()
    
    // 오늘 완료한 시나리오 수
    private val _completedScenariosToday = MutableStateFlow(0)
    val completedScenariosToday: StateFlow<Int> = _completedScenariosToday.asStateFlow()
    
    init {
        loadRandomScenario()
        loadDailyProgress()
        loadUserStats()
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
     * 일일 진행도 로드 (SharedPreferences에서)
     */
    private fun loadDailyProgress() {
        if (prefs == null) {
            _dailyProgress.value = 0f
            _completedScenariosToday.value = 0
            return
        }
        
        val today = getTodayDateString()
        val lastDate = prefs.getString(KEY_LAST_DATE, "")
        
        // 날짜가 바뀌었으면 리셋
        if (lastDate != today) {
            resetDailyProgress()
        } else {
            // 오늘 날짜면 진행도 로드
            val completed = prefs.getInt(KEY_COMPLETED_SCENARIOS, 0)
            _completedScenariosToday.value = completed
            _dailyProgress.value = (completed.toFloat() / DAILY_GOAL).coerceIn(0f, 1f)
        }
    }
    
    /**
     * 일일 진행도 리셋 (새로운 날짜)
     */
    private fun resetDailyProgress() {
        if (prefs == null) return
        
        val today = getTodayDateString()
        prefs.edit()
            .putString(KEY_LAST_DATE, today)
            .putInt(KEY_COMPLETED_SCENARIOS, 0)
            .apply()
        
        _completedScenariosToday.value = 0
        _dailyProgress.value = 0f
    }
    
    /**
     * 시나리오 완료 처리 (33.33%씩 증가 + 통계 업데이트)
     * 
     * @param score 시나리오 완료 점수 (0-100)
     */
    fun onScenarioCompleted(score: Int = 0) {
        if (prefs == null) return
        
        val today = getTodayDateString()
        val lastDate = prefs.getString(KEY_LAST_DATE, "")
        
        // 날짜가 바뀌었으면 리셋 후 시작
        if (lastDate != today) {
            resetDailyProgress()
        }
        
        val currentCompleted = _completedScenariosToday.value
        
        // 이미 목표를 달성했으면 더 이상 증가하지 않음
        if (currentCompleted >= DAILY_GOAL) {
            // 진행도는 증가하지 않지만 통계는 업데이트
            updateUserStats(score)
            return
        }
        
        val newCompleted = currentCompleted + 1
        val newProgress = (newCompleted.toFloat() / DAILY_GOAL).coerceIn(0f, 1f)
        
        // SharedPreferences에 저장
        prefs.edit()
            .putString(KEY_LAST_DATE, today)
            .putInt(KEY_COMPLETED_SCENARIOS, newCompleted)
            .apply()
        
        // 상태 업데이트
        _completedScenariosToday.value = newCompleted
        _dailyProgress.value = newProgress
        
        // 통계 업데이트
        updateUserStats(score)
    }
    
    /**
     * 사용자 통계 업데이트
     */
    private fun updateUserStats(score: Int) {
        if (prefs == null) return
        
        val today = getTodayDateString()
        val yesterday = getYesterdayDateString()
        
        // 총 시나리오 수 증가
        val totalScenarios = prefs.getInt(KEY_TOTAL_SCENARIOS, 0) + 1
        
        // 점수 합계 업데이트
        val totalScoreSum = prefs.getInt(KEY_TOTAL_SCORE_SUM, 0) + score
        val averageScore = if (totalScenarios > 0) totalScoreSum / totalScenarios else 0
        
        // 연속 학습 일수 업데이트
        val lastStudyDate = prefs.getString(KEY_LAST_STUDY_DATE, "")
        val currentStreak = prefs.getInt(KEY_CURRENT_STREAK, 0)
        val streakStartDate = prefs.getString(KEY_STREAK_START_DATE, "")
        
        val (newStreak, newStreakStartDate) = when {
            // 오늘 이미 학습했으면 연속 일수 유지 (중복 카운트 방지)
            lastStudyDate == today -> {
                Pair(currentStreak, streakStartDate?.ifEmpty { today } ?: today)
            }
            // 어제 학습했으면 연속 일수 +1
            lastStudyDate == yesterday -> {
                val newStreakValue = currentStreak + 1
                val startDate = if (streakStartDate.isNullOrEmpty() || streakStartDate == yesterday) {
                    yesterday // 연속 학습 시작일
                } else {
                    streakStartDate // 기존 연속 학습 시작일 유지
                }
                Pair(newStreakValue, startDate)
            }
            // 연속이 끊어졌으면 1로 리셋 (오늘부터 새로 시작)
            else -> {
                Pair(1, today)
            }
        }
        
        // SharedPreferences에 저장
        prefs.edit()
            .putInt(KEY_TOTAL_SCENARIOS, totalScenarios)
            .putInt(KEY_TOTAL_SCORE_SUM, totalScoreSum)
            .putString(KEY_LAST_STUDY_DATE, today)
            .putInt(KEY_CURRENT_STREAK, newStreak)
            .putString(KEY_STREAK_START_DATE, newStreakStartDate)
            .apply()
        
        // 상태 업데이트
        _userStats.value = UserStats(
            todayStudyTime = 0, // TODO: 학습 시간 추적 구현 필요
            weekStreak = newStreak,
            totalScenarios = totalScenarios,
            averageScore = averageScore
        )
    }
    
    /**
     * 사용자 통계 로드
     */
    private fun loadUserStats() {
        if (prefs == null) {
            _userStats.value = UserStats()
            return
        }
        
        val totalScenarios = prefs.getInt(KEY_TOTAL_SCENARIOS, 0)
        val totalScoreSum = prefs.getInt(KEY_TOTAL_SCORE_SUM, 0)
        val averageScore = if (totalScenarios > 0) totalScoreSum / totalScenarios else 0
        val currentStreak = prefs.getInt(KEY_CURRENT_STREAK, 0)
        
        // 연속 학습 일수 확인 (날짜가 바뀌었는지 체크)
        val lastStudyDate = prefs.getString(KEY_LAST_STUDY_DATE, "")
        val today = getTodayDateString()
        val yesterday = getYesterdayDateString()
        
        // 연속 학습이 끊어졌는지 확인 (2일 이상 학습하지 않았으면)
        val finalStreak = when {
            lastStudyDate == today -> currentStreak // 오늘 학습함 (연속 유지)
            lastStudyDate == yesterday -> currentStreak // 어제 학습함 (연속 유지, 오늘 학습하면 +1)
            else -> {
                // 연속이 끊어졌으면 0으로 리셋
                if (currentStreak > 0) {
                    prefs.edit()
                        .putInt(KEY_CURRENT_STREAK, 0)
                        .putString(KEY_STREAK_START_DATE, "")
                        .apply()
                }
                0
            }
        }
        
        _userStats.value = UserStats(
            todayStudyTime = 0, // TODO: 학습 시간 추적 구현 필요
            weekStreak = finalStreak,
            totalScenarios = totalScenarios,
            averageScore = averageScore
        )
    }
    
    /**
     * 어제 날짜 문자열 가져오기 (YYYY-MM-DD)
     */
    private fun getYesterdayDateString(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(calendar.time)
    }
    
    /**
     * 오늘 날짜 문자열 가져오기 (YYYY-MM-DD)
     */
    private fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
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

