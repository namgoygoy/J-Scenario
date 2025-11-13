"""
Scenarios API routes
"""
from fastapi import APIRouter, HTTPException
from app.models.scenario import ScenarioResponse
from app.services.scenario_service import ScenarioService

router = APIRouter()
scenario_service = ScenarioService()


@router.get("/random", response_model=ScenarioResponse)
async def get_random_scenario():
    """
    랜덤 시나리오 조회
    
    Returns:
        ScenarioResponse: 랜덤으로 선택된 시나리오
    """
    try:
        scenario = await scenario_service.get_random_scenario()
        return ScenarioResponse(
            scenario=scenario,
            success=True,
            message="시나리오를 성공적으로 조회했습니다"
        )
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"시나리오 조회 중 오류가 발생했습니다: {str(e)}"
        )


@router.get("/{scenario_id}", response_model=ScenarioResponse)
async def get_scenario_by_id(scenario_id: str):
    """
    특정 시나리오 조회
    
    Args:
        scenario_id: 시나리오 ID
        
    Returns:
        ScenarioResponse: 요청한 시나리오
    """
    try:
        scenario = await scenario_service.get_scenario_by_id(scenario_id)
        if not scenario:
            raise HTTPException(
                status_code=404,
                detail=f"시나리오를 찾을 수 없습니다: {scenario_id}"
            )
        return ScenarioResponse(
            scenario=scenario,
            success=True,
            message="시나리오를 성공적으로 조회했습니다"
        )
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"시나리오 조회 중 오류가 발생했습니다: {str(e)}"
        )

