"""
Application configuration
"""
import os
from pathlib import Path
from pydantic import Field, field_validator
from pydantic_settings import BaseSettings, SettingsConfigDict
from functools import lru_cache
from typing import Optional

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
    
    # Google Gemini API (필수)
    gemini_api_key: str = Field(default="", description="Google Gemini API key")
    
    # Google Cloud (STT, TTS) - 선택사항
    google_application_credentials: str = Field(default="", description="Path to Google Cloud credentials JSON")
    google_cloud_project_id: str = Field(default="", description="Google Cloud project ID")
    
    # Azure Cognitive Services (발음 평가) - 선택사항
    azure_speech_key: str = Field(default="", description="Azure Speech service key")
    azure_speech_region: str = Field(default="", description="Azure Speech service region")
    
    # File Upload
    upload_dir: str = "./uploads"
    max_audio_size_mb: int = 10
    
    # CORS
    allowed_origins: str = "http://localhost:3000,http://localhost:8080,http://10.0.2.2:8000"  # 개발 환경 기본값
    
    model_config = SettingsConfigDict(
        env_file=str(ENV_FILE) if ENV_FILE.exists() else None,
        env_file_encoding="utf-8",
        case_sensitive=False,
        extra="ignore"
    )
    
    @field_validator("gemini_api_key")
    @classmethod
    def validate_gemini_api_key(cls, v: str, info) -> str:
        """Validate Gemini API key is set in production"""
        if not v and not info.data.get("debug", True):
            raise ValueError(
                "GEMINI_API_KEY is required in production. "
                "Please set it in .env file or environment variables."
            )
        return v
    
    @field_validator("google_application_credentials")
    @classmethod
    def validate_google_credentials(cls, v: str) -> str:
        """Validate Google Cloud credentials file exists if provided"""
        if v and not Path(v).exists():
            # 상대 경로도 확인
            abs_path = BASE_DIR / v
            if not abs_path.exists():
                raise ValueError(
                    f"Google Cloud credentials file not found: {v}. "
                    f"Please check GOOGLE_APPLICATION_CREDENTIALS path."
                )
        return v
    
    def validate_required_services(self) -> None:
        """
        Validate that required services are configured.
        Called after initialization to provide clear error messages.
        """
        errors = []
        
        # Gemini API는 필수
        if not self.gemini_api_key:
            errors.append("GEMINI_API_KEY is required but not set")
        
        # Google Cloud는 선택사항이지만, 설정된 경우 파일 존재 확인
        if self.google_application_credentials:
            cred_path = Path(self.google_application_credentials)
            if not cred_path.exists():
                # 상대 경로 확인
                abs_path = BASE_DIR / self.google_application_credentials
                if not abs_path.exists():
                    errors.append(
                        f"GOOGLE_APPLICATION_CREDENTIALS file not found: "
                        f"{self.google_application_credentials}"
                    )
        
        # Azure는 선택사항이지만, 키가 있으면 지역도 필요
        if self.azure_speech_key and not self.azure_speech_region:
            errors.append(
                "AZURE_SPEECH_KEY is set but AZURE_SPEECH_REGION is missing"
            )
        
        if errors:
            error_msg = "Configuration errors:\n" + "\n".join(f"  - {e}" for e in errors)
            raise ValueError(error_msg)


@lru_cache()
def get_settings() -> Settings:
    """
    Get cached settings instance with validation.
    
    Raises:
        ValueError: If required configuration is missing or invalid
    """
    settings = Settings()
    settings.validate_required_services()
    return settings



