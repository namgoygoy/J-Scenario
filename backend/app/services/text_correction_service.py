"""
Text correction service using Google Gemini API
문맥(Context)을 기반으로 STT 결과를 보정하는 서비스
"""
from typing import Optional, Any
import google.generativeai as genai  # type: ignore
from app.config import get_settings

settings = get_settings()


class TextCorrectionService:
    """문맥 기반 텍스트 보정 서비스"""
    
    def __init__(self):
        """Initialize text correction service"""
        self.api_key = settings.gemini_api_key
        self.model: Optional[Any] = None  # type: ignore
        
        print(f"[DEBUG] TextCorrection - Gemini API Key present: {bool(self.api_key)}")
        
        if self.api_key:
            try:
                genai.configure(api_key=self.api_key)  # type: ignore
                self.model = genai.GenerativeModel("gemini-2.0-flash")  # type: ignore
                print(f"[DEBUG] TextCorrection model initialized: {self.model}")
                print("Gemini API initialized for text correction")
            except Exception as e:
                print(f"Warning: Gemini API initialization failed: {str(e)}")
                import traceback
                traceback.print_exc()
                self.model = None
        else:
            print("[WARNING] No Gemini API key found for text correction")
    
    async def correct_text_with_context(
        self,
        raw_text: str,
        scenario_context: str
    ) -> str:
        """
        문맥을 고려하여 STT 결과를 보정
        
        Args:
            raw_text: Google STT로부터 얻은 원본 텍스트
            scenario_context: 현재 시나리오 상황 설명
            
        Returns:
            str: 보정된 일본어 텍스트
        """
        # Gemini가 없으면 원본 그대로 반환
        if self.model is None:
            print("Warning: Gemini not available, returning raw text")
            return raw_text
        
        model = self.model
        
        try:
            # 문맥 기반 보정 프롬프트
            prompt = self._create_correction_prompt(raw_text, scenario_context)
            
            # Gemini API 호출 (간결한 응답을 위해 temperature 낮춤)
            generation_config = genai.types.GenerationConfig(  # type: ignore
                temperature=0.1,  # 창의성 최소화, 정확성 최대화
                top_p=0.9,
                top_k=20,
                max_output_tokens=100,  # 짧은 문장만 필요
            )
            
            response = await model.generate_content_async(
                prompt,
                generation_config=generation_config
            )
            
            # 응답 검증 - 디버깅 로그 추가
            print(f"[DEBUG] TextCorrection - Gemini Response received")
            
            if not response.candidates or len(response.candidates) == 0:
                print("Warning: No candidates in Gemini correction response")
                return raw_text
            
            candidate = response.candidates[0]
            print(f"[DEBUG] TextCorrection finish_reason: {candidate.finish_reason}")
            
            # finish_reason 체크 완화 (Gemini 2.0에서는 값이 다를 수 있음)
            if hasattr(candidate.finish_reason, 'name'):
                finish_reason_name = candidate.finish_reason.name
                print(f"[DEBUG] TextCorrection finish_reason name: {finish_reason_name}")
                if finish_reason_name not in ['STOP', 'MAX_TOKENS']:
                    print(f"Warning: Gemini correction finish_reason={finish_reason_name}")
                    return raw_text
            elif candidate.finish_reason not in [1, 2]:  # 1=STOP, 2=MAX_TOKENS
                print(f"Warning: Gemini correction finish_reason={candidate.finish_reason}")
                return raw_text
            
            if not hasattr(response, 'text') or not response.text:
                print("Warning: No text in Gemini correction response")
                return raw_text
            
            # 보정된 텍스트 추출 (불필요한 공백 제거)
            corrected_text = response.text.strip()
            
            # 혹시 불필요한 설명이 붙어있으면 첫 줄만 추출
            if '\n' in corrected_text:
                corrected_text = corrected_text.split('\n')[0].strip()
            
            # 따옴표 제거 (Gemini가 가끔 따옴표로 감싸서 반환)
            corrected_text = corrected_text.strip('"').strip("'").strip('「').strip('」')
            
            print(f"Text Correction: '{raw_text}' -> '{corrected_text}'")
            
            return corrected_text
            
        except Exception as e:
            print(f"Text Correction Error: {str(e)}")
            import traceback
            traceback.print_exc()
            # 에러 발생 시 원본 반환
            return raw_text
    
    def _create_correction_prompt(self, raw_text: str, scenario_context: str) -> str:
        """
        문맥 기반 텍스트 보정 프롬프트 생성
        
        핵심: Gemini가 불필요한 설명 없이 오직 일본어 문장만 반환하도록 명확히 지시
        """
        return f"""あなたは日本語音声認識の補正専門家です。

**状況（シナリオ）:**
{scenario_context}

**音声認識結果（STT）:**
{raw_text}

**指示:**
上記の状況において、ユーザーが実際に言おうとした日本語の文章を推測し、補正してください。

**補正項目:**
1. 同音異義語の誤認識 (例: 太陽→財布、会計→海底)
2. 不自然な表現 (例: すいません→すみません)
3. 文法的な誤り
4. 状況に合わない単語の置き換え

**補正例:**
- 入力: "すいませんですか？私、太陽をなくしてしまいました。"
- 補正: "すみません。私、財布をなくしてしまいました。"

**重要:**
- 説明は一切不要です
- 補正された日本語の文章だけを1行で出力してください
- 引用符やコメントは付けないでください
- 元の文章に誤りがなければそのまま返してください

補正結果:"""
    
    async def get_scenario_context(self, scenario_id: str) -> str:
        """
        시나리오 ID로부터 상황 설명 추출
        
        Args:
            scenario_id: 시나리오 ID
            
        Returns:
            str: 시나리오 상황 설명
        """
        # TODO: 실제로는 데이터베이스나 scenarios.json에서 조회
        # 현재는 하드코딩된 매핑 사용
        scenario_contexts = {
            # Scenario 001 - 3 Chapters (잃어버린 지갑)
            "scenario_001_1": "電車の駅で財布をなくしました。駅員に紛失届を出したいです。財布の特徴を説明します。",
            "scenario_001_2": "駅員が似たような財布を見つけました。本人のものか確認するために中身を説明します。",
            "scenario_001_3": "財布が見つかりました。内容物を確認し、駅員に感謝の気持ちを伝えます。",
            
            # 기존 시나리오들
            "scenario_001": "電車の駅で財布をなくしました。駅員に紛失届を出したいです。",
            "scenario_002": "日本の会社で同僚と会議の日程を調整しています。",
            "scenario_003": "日本のホテルに到着し、フロントでチェックインをします。",
            "scenario_004": "急にお腹が痛くなり、病院で医師に症状を説明します。",
            "scenario_005": "重要なクライアントとの初回ミーティングで自己紹介をします。",
            "scenario_006": "暗い路地で怪しい男たちに追われており、警察に通報します。",
            "scenario_007": "予期せぬ事故で重要な会議に30分遅刻しました。",
            "scenario_008": "会社の監査チームからプロジェクト経費について質問されています。",
        }
        
        return scenario_contexts.get(
            scenario_id,
            "日本語会話の練習をしています。"  # 기본값
        )

