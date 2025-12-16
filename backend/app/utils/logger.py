"""
Centralized logging configuration
"""
import sys
from loguru import logger
from app.config import get_settings

settings = get_settings()


def setup_logging():
    """
    Configure loguru logger with appropriate settings
    """
    # 기존 핸들러 제거
    logger.remove()
    
    # 콘솔 출력 설정
    log_level = "DEBUG" if settings.debug else "INFO"
    
    logger.add(
        sys.stderr,
        format="<green>{time:YYYY-MM-DD HH:mm:ss}</green> | <level>{level: <8}</level> | <cyan>{name}</cyan>:<cyan>{function}</cyan>:<cyan>{line}</cyan> - <level>{message}</level>",
        level=log_level,
        colorize=True
    )
    
    # 파일 로깅 (프로덕션)
    if not settings.debug:
        logger.add(
            "logs/app_{time:YYYY-MM-DD}.log",
            rotation="00:00",  # 매일 자정에 로테이션
            retention="30 days",  # 30일 보관
            compression="zip",  # 압축 저장
            level="INFO",
            format="{time:YYYY-MM-DD HH:mm:ss} | {level: <8} | {name}:{function}:{line} - {message}"
        )
    
    return logger


# 전역 로거 인스턴스
app_logger = setup_logging()

