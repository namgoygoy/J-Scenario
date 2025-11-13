"""
Application configuration
"""
from pydantic_settings import BaseSettings
from functools import lru_cache


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
    
    # File Upload
    upload_dir: str = "./uploads"
    max_audio_size_mb: int = 10
    
    class Config:
        env_file = ".env"
        case_sensitive = False


@lru_cache()
def get_settings() -> Settings:
    """Get cached settings instance"""
    return Settings()

