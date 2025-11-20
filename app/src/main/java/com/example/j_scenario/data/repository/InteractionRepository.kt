package com.example.j_scenario.data.repository

import android.util.Log
import com.example.j_scenario.data.api.JScenarioApiService
import com.example.j_scenario.data.model.InteractionResponse
import com.example.j_scenario.data.model.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

/**
 * 인터랙션 데이터 저장소
 * 
 * 사용자 발화 처리 및 평가를 관리합니다.
 */
class InteractionRepository(
    private val apiService: JScenarioApiService
) {
    
    /**
     * 음성 파일을 전송하여 평가 받기
     * 
     * @param scenarioId 시나리오 ID
     * @param userId 사용자 ID (선택)
     * @param audioFile 음성 파일
     * @return Flow<NetworkResult<InteractionResponse>> 평가 결과
     */
    fun processAudioInteraction(
        scenarioId: String,
        userId: String? = null,
        audioFile: File
    ): Flow<NetworkResult<InteractionResponse>> = flow {
        try {
            emit(NetworkResult.Loading)
            
            // 파일 크기 제한 확인 (10MB)
            val maxSize = 10 * 1024 * 1024
            if (audioFile.length() > maxSize) {
                emit(NetworkResult.Error(
                    Exception("파일 크기가 너무 큽니다"),
                    "파일 크기는 10MB 이하여야 합니다"
                ))
                return@flow
            }
            
            // Multipart 데이터 준비
            val scenarioIdBody = scenarioId.toRequestBody("text/plain".toMediaTypeOrNull())
            val userIdBody = userId?.toRequestBody("text/plain".toMediaTypeOrNull())
            
            val requestFile = audioFile.asRequestBody("audio/*".toMediaTypeOrNull())
            val audioPart = MultipartBody.Part.createFormData(
                "audio_file",
                audioFile.name,
                requestFile
            )
            
            // API 호출
            val response = apiService.processInteraction(
                scenarioId = scenarioIdBody,
                userId = userIdBody,
                audioFile = audioPart
            )
            
            if (response.isSuccessful) {
                val interactionResponse = response.body()
                if (interactionResponse != null && interactionResponse.success) {
                    emit(NetworkResult.Success(interactionResponse))
                } else {
                    emit(NetworkResult.Error(
                        Exception("평가 처리에 실패했습니다"),
                        interactionResponse?.message ?: "알 수 없는 오류"
                    ))
                }
            } else {
                emit(NetworkResult.Error(
                    Exception("HTTP ${response.code()}"),
                    "서버 오류가 발생했습니다"
                ))
            }
        } catch (e: Exception) {
            Log.e(TAG, "processAudioInteraction error", e)
            emit(NetworkResult.Error(
                e,
                "네트워크 연결을 확인해주세요"
            ))
        }
    }.flowOn(Dispatchers.IO)
    
    companion object {
        private const val TAG = "InteractionRepository"
    }
}

