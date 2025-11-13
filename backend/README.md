# J-Scenario Backend API

롤플레잉 일본어 회화 학습 앱 'J-Scenario'의 백엔드 API 서버

## 기술 스택

- **Python 3.11+**
- **FastAPI** - 비동기 웹 프레임워크
- **SQLite** - 데이터베이스 (초기 개발용)
- **Google Gemini API** - 평가 및 AI 응답 생성
- **Google Cloud Speech-to-Text** - 음성 인식 (STT)
- **Google Cloud TTS** - 음성 합성

## 설치 및 실행

### 1. 가상환경 생성 및 활성화

```bash
python -m venv venv
source venv/bin/activate  # macOS/Linux
# or
venv\Scripts\activate  # Windows
```

### 2. 의존성 설치

```bash
pip install -r requirements.txt
```

### 3. 환경 변수 설정

`env.example.txt`를 복사하여 `.env` 파일을 생성하고 API 키를 설정합니다.

```bash
cp env.example.txt .env
# .env 파일을 편집하여 실제 API 키 입력
```

필수 설정:
- `GEMINI_API_KEY`: Google Gemini API 키 (https://makersuite.google.com/app/apikey)
- `GOOGLE_APPLICATION_CREDENTIALS`: Google Cloud 서비스 계정 키 (STT, TTS용, 선택사항)

### 4. 서버 실행

SQLite는 별도 설정 없이 자동으로 생성됩니다.

```bash
uvicorn app.main:app --reload
```

서버가 실행되면 다음 주소에서 API 문서를 확인할 수 있습니다:
- Swagger UI: http://localhost:8000/docs
- ReDoc: http://localhost:8000/redoc

## API 엔드포인트

### Scenarios

- `GET /api/scenarios/random` - 랜덤 시나리오 조회

### Interactions

- `POST /api/interactions` - 사용자 발화 처리 및 평가

## 프로젝트 구조

```
backend/
├── app/
│   ├── __init__.py
│   ├── main.py              # FastAPI 앱 진입점
│   ├── config.py            # 설정 관리
│   ├── models/              # Pydantic 모델
│   ├── services/            # 비즈니스 로직
│   ├── routes/              # API 라우터
│   ├── db/                  # 데이터베이스 관련
│   └── utils/               # 유틸리티 함수
├── data/                    # 시나리오 데이터
├── uploads/                 # 업로드된 음성 파일
├── tests/                   # 테스트
├── .env                     # 환경 변수 (gitignore)
├── .env.example             # 환경 변수 예시
├── requirements.txt         # Python 의존성
└── README.md
```

## 개발

### 코드 포맷팅

```bash
black app/
```

### 린팅

```bash
flake8 app/
```

### 타입 체크

```bash
mypy app/
```

### 테스트

```bash
pytest
```

