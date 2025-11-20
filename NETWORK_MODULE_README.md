# 네트워크 모듈 구현 완료

## 📋 개요

Android 앱과 Backend API 간의 통신을 위한 네트워크 모듈을 완료했습니다. MVVM 아키텍처 패턴을 따르며, Retrofit, OkHttp, Moshi를 사용하여 구현했습니다.

## 🎯 구현된 기능

### 1. 의존성 추가 ✅
- **Retrofit 2.11.0**: REST API 통신
- **OkHttp 4.12.0**: HTTP 클라이언트 및 로깅
- **Moshi 1.15.1**: JSON 직렬화/역직렬화
- **Coroutines 1.8.1**: 비동기 처리
- **Lifecycle ViewModel 2.8.7**: Compose ViewModel 지원

### 2. 데이터 모델 (Data Models) ✅

**파일 위치**: `app/src/main/java/com/example/j_scenario/data/model/`

- `Scenario.kt`: 시나리오 데이터 모델
  - `ScenarioCategory`: 시나리오 카테고리 Enum
  - `Scenario`: 시나리오 상세 정보
  - `ScenarioResponse`: API 응답 래퍼

- `Interaction.kt`: 인터랙션 데이터 모델
  - `FeedbackCategory`: 피드백 카테고리 (발음, 문법, TPO)
  - `EvaluationResult`: 평가 결과
  - `InteractionRequest`: 사용자 발화 요청
  - `InteractionResponse`: 평가 응답

- `NetworkResult.kt`: 네트워크 상태 관리
  - `Success<T>`: 성공 상태
  - `Error`: 에러 상태
  - `Loading`: 로딩 상태
  - 확장 함수: `onSuccess`, `onError`, `onLoading`

### 3. API 서비스 (API Service) ✅

**파일 위치**: `app/src/main/java/com/example/j_scenario/data/api/`

- `JScenarioApiService.kt`: Retrofit API 인터페이스
  - `GET /api/scenarios/random`: 랜덤 시나리오 조회
  - `GET /api/scenarios/{scenario_id}`: 특정 시나리오 조회
  - `POST /api/interactions`: 음성 파일 업로드 및 평가

- `NetworkModule.kt`: 네트워크 싱글톤 모듈
  - Moshi JSON 컨버터 설정
  - OkHttp 클라이언트 설정 (로깅 포함)
  - Retrofit 인스턴스 생성

### 4. Repository 레이어 ✅

**파일 위치**: `app/src/main/java/com/example/j_scenario/data/repository/`

- `ScenarioRepository.kt`: 시나리오 데이터 접근
  - `getRandomScenario()`: Flow로 랜덤 시나리오 반환
  - `getScenarioById()`: 특정 시나리오 조회
  - 에러 처리 및 로깅 포함

- `InteractionRepository.kt`: 인터랙션 데이터 접근
  - `processAudioInteraction()`: 음성 파일 전송 및 평가 결과 수신
  - 파일 크기 제한 검증 (10MB)
  - MultipartBody를 사용한 파일 업로드

### 5. ViewModel 레이어 ✅

**파일 위치**: `app/src/main/java/com/example/j_scenario/ui/viewmodel/`

- `HomeViewModel.kt`: 홈 화면 상태 관리
  - 랜덤 시나리오 로드
  - 사용자 통계 데이터 관리
  - StateFlow를 통한 상태 노출

- `ScenarioViewModel.kt`: 시나리오 진행 상태 관리
  - 현재 시나리오 저장
  - 녹음 상태 관리
  - 인터랙션 처리 상태 관리

- `FeedbackViewModel.kt`: 피드백 화면 상태 관리
  - 평가 결과 저장 및 표시
  - 다음 시나리오/홈 이동 처리

### 6. UI 업데이트 ✅

**파일 위치**: `app/src/main/java/com/example/j_scenario/ui/screens/`

- `HomeScreen.kt`: 백엔드에서 시나리오 로드 및 표시
  - 로딩, 성공, 에러 상태 처리
  - 새로고침 기능
  - 동적 시나리오 카드 렌더링

