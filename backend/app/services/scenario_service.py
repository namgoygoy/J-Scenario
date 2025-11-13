"""
Scenario service for managing scenarios
"""
import json
import random
from pathlib import Path
from typing import Optional
from app.models.scenario import Scenario


class ScenarioService:
    """시나리오 관리 서비스"""
    
    def __init__(self):
        """Initialize scenario service and load scenarios"""
        self.scenarios_file = Path(__file__).parent.parent.parent / "data" / "scenarios.json"
        self.scenarios = self._load_scenarios()
    
    def _load_scenarios(self) -> list[Scenario]:
        """
        Load scenarios from JSON file
        
        Returns:
            list[Scenario]: 로드된 시나리오 목록
        """
        try:
            with open(self.scenarios_file, "r", encoding="utf-8") as f:
                data = json.load(f)
                return [Scenario(**scenario) for scenario in data]
        except FileNotFoundError:
            print(f"Warning: Scenarios file not found at {self.scenarios_file}")
            return []
        except Exception as e:
            print(f"Error loading scenarios: {str(e)}")
            return []
    
    async def get_random_scenario(self) -> Scenario:
        """
        Get a random scenario
        
        Returns:
            Scenario: 랜덤으로 선택된 시나리오
            
        Raises:
            ValueError: 시나리오가 없을 경우
        """
        if not self.scenarios:
            raise ValueError("사용 가능한 시나리오가 없습니다")
        
        return random.choice(self.scenarios)
    
    async def get_scenario_by_id(self, scenario_id: str) -> Optional[Scenario]:
        """
        Get a specific scenario by ID
        
        Args:
            scenario_id: 시나리오 ID
            
        Returns:
            Optional[Scenario]: 찾은 시나리오 또는 None
        """
        for scenario in self.scenarios:
            if scenario.id == scenario_id:
                return scenario
        return None
    
    async def get_scenarios_by_category(self, category: str) -> list[Scenario]:
        """
        Get scenarios by category
        
        Args:
            category: 카테고리 이름
            
        Returns:
            list[Scenario]: 해당 카테고리의 시나리오 목록
        """
        return [s for s in self.scenarios if s.category == category]

