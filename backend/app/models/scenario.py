"""
Scenario data models
"""
from pydantic import BaseModel, Field
from typing import Optional
from enum import Enum


class ScenarioCategory(str, Enum):
    """시나리오 카테고리"""
    DAILY = "daily"  # 일상
    EMERGENCY = "emergency"  # 돌발/위기
    BUSINESS = "business"  # 비즈니스
    RELATIONSHIP = "relationship"  # 관계
    TRAVEL = "travel"  # 여행
    SHOPPING = "shopping"  # 쇼핑


class Scenario(BaseModel):
    """시나리오 모델"""
    id: str = Field(..., description="시나리오 고유 ID")
    category: ScenarioCategory = Field(..., description="시나리오 카테고리")
    title: str = Field(..., description="시나리오 제목")
    description: str = Field(..., description="시나리오 설명")
    mission: str = Field(..., description="사용자에게 주어진 미션")
    image_url: str = Field(..., description="시나리오 이미지 URL")
    character_audio_url: Optional[str] = Field(None, description="캐릭터 음성 URL")
    difficulty_level: int = Field(..., ge=1, le=5, description="난이도 (1-5)")
    expected_keywords: list[str] = Field(default_factory=list, description="기대되는 키워드")
    
    class Config:
        json_schema_extra = {
            "example": {
                "id": "scenario_001",
                "category": "emergency",
                "title": "잃어버린 지갑",
                "description": "당신은 지갑을 잃어버렸습니다. 경찰서에서 분실 신고를 해야 합니다.",
                "mission": "경찰관에게 지갑을 잃어버린 경위와 지갑의 특징을 설명하세요.",
                "image_url": "https://example.com/images/lost_wallet.jpg",
                "character_audio_url": "https://example.com/audio/police_greeting.mp3",
                "difficulty_level": 3,
                "expected_keywords": ["財布", "なくしました", "警察", "届け出"]
            }
        }


class ScenarioResponse(BaseModel):
    """시나리오 응답 모델"""
    scenario: Scenario
    success: bool = True
    message: str = "시나리오를 성공적으로 조회했습니다"