- `ScenarioScreen.kt`: 시나리오 상세 정보 표시 및 음성 녹음
  - 현재 시나리오 데이터 표시
  - 마이크 권한 요청 로직
  - 실시간 녹음 기능 (AudioRecorder 연동)
  - 녹음 상태 UI (타이머, 펄스 애니메이션)

- `LoadingScreen.kt`: 평가 처리 중 로딩
  - 인터랙션 상태 관찰
  - 결과 수신 시 자동 전환

- `FeedbackScreen.kt`: 평가 결과 표시
  - 동적 피드백 데이터 렌더링
  - 점수 및 카테고리별 상세 피드백

- `NavGraph.kt`: Navigation 업데이트
  - 공유 ViewModel 설정
  - 화면 간 데이터 전달

### 7. 음성 녹음 기능 ✅

**파일 위치**: `app/src/main/java/com/example/j_scenario/utils/`

- `AudioRecorder.kt`: MediaRecorder 래퍼 클래스
  - 음성 녹음 시작/중지/취소 기능
  - AAC 포맷으로 M4A 파일 생성
  - 라이프사이클 안전 관리
  - 에러 처리 및 로깅

**ViewModel 업데이트**:
- `ScenarioViewModel.kt`: 녹음 상태 관리
  - 녹음 타이머 (초 단위)
  - 녹음 시작/중지/취소 메서드
  - 녹음 파일 서버 전송 로직

## 🏗️ 아키텍처

```
UI Layer (Compose)
    ↓
ViewModel Layer (StateFlow)
    ↓
Repository Layer (Flow)
    ↓
API Service (Retrofit)
    ↓
Network (OkHttp)
    ↓
Backend API (FastAPI)
```

## 🔧 설정 방법

### 1. Backend 서버 실행

```bash
cd backend
source venv/bin/activate
python run.py
```

서버는 `http://localhost:8000`에서 실행됩니다.

### 2. Android Emulator 설정

- Android Emulator를 사용하는 경우, `localhost`는 `10.0.2.2`로 접근합니다.
- 실제 기기를 사용하는 경우, `JScenarioApiService.kt`의 `BASE_URL`을 컴퓨터의 IP 주소로 변경하세요.

```kotlin
// Emulator
const val BASE_URL = "http://10.0.2.2:8000/api/"

// 실제 기기
const val BASE_URL = "http://192.168.x.x:8000/api/"
```

### 3. 인터넷 권한 확인

`AndroidManifest.xml`에 인터넷 권한이 추가되어 있는지 확인하세요:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

## 📝 TODO (다음 단계)

### Phase 3-2: 음성 녹음 기능 구현 🎤 ✅ 완료
- [x] 마이크 권한 요청 (`RECORD_AUDIO`)
- [x] MediaRecorder를 사용한 음성 녹음
- [x] 녹음된 파일을 `ScenarioViewModel`을 통해 서버로 전송
- [x] 실시간 녹음 상태 UI 업데이트

### Phase 3-3: Room 데이터베이스 구현 💾
- [ ] User, Scenario, UserProgress 엔티티 정의
- [ ] DAO 인터페이스 작성
- [ ] Room Database 설정
- [ ] 오프라인 캐싱 로직 구현

### Phase 3-4: 음성 재생 기능 🔊
- [ ] ExoPlayer 또는 MediaPlayer 통합
- [ ] 캐릭터 음성 자동 재생
- [ ] AI 응답 음성 재생 버튼

### Phase 3-5: 게임화 요소 🎮
- [ ] 경험치 및 레벨 시스템 UI
- [ ] 도전과제 화면
- [ ] 진행 상황 추적

## 🐛 알려진 이슈

1. ~~**음성 녹음 기능 미구현**~~: ✅ 완료 (Phase 3-2)
2. **에러 처리 개선 필요**: 네트워크 에러 시 더 상세한 사용자 피드백이 필요합니다.
3. **오프라인 모드 없음**: 인터넷 연결이 필수입니다. (Phase 3-3에서 Room DB 구현 예정)

## 📚 참고 자료

- [Retrofit 공식 문서](https://square.github.io/retrofit/)
- [Moshi 공식 문서](https://github.com/square/moshi)
- [Android Compose ViewModel](https://developer.android.com/jetpack/compose/libraries#viewmodel)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)

---

**완료일**: 2025-11-13
**개발자**: AI Assistant with User

