package com.example.j_scenario.utils

/**
 * URL 유틸리티 함수
 */
object UrlUtils {
    
    /**
     * 백엔드 기본 URL
     * 
     * ⚠️ 중요: 에뮬레이터와 실제 기기 간 전환 시 이 값을 변경해야 합니다!
     * 
     * - 에뮬레이터 테스트: "http://10.0.2.2:8000"
     * - 실제 기기 테스트: "http://[컴퓨터의_IP_주소]:8000"
     * 
     * 컴퓨터 IP 주소 확인 방법:
     * - macOS/Linux: ifconfig | grep "inet " | grep -v 127.0.0.1
     * - Windows: ipconfig (IPv4 주소 확인)
     * 
     * 현재 설정된 IP: 192.168.123.101 (실제 기기용)
     * 에뮬레이터 사용 시: 10.0.2.2로 변경 필요
     */
    private const val BASE_URL = "http://192.168.123.101:8000"
    
    /**
     * 상대 경로를 절대 URL로 변환
     * 
     * @param url 상대 경로 또는 절대 URL
     * @return 절대 URL
     */
    fun toAbsoluteUrl(url: String?): String {
        if (url == null || url.isBlank()) {
            return ""
        }
        
        // 이미 절대 URL인 경우 그대로 반환
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url
        }
        
        // 상대 경로인 경우 백엔드 기본 URL과 결합
        return "$BASE_URL$url"
    }
}


