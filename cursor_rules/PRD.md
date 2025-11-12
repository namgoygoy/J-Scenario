# [PRD] 롤플레잉 일본어 회화 학습 앱 'J-Scenario'

---

## 1. 개요 (Overview)

-   **제품명**: J-Scenario (제이-시나리오)
-   **부제**: 살아남아라! 실전 일본어 서바이벌
-   **문서 목적**: 본 문서는 'J-Scenario' 앱 개발을 위한 목표, 기능, 사용자 요구사항을 정의하여 모든 이해관계자가 프로젝트에 대한 공통된 이해를 갖도록 하는 것을 목표로 합니다.

## 2. 문제 정의 및 목표 (Problem & Goal)

### 2-1. 문제점 (Problem Statement)
-   많은 일본어 중급 학습자들이 문법과 어휘 지식은 갖추었으나, 예측 불가능한 실제 대화 상황에서 순발력 있게 대응하는 데 어려움을 겪습니다.
-   기존의 학습 앱들은 정해진 스크립트에 의존하는 경우가 많아, 실전 회화 능력과 자신감을 키우는 데 한계가 있습니다.

### 2-2. 프로젝트 목표 (Goals)
-   **제품 목표**: 실제와 같은 돌발 상황 롤플레잉을 통해 사용자의 일본어 회화 순발력과 응용 능력을 극대화합니다.
-   **비즈니스 목표**: 게임화(Gamification) 요소를 도입하여 사용자의 학습 동기와 몰입도를 높여, 지속적인 앱 사용 및 학습을 유도합니다.

## 3. 타겟 사용자 (Target Audience)

-   **주요 사용자 (Primary)**: 일본어 능력 시험(JLPT) N3 ~ N2 수준의 중급 학습자로, 회화 실력을 한 단계 끌어올리고 싶은 사람.
-   **확장 사용자 (Secondary)**: 일본 여행, 유학, 취업 등을 앞두고 단기간에 실전 회화 경험을 쌓고 싶은 사람.

## 4. 사용자 스토리 및 기능 요구사항 (User Stories & Features)

### Epic 1: 시나리오 기반 롤플레잉 학습

-   **As a user, I want to** be presented with random scenarios from various categories (일상, 돌발, 비즈니스 등), **so that** I can practice Japanese for different real-life situations.
-   **As a user, I want to** see a relevant image and hear a character's voice for each scenario, **so that** I can be more immersed in the situation.
-   **As a user, I want to** receive a clear mission (e.g., "경찰에게 지갑의 특징을 설명하세요"), **so that** I understand my objective in the conversation.

### Epic 2: AI 기반 음성 인식 및 피드백

-   **As a user, I want to** use my microphone to speak my response in Japanese, **so that** I can practice actual speaking.
-   **As a user, I want to** receive an immediate and comprehensive evaluation of my speech, **so that** I can understand my strengths and weaknesses.
-   **As a user, I want my response to be** assessed on pronunciation, grammar, vocabulary, and appropriateness (TPO), **so that** I can learn to speak more naturally and accurately.
-   **As a user, I want to** see a score, detailed corrections, and suggestions for better expressions, **so that** I can effectively review and improve.
-   **As a user, I want to** hear a contextual audio response from the AI character based on my answer, **so that** the conversation feels interactive.

### Epic 3: 게임화 (Gamification)

-   **As a user, I want to** earn experience points (EXP) and level up after clearing a scenario, **so that** I feel a sense of progression.
-   **As a user, I want to** complete specific challenges to earn badges and titles, **so that** I feel a sense of accomplishment.

## 5. 기술 스택 및 시스템 아키텍처 (Tech Stack & Architecture)

### 5-1. 아키텍처
-   **구조**: Client-Server 모델
-   **설명**: 사용자 데이터 관리 및 AI 분석/연산은 서버에서 처리하고, 사용자는 안드로이드 클라이언트를 통해 서비스와 상호작용합니다.

### 5-2. Client (Android App)
-   **언어**: **Kotlin**
-   **UI 툴킷**: **Jetpack Compose**
-   **아키텍처 패턴**: **MVVM (Model-View-ViewModel)**
-   **주요 라이브러리**:
    -   `Retrofit2`: 서버와의 비동기 HTTP 통신
    -   `Coroutines`: 비동기 처리 관리
    -   `Room`: 사용자 설정, 학습 기록 등 로컬 데이터 캐싱

### 5-3. Server (Backend)
-   **언어**: **Python**
-   **프레임워크**: **FastAPI**
-   **선택 이유**: Python은 다양한 AI/ML 서비스(OpenAI, Google Cloud 등)와의 연동이 용이하며, FastAPI는 비동기 처리를 지원하여 빠른 응답 속도를 보장하므로 본 프로젝트에 가장 적합합니다.

### 5-4. 외부 AI/ML 서비스 (3rd Party APIs)
-   **음성 인식 (STT)**: `Google Cloud Speech-to-Text` 또는 `OpenAI Whisper API`
-   **음성 합성 (TTS)**: `Google Cloud TTS` 또는 `Naver CLOVA Voice`
-   **언어 모델 (LLM)**: `OpenAI GPT-4` 또는 `Google Gemini API` (사용자 발화 평가 및 피드백 생성)
-   **이미지 생성**: `Stable Diffusion API` 또는 `Midjourney API`

### 5-5. 데이터베이스 (Database)
-   **종류**: **PostgreSQL** 또는 **MySQL** (RDBMS)
-   **용도**: 사용자 정보, 학습 데이터, 시나리오 등 정형 데이터 관리
-   **참고**: 랭킹 시스템은 구현하지 않음

## 6. 성공 지표 (Success Metrics)

-   **사용자 참여도**: 일일 활성 사용자 수(DAU), 평균 세션 시간
-   **사용자 유지율 (Retention)**: 1주, 1개월 후 재방문율
-   **핵심 기능 사용률**: 일일 시나리오 완료율, 피드백 확인율
-   **사용자 만족도**: 앱 스토어 평점, 긍정적 리뷰 수 (특히 '자신감 향상', '실력 향상' 관련 키워드)