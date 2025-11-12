document.addEventListener('DOMContentLoaded', () => {

    const screens = document.querySelectorAll('.screen');
    const navItems = document.querySelectorAll('.nav-item');
    
    const startScenarioBtn = document.getElementById('start-scenario-btn');
    const recordBtn = document.getElementById('record-btn');
    const continueBtn = document.getElementById('continue-btn');
    const backBtns = document.querySelectorAll('.icon-btn[data-target]');

    // 화면 전환 함수
    function showScreen(screenId) {
        screens.forEach(screen => {
            screen.classList.remove('active');
        });
        
        const activeScreen = document.getElementById(screenId);
        
        if (activeScreen) {
            activeScreen.classList.add('active');
        } else {
            // 존재하지 않는 화면 ID일 경우 (예: Profile) 홈으로 이동
            console.warn(`Screen with id "${screenId}" not found.`);
            document.getElementById('home-screen').classList.add('active');
            updateNav('home-screen'); // 네비도 홈으로 맞춰줌
            return;
        }

        updateNav(screenId);
    }

    // 네비게이션 상태 업데이트 함수
    function updateNav(screenId) {
        navItems.forEach(item => {
            if (item.dataset.target === screenId) {
                item.classList.add('active');
            } else {
                item.classList.remove('active');
            }
        });
    }

    // 1. 하단 네비게이션 클릭 이벤트 (수정됨: Profile 로직 제거)
    navItems.forEach(item => {
        item.addEventListener('click', () => {
            const targetScreenId = item.dataset.target;
            showScreen(targetScreenId); // showScreen 함수가 존재하지 않는 ID를 처리함
        });
    });

    // 2. 헤더의 뒤로가기/닫기 버튼 이벤트
    backBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            const targetScreenId = btn.dataset.target;
            showScreen(targetScreenId);
        });
    });

    // 3. 화면별 버튼 로직
    // (홈 -> 시나리오)
    if (startScenarioBtn) {
        startScenarioBtn.addEventListener('click', () => {
            showScreen('scenario-screen');
        });
    }

    // (시나리오 -> 로딩 -> 피드백)
    if (recordBtn) {
        recordBtn.addEventListener('click', () => {
            showScreen('loading-screen');
            
            setTimeout(() => {
                showScreen('feedback-screen');
            }, 2500);
        });
    }

    // (피드백 -> 홈)
    if (continueBtn) {
        continueBtn.addEventListener('click', () => {
            showScreen('home-screen');
        });
    }

    // 초기 화면 로드
    showScreen('home-screen');
});