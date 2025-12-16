"""
Input validation utilities
"""
import re
from typing import Optional
from fastapi import HTTPException, status


# 시나리오 ID 패턴: scenario_XXX 또는 scenario_XXX_Y (Y는 챕터 번호)
SCENARIO_ID_PATTERN = re.compile(r'^scenario_\d{3}(_\d+)?$')

# 허용된 오디오 파일 확장자
ALLOWED_AUDIO_EXTENSIONS = {'.wav', '.mp3', '.amr', '.m4a', '.ogg', '.flac'}

# 허용된 오디오 MIME 타입
ALLOWED_AUDIO_MIME_TYPES = {
    'audio/wav',
    'audio/wave',
    'audio/x-wav',
    'audio/mpeg',
    'audio/mp3',
    'audio/amr',
    'audio/amr-wb',
    'audio/mp4',
    'audio/m4a',
    'audio/ogg',
    'audio/flac',
    'application/octet-stream'  # 일부 클라이언트가 이 타입으로 전송
}


def validate_scenario_id(scenario_id: str) -> None:
    """
    Validate scenario ID format
    
    Args:
        scenario_id: 시나리오 ID
        
    Raises:
        HTTPException: If scenario ID format is invalid
    """
    if not scenario_id:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="시나리오 ID가 제공되지 않았습니다"
        )
    
    if not SCENARIO_ID_PATTERN.match(scenario_id):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"잘못된 시나리오 ID 형식입니다: {scenario_id}. "
                   f"올바른 형식: scenario_XXX 또는 scenario_XXX_Y"
        )


def validate_audio_file(filename: Optional[str], content_type: Optional[str], file_size: int, max_size_mb: int = 10) -> None:
    """
    Validate audio file
    
    Args:
        filename: 파일명
        content_type: MIME 타입
        file_size: 파일 크기 (bytes)
        max_size_mb: 최대 파일 크기 (MB)
        
    Raises:
        HTTPException: If file validation fails
    """
    # 파일명 검증
    if not filename:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="파일명이 제공되지 않았습니다"
        )
    
    # 확장자 검증
    file_ext = None
    if '.' in filename:
        file_ext = '.' + filename.rsplit('.', 1)[1].lower()
    
    if file_ext and file_ext not in ALLOWED_AUDIO_EXTENSIONS:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"지원하지 않는 파일 형식입니다: {file_ext}. "
                   f"지원 형식: {', '.join(ALLOWED_AUDIO_EXTENSIONS)}"
        )
    
    # MIME 타입 검증 (선택사항, content_type이 제공된 경우)
    if content_type:
        # MIME 타입에서 파라미터 제거 (예: "audio/wav; charset=utf-8" -> "audio/wav")
        mime_type = content_type.split(';')[0].strip().lower()
        if mime_type not in ALLOWED_AUDIO_MIME_TYPES:
            # MIME 타입이 허용 목록에 없어도 확장자가 유효하면 경고만 (엄격하지 않게)
            pass
    
    # 파일 크기 검증
    max_size_bytes = max_size_mb * 1024 * 1024
    if file_size > max_size_bytes:
        raise HTTPException(
            status_code=status.HTTP_413_REQUEST_ENTITY_TOO_LARGE,
            detail=f"파일 크기가 {max_size_mb}MB를 초과했습니다. "
                   f"현재 크기: {file_size / (1024 * 1024):.2f}MB"
        )
    
    # 최소 파일 크기 검증 (너무 작은 파일은 유효하지 않은 오디오일 가능성)
    min_size_bytes = 1024  # 1KB
    if file_size < min_size_bytes:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"파일 크기가 너무 작습니다. 최소 크기: {min_size_bytes} bytes"
        )


def sanitize_user_id(user_id: Optional[str]) -> Optional[str]:
    """
    Sanitize user ID to prevent injection attacks
    
    Args:
        user_id: 사용자 ID
        
    Returns:
        Sanitized user ID or None
    """
    if not user_id:
        return None
    
    # 알파벳, 숫자, 하이픈, 언더스코어만 허용
    sanitized = re.sub(r'[^a-zA-Z0-9_-]', '', user_id)
    
    # 길이 제한
    if len(sanitized) > 64:
        sanitized = sanitized[:64]
    
    return sanitized if sanitized else None

