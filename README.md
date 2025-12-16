# J-Scenario

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Python](https://img.shields.io/badge/Python-3776AB?style=for-the-badge&logo=python&logoColor=white)
![FastAPI](https://img.shields.io/badge/FastAPI-009688?style=for-the-badge&logo=fastapi&logoColor=white)
![Google Cloud](https://img.shields.io/badge/Google%20Cloud-4285F4?style=for-the-badge&logo=googlecloud&logoColor=white)
![Azure](https://img.shields.io/badge/Azure%20Speech-0078D4?style=for-the-badge&logo=microsoftazure&logoColor=white)

> **살아남아라! 실전 일본어 서바이벌** — AI 기반 롤플레잉으로 실전 일본어 회화 능력을 극대화하는 학습 앱

---

## Demo

[여기에 데모 영상 첨부]

---

## Description

**J-Scenario**는 일본어 중급 학습자(JLPT N3~N2)를 위한 **시나리오 기반 롤플레잉 회화 학습 앱**입니다.

기존의 일본어 학습 앱들이 정해진 스크립트를 반복하는 방식에 머물러 있는 반면, J-Scenario는 **예측 불가능한 실제 상황**을 제시하고 사용자가 **자신의 목소리로 직접 대응**하도록 합니다. AI가 사용자의 발화를 실시간으로 분석하여 발음, 문법, 상황 적절성(TPO)에 대한 상세한 피드백을 제공합니다.

### 프로젝트 배경

- 많은 일본어 학습자들이 문법과 어휘 지식은 갖추었으나, **실제 대화 상황에서 순발력 있게 대응하는 데 어려움**을 겪습니다.
- 기존 학습 앱들은 **정해진 문장을 따라 읽는 방식**에 그쳐, 실전 회화 능력 향상에 한계가 있습니다.
- 일본 여행, 유학, 취업을 앞둔 학습자들에게 **실전과 같은 연습 환경**이 필요합니다.

### Why This Project? (Key Differentiators)

기존의 발음 평가 앱들은 **미리 정해진 문장**만 평가할 수 있습니다. 사용자는 주어진 문장을 그대로 읽어야만 발음 점수를 받을 수 있죠.

**J-Scenario는 다릅니다.**

[여기에 파이프라인 다이어그램 이미지 첨부]

이 파이프라인의 핵심은 **2단계 Gemini 문맥 보정**입니다:

1. **STT 오류 교정**: 음성 인식 과정에서 발생하는 오탈자를 시나리오 문맥에 맞게 보정
2. **Reference Text 생성**: Azure 발음 평가를 위한 정확한 기준 텍스트 제공

이를 통해 사용자는 **정해진 문장이 아닌, 즉석에서 만든 자유로운 문장**으로도 정확한 발음 평가를 받을 수 있습니다. 마치 실제 일본인과 대화하듯, 상황에 맞는 자신만의 표현을 구사하고 그에 대한 피드백을 받는 것이 가능합니다.

---

## Main Features

### 시나리오 기반 롤플레잉
- **다양한 상황**: 긴급상황, 비즈니스, 여행 등 10개 이상의 시나리오
- **멀티 챕터**: 하나의 스토리가 여러 챕터로 연결되어 몰입감 있는 학습
- **실감 나는 연출**: AI 생성 이미지와 캐릭터 음성으로 높은 몰입감 제공

### 음성 인식 및 AI 평가
- **실시간 음성 녹음**: 마이크 버튼으로 간편하게 응답 녹음 (WAV 16kHz mono)
- **다각적 평가**: 발음 정확도, 문법, 상황 적절성(TPO) 3가지 기준
- **상세 피드백**: 점수와 함께 구체적인 개선 포인트 및 모범 답안 제공
- **문맥 기반 보정**: STT 오류를 시나리오 문맥에 맞게 자동 보정

### AI 캐릭터 응답
- **문맥 기반 응답**: 사용자의 발화 내용과 점수에 따른 자연스러운 AI 응답
- **음성 합성**: 일본어 네이티브 음성으로 AI 캐릭터 대사 재생

### 게임화 요소
- **경험치 시스템**: 시나리오 완료 시 점수에 따른 EXP 획득
- **일일 진행도**: 하루 목표 달성을 위한 프로그레스 바
- **연속 학습 일수**: 스트릭 시스템으로 학습 동기 부여
- **시각적 피드백**: 점수 애니메이션, 80점 이상 시 축하 이펙트

### 보안 및 안정성
- **입력 검증**: 파일 타입, 크기, 시나리오 ID 패턴 검증
- **보안 통신**: 개발 환경에서만 HTTP 허용, 프로덕션은 HTTPS만 허용
- **에러 처리**: 통합 로깅 시스템 (Timber/loguru) 및 사용자 친화적 에러 메시지

---

## System Architecture

<img width="2816" height="1536" alt="Gemini_Generated_Image_a0p3dea0p3dea0p3" src="https://github.com/user-attachments/assets/a2abcea3-5f62-4bd2-9a55-e7d575e3db41" />

---

## Logic Flow (Sequence Diagram)

<div align="center">
  <img width="871" height="575" alt="KakaoTalk_20251118_230927316" src="https://github.com/user-attachments/assets/29708c40-cf94-45cc-b947-0ca203d8f2e9" />
</div>

---

## API Endpoints

### Scenarios
- `GET /api/scenarios/random` - 랜덤 시나리오 조회
- `GET /api/scenarios/{scenario_id}` - 특정 시나리오 조회

### Interactions
- `POST /api/interactions` - 사용자 발화 처리 및 평가
  - 요청: `multipart/form-data`
    - `scenario_id` (필수): 시나리오 ID (형식: `scenario_XXX` 또는 `scenario_XXX_Y`)
    - `user_id` (선택): 사용자 ID
    - `audio_file` (필수): 오디오 파일 (WAV, MP3, AMR 등, 최대 10MB)
  - 응답: 평가 결과 및 AI 응답

### Health Check
- `GET /health` - 서버 상태 확인
- `GET /` - API 정보

**상세 API 문서**: 서버 실행 후 http://localhost:8000/docs 에서 확인 가능

---

## Stack

### Language
| Category | Technology |
|----------|------------|
| Android | Kotlin |
| Backend | Python 3.11+ |

### Framework & Library
| Category | Technology |
|----------|------------|
| Android UI | Jetpack Compose |
| Android Architecture | MVVM, Coroutines, Flow |
| Android Networking | Retrofit2, Moshi |
| Android Media | MediaRecorder, ExoPlayer |
| Android Logging | Timber |
| Animation | Lottie |
| Backend | FastAPI |
| Backend Logging | loguru |

### AI/ML Services
| Category | Technology |
|----------|------------|
| Speech-to-Text | Google Cloud Speech-to-Text |
| Pronunciation Assessment | Azure Cognitive Services Speech |
| LLM (Text Correction & Evaluation) | Google Gemini API |
| Text-to-Speech | Google Cloud TTS |

### Database & Storage
| Category | Technology |
|----------|------------|
| Backend DB | SQLite (Development) |
| File Storage | Local File System |

### Infrastructure
| Category | Technology |
|----------|------------|
| Backend Server | Uvicorn (ASGI) |
| API Documentation | Swagger UI, ReDoc |
| Logging | Timber (Android), loguru (Backend) |
| Input Validation | Pydantic Validators, Custom Validators |

---

## Project Structure

```
JScenario/
├── app/                                    # Android Application
│   └── src/main/
│       ├── java/com/example/j_scenario/
│       │   ├── data/
│       │   │   ├── api/                    # Retrofit API Service
│       │   │   │   ├── JScenarioApiService.kt
│       │   │   │   └── NetworkModule.kt
│       │   │   ├── model/                  # Data Models
│       │   │   │   ├── Interaction.kt
│       │   │   │   ├── NetworkResult.kt
│       │   │   │   └── Scenario.kt
│       │   │   └── repository/             # Data Repositories
│       │   │       ├── InteractionRepository.kt
│       │   │       └── ScenarioRepository.kt
│       │   ├── ui/
│       │   │   ├── screens/                # Compose Screens
│       │   │   │   ├── HomeScreen.kt
│       │   │   │   ├── ScenarioScreen.kt
│       │   │   │   ├── FeedbackScreen.kt
│       │   │   │   └── LoadingScreen.kt
│       │   │   ├── viewmodel/              # ViewModels
│       │   │   │   ├── HomeViewModel.kt
│       │   │   │   ├── ScenarioViewModel.kt
│       │   │   │   └── FeedbackViewModel.kt
│       │   │   ├── components/             # Reusable UI Components
│       │   │   │   ├── AudioPlayer.kt
│       │   │   │   ├── BottomNavigationBar.kt
│       │   │   │   └── CustomProgressBar.kt
│       │   │   └── theme/                  # App Theme
│       │   │       ├── Color.kt
│       │   │       ├── Theme.kt
│       │   │       └── Type.kt
│       │   ├── navigation/                 # Navigation Graph
│       │   │   ├── NavGraph.kt
│       │   │   └── Screen.kt
│       │   ├── utils/                      # Utility Classes
│       │   │   ├── AudioRecorder.kt
│       │   │   ├── ScoreUtils.kt
│       │   │   └── UrlUtils.kt
│       │   ├── JScenarioApplication.kt     # Application Class
│       │   └── MainActivity.kt
│       └── res/                            # Resources
│           ├── xml/
│           │   ├── network_security_config.xml  # Network Security Config
│           │   ├── backup_rules.xml
│           │   └── data_extraction_rules.xml
│           └── ...
│
├── backend/                                # Python Backend
│   ├── app/
│   │   ├── main.py                         # FastAPI Entry Point
│   │   ├── config.py                       # Configuration & Validation
│   │   ├── models/                         # Pydantic Models
│   │   │   ├── interaction.py
│   │   │   └── scenario.py
│   │   ├── routes/                         # API Routes
│   │   │   ├── scenarios.py
│   │   │   └── interactions.py
│   │   ├── services/                       # Business Logic
│   │   │   ├── interaction_service.py      # Main Pipeline
│   │   │   ├── stt_service.py              # Google STT
│   │   │   ├── text_correction_service.py  # Gemini Correction
│   │   │   ├── azure_pronunciation_service.py
│   │   │   ├── evaluation_service.py       # Gemini Evaluation
│   │   │   ├── tts_service.py              # Google TTS
│   │   │   └── scenario_service.py
│   │   └── utils/                          # Utility Functions
│   │       ├── exceptions.py               # Custom Exceptions
│   │       ├── logger.py                    # Logging Configuration
│   │       └── validators.py                # Input Validation
│   ├── data/
│   │   └── scenarios.json                  # Scenario Data
│   ├── uploads/                            # Audio & Image Files
│   ├── requirements.txt
│   ├── run.py
│   └── README.md
│
└── cursor_rules/                           # Project Documentation
    ├── PRD.md
    ├── plan.md
    └── Todo.md
```

---

## Getting Started

### Prerequisites

- **Android Studio** Arctic Fox 이상
- **Python** 3.11+
- **Google Cloud** 서비스 계정 (STT, TTS)
- **Azure** Speech 서비스 키
- **Google Gemini** API 키

### Backend Installation

```bash
# 1. 백엔드 디렉토리로 이동
cd backend

# 2. 가상환경 생성 및 활성화
python -m venv venv
source venv/bin/activate  # macOS/Linux
# venv\Scripts\activate   # Windows

# 3. 의존성 설치
pip install -r requirements.txt

# 4. 환경 변수 설정
# .env 파일이 이미 존재하는 경우 편집, 없으면 생성
# 필수 환경 변수:
# - GEMINI_API_KEY (필수)
# - GOOGLE_APPLICATION_CREDENTIALS (STT/TTS용, 선택)
# - AZURE_SPEECH_KEY (발음 평가용, 선택)
# - AZURE_SPEECH_REGION (발음 평가용, 선택)
#
# 환경 변수 검증: 서버 시작 시 자동으로 필수 키 확인

# 5. 서버 실행
python run.py
# 또는
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

### Android Installation

```bash
# 1. Android Studio에서 프로젝트 열기
# File > Open > JScenario 폴더 선택

# 2. local.properties 설정 확인
# sdk.dir=/path/to/your/Android/sdk

# 3. BASE_URL 설정
# app/build.gradle.kts의 buildTypes에서 BASE_URL 확인:
# - Debug: http://10.0.2.2:8000/api/ (에뮬레이터용)
# - 실제 기기 테스트 시 IP 주소 변경 필요

# 4. Gradle Sync 실행
# File > Sync Project with Gradle Files
# (Timber 라이브러리 자동 다운로드)

# 5. 앱 빌드 및 실행
# Run > Run 'app' 또는 Shift + F10
```

### API Documentation

서버 실행 후 아래 URL에서 API 문서 확인:
- **Swagger UI**: http://localhost:8000/docs
- **ReDoc**: http://localhost:8000/redoc

