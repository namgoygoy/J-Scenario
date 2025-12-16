"""
Interaction and evaluation data models
"""
from pydantic import BaseModel, Field
from typing import Optional
from datetime import datetime


class FeedbackCategory(BaseModel):
    """피드백 카테고리 (발음, 문법, TPO)"""
    name: str = Field(..., description="카테고리 이름")
    score: int = Field(..., ge=0, le=100, description="점수 (0-100)")
    description: str = Field(..., description="상세 설명")
    suggestions: list[str] = Field(default_factory=list, description="개선 제안")
    
    class Config:
        json_schema_extra = {
            "example": {
                "name": "발음",
                "score": 95,
                "description": "명확하고 자연스러움",
                "suggestions": ["「さいふ」의 발음이 더 정확하면 좋습니다"]
            }
        }


class EvaluationResult(BaseModel):
    """평가 결과"""
    overall_score: int = Field(..., ge=0, le=100, description="전체 점수")
    pronunciation: FeedbackCategory = Field(..., description="발음 평가")
    grammar: FeedbackCategory = Field(..., description="문법 평가")
    appropriateness: FeedbackCategory = Field(..., description="적절성(TPO) 평가")
    transcription: str = Field(..., description="음성을 텍스트로 변환한 결과")
    corrected_text: Optional[str] = Field(None, description="교정된 텍스트")
    example_responses: list[str] = Field(default_factory=list, description="모범 답안 예시")
    coaching_advice: str = Field(default="", description="한국어 코칭 조언")


class InteractionRequest(BaseModel):
    """사용자 발화 처리 요청"""
    scenario_id: str = Field(..., description="시나리오 ID")
    user_id: Optional[str] = Field(None, description="사용자 ID (선택)")
    audio_data: Optional[str] = Field(None, description="Base64 인코딩된 오디오 데이터")
    
    class Config:
        json_schema_extra = {
            "example": {
                "scenario_id": "scenario_001",
                "user_id": "user123",
                "audio_data": "base64_encoded_audio_data_here"
            }
        }


class InteractionResponse(BaseModel):
    """사용자 발화 처리 응답"""
    interaction_id: str = Field(..., description="인터랙션 고유 ID")
    scenario_id: str = Field(..., description="시나리오 ID")
    evaluation: EvaluationResult = Field(..., description="평가 결과")
    ai_response_text: str = Field(..., description="AI 캐릭터의 응답 대사")
    ai_response_audio_url: Optional[str] = Field(None, description="AI 응답 음성 URL")
    exp_earned: int = Field(default=0, description="획득한 경험치")
    timestamp: datetime = Field(default_factory=datetime.now, description="처리 시각")
    success: bool = True
    message: str = "평가가 완료되었습니다"
    
    class Config:
        json_schema_extra = {
            "example": {
                "interaction_id": "int_001",
                "scenario_id": "scenario_001",
                "evaluation": {
                    "overall_score": 92,
                    "pronunciation": {
                        "name": "발음",
                        "score": 95,
                        "description": "명확하고 자연스러움",
                        "suggestions": []
                    },
                    "grammar": {
                        "name": "문법",
                        "score": 88,
                        "description": "사소한 오류",
                        "suggestions": ["조사 사용에 주의하세요"]
                    },
                    "appropriateness": {
                        "name": "적절성 (TPO)",
                        "score": 93,
                        "description": "상황에 잘 맞음",
                        "suggestions": []
                    },
                    "transcription": "財布をなくしました。警察に届け出したいです。",
                    "corrected_text": "財布をなくしました。警察に届け出をしたいです。",
                    "example_responses": [
                        "財布を紛失しました。届け出をお願いします。",
                        "財布をなくしてしまいました。遺失物として届け出たいのですが。"
                    ]
                },
                "ai_response_text": "わかりました。詳しくお話を聞かせてください。",
                "ai_response_audio_url": "https://example.com/audio/response_001.mp3",
                "exp_earned": 150,
                "timestamp": "2024-01-01T12:00:00",
                "success": True,
                "message": "평가가 완료되었습니다"
            }
        }

