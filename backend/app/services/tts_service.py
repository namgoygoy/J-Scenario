"""
Text-to-Speech service using Google Cloud TTS
"""
import os
from pathlib import Path
from typing import Optional
from app.config import get_settings

settings = get_settings()


class TTSService:
    """텍스트를 음성으로 변환하는 서비스"""
    
    def __init__(self):
        """Initialize TTS service"""
        self.credentials_path = settings.google_application_credentials
        self.project_id = settings.google_cloud_project_id
        # 절대 경로로 설정 (backend/uploads/audio)
        base_dir = Path(__file__).parent.parent.parent
        self.upload_dir = base_dir / "uploads" / "audio"
        self.upload_dir.mkdir(parents=True, exist_ok=True)
        print(f"TTS upload_dir initialized: {self.upload_dir.absolute()}")
        
        # Google Cloud TTS 클라이언트 초기화 (지연 로딩)
        self.client = None
        self.texttospeech = None
        self._initialized = False
    
    def _ensure_client_initialized(self):
        """Ensure Google Cloud TTS client is initialized"""
        if self._initialized:
            return
        
        self._initialized = True
        
        # Google Cloud TTS 클라이언트 초기화
        print(f"TTS Service init: credentials_path={self.credentials_path}")
        if self.credentials_path and os.path.exists(self.credentials_path):
            try:
                os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = self.credentials_path
                print(f"Setting GOOGLE_APPLICATION_CREDENTIALS for TTS: {self.credentials_path}")
                from google.cloud import texttospeech
                self.client = texttospeech.TextToSpeechAsyncClient()
                self.texttospeech = texttospeech
                print("Google Cloud TTS client initialized successfully")
            except ImportError:
                print("Warning: google-cloud-texttospeech not installed")
                self.client = None
                self.texttospeech = None
            except Exception as e:
                print(f"Warning: Google Cloud TTS client initialization failed: {str(e)}")
                import traceback
                traceback.print_exc()
                self.client = None
                self.texttospeech = None
        else:
            print(f"Warning: TTS credentials file not found at {self.credentials_path}")
            self.client = None
            self.texttospeech = None
    
    async def synthesize_speech(
        self,
        text: str,
        interaction_id: str
    ) -> Optional[str]:
        """
        Synthesize speech from text using Google Cloud TTS
        
        Args:
            text: 변환할 텍스트
            interaction_id: 인터랙션 ID (파일명 생성용)
            
        Returns:
            Optional[str]: 생성된 음성 파일 URL
        """
        # 클라이언트 초기화 확인
        self._ensure_client_initialized()
        
        if not self.client or not self.texttospeech:
            # TTS 서비스가 없으면 None 반환
            print("Warning: TTS client is None, skipping speech synthesis")
            return None
        
        try:
            # 음성 합성 요청 구성
            synthesis_input = self.texttospeech.SynthesisInput(text=text)
            
            # 일본어 음성 설정
            voice = self.texttospeech.VoiceSelectionParams(
                language_code="ja-JP",
                name="ja-JP-Wavenet-A",  # 자연스러운 여성 음성
                ssml_gender=self.texttospeech.SsmlVoiceGender.FEMALE
            )
            
            # 오디오 설정
            audio_config = self.texttospeech.AudioConfig(
                audio_encoding=self.texttospeech.AudioEncoding.MP3,
                speaking_rate=1.0,
                pitch=0.0
            )
            
            # TTS API 호출
            response = await self.client.synthesize_speech(
                input=synthesis_input,
                voice=voice,
                audio_config=audio_config
            )
            
            # 오디오 파일 저장
            filename = f"{interaction_id}_response.mp3"
            filepath = self.upload_dir / filename
            
            with open(filepath, "wb") as out:
                out.write(response.audio_content)
            
            print(f"TTS file saved: {filepath}")
            print(f"TTS file exists: {filepath.exists()}")
            
            # URL 반환 (uploads/audio/ 디렉토리에 저장)
            return f"/uploads/audio/{filename}"
            
        except Exception as e:
            print(f"TTS Error: {str(e)}")
            return None

