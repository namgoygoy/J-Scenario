"""
Evaluation service using Google Gemini API
문법 및 표현 피드백 전담 (발음 평가는 Azure에서 처리)
"""
import json
from typing import Optional, Any
import google.generativeai as genai  # type: ignore
from app.config import get_settings

settings = get_settings()


class EvaluationService:
    """문법 및 표현 피드백 생성 서비스"""
    
    def __init__(self):
        """Initialize evaluation service"""
        self.api_key = settings.gemini_api_key
        self.model: Optional[Any] = None  # type: ignore
        
        print(f"[DEBUG] Gemini API Key present: {bool(self.api_key)}")
        
        if self.api_key:
            try:
                genai.configure(api_key=self.api_key)  # type: ignore
                self.model = genai.GenerativeModel("gemini-2.0-flash")  # type: ignore
                print(f"[DEBUG] Gemini model initialized: {self.model}")
                print("Gemini API initialized for evaluation")
            except Exception as e:
                print(f"Warning: Gemini API initialization failed: {str(e)}")
                import traceback
                traceback.print_exc()
                self.model = None
        else:
            print("[WARNING] No Gemini API key found - using mock responses")
    
    async def evaluate_grammar_and_expression(
        self,
        corrected_text: str,
        scenario_context: str,
        raw_text: str = ""
    ) -> dict:
        """
        보정된 텍스트에 대한 문법 및 표현 평가
        
        Args:
            corrected_text: 보정된 일본어 텍스트
            scenario_context: 시나리오 상황
            raw_text: 원본 STT 텍스트 (교정 전)
            
        Returns:
            dict: {
                "grammar_score": 85,
                "grammar_feedback": "문법적으로 정확합니다",
                "appropriateness_score": 90,
                "appropriateness_feedback": "상황에 매우 적절합니다",
                "better_expressions": ["더 좋은 표현 1", "더 좋은 표현 2"],
                "coaching_advice": "한국어 코칭 조언"
            }
        """
        if self.model is None:
            return self._create_mock_grammar_evaluation()
        
        model = self.model
        
        try:
            prompt = self._create_grammar_evaluation_prompt(
                corrected_text,
                scenario_context,
                raw_text
            )
            
            generation_config = genai.types.GenerationConfig(  # type: ignore
                temperature=0.3,
                top_p=0.95,
                top_k=40,
                max_output_tokens=512,
            )
            
            response = await model.generate_content_async(
                prompt,
                generation_config=generation_config
            )
            
            # 응답 검증 - 디버깅 로그 추가
            print(f"[DEBUG] Gemini Response received")
            
            if not response.candidates or len(response.candidates) == 0:
                print("Warning: No candidates in Gemini grammar evaluation")
                return self._create_mock_grammar_evaluation()
            
            candidate = response.candidates[0]
            print(f"[DEBUG] finish_reason: {candidate.finish_reason} (type: {type(candidate.finish_reason)})")
            
            # finish_reason 체크 완화 (STOP=1 외에도 다른 정상 완료 값 허용)
            # Gemini 2.5에서는 finish_reason이 다를 수 있음
            if hasattr(candidate.finish_reason, 'name'):
                finish_reason_name = candidate.finish_reason.name
                print(f"[DEBUG] finish_reason name: {finish_reason_name}")
                if finish_reason_name not in ['STOP', 'MAX_TOKENS']:
                    print(f"Warning: Gemini grammar evaluation finish_reason={finish_reason_name}")
                    return self._create_mock_grammar_evaluation()
            elif candidate.finish_reason not in [1, 2]:  # 1=STOP, 2=MAX_TOKENS
                print(f"Warning: Gemini grammar evaluation finish_reason={candidate.finish_reason}")
                return self._create_mock_grammar_evaluation()
            
            if not hasattr(response, 'text') or not response.text:
                print("Warning: No text in Gemini grammar evaluation")
                print(f"[DEBUG] response attributes: {dir(response)}")
                return self._create_mock_grammar_evaluation()
            
            response_text = response.text.strip()
            print(f"[DEBUG] Raw response text: {response_text[:200]}...")
            
            # JSON 추출
            if "```json" in response_text:
                json_start = response_text.find("```json") + 7
                json_end = response_text.find("```", json_start)
                response_text = response_text[json_start:json_end].strip()
            elif "```" in response_text:
                json_start = response_text.find("```") + 3
                json_end = response_text.find("```", json_start)
                response_text = response_text[json_start:json_end].strip()
            
            result = json.loads(response_text)
            
            print(f"Grammar Evaluation Result: {result}")
            
            return {
                "grammar_score": result.get("grammar_score", 85),
                "grammar_feedback": result.get("grammar_feedback", ""),
                "appropriateness_score": result.get("appropriateness_score", 90),
                "appropriateness_feedback": result.get("appropriateness_feedback", ""),
                "better_expressions": result.get("better_expressions", []),
                "coaching_advice": result.get("coaching_advice", "")
            }
            
        except Exception as e:
            print(f"Grammar Evaluation Error: {str(e)}")
            import traceback
            traceback.print_exc()
            return self._create_mock_grammar_evaluation()
    
    def _create_grammar_evaluation_prompt(
        self,
        corrected_text: str,
        scenario_context: str,
        raw_text: str = ""
    ) -> str:
        """문법 및 표현 평가 프롬프트 생성"""
        raw_text_info = f"""
**ユーザーの実際の発言（STT原文）:**
{raw_text}
""" if raw_text else ""
        
        return f"""あなたは優しく厳格な日本語コーチです。学習者が成長できるよう、具体的で実践的なアドバイスをしてください。

**状況（シナリオ）:**
{scenario_context}
{raw_text_info}
**ユーザーの発言（補正済み）:**
{corrected_text}

**評価項目:**
1. 文法の正確性（0-100点）
2. 状況への適切性（TPO、0-100点）
3. より良い表現の提案
4. **韓国語コーチングアドバイス（必須）**

**フィードバック原則:**
- 80点以下: 具体的な誤りや改善点を明示
- 81-89点: 改善できる具体的なポイントを提示
- 90-95点: さらに洗練できる点を提案
- 96-100点のみ: "文法的に正確です" または "状況に適切です"

**コーチングアドバイス作成ルール（韓国語で作成）:**
1. 具体的な誤りを指摘 (例: "左右"は"財布"の誤認識)
2. なぜ誤ったのか説明 (例: 音声認識の同音異義語ミス)
3. 正しい表現と使い方を教える (例: "財布をなくしました"が自然)
4. ベストプラクティス提示 (例: "財布を紛失しました。届け出をお願いします。")
5. 励ましの言葉で締めくくる (例: "次はもっと良くなりますよ！")

**コーチング例:**
"좋은 시도예요! 다만 '左右(사유)'는 '財布(사이후, 지갑)'의 음성 인식 오류입니다. 이 상황에서는 '財布をなくしてしまいました'라고 말하는 것이 자연스러워요. 더 격식있게는 '財布を紛失しました。届け出をお願いします。'라고 표현하면 완벽합니다! 다음엔 더 잘하실 거예요 💪"

**出力形式（JSONのみ）:**
{{
    "grammar_score": 点数,
    "grammar_feedback": "文法評価（具体的改善点、30文字以内）",
    "appropriateness_score": 点数,
    "appropriateness_feedback": "TPO評価（具体的改善点、30文字以内）",
    "better_expressions": ["より自然な表現1", "より丁寧な表現2"],
    "coaching_advice": "韓国語で200-300文字の具体的コーチング（必ず上記ルールに従う）"
}}

**重要:** 説明不要。JSON形式のみ出力してください。coaching_adviceは必須です。"""
    
    def _create_mock_grammar_evaluation(self) -> dict:
        """Mock 문법 평가 결과"""
        return {
            "grammar_score": 88,
            "grammar_feedback": "文法的に正確です",
            "appropriateness_score": 92,
            "appropriateness_feedback": "状況に非常に適切です",
            "better_expressions": [
                "財布を紛失しました。届け出をお願いします。",
                "財布をなくしてしまいました。遺失物として届けたいのですが。"
            ],
            "coaching_advice": "좋은 시도예요! 전체적으로 자연스러운 표현이지만, 더 격식있게는 '財布を紛失しました。届け出をお願いします。'라고 표현하면 완벽합니다! 계속 연습하시면 더 잘하실 거예요 💪"
        }
    
    async def generate_ai_response(
        self,
        corrected_text: str,
        scenario_context: str,
        overall_score: int
    ) -> str:
        """
        AI 캐릭터의 응답 생성
        
        Args:
            corrected_text: 보정된 텍스트
            scenario_context: 시나리오 상황
            overall_score: 전체 점수
            
        Returns:
            str: AI 캐릭터의 응답 대사
        """
        if self.model is None:
            return "わかりました。詳しくお話を聞かせてください。"
        
        model = self.model
        
        try:
            prompt = f"""あなたは親切な日本人のキャラクターです。

**状況:**
{scenario_context}

**ユーザーの発言:**
{corrected_text}

**指示:**
上記の状況で、ユーザーの発言に対して自然で助けになる日本語で応答してください。
1文だけで、説明や引用符は不要です。

応答:"""
            
            generation_config = genai.types.GenerationConfig(  # type: ignore
                temperature=0.7,
                top_p=0.95,
                top_k=40,
                max_output_tokens=100,
            )
            
            response = await model.generate_content_async(
                prompt,
                generation_config=generation_config
            )
            
            # 응답 검증 - 디버깅 로그 추가
            print(f"[DEBUG] AI Response - Gemini Response received")
            
            if not response.candidates or len(response.candidates) == 0:
                print("Warning: No candidates in AI response")
                return "わかりました。詳しくお話を聞かせてください。"
            
            candidate = response.candidates[0]
            print(f"[DEBUG] AI Response finish_reason: {candidate.finish_reason}")
            
            # finish_reason 체크 완화
            if hasattr(candidate.finish_reason, 'name'):
                finish_reason_name = candidate.finish_reason.name
                if finish_reason_name not in ['STOP', 'MAX_TOKENS']:
                    print(f"Warning: AI response finish_reason={finish_reason_name}")
                    return "わかりました。詳しくお話を聞かせてください。"
            elif candidate.finish_reason not in [1, 2]:
                print(f"Warning: AI response finish_reason={candidate.finish_reason}")
                return "わかりました。詳しくお話を聞かせてください。"
            
            if not hasattr(response, 'text') or not response.text:
                print("Warning: No text in AI response")
                return "わかりました。詳しくお話を聞かせてください。"
            
            return response.text.strip()
            
        except Exception as e:
            print(f"AI Response Generation Error: {str(e)}")
            return "わかりました。詳しくお話を聞かせてください。"
