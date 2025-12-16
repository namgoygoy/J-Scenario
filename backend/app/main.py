"""
FastAPI application entry point
"""
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from pathlib import Path
from app.config import get_settings
from app.routes import scenarios, interactions
from app.utils.logger import setup_logging

# 로깅 초기화
logger = setup_logging()

settings = get_settings()

app = FastAPI(
    title=settings.app_name,
    version=settings.app_version,
    description="롤플레잉 일본어 회화 학습 앱 백엔드 API",
    docs_url="/docs",
    redoc_url="/redoc"
)

# 정적 파일 서빙 설정 (uploads 디렉토리)
uploads_dir = Path(__file__).parent.parent / "uploads"
if uploads_dir.exists():
    app.mount("/uploads", StaticFiles(directory=str(uploads_dir)), name="uploads")

# CORS 설정
# 환경 변수로 허용된 origin 목록 관리
# .env 파일에 ALLOWED_ORIGINS="http://localhost:3000,https://yourdomain.com" 형식으로 설정
allowed_origins_str = settings.allowed_origins
allowed_origins_set = set()

# 환경 변수에서 origin 목록 파싱
if allowed_origins_str:
    allowed_origins_set.update([
        origin.strip() 
        for origin in allowed_origins_str.split(",") 
        if origin.strip()
    ])

# 프로덕션 환경에서는 "*" 사용 금지
if settings.debug:
    # 개발 환경: localhost 및 에뮬레이터 IP 허용 (중복 제거를 위해 set 사용)
    allowed_origins_set.update([
        "http://localhost:3000",
        "http://localhost:8080",
        "http://10.0.2.2:8000",  # Android 에뮬레이터
        "http://127.0.0.1:8000"
    ])
else:
    # 프로덕션 환경: 환경 변수에 명시된 origin만 허용
    if not allowed_origins_set:
        # 프로덕션에서 origin이 설정되지 않으면 경고
        import warnings
        warnings.warn(
            "ALLOWED_ORIGINS not set in production environment. "
            "This may cause CORS issues. Please set ALLOWED_ORIGINS in .env file."
        )

allowed_origins_list = list(allowed_origins_set) if allowed_origins_set else ["http://localhost:3000"]

app.add_middleware(
    CORSMiddleware,
    allow_origins=allowed_origins_list,
    allow_credentials=True,
    allow_methods=["GET", "POST"],  # 필요한 메서드만 허용
    allow_headers=["*"],
)

# 라우터 등록
app.include_router(
    scenarios.router,
    prefix="/api/scenarios",
    tags=["scenarios"]
)

app.include_router(
    interactions.router,
    prefix="/api/interactions",
    tags=["interactions"]
)


@app.get("/")
async def root():
    """Root endpoint"""
    return {
        "message": "J-Scenario API is running",
        "version": settings.app_version,
        "docs": "/docs"
    }


@app.get("/health")
async def health_check():
    """Health check endpoint"""
    return {
        "status": "healthy",
        "service": settings.app_name,
        "version": settings.app_version
    }

