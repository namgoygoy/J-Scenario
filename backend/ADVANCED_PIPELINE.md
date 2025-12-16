# J-Scenario Advanced Audio Processing Pipeline

## 🎯 개요

단순 STT → 평가가 아닌, **문맥 기반 텍스트 보정 + 정밀 발음 평가**를 통한 고급 음성 처리 파이프라인입니다.

## 🔥 핵심 차별화 포인트

### 기존 방식의 문제점
```
사용자: "고히 쿠다사이" (발음 구림)
  ↓
STT: "孤児ください" (?! 고아를 주세요)
  ↓
평가: "문법 오류입니다" (❌ 잘못된 피드백)
```

### 우리의 방식 (UX 최적화)
```
사용자: "고히 쿠다사이" (발음 구림)
  ↓
[Step 1] Google STT: "孤児ください"
  ↓
[Step 2] Gemini 보정: "카페 상황이니까... 'コーヒーください'를 말하려던 거구나!"
  ↓
[Step 3] Azure 발음 평가: 오디오 vs "コーヒーください" 비교
  → "발음이 부정확합니다. 'コ'를 길게 발음하세요" (✅ 정확한 피드백)
  ↓
[Step 4] Gemini 문법 평가: "문법은 정확하나, '珈琲をお願いします'가 더 자연스럽습니다"
```

## 📊 Sequential Processing Pipeline

```
┌─────────────────────────────────────────────────────────────┐
│                   Audio File (User Voice)                   │
└────────────────────┬────────────────────────────────────────┘
                     │
         ┌───────────▼───────────┐
         │   Step 1: Google STT  │  1차 텍스트 변환
         │   (Raw Transcription) │  동음이의어 오류 포함 가능
         └───────────┬───────────┘
                     │ raw_text
         ┌───────────▼────────────┐
         │ Step 2: Gemini         │  문맥 기반 텍스트 보정 ⭐
         │ (Context Correction)   │  시나리오 상황 고려
         └───────────┬────────────┘
                     │ corrected_text
         ┌───────────▼─────────────┐
         │ Step 3: Azure Speech    │  발음 정확도 평가
         │ (Pronunciation Check)   │  Reference = corrected_text
         └───────────┬─────────────┘
                     │ pronunciation_scores
         ┌───────────▼─────────────┐
         │ Step 4: Gemini          │  문법/표현 피드백
         │ (Grammar & Expression)  │  
         └───────────┬─────────────┘
                     │ grammar_eval
         ┌───────────▼─────────────┐
         │ Step 5: Response Gen    │  AI 응답 생성 + TTS
         │ (AI Reply + TTS)        │  
         └───────────┬─────────────┘
                     │
              ┌──────▼──────┐
              │   Result    │
              │   {JSON}    │
              └─────────────┘
```

## 🛠️ 기술 스택

| Component | Technology | Purpose |
|-----------|-----------|---------|
| 1차 STT | Google Cloud Speech-to-Text | 음성 → 텍스트 변환 (raw) |
| 문맥 보정 | Google Gemini API | 시나리오 기반 텍스트 보정 |
| 발음 평가 | Azure Cognitive Speech | Reference 기준 음소 분석 |
| 문법 평가 | Google Gemini API | 문법/표현 피드백 |
| 응답 생성 | Google Gemini API | AI 캐릭터 응답 |
| TTS | Google Cloud TTS | 응답 음성 합성 |

## 📝 API 응답 예시

### Request
```bash
POST /api/interactions
Content-Type: multipart/form-data

- scenario_id: "scenario_001"  # 카페에서 주문
- audio_file: user_voice.amr
```

### Response
```json
{
  "interaction_id": "int_abc123def456",
  "scenario_id": "scenario_001",
  "evaluation": {
    "overall_score": 87,
    "pronunciation": {
      "name": "発音",
      "score": 85,
      "description": "Accuracy: 82, Fluency: 88",
      "suggestions": [
        "「コーヒー」の発音に注意してください",
        "ゆっくり、はっきりと発音してください"
      ]
    },
    "grammar": {
      "name": "文法",
      "score": 92,
      "description": "文法的に正確です",
      "suggestions": []
    },
    "appropriateness": {
      "name": "適切性 (TPO)",
      "score": 88,
      "description": "状況に適切です",
      "suggestions": []
    },
    "transcription": "孤児ください",  // ← 원본 STT (디버깅용)
    "corrected_text": "コーヒーください",  // ← 보정된 텍스트
    "example_responses": [
      "珈琲をお願いします。",
      "コーヒーを一つください。"
    ]
  },
  "ai_response_text": "かしこまりました。ホットですか、アイスですか。",
  "ai_response_audio_url": "/uploads/audio/int_abc123def456_response.mp3",
  "exp_earned": 150,
  "timestamp": "2025-11-23T12:34:56",
  "success": true,
  "message": "評価が完了しました"
}
```

## 🔧 설치 및 설정

### 1. 의존성 설치
```bash
cd backend
pip install -r requirements.txt
```

### 2. 환경 변수 설정
```bash
cp env.example.txt .env
```

`.env` 파일 편집:
```env
# Google Gemini API
GEMINI_API_KEY=your_gemini_api_key

# Google Cloud (STT, TTS)
GOOGLE_APPLICATION_CREDENTIALS=./your-credentials.json

# Azure Speech (발음 평가)
AZURE_SPEECH_KEY=your_azure_key
AZURE_SPEECH_REGION=japaneast
```

### 3. API 키 발급

#### Google Gemini API
1. https://makersuite.google.com/app/apikey 방문
2. API 키 생성
3. `.env`에 `GEMINI_API_KEY` 설정

#### Google Cloud (STT/TTS)
1. https://console.cloud.google.com 방문
2. Speech-to-Text API, Text-to-Speech API 활성화
3. 서비스 계정 생성 및 JSON 키 다운로드
4. `.env`에 경로 설정

#### Azure Speech Services
1. https://portal.azure.com 방문
2. Cognitive Services → Speech 리소스 생성
3. 키 및 지역 확인
4. `.env`에 설정

### 4. 서버 실행
```bash
python run.py
```

서버: http://localhost:8000
API 문서: http://localhost:8000/docs

## 🧪 테스트

```bash
# API 테스트
python test_api.py

# 또는 curl
curl -X POST "http://localhost:8000/api/interactions" \
  -H "Content-Type: multipart/form-data" \
  -F "scenario_id=scenario_001" \
  -F "audio_file=@test_audio.amr"
```

## 💡 포트폴리오 포인트

### 1. 사용자 경험 최적화
- 부정확한 발음도 문맥으로 이해
- "내가 뭘 말하려는지 알아주는" UX

### 2. 정밀한 피드백
- 단순 STT 평가가 아닌 음소 단위 분석
- 어느 부분이 틀렸는지 구체적 제시

### 3. Sequential Pipeline
- 각 단계의 출력이 다음 단계의 입력
- 에러 전파 방지 (Fallback 전략)

### 4. 멀티 AI 활용
- Google Gemini: 자연어 이해, 보정, 평가
- Azure Speech: 음성 분석
- 각 서비스의 강점 활용

## 📚 참고 자료

- [Azure Speech Pronunciation Assessment](https://learn.microsoft.com/azure/ai-services/speech-service/how-to-pronunciation-assessment)
- [Google Gemini API](https://ai.google.dev/gemini-api/docs)
- [Google Cloud Speech-to-Text](https://cloud.google.com/speech-to-text)

---

**구현일**: 2025-11-23
**버전**: 2.0.0 (Advanced Pipeline)

