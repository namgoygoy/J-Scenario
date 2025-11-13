"""
Interactions API routes
"""
from fastapi import APIRouter, HTTPException, UploadFile, File, Form
from app.models.interaction import InteractionRequest, InteractionResponse
from app.services.interaction_service import InteractionService

router = APIRouter()
interaction_service = InteractionService()


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
        # 파일 크기 제한 확인 (10MB)
        contents = await audio_file.read()
        if len(contents) > 10 * 1024 * 1024:
            raise HTTPException(
                status_code=413,
                detail="파일 크기가 10MB를 초과했습니다"
            )
        
        # 처리
        result = await interaction_service.process_audio_interaction(
            scenario_id=scenario_id,
            user_id=user_id,
            audio_data=contents,
            filename=audio_file.filename
        )
        
        return result
        
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"인터랙션 처리 중 오류가 발생했습니다: {str(e)}"
        )

