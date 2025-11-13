"""
Simple API test script
"""
import asyncio
import httpx

BASE_URL = "http://localhost:8000"


async def test_health():
    """Test health check endpoint"""
    async with httpx.AsyncClient() as client:
        response = await client.get(f"{BASE_URL}/health")
        print("Health Check:", response.json())


async def test_get_random_scenario():
    """Test get random scenario endpoint"""
    async with httpx.AsyncClient() as client:
        response = await client.get(f"{BASE_URL}/api/scenarios/random")
        print("\nRandom Scenario:", response.json())


async def main():
    print("=" * 50)
    print("J-Scenario API Test")
    print("=" * 50)
    
    await test_health()
    await test_get_random_scenario()
    
    print("\n" + "=" * 50)
    print("테스트 완료!")
    print("=" * 50)


if __name__ == "__main__":
    asyncio.run(main())

