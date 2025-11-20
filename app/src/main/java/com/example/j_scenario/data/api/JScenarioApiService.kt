package com.example.j_scenario.data.api

import com.example.j_scenario.data.model.InteractionResponse
import com.example.j_scenario.data.model.ScenarioResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * J-Scenario Backend API Service
 */
interface JScenarioApiService {
    
    /**
     * 랜덤 시나리오 조회
     * 
     * @return 랜덤으로 선택된 시나리오
     */
    @GET("scenarios/random")
    suspend fun getRandomScenario(): Response<ScenarioResponse>
    
    /**
     * 특정 시나리오 조회
     * 
     * @param scenarioId 시나리오 ID
     * @return 요청한 시나리오
     */
    @GET("scenarios/{scenario_id}")
    suspend fun getScenarioById(
        @Path("scenario_id") scenarioId: String
    ): Response<ScenarioResponse>
    
    /**
     * 사용자 발화 처리 및 평가
     * 
     * @param scenarioId 시나리오 ID
     * @param userId 사용자 ID (선택)
     * @param audioFile 음성 파일
     * @return 평가 결과 및 AI 응답
     */
    @Multipart
    @POST("interactions")
    suspend fun processInteraction(
        @Part("scenario_id") scenarioId: RequestBody,
        @Part("user_id") userId: RequestBody? = null,
        @Part audioFile: MultipartBody.Part
    ): Response<InteractionResponse>
    
    companion object {
        // 실제 기기 테스트: 컴퓨터의 IP 주소 사용 (예: 192.168.123.172)
        // 에뮬레이터 테스트: "http://10.0.2.2:8000/api/" 사용
        const val BASE_URL = "http://10.0.2.2:8000/api/"
        
        // IP 주소 확인 방법:
        // macOS/Linux: ifconfig | grep "inet " | grep -v 127.0.0.1
        // Windows: ipconfig (IPv4 주소 확인)
        // 
        // 주의: 에뮬레이터와 실제 기기 간 전환 시 이 값을 변경해야 합니다.
        // - 에뮬레이터: 10.0.2.2
        // - 실제 기기: 컴퓨터의 로컬 네트워크 IP (예: 192.168.123.172)
    }
}

