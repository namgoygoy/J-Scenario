"""
Interaction service for processing user audio and evaluations
"""
import uuid
from datetime import datetime
from typing import Optional
from app.models.interaction import (
    InteractionResponse,
    EvaluationResult,
    FeedbackCategory
)
from app.services.stt_service import STTService
from app.services.evaluation_service import EvaluationService
from app.services.tts_service import TTSService


class InteractionService:
    """사용자 인터랙션 처리 서비스"""
    
    def __init__(self):
        """Initialize services"""
        self.stt_service = STTService()
        self.evaluation_service = EvaluationService()
        self.tts_service = TTSService()
    
    async def process_audio_interaction(
        self,
        scenario_id: str,
        audio_data: bytes,
        filename: str,
        user_id: Optional[str] = None
    ) -> InteractionResponse:
        """
        Process user audio interaction
        
        Args:
            scenario_id: 시나리오 ID
            audio_data: 오디오 바이너리 데이터
            filename: 파일명
            user_id: 사용자 ID (선택)
            
        Returns:
            InteractionResponse: 처리 결과
        """
        interaction_id = f"int_{uuid.uuid4().hex[:12]}"
        
        try:
            # Step 1: STT - 음성을 텍스트로 변환
            print(f"Processing audio: filename={filename}, size={len(audio_data)} bytes")
            transcription = await self.stt_service.transcribe_audio(audio_data, filename)
            print(f"Transcription result: {transcription}")
            
            # Step 2: LLM - 평가 및 피드백 생성
            evaluation = await self.evaluation_service.evaluate_response(
                scenario_id=scenario_id,
                user_text=transcription
            )
            
            # Step 3: AI 응답 생성
            ai_response_text = await self.evaluation_service.generate_ai_response(
                scenario_id=scenario_id,
                user_text=transcription,
                evaluation_score=evaluation.overall_score
            )
            
            # Step 4: TTS - AI 응답을 음성으로 변환
            ai_audio_url = await self.tts_service.synthesize_speech(
                text=ai_response_text,
                interaction_id=interaction_id
            )
            
            # Step 5: 경험치 계산
            exp_earned = self._calculate_exp(evaluation.overall_score)
            
            return InteractionResponse(
                interaction_id=interaction_id,
                scenario_id=scenario_id,
                evaluation=evaluation,
                ai_response_text=ai_response_text,
                ai_response_audio_url=ai_audio_url,
                exp_earned=exp_earned,
                timestamp=datetime.now(),
                success=True,
                message="평가가 완료되었습니다"
            )
            
        except Exception as e:
            # 에러 발생 시 더미 응답 반환 (개발 중)
            return self._create_mock_response(interaction_id, scenario_id, str(e))
    
    def _calculate_exp(self, score: int) -> int:
        """
        Calculate experience points based on score
        
        Args:
            score: 평가 점수 (0-100)
            
        Returns:
            int: 획득 경험치
        """
        if score >= 90:
            return 200
        elif score >= 80:
            return 150
        elif score >= 70:
            return 100
        elif score >= 60:
            return 70
        else:
            return 50
    
    def _create_mock_response(
        self,
        interaction_id: str,
        scenario_id: str,
        error_msg: str = ""
    ) -> InteractionResponse:
        """
        Create a mock response for development/testing
        
        Args:
            interaction_id: 인터랙션 ID
            scenario_id: 시나리오 ID
            error_msg: 에러 메시지 (선택)
            
        Returns:
            InteractionResponse: 목 응답
        """
        mock_evaluation = EvaluationResult(
            overall_score=85,
            pronunciation=FeedbackCategory(
                name="발음",
                score=88,
                description="명확하고 자연스러움",
                suggestions=["더 천천히 발음하면 좋습니다"]
            ),
            grammar=FeedbackCategory(
                name="문법",
                score=82,
                description="대체로 정확함",
                suggestions=["조사 사용에 주의하세요"]
            ),
            appropriateness=FeedbackCategory(
                name="적절성 (TPO)",
                score=85,
                description="상황에 적절함",
                suggestions=[]
            ),
            transcription="[음성 인식 결과]",
            corrected_text="[교정된 텍스트]",
            example_responses=[
                "죄송합니다. 도움을 부탁드립니다.",
                "すみません。手伝っていただけますか。"
            ]
        )
        
        return InteractionResponse(
            interaction_id=interaction_id,
            scenario_id=scenario_id,
            evaluation=mock_evaluation,
            ai_response_text="わかりました。どうしましたか。",
            ai_response_audio_url=None,
            exp_earned=150,
            timestamp=datetime.now(),
            success=True,
            message=f"평가 완료 (Mock 응답) {error_msg}"
        )

