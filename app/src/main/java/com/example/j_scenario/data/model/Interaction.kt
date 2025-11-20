package com.example.j_scenario.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 피드백 카테고리 (발음, 문법, TPO)
 */
@JsonClass(generateAdapter = true)
data class FeedbackCategory(
    @Json(name = "name")
    val name: String,
    
    @Json(name = "score")
    val score: Int,
    
    @Json(name = "description")
    val description: String,
    
    @Json(name = "suggestions")
    val suggestions: List<String> = emptyList()
)

/**
 * 평가 결과
 */
@JsonClass(generateAdapter = true)
data class EvaluationResult(
    @Json(name = "overall_score")
    val overallScore: Int,
    
    @Json(name = "pronunciation")
    val pronunciation: FeedbackCategory,
    
    @Json(name = "grammar")
    val grammar: FeedbackCategory,
    
    @Json(name = "appropriateness")
    val appropriateness: FeedbackCategory,
    
    @Json(name = "transcription")
    val transcription: String,
    
    @Json(name = "corrected_text")
    val correctedText: String? = null,
    
    @Json(name = "example_responses")
    val exampleResponses: List<String> = emptyList()
)

/**
 * 사용자 발화 처리 요청
 */
@JsonClass(generateAdapter = true)
data class InteractionRequest(
    @Json(name = "scenario_id")
    val scenarioId: String,
    
    @Json(name = "user_id")
    val userId: String? = null,
    
    @Json(name = "audio_data")
    val audioData: String? = null
)

/**
 * 사용자 발화 처리 응답
 */
@JsonClass(generateAdapter = true)
data class InteractionResponse(
    @Json(name = "interaction_id")
    val interactionId: String,
    
    @Json(name = "scenario_id")
    val scenarioId: String,
    
    @Json(name = "evaluation")
    val evaluation: EvaluationResult,
    
    @Json(name = "ai_response_text")
    val aiResponseText: String,
    
    @Json(name = "ai_response_audio_url")
    val aiResponseAudioUrl: String? = null,
    
    @Json(name = "exp_earned")
    val expEarned: Int = 0,
    
    @Json(name = "timestamp")
    val timestamp: String,
    
    @Json(name = "success")
    val success: Boolean = true,
    
    @Json(name = "message")
    val message: String = ""
)

