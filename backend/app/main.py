"""
FastAPI application entry point
"""
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from pathlib import Path
from app.config import get_settings
from app.routes import scenarios, interactions

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
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 프로덕션에서는 특정 도메인으로 제한
    allow_credentials=True,
    allow_methods=["*"],
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

