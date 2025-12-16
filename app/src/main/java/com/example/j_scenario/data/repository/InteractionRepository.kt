package com.example.j_scenario.data.repository

import timber.log.Timber
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
            
            // 입력 검증
            validateScenarioId(scenarioId)
            validateAudioFile(audioFile)
            
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
            Timber.e(e, "processAudioInteraction error")
            emit(NetworkResult.Error(
                e,
                "네트워크 연결을 확인해주세요"
            ))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * 시나리오 ID 검증
     */
    private fun validateScenarioId(scenarioId: String) {
        if (scenarioId.isBlank()) {
            throw IllegalArgumentException("시나리오 ID가 비어있습니다")
        }
        // 패턴: scenario_XXX 또는 scenario_XXX_Y
        val pattern = Regex("^scenario_\\d{3}(_\\d+)?$")
        if (!pattern.matches(scenarioId)) {
            throw IllegalArgumentException("잘못된 시나리오 ID 형식입니다: $scenarioId")
        }
    }
    
    /**
     * 오디오 파일 검증
     */
    private fun validateAudioFile(audioFile: File) {
        if (!audioFile.exists()) {
            throw IllegalArgumentException("오디오 파일이 존재하지 않습니다")
        }
        
        if (!audioFile.isFile) {
            throw IllegalArgumentException("파일이 아닙니다")
        }
        
        // 파일 확장자 검증
        val allowedExtensions = setOf(".wav", ".mp3", ".amr", ".m4a", ".ogg", ".flac")
        val fileName = audioFile.name.lowercase()
        val hasValidExtension = allowedExtensions.any { fileName.endsWith(it) }
        
        if (!hasValidExtension) {
            throw IllegalArgumentException(
                "지원하지 않는 파일 형식입니다. 지원 형식: ${allowedExtensions.joinToString(", ")}"
            )
        }
        
        // 최소 파일 크기 검증 (1KB)
        val minSize = 1024
        if (audioFile.length() < minSize) {
            throw IllegalArgumentException("파일 크기가 너무 작습니다. 최소 크기: $minSize bytes")
        }
    }
}

