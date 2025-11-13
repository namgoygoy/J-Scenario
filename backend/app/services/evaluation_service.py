"""
Evaluation service using Google Gemini API
"""
import json
from typing import Optional, Any
import google.generativeai as genai  # type: ignore
from app.config import get_settings
from app.models.interaction import EvaluationResult, FeedbackCategory

settings = get_settings()


class EvaluationService:
    """평가 및 피드백 생성 서비스"""
    
    def __init__(self):
        """Initialize evaluation service"""
        self.api_key = settings.gemini_api_key
        self.model: Optional[Any] = None  # type: ignore
        
        if self.api_key:
            try:
                genai.configure(api_key=self.api_key)  # type: ignore
                self.model = genai.GenerativeModel("gemini-2.5-flash")  # type: ignore
            except Exception as e:
                print(f"Warning: Gemini API initialization failed: {str(e)}")
                self.model = None
    
    async def evaluate_response(
        self,
        scenario_id: str,
        user_text: str
    ) -> EvaluationResult:
        """
        Evaluate user's response using Gemini API
        
        Args:
            scenario_id: 시나리오 ID
            user_text: 사용자가 말한 텍스트
            
        Returns:
            EvaluationResult: 평가 결과
        """
        # 타입 체크: self.model이 None이 아님을 확인
        if self.model is None:
            return self._create_mock_evaluation(user_text)
        
        model = self.model
        
        try:
            # Gemini를 사용하여 평가
            prompt = self._create_evaluation_prompt(scenario_id, user_text)
            
            # Gemini API 호출
            generation_config = genai.types.GenerationConfig(  # type: ignore
                temperature=0.3,
                top_p=0.95,
                top_k=40,
                max_output_tokens=1024,
            )
            response = await model.generate_content_async(prompt, generation_config=generation_config)
            
            # 응답 파싱
            response_text = response.text.strip()
            
            # JSON 추출 (마크다운 코드 블록 제거)
            if "```json" in response_text:
                json_start = response_text.find("```json") + 7
                json_end = response_text.find("```", json_start)
                response_text = response_text[json_start:json_end].strip()
            elif "```" in response_text:
                json_start = response_text.find("```") + 3
                json_end = response_text.find("```", json_start)
                response_text = response_text[json_start:json_end].strip()
            
            result = json.loads(response_text)
            
            return EvaluationResult(
                overall_score=result.get("overall_score", 85),
                pronunciation=FeedbackCategory(**result.get("pronunciation", {})),
                grammar=FeedbackCategory(**result.get("grammar", {})),
                appropriateness=FeedbackCategory(**result.get("appropriateness", {})),
                transcription=user_text,
                corrected_text=result.get("corrected_text", user_text),
                example_responses=result.get("example_responses", [])
            )
            
        except Exception as e:
            print(f"Evaluation Error: {str(e)}")
            return self._create_mock_evaluation(user_text)
    
    def _create_evaluation_prompt(self, scenario_id: str, user_text: str) -> str:
        """Create evaluation prompt for Gemini"""
        return f"""당신은 일본어 회화 평가 전문가입니다. 사용자의 일본어 응답을 발음, 문법, 적절성(TPO) 세 가지 기준으로 평가해주세요.

시나리오 ID: {scenario_id}
사용자 응답: {user_text}

다음 JSON 형식으로만 응답해주세요 (다른 설명 없이 JSON만):
{{
    "overall_score": 전체 점수 (0-100),
    "pronunciation": {{
        "name": "발음",
        "score": 점수 (0-100),
        "description": "평가 설명",
        "suggestions": ["개선 제안 1", "개선 제안 2"]
    }},
    "grammar": {{
        "name": "문법",
        "score": 점수 (0-100),
        "description": "평가 설명",
        "suggestions": ["개선 제안 1"]
    }},
    "appropriateness": {{
        "name": "적절성 (TPO)",
        "score": 점수 (0-100),
        "description": "평가 설명",
        "suggestions": []
    }},
    "corrected_text": "교정된 텍스트",
    "example_responses": ["모범 답안 1", "모범 답안 2"]
}}"""
    
    def _create_mock_evaluation(self, user_text: str) -> EvaluationResult:
        """Create mock evaluation for fallback"""
        return EvaluationResult(
            overall_score=92,
            pronunciation=FeedbackCategory(
                name="발음",
                score=95,
                description="명확하고 자연스러움",
                suggestions=[]
            ),
            grammar=FeedbackCategory(
                name="문법",
                score=88,
                description="사소한 오류",
                suggestions=["조사 사용에 주의하세요"]
            ),
            appropriateness=FeedbackCategory(
                name="적절성 (TPO)",
                score=93,
                description="상황에 잘 맞음",
                suggestions=[]
            ),
            transcription=user_text,
            corrected_text=user_text,
            example_responses=[
                "財布を紛失しました。落とし物として届けたいのですが。",
                "財布をなくしてしまいました。見つかりませんでしたか。"
            ]
        )
    
    async def generate_ai_response(
        self,
        scenario_id: str,
        user_text: str,
        evaluation_score: int
    ) -> str:
        """
        Generate AI character's response using Gemini
        
        Args:
            scenario_id: 시나리오 ID
            user_text: 사용자 텍스트
            evaluation_score: 평가 점수
            
        Returns:
            str: AI 캐릭터의 응답 대사
        """
        # 타입 체크: self.model이 None이 아님을 확인
        if self.model is None:
            return "わかりました。詳しくお話を聞かせてください。"
        
        model = self.model
        
        try:
            prompt = f"""당신은 친절한 일본인 캐릭터입니다. 시나리오 상황에서 역무원/상인/캐릭터로서 사용자의 다음 말에 적절하게 응답하세요.

사용자: {user_text}

자연스럽고 도움이 되는 일본어로 한 문장으로만 답변해주세요. 다른 설명 없이 응답만 작성해주세요.
"""
            
            generation_config = genai.types.GenerationConfig(  # type: ignore
                temperature=0.7,
                top_p=0.95,
                top_k=40,
                max_output_tokens=100,
            )
            response = await model.generate_content_async(prompt, generation_config=generation_config)
            
            return response.text.strip()
            
        except Exception as e:
            print(f"AI Response Generation Error: {str(e)}")
            return "わかりました。詳しくお話を聞かせてください。"

