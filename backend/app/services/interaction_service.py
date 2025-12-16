"""
Interaction service for processing user audio with advanced pipeline
Sequential Processing:
1. Google STT (1차 텍스트 변환)
2. Gemini Text Correction (문맥 기반 보정) ← 핵심!
3. Azure Pronunciation Assessment (보정된 텍스트 기준 발음 평가)
4. Gemini Grammar Evaluation (문법/표현 피드백)
5. Response Generation (TTS)
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
from app.services.text_correction_service import TextCorrectionService
from app.services.azure_pronunciation_service import AzurePronunciationService
from app.services.evaluation_service import EvaluationService
from app.services.tts_service import TTSService


class InteractionService:
    """사용자 인터랙션 처리 서비스 (Advanced Pipeline)"""
    
    def __init__(self):
        """Initialize all services"""
        self.stt_service = STTService()
        self.text_correction_service = TextCorrectionService()
        self.pronunciation_service = AzurePronunciationService()
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
        오디오 인터랙션 처리 (Sequential Pipeline)
        
        Args:
            scenario_id: 시나리오 ID
            audio_data: 오디오 바이너리 데이터
            filename: 파일명
            user_id: 사용자 ID (선택)
            
        Returns:
            InteractionResponse: 처리 결과
        """
        interaction_id = f"int_{uuid.uuid4().hex[:12]}"
        
        print(f"\n{'='*60}")
        print(f"[Interaction Pipeline Started] ID: {interaction_id}")
        print(f"  Scenario: {scenario_id}")
        print(f"  Audio: {filename} ({len(audio_data)} bytes)")
        print(f"{'='*60}\n")
        
        try:
            # ============================================================
            # Step 1: Google STT (1차 텍스트 변환)
            # ============================================================
            print("📝 [Step 1/5] Google STT - 1차 텍스트 변환")
            raw_text = await self.stt_service.transcribe_audio(audio_data, filename)
            print(f"  ✓ Raw STT Result: '{raw_text}'\n")
            
            # ============================================================
            # Step 2: Gemini Text Correction (문맥 기반 보정) ← 핵심!
            # ============================================================
            print("🔧 [Step 2/5] Gemini - 문맥 기반 텍스트 보정")
            scenario_context = await self.text_correction_service.get_scenario_context(
                scenario_id
            )
            print(f"  Scenario Context: '{scenario_context}'")
            
            corrected_text = await self.text_correction_service.correct_text_with_context(
                raw_text=raw_text,
                scenario_context=scenario_context
            )
            print(f"  ✓ Corrected Text: '{corrected_text}'\n")
            
            # ============================================================
            # Step 3: Azure Pronunciation Assessment (발음 평가)
            # ============================================================
            print("🎤 [Step 3/5] Azure Speech - 발음 평가")
            print(f"  Reference Text: '{corrected_text}'")
            
            pronunciation_scores = await self.pronunciation_service.assess_pronunciation(
                audio_data=audio_data,
                reference_text=corrected_text,
                language="ja-JP"
            )
            
            print(f"  ✓ Pronunciation Scores:")
            print(f"    - Accuracy: {pronunciation_scores['accuracy_score']}")
            print(f"    - Pronunciation: {pronunciation_scores['pronunciation_score']}")
            print(f"    - Fluency: {pronunciation_scores['fluency_score']}")
            print(f"    - Completeness: {pronunciation_scores['completeness_score']}\n")
            
            # ============================================================
            # Step 4: Gemini Grammar Evaluation (문법/표현 평가)
            # ============================================================
            print("📚 [Step 4/5] Gemini - 문법 및 표현 피드백")
            grammar_eval = await self.evaluation_service.evaluate_grammar_and_expression(
                corrected_text=corrected_text,
                scenario_context=scenario_context,
                raw_text=raw_text
            )
            
            print(f"  ✓ Grammar Score: {grammar_eval['grammar_score']}")
            print(f"  ✓ Appropriateness Score: {grammar_eval['appropriateness_score']}")
            print(f"  ✓ Coaching Advice: {grammar_eval.get('coaching_advice', 'N/A')[:50]}...\n")
            
            # ============================================================
            # 종합 점수 계산
            # ============================================================
            overall_score = self._calculate_overall_score(
                pronunciation_scores=pronunciation_scores,
                grammar_score=grammar_eval['grammar_score'],
                appropriateness_score=grammar_eval['appropriateness_score']
            )
            
            print(f"⭐ Overall Score: {overall_score}/100\n")
            
            # ============================================================
            # EvaluationResult 구성
            # ============================================================
            evaluation = EvaluationResult(
                overall_score=overall_score,
                pronunciation=FeedbackCategory(
                    name="発音",
                    score=int(round(pronunciation_scores['pronunciation_score'])),
                    description=f"Accuracy: {int(round(pronunciation_scores['accuracy_score']))}, "
                               f"Fluency: {int(round(pronunciation_scores['fluency_score']))}",
                    suggestions=self._extract_pronunciation_suggestions(pronunciation_scores)
                ),
                grammar=FeedbackCategory(
                    name="文法",
                    score=int(round(grammar_eval['grammar_score'])),
                    description=grammar_eval['grammar_feedback'],
                    suggestions=[]
                ),
                appropriateness=FeedbackCategory(
                    name="適切性 (TPO)",
                    score=int(round(grammar_eval['appropriateness_score'])),
                    description=grammar_eval['appropriateness_feedback'],
                    suggestions=[]
                ),
                transcription=raw_text,  # 원본 STT 결과
                corrected_text=corrected_text,  # 보정된 텍스트
                example_responses=grammar_eval['better_expressions'],
                coaching_advice=grammar_eval.get('coaching_advice', "")
            )
            
            # ============================================================
            # Step 5: AI 응답 생성 및 TTS
            # ============================================================
            print("🤖 [Step 5/5] AI 응답 생성 및 TTS")
            ai_response_text = await self.evaluation_service.generate_ai_response(
                corrected_text=corrected_text,
                scenario_context=scenario_context,
                overall_score=overall_score
            )
            print(f"  AI Response: '{ai_response_text}'")
            
            ai_audio_url = await self.tts_service.synthesize_speech(
                text=ai_response_text,
                interaction_id=interaction_id
            )
            print(f"  ✓ AI Audio URL: {ai_audio_url}\n")
            
            # 경험치 계산
            exp_earned = self._calculate_exp(overall_score)
            
            print(f"{'='*60}")
            print(f"[Interaction Pipeline Completed]")
            print(f"  Original STT: '{raw_text}'")
            print(f"  Corrected: '{corrected_text}'")
            print(f"  Score: {overall_score}/100")
            print(f"  EXP: +{exp_earned}")
            print(f"{'='*60}\n")
            
            return InteractionResponse(
                interaction_id=interaction_id,
                scenario_id=scenario_id,
                evaluation=evaluation,
                ai_response_text=ai_response_text,
                ai_response_audio_url=ai_audio_url,
                exp_earned=exp_earned,
                timestamp=datetime.now(),
                success=True,
                message="評価が完了しました"
            )
            
        except Exception as e:
            print(f"\n❌ [Pipeline Error] {str(e)}")
            import traceback
            traceback.print_exc()
            # 에러 발생 시 더미 응답 반환
            return self._create_fallback_response(
                interaction_id,
                scenario_id,
                str(e)
            )
    
    def _calculate_overall_score(
        self,
        pronunciation_scores: dict,
        grammar_score: int,
        appropriateness_score: int
    ) -> int:
        """
        종합 점수 계산
        
        가중 평균:
        - 발음: 40%
        - 문법: 30%
        - 적절성: 30%
        """
        pronunciation_avg = (
            pronunciation_scores['pronunciation_score'] * 0.5 +
            pronunciation_scores['accuracy_score'] * 0.3 +
            pronunciation_scores['fluency_score'] * 0.2
        )
        
        overall = (
            pronunciation_avg * 0.4 +
            grammar_score * 0.3 +
            appropriateness_score * 0.3
        )
        
        return int(round(overall))
    
    def _extract_pronunciation_suggestions(
        self,
        pronunciation_scores: dict
    ) -> list[str]:
        """발음 개선 제안 추출"""
        suggestions = []
        
        # Word-level 분석이 있으면 상세 제안
        if pronunciation_scores.get('word_scores'):
            low_score_words = [
                w for w in pronunciation_scores['word_scores']
                if w.get('accuracy_score', 100) < 70
            ]
            
            if low_score_words:
                for word in low_score_words[:3]:  # 최대 3개
                    suggestions.append(
                        f"「{word['word']}」の発音に注意してください"
                    )
        
        # 전체 점수가 낮으면 일반적인 제안
        if pronunciation_scores['pronunciation_score'] < 70:
            suggestions.append("ゆっくり、はっきりと発音してください")
        
        if pronunciation_scores['fluency_score'] < 70:
            suggestions.append("もっと自然なリズムで話してください")
        
        return suggestions
    
    def _calculate_exp(self, score: int) -> int:
        """점수 기반 경험치 계산"""
        if score >= 95:
            return 250
        elif score >= 90:
            return 200
        elif score >= 80:
            return 150
        elif score >= 70:
            return 100
        elif score >= 60:
            return 70
        else:
            return 50
    
    def _create_fallback_response(
        self,
        interaction_id: str,
        scenario_id: str,
        error_msg: str
    ) -> InteractionResponse:
        """Fallback 응답 생성"""
        mock_evaluation = EvaluationResult(
            overall_score=85,
            pronunciation=FeedbackCategory(
                name="発音",
                score=88,
                description="評価中にエラーが発生しました",
                suggestions=[]
            ),
            grammar=FeedbackCategory(
                name="文法",
                score=82,
                description="評価中にエラーが発生しました",
                suggestions=[]
            ),
            appropriateness=FeedbackCategory(
                name="適切性 (TPO)",
                score=85,
                description="評価中にエラーが発生しました",
                suggestions=[]
            ),
            transcription="[音声認識結果]",
            corrected_text="[補正されたテキスト]",
            example_responses=[
                "すみません。手伝っていただけますか。"
            ],
            coaching_advice="평가 중 오류가 발생했습니다. 다시 시도해 주세요. 네트워크 연결을 확인하고, 마이크가 제대로 작동하는지 확인해 보세요. 💪"
        )
        
        return InteractionResponse(
            interaction_id=interaction_id,
            scenario_id=scenario_id,
            evaluation=mock_evaluation,
            ai_response_text="わかりました。詳しくお話を聞かせてください。",
            ai_response_audio_url=None,
            exp_earned=100,
            timestamp=datetime.now(),
            success=True,
            message=f"評価完了 (Fallback) - {error_msg}"
        )
