"""
Interactions API routes
"""
from fastapi import APIRouter, HTTPException, UploadFile, File, Form, status
from app.models.interaction import InteractionRequest, InteractionResponse
from app.services.interaction_service import InteractionService
from app.utils.exceptions import ServiceUnavailableError, ServiceExecutionError, ServiceError
from app.utils.validators import validate_scenario_id, validate_audio_file, sanitize_user_id
from app.config import get_settings

router = APIRouter()
interaction_service = InteractionService()
settings = get_settings()


@router.post("", response_model=InteractionResponse)
async def process_interaction(
    scenario_id: str = Form(...),
    user_id: str = Form(None),
    audio_file: UploadFile = File(...)
):
    """
    사용자 발화 처리 및 평가
    
    Args:
        scenario_id: 시나리오 ID
        user_id: 사용자 ID (선택)
        audio_file: 음성 파일 (WAV, MP3 등)
        
    Returns:
        InteractionResponse: 평가 결과 및 AI 응답
    """
    try:
        # 입력 검증
        validate_scenario_id(scenario_id)
        sanitized_user_id = sanitize_user_id(user_id)
        
        # 파일 읽기
        contents = await audio_file.read()
        
        # 파일 검증 (크기, 타입, 확장자)
        validate_audio_file(
            filename=audio_file.filename,
            content_type=audio_file.content_type,
            file_size=len(contents),
            max_size_mb=settings.max_audio_size_mb
        )
        
        # 처리
        result = await interaction_service.process_audio_interaction(
            scenario_id=scenario_id,
            user_id=sanitized_user_id,
            audio_data=contents,
            filename=audio_file.filename or "audio.wav"
        )
        
        return result
        
    except ServiceUnavailableError as e:
        # 서비스 사용 불가 (API 키 없음, 초기화 실패 등)
        raise HTTPException(
            status_code=503,  # Service Unavailable
            detail=f"서비스를 사용할 수 없습니다: {str(e)}"
        )
    except ServiceExecutionError as e:
        # 서비스 실행 중 에러
        raise HTTPException(
            status_code=500,
            detail=f"서비스 처리 중 오류가 발생했습니다: {str(e)}"
        )
    except ServiceError as e:
        # 기타 서비스 에러
        raise HTTPException(
            status_code=500,
            detail=f"서비스 오류: {str(e)}"
        )
    except HTTPException:
        raise
    except Exception as e:
        # 예상치 못한 에러
        raise HTTPException(
            status_code=500,
            detail=f"인터랙션 처리 중 예상치 못한 오류가 발생했습니다: {str(e)}"
        )

