package com.example.j_scenario.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 시나리오 카테고리
 */
enum class ScenarioCategory {
    @Json(name = "daily")
    DAILY,
    
    @Json(name = "emergency")
    EMERGENCY,
    
    @Json(name = "business")
    BUSINESS,
    
    @Json(name = "relationship")
    RELATIONSHIP,
    
    @Json(name = "travel")
    TRAVEL,
    
    @Json(name = "shopping")
    SHOPPING
}

/**
 * 시나리오 모델
 */
@JsonClass(generateAdapter = true)
data class Scenario(
    @Json(name = "id")
    val id: String,
    
    @Json(name = "category")
    val category: ScenarioCategory,
    
    @Json(name = "title")
    val title: String,
    
    @Json(name = "description")
    val description: String,
    
    @Json(name = "mission")
    val mission: String,
    
    @Json(name = "image_url")
    val imageUrl: String,
    
    @Json(name = "character_audio_url")
    val characterAudioUrl: String? = null,
    
    @Json(name = "difficulty_level")
    val difficultyLevel: Int,
    
    @Json(name = "expected_keywords")
    val expectedKeywords: List<String> = emptyList()
)

/**
 * 시나리오 응답 모델
 */
@JsonClass(generateAdapter = true)
data class ScenarioResponse(
    @Json(name = "scenario")
    val scenario: Scenario,
    
    @Json(name = "success")
    val success: Boolean = true,
    
    @Json(name = "message")
    val message: String = ""
)

