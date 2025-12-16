package com.example.j_scenario.data.api

import com.example.j_scenario.BuildConfig
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
        /**
         * 백엔드 서버 Base URL
         * 
         * BuildConfig를 통해 빌드 타입별로 자동 설정됩니다:
         * - Debug: http://10.0.2.2:8000/api/ (에뮬레이터용)
         * - Release: https://api.jscenario.com/api/ (프로덕션용)
         * 
         * 실제 기기에서 테스트할 경우:
         * app/build.gradle.kts의 debug buildType에서 BASE_URL을 실제 IP로 변경하세요.
         */
        const val BASE_URL = BuildConfig.BASE_URL
    }
}

