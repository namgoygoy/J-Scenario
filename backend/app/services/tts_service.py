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
        self.upload_dir = Path(settings.upload_dir)
        self.upload_dir.mkdir(exist_ok=True)
        
        # Google Cloud TTS 클라이언트 초기화
        self.client = None
        if self.credentials_path and os.path.exists(self.credentials_path):
            try:
                from google.cloud import texttospeech
                self.client = texttospeech.TextToSpeechAsyncClient()
                self.texttospeech = texttospeech
            except ImportError:
                print("Warning: google-cloud-texttospeech not installed")
    
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
        if not self.client or not self.texttospeech:
            # TTS 서비스가 없으면 None 반환
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
            
            # URL 반환 (실제 환경에서는 S3 등의 URL을 반환)
            return f"/uploads/{filename}"
            
        except Exception as e:
            print(f"TTS Error: {str(e)}")
            return None

