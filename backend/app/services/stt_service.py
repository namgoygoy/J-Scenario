"""
Speech-to-Text service using Google Cloud Speech-to-Text API
"""
import os
import asyncio
from typing import Optional
from google.cloud import speech
from app.config import get_settings
from app.utils.exceptions import ServiceUnavailableError

settings = get_settings()


class STTService:
    """음성을 텍스트로 변환하는 서비스"""
    
    def __init__(self):
        """Initialize STT service"""
        self.credentials_path = settings.google_application_credentials
        self.client: Optional[speech.SpeechClient] = None
        self._initialized = False
    
    def _ensure_client_initialized(self):
        """Ensure Google Cloud Speech client is initialized"""
        if self._initialized:
            return
        
        self._initialized = True
        
        # Google Cloud Speech-to-Text 클라이언트 초기화
        print(f"STT Service init: credentials_path={self.credentials_path}")
        if self.credentials_path and os.path.exists(self.credentials_path):
            try:
                os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = self.credentials_path
                print(f"Setting GOOGLE_APPLICATION_CREDENTIALS={self.credentials_path}")
                self.client = speech.SpeechClient()
                print("Google Cloud Speech-to-Text client initialized successfully")
            except Exception as e:
                print(f"Warning: Google Cloud Speech client initialization failed: {str(e)}")
                import traceback
                traceback.print_exc()
                self.client = None
        else:
            print(f"Warning: Credentials file not found at {self.credentials_path}")
            if self.credentials_path:
                print(f"  Absolute path check: {os.path.abspath(self.credentials_path)}")
                print(f"  File exists (abs): {os.path.exists(os.path.abspath(self.credentials_path))}")
            self.client = None
    
    async def transcribe_audio(self, audio_data: bytes, filename: str = "") -> str:
        """
        Transcribe audio to text using Google Cloud Speech-to-Text
        
        Args:
            audio_data: 오디오 바이너리 데이터
            filename: 파일명 (확장자로 포맷 감지용, 선택)
            
        Returns:
            str: 변환된 텍스트
        """
        # 클라이언트 초기화 확인
        self._ensure_client_initialized()
        
        # 타입 체크: self.client가 None이 아님을 확인
        if self.client is None:
            # API 키가 없으면 명확한 에러 발생
            error_details = f"Credentials path: {self.credentials_path}, File exists: {os.path.exists(self.credentials_path) if self.credentials_path else False}"
            raise ServiceUnavailableError(
                service_name="STT",
                details=error_details
            )
        
        # 타입 가드를 위해 로컬 변수에 할당
        client = self.client
        
        try:
            # 비동기 실행을 위해 스레드 풀 사용
            loop = asyncio.get_event_loop()
            
            # 파일 확장자로 포맷 감지
            # Android 앱은 AMR-WB 포맷으로 녹음 (Google STT 지원)
            filename_lower = filename.lower() if filename else ""
            
            # AMR 파일: Google STT가 공식 지원하는 포맷
            if filename_lower.endswith('.amr'):
                # AMR-WB는 16kHz 고정, 명시적으로 지정 필요
                config = speech.RecognitionConfig(
                    encoding=speech.RecognitionConfig.AudioEncoding.AMR_WB,
                    sample_rate_hertz=16000,  # AMR-WB는 16kHz 고정
                    language_code="ja-JP",
                    alternative_language_codes=["en-US"],
                    enable_automatic_punctuation=True,
                    use_enhanced=True,  # AMR은 use_enhanced 지원
                )
            elif filename_lower.endswith('.wav'):
                # WAV 파일은 LINEAR16 인코딩, 샘플레이트 지정 필요
                config = speech.RecognitionConfig(
                    encoding=speech.RecognitionConfig.AudioEncoding.LINEAR16,
                    sample_rate_hertz=16000,
                    language_code="ja-JP",
                    alternative_language_codes=["en-US"],
                    enable_automatic_punctuation=True,
                    use_enhanced=True,
                )
            elif filename_lower.endswith(('.mp3', '.mp4')):
                # MP3 파일 (Google STT 지원)
                config = speech.RecognitionConfig(
                    encoding=speech.RecognitionConfig.AudioEncoding.MP3,
                    language_code="ja-JP",
                    alternative_language_codes=["en-US"],
                    enable_automatic_punctuation=True,
                )
            else:
                # 기본값: ENCODING_UNSPECIFIED (자동 감지)
                config = speech.RecognitionConfig(
                    encoding=speech.RecognitionConfig.AudioEncoding.ENCODING_UNSPECIFIED,
                    language_code="ja-JP",
                    alternative_language_codes=["en-US"],
                    enable_automatic_punctuation=True,
                )
            
            audio = speech.RecognitionAudio(content=audio_data)
            
            # 디버깅: 설정 정보 출력
            print(f"STT Config: encoding={config.encoding}, sample_rate={config.sample_rate_hertz if hasattr(config, 'sample_rate_hertz') else 'N/A'}, language={config.language_code}")
            print(f"STT Audio: size={len(audio_data)} bytes, filename={filename}")
            
            # 동기 호출을 비동기로 실행
            response = await loop.run_in_executor(
                None,
                lambda: client.recognize(config=config, audio=audio)
            )
            
            # 결과 추출 및 상세 로깅
            print(f"STT Response: results_count={len(response.results)}")
            if response.results:
                for i, result in enumerate(response.results):
                    print(f"  Result {i}: alternatives={len(result.alternatives)}")
                    for j, alternative in enumerate(result.alternatives):
                        print(f"    Alternative {j}: transcript='{alternative.transcript}', confidence={alternative.confidence}")
                
                transcript = response.results[0].alternatives[0].transcript
                confidence = response.results[0].alternatives[0].confidence
                print(f"STT Success: transcript='{transcript}', confidence={confidence}")
                return transcript
            else:
                print("STT Warning: No results returned")
                print("  Possible reasons: audio too short, too quiet, or encoding mismatch")
                # 더 자세한 에러 정보 확인
                if hasattr(response, 'error'):
                    print(f"  Error: {response.error}")
                return "音声を認識できませんでした。"
            
        except ServiceUnavailableError:
            # ServiceUnavailableError는 그대로 전파
            raise
        except Exception as e:
            print(f"STT Error: {str(e)}")
            import traceback
            traceback.print_exc()
            # 기타 예외는 ServiceExecutionError로 변환
            from app.utils.exceptions import ServiceExecutionError
            raise ServiceExecutionError(
                service_name="STT",
                details=str(e)
            ) from e

