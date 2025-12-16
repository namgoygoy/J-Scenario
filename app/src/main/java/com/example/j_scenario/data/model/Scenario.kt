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
) {
    /**
     * 다음 챕터 ID 가져오기
     * 
     * 예: scenario_001_1 → scenario_001_2
     *     scenario_001_3 → null (마지막 챕터)
     *     scenario_002 → null (단일 챕터)
     */
    fun getNextChapterId(): String? {
        // ID 패턴: scenario_XXX_Y (Y는 챕터 번호)
        val regex = """(.+)_(\d+)$""".toRegex()
        val matchResult = regex.find(id)
        
        return if (matchResult != null) {
            val baseId = matchResult.groupValues[1]  // scenario_001
            val currentChapter = matchResult.groupValues[2].toInt()  // 1
            val nextChapter = currentChapter + 1
            "${baseId}_${nextChapter}"  // scenario_001_2
        } else {
            // 단일 챕터 시나리오 (예: scenario_002)
            null
        }
    }
    
    /**
     * 챕터가 있는 시나리오인지 확인
     */
    fun isChapteredScenario(): Boolean {
        return id.matches("""(.+)_(\d+)$""".toRegex())
    }
    
    /**
     * 현재 챕터 번호 가져오기
     * 
     * 예: scenario_001_1 → 1
     *     scenario_002 → null
     */
    fun getCurrentChapter(): Int? {
        val regex = """(.+)_(\d+)$""".toRegex()
        val matchResult = regex.find(id)
        return matchResult?.groupValues?.get(2)?.toInt()
    }
}

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

