"""
Application configuration
"""
import os
from pathlib import Path
from pydantic_settings import BaseSettings, SettingsConfigDict
from functools import lru_cache

# .env 파일의 절대 경로 찾기
BASE_DIR = Path(__file__).parent.parent
ENV_FILE = BASE_DIR / ".env"


class Settings(BaseSettings):
    """Application settings"""
    
    # App
    app_name: str = "J-Scenario API"
    app_version: str = "1.0.0"
    debug: bool = True
    
    # Server
    host: str = "0.0.0.0"
    port: int = 8000
    
    # Database (SQLite)
    database_url: str = "sqlite:///./jscenario.db"
    
    # Google Gemini API
    gemini_api_key: str = ""
    
    # Google Cloud (STT, TTS)
    google_application_credentials: str = ""
    google_cloud_project_id: str = ""
    
    # Azure Cognitive Services (발음 평가)
    azure_speech_key: str = ""
    azure_speech_region: str = ""
    
    # File Upload
    upload_dir: str = "./uploads"
    max_audio_size_mb: int = 10
    
    model_config = SettingsConfigDict(
        env_file=str(ENV_FILE) if ENV_FILE.exists() else None,
        env_file_encoding="utf-8",
        case_sensitive=False,
        extra="ignore"
    )


@lru_cache()
def get_settings() -> Settings:
    """Get cached settings instance"""
    return Settings()



