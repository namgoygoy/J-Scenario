"""
Speech-to-Text service using Google Cloud Speech-to-Text API
"""
import os
import asyncio
from typing import Optional
from google.cloud import speech
from app.config import get_settings

settings = get_settings()


class STTService:
    """음성을 텍스트로 변환하는 서비스"""
    
    def __init__(self):
        """Initialize STT service"""
        self.credentials_path = settings.google_application_credentials
        self.client: Optional[speech.SpeechClient] = None
        
        # Google Cloud Speech-to-Text 클라이언트 초기화
        if self.credentials_path and os.path.exists(self.credentials_path):
            try:
                os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = self.credentials_path
                self.client = speech.SpeechClient()
            except Exception as e:
                print(f"Warning: Google Cloud Speech client initialization failed: {str(e)}")
                self.client = None
    
    async def transcribe_audio(self, audio_data: bytes) -> str:
        """
        Transcribe audio to text using Google Cloud Speech-to-Text
        
        Args:
            audio_data: 오디오 바이너리 데이터
            
        Returns:
            str: 변환된 텍스트
        """
        # 타입 체크: self.client가 None이 아님을 확인
        if self.client is None:
            # API 키가 없으면 더미 응답 반환
            return "財布をなくしました。どこにありますか。"
        
        # 타입 가드를 위해 로컬 변수에 할당
        client = self.client
        
        try:
            # 비동기 실행을 위해 스레드 풀 사용
            loop = asyncio.get_event_loop()
            
            # 음성 인식 설정
            config = speech.RecognitionConfig(
                encoding=speech.RecognitionConfig.AudioEncoding.LINEAR16,
                sample_rate_hertz=16000,
                language_code="ja-JP",  # 일본어
                alternative_language_codes=["en-US"],  # 대체 언어
            )
            
            audio = speech.RecognitionAudio(content=audio_data)
            
            # 동기 호출을 비동기로 실행 (로컬 변수 사용으로 타입 체크 통과)
            response = await loop.run_in_executor(
                None,
                lambda: client.recognize(config=config, audio=audio)
            )
            
            # 결과 추출
            if response.results:
                return response.results[0].alternatives[0].transcript
            else:
                return "音声を認識できませんでした。"
            
        except Exception as e:
            print(f"STT Error: {str(e)}")
            # 에러 발생 시 더미 응답 반환
            return "財布をなくしました。どこにありますか。"

