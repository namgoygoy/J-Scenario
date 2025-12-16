"""
Scenario service for managing scenarios
"""
import json
import random
import re
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
    
    def _is_first_chapter(self, scenario_id: str) -> bool:
        """
        시나리오가 첫 번째 챕터인지 확인
        
        Args:
            scenario_id: 시나리오 ID
            
        Returns:
            bool: 첫 번째 챕터이거나 단일 챕터면 True
            
        Examples:
            scenario_001_1 → True (Chapter 1)
            scenario_001_2 → False (Chapter 2)
            scenario_002 → True (단일 챕터)
        """
        # 언더스코어로 split해서 개수 확인
        parts = scenario_id.split("_")
        
        if len(parts) == 3:
            # scenario_001_1 형태 (챕터가 있는 시나리오)
            # parts = ["scenario", "001", "1"]
            try:
                chapter_number = int(parts[2])
                is_first = chapter_number == 1
                print(f"  📄 {scenario_id}: Chapter {chapter_number} → {'✅ First' if is_first else '❌ Not first'}")
                return is_first
            except ValueError:
                # 숫자가 아니면 단일 챕터로 간주
                print(f"  📄 {scenario_id}: Single chapter (invalid chapter number) → ✅ First")
                return True
        else:
            # scenario_002 형태 (단일 챕터 시나리오)
            print(f"  📄 {scenario_id}: Single chapter → ✅ First")
            return True
    
    async def get_random_scenario(self) -> Scenario:
        """
        Get a random scenario (Chapter 1 only)
        
        홈 화면에 표시할 시나리오를 반환합니다.
        멀티 챕터 시나리오의 경우 무조건 Chapter 1만 반환합니다.
        
        Returns:
            Scenario: 랜덤으로 선택된 시나리오 (Chapter 1 또는 단일 챕터)
            
        Raises:
            ValueError: 시나리오가 없을 경우
        """
        print("\n" + "="*60)
        print("🎲 GET RANDOM SCENARIO CALLED")
        print("="*60)
        
        if not self.scenarios:
            raise ValueError("사용 가능한 시나리오가 없습니다")
        
        print(f"📚 Total scenarios loaded: {len(self.scenarios)}")
        print(f"📋 All scenario IDs: {[s.id for s in self.scenarios]}")
        
        # Chapter 1만 필터링 (scenario_001_1, scenario_002, scenario_003 등)
        # Chapter 2, 3 제외 (scenario_001_2, scenario_001_3)
        first_chapter_scenarios = [
            s for s in self.scenarios 
            if self._is_first_chapter(s.id)
        ]
        
        print(f"📖 First chapter scenarios: {len(first_chapter_scenarios)}")
        print(f"📝 First chapter IDs: {[s.id for s in first_chapter_scenarios]}")
        
        if not first_chapter_scenarios:
            # 만약 Chapter 1이 없으면 전체에서 선택 (Fallback)
            print("⚠️  Warning: No first chapter scenarios found, returning any scenario")
            return random.choice(self.scenarios)
        
        selected = random.choice(first_chapter_scenarios)
        
        print(f"✅ Random Scenario Selected: {selected.id} - {selected.title}")
        print("="*60 + "\n")
        
        return selected
    
    async def get_scenario_by_id(self, scenario_id: str) -> Optional[Scenario]:
        """
        Get a specific scenario by ID
        
        모든 챕터 접근 가능 (Chapter 2, 3 포함)
        
        Args:
            scenario_id: 시나리오 ID
            
        Returns:
            Optional[Scenario]: 찾은 시나리오 또는 None
        """
        for scenario in self.scenarios:
            if scenario.id == scenario_id:
                print(f"Scenario Retrieved: {scenario.id} - {scenario.title}")
                return scenario
        
        print(f"Warning: Scenario not found: {scenario_id}")
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
    
    def get_all_first_chapters(self) -> list[Scenario]:
        """
        Get all first chapter scenarios
        
        홈 화면에 표시 가능한 모든 시나리오를 반환합니다.
        
        Returns:
            list[Scenario]: Chapter 1 또는 단일 챕터 시나리오 목록
        """
        return [
            s for s in self.scenarios 
            if self._is_first_chapter(s.id)
        ]
