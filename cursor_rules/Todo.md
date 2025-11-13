# 'J-Scenario' 앱 개발 Todo List

---

## 🚀 Phase 1: 프로젝트 준비 및 기반 설정 (Project Setup & Infrastructure)

-   [ ] **Infrastructure & Architecture**
    -   [ ] Database 스키마 설계 (User, Scenario, UserProgress 등) - ~~Ranking 제외~~
    -   [x] Android 프로젝트 초기 설정 (Kotlin, Jetpack Compose, MVVM 구조)
    -   [ ] 외부 API 키 발급 및 보안 관리 방안 수립 (Google Cloud, OpenAI 등)
    -   [ ] CI/CD 파이프라인 기본 구성 (GitHub Actions 등)

## 💻 Phase 2: 백엔드(서버) 개발 (Backend Development)

-   [ ] **API - Core Scenario**
    -   [ ] 시나리오 제공 API 구현 (`GET /api/scenarios/random`)
        -   [ ] 요청 시 랜덤 시나리오 (텍스트, 이미지 URL, 음성 URL, 미션) 반환
    -   [ ] 사용자 발화 처리 및 평가 API 구현 (`POST /api/interactions`)
        -   [ ] (Input) 사용자 음성 파일 수신
        -   [ ] (Process) STT API 연동: 음성 -> 텍스트 변환
        -   [ ] (Process) LLM API 연동: 텍스트 평가 (발음, 문법, TPO) 및 피드백 생성
        -   [ ] (Process) LLM API 연동: AI 캐릭터의 응답 대사 생성
        -   [ ] (Process) TTS API 연동: AI 응답 대사 -> 음성 파일 변환
        -   [ ] (Output) 평가 결과(점수, 피드백, 모범 답안) 및 AI 응답 음성 파일 URL 반환
-   [ ] **API - Gamification**
    -   [ ] 시나리오 클리어 시 경험치(EXP) 업데이트 로직 구현
    -   [ ] 레벨업 시스템 로직 구현
    -   [ ] 도전과제 달성 여부 확인 및 보상 지급 로직 구현
    -   [ ] ~~랭킹 데이터 집계 및 조회 API 구현~~ (구현 안함)

## 📱 Phase 3: 클라이언트(안드로이드) 개발 (Android Development)

-   [ ] **UI/UX - 공통**
    -   [x] 앱 전체 디자인 시스템(Color, Typography, Component) 정의
    -   [x] 메인 화면, 프로필 화면 등 기본 Navigation Flow 구현 (~~랭킹 화면 제외~~)
    -   [ ] Retrofit, Coroutines을 사용한 네트워크 모듈 구현
    -   [ ] Room을 사용한 로컬 DB 모듈 구현
-   [ ] **UI/UX - 핵심 기능 (시나리오)**
    -   [x] 시나리오 제시 화면 구현 (이미지, 텍스트, 미션 표시)
    -   [ ] 캐릭터 음성 자동 재생 기능 구현
    -   [ ] 마이크 권한 요청 및 음성 녹음 기능 구현
    -   [ ] 녹음된 음성 파일을 서버로 전송하는 로직 구현
-   [ ] **UI/UX - 결과 및 피드백**
    -   [x] 평가 결과(점수, 상세 피드백, 추천 표현) 표시 화면 구현
    -   [ ] AI 응답 음성 재생 기능 구현
    -   [ ] 경험치 획득 및 레벨업 시각적 효과(애니메이션) 구현
-   [ ] **UI/UX - Gamification**
    -   [ ] 마이페이지(프로필) UI 구현 (레벨, 경험치, 획득 뱃지 등 표시)
    -   [ ] 도전과제 목록 및 달성 현황 UI 구현
    -   [ ] ~~랭킹 화면 UI 구현~~ (구현 안함)

## ✅ Phase 4: 테스트, 배포 및 운영 (Test, Deploy & Operation)

-   [ ] **Testing**
    -   [ ] 서버 API 단위 테스트 및 통합 테스트 (Postman, Swagger 등 활용)
    -   [ ] 안드로이드 기능별 단위 테스트 (ViewModel 로직 등)
    -   [ ] QA: 전체 시나리오 플로우에 대한 E2E(End-to-End) 테스트
-   [ ] **Deployment**
    -   [ ] Backend 서버 프로덕션 환경에 배포
    -   [ ] Android 앱 빌드 및 Google Play Store 출시 준비 (스크린샷, 설명 등)
    -   [ ] Google Play Store 앱 심사 제출
-   [ ] **Monitoring & Maintenance**
    -   [ ] 서버 API 모니터링 및 로그 시스템 구축
    -   [ ] 사용자 피드백 수집 및 버그 트래킹 채널 마련