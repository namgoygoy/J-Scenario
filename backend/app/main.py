"""
FastAPI application entry point
"""
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
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

