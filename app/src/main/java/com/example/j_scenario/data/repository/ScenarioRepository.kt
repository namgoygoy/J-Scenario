package com.example.j_scenario.data.repository

import android.util.Log
import com.example.j_scenario.data.api.JScenarioApiService
import com.example.j_scenario.data.model.NetworkResult
import com.example.j_scenario.data.model.Scenario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * 시나리오 데이터 저장소
 * 
 * 네트워크를 통해 시나리오 데이터를 가져오고 관리합니다.
 */
class ScenarioRepository(
    private val apiService: JScenarioApiService
) {
    
    /**
     * 랜덤 시나리오 조회
     * 
     * @return Flow<NetworkResult<Scenario>> 시나리오 조회 결과
     */
    fun getRandomScenario(): Flow<NetworkResult<Scenario>> = flow {
        try {
            emit(NetworkResult.Loading)
            
            val response = apiService.getRandomScenario()
            
            if (response.isSuccessful) {
                val scenarioResponse = response.body()
                if (scenarioResponse != null && scenarioResponse.success) {
                    emit(NetworkResult.Success(scenarioResponse.scenario))
                } else {
                    emit(NetworkResult.Error(
                        Exception("시나리오 조회에 실패했습니다"),
                        scenarioResponse?.message ?: "알 수 없는 오류"
                    ))
                }
            } else {
                emit(NetworkResult.Error(
                    Exception("HTTP ${response.code()}"),
                    "서버 오류가 발생했습니다"
                ))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getRandomScenario error", e)
            emit(NetworkResult.Error(
                e,
                "네트워크 연결을 확인해주세요"
            ))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * 특정 시나리오 조회
     * 
     * @param scenarioId 시나리오 ID
     * @return Flow<NetworkResult<Scenario>> 시나리오 조회 결과
     */
    fun getScenarioById(scenarioId: String): Flow<NetworkResult<Scenario>> = flow {
        try {
            emit(NetworkResult.Loading)
            
            val response = apiService.getScenarioById(scenarioId)
            
            if (response.isSuccessful) {
                val scenarioResponse = response.body()
                if (scenarioResponse != null && scenarioResponse.success) {
                    emit(NetworkResult.Success(scenarioResponse.scenario))
                } else {
                    emit(NetworkResult.Error(
                        Exception("시나리오 조회에 실패했습니다"),
                        scenarioResponse?.message ?: "알 수 없는 오류"
                    ))
                }
            } else {
                emit(NetworkResult.Error(
                    Exception("HTTP ${response.code()}"),
                    "서버 오류가 발생했습니다"
                ))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getScenarioById error", e)
            emit(NetworkResult.Error(
                e,
                "네트워크 연결을 확인해주세요"
            ))
        }
    }.flowOn(Dispatchers.IO)
    
    companion object {
        private const val TAG = "ScenarioRepository"
    }
}

