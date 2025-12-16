#!/usr/bin/env python3
"""
랜덤 시나리오 테스트 스크립트
백엔드 서버를 실행한 상태에서 이 스크립트를 실행하세요.
"""
import asyncio
import httpx

BASE_URL = "http://localhost:8000"


async def test_random_scenarios(count: int = 10):
    """
    랜덤 시나리오를 여러 번 호출해서 다양한 시나리오가 반환되는지 확인
    
    Args:
        count: 테스트 횟수
    """
    print("="*60)
    print(f"🎲 랜덤 시나리오 테스트 (총 {count}회)")
    print("="*60 + "\n")
    
    scenario_counts = {}
    
    async with httpx.AsyncClient() as client:
        for i in range(count):
            try:
                response = await client.get(f"{BASE_URL}/api/scenarios/random")
                
                if response.status_code == 200:
                    data = response.json()
                    scenario = data.get("scenario", {})
                    scenario_id = scenario.get("id", "unknown")
                    scenario_title = scenario.get("title", "unknown")
                    
                    # 카운트
                    scenario_counts[scenario_id] = scenario_counts.get(scenario_id, 0) + 1
                    
                    print(f"[{i+1:2d}] {scenario_id:20s} - {scenario_title}")
                else:
                    print(f"[{i+1:2d}] ❌ Error: HTTP {response.status_code}")
                    
            except Exception as e:
                print(f"[{i+1:2d}] ❌ Exception: {str(e)}")
    
    # 결과 요약
    print("\n" + "="*60)
    print("📊 결과 요약")
    print("="*60)
    print(f"총 호출 횟수: {count}")
    print(f"고유 시나리오 수: {len(scenario_counts)}")
    print("\n시나리오별 선택 횟수:")
    for scenario_id, cnt in sorted(scenario_counts.items()):
        percentage = (cnt / count) * 100
        bar = "█" * int(percentage / 2)
        print(f"  {scenario_id:20s}: {cnt:2d}회 ({percentage:5.1f}%) {bar}")
    
    print("\n" + "="*60)
    if len(scenario_counts) == 1:
        print("⚠️  경고: 항상 같은 시나리오만 반환됩니다!")
        print("   백엔드 서버를 재시작해주세요.")
    elif len(scenario_counts) < 3:
        print("⚠️  주의: 시나리오 다양성이 부족합니다.")
    else:
        print("✅ 성공: 다양한 시나리오가 랜덤하게 선택됩니다!")
    print("="*60 + "\n")


if __name__ == "__main__":
    asyncio.run(test_random_scenarios(count=20))


