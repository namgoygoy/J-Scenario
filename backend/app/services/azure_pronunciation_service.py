"""
Azure Speech Service for pronunciation assessment
보정된 텍스트를 Reference로 사용하여 발음 정확도를 평가
"""
import os
import asyncio
import io
import subprocess
import tempfile
from typing import Optional, Dict, Any
from app.config import get_settings
from app.utils.exceptions import ServiceUnavailableError, ServiceExecutionError

settings = get_settings()


class AzurePronunciationService:
    """Azure Speech 발음 평가 서비스"""
    
    def __init__(self):
        """Initialize Azure Speech service"""
        self.speech_key = settings.azure_speech_key
        self.speech_region = settings.azure_speech_region
        self.speech_sdk = None
        self._initialized = False
    
    def _ensure_sdk_initialized(self):
        """Ensure Azure Speech SDK is initialized"""
        if self._initialized:
            return
        
        self._initialized = True
        
        if not self.speech_key or not self.speech_region:
            print("Warning: Azure Speech credentials not configured")
            print(f"  speech_key exists: {bool(self.speech_key)}")
            print(f"  speech_region: {self.speech_region}")
            self.speech_sdk = None
            return
        
        try:
            import azure.cognitiveservices.speech as speechsdk
            self.speech_sdk = speechsdk
            print(f"Azure Speech SDK initialized (region: {self.speech_region})")
        except ImportError:
            print("Warning: azure-cognitiveservices-speech not installed")
            print("  Run: pip install azure-cognitiveservices-speech")
            self.speech_sdk = None
        except Exception as e:
            print(f"Warning: Azure Speech SDK initialization failed: {str(e)}")
            import traceback
            traceback.print_exc()
            self.speech_sdk = None
    
    async def assess_pronunciation(
        self,
        audio_data: bytes,
        reference_text: str,
        language: str = "ja-JP"
    ) -> Dict[str, Any]:
        """
        보정된 텍스트를 기준으로 발음 평가
        
        Args:
            audio_data: 오디오 바이너리 데이터
            reference_text: 보정된 텍스트 (정답지)
            language: 언어 코드 (기본값: ja-JP)
            
        Returns:
            Dict: 발음 평가 결과
            {
                "accuracy_score": 95,  # 정확도 점수 (0-100)
                "pronunciation_score": 90,  # 발음 점수 (0-100)
                "completeness_score": 88,  # 완성도 점수 (0-100)
                "fluency_score": 92,  # 유창성 점수 (0-100)
                "word_scores": [...]  # 단어별 상세 점수
            }
        """
        # SDK 초기화 확인
        self._ensure_sdk_initialized()
        
        if self.speech_sdk is None:
            raise ServiceUnavailableError(
                service_name="Azure Pronunciation Assessment",
                details="Azure Speech SDK is not initialized. Please check AZURE_SPEECH_KEY and AZURE_SPEECH_REGION configuration."
            )
        
        try:
            # 비동기 실행을 위해 스레드 풀 사용
            loop = asyncio.get_event_loop()
            result = await loop.run_in_executor(
                None,
                lambda: self._perform_pronunciation_assessment(
                    audio_data,
                    reference_text,
                    language
                )
            )
            return result
            
        except ServiceUnavailableError:
            # ServiceUnavailableError는 그대로 전파
            raise
        except Exception as e:
            print(f"Azure Pronunciation Assessment Error: {str(e)}")
            import traceback
            traceback.print_exc()
            raise ServiceExecutionError(
                service_name="Azure Pronunciation Assessment",
                details=str(e)
            ) from e
    
    def _convert_audio_to_wav(self, audio_data: bytes, filename: str = "audio.amr") -> bytes:
        """
        AMR/기타 포맷을 WAV(16kHz, mono, 16-bit PCM)로 변환
        Azure Speech SDK가 선호하는 포맷
        ffmpeg을 직접 호출하여 변환 (Python 3.13+ 호환)
        
        Args:
            audio_data: 원본 오디오 데이터
            filename: 파일명 (확장자로 포맷 감지)
            
        Returns:
            bytes: WAV 포맷 오디오 데이터
        """
        try:
            # 파일 확장자로 포맷 결정
            file_ext = filename.lower().split('.')[-1]
            
            print(f"  Converting {file_ext.upper()} to WAV using ffmpeg...")
            
            # 임시 파일 생성
            with tempfile.NamedTemporaryFile(suffix=f'.{file_ext}', delete=False) as temp_input:
                temp_input.write(audio_data)
                temp_input_path = temp_input.name
            
            with tempfile.NamedTemporaryFile(suffix='.wav', delete=False) as temp_output:
                temp_output_path = temp_output.name
            
            try:
                # ffmpeg 명령 실행
                # -ar 16000: 16kHz 샘플링 레이트
                # -ac 1: 모노 채널
                # -sample_fmt s16: 16-bit PCM
                cmd = [
                    'ffmpeg',
                    '-i', temp_input_path,  # 입력 파일
                    '-ar', '16000',          # 샘플링 레이트
                    '-ac', '1',              # 모노 채널
                    '-sample_fmt', 's16',    # 16-bit PCM
                    '-y',                    # 덮어쓰기
                    temp_output_path         # 출력 파일
                ]
                
                result = subprocess.run(
                    cmd,
                    capture_output=True,
                    text=True,
                    timeout=10
                )
                
                if result.returncode != 0:
                    print(f"  ⚠ ffmpeg error: {result.stderr}")
                    return audio_data
                
                # 변환된 WAV 파일 읽기
                with open(temp_output_path, 'rb') as f:
                    wav_data = f.read()
                
                print(f"  ✓ Converted: {len(audio_data)} bytes → {len(wav_data)} bytes (WAV 16kHz mono)")
                
                return wav_data
                
            finally:
                # 임시 파일 삭제
                try:
                    os.unlink(temp_input_path)
                    os.unlink(temp_output_path)
                except:
                    pass
                    
        except FileNotFoundError:
            print("  ⚠ Warning: ffmpeg not found")
            print("    Install: brew install ffmpeg (macOS)")
            return audio_data
        except subprocess.TimeoutExpired:
            print("  ⚠ Warning: ffmpeg conversion timeout")
            return audio_data
        except Exception as e:
            print(f"  ⚠ Warning: Audio conversion failed: {str(e)}")
            import traceback
            traceback.print_exc()
            return audio_data
    
    def _perform_pronunciation_assessment(
        self,
        audio_data: bytes,
        reference_text: str,
        language: str
    ) -> Dict[str, Any]:
        """
        실제 Azure Speech API 호출 (동기 함수)
        
        Args:
            audio_data: 오디오 데이터
            reference_text: 참조 텍스트
            language: 언어 코드
            
        Returns:
            Dict: 평가 결과
        """
        speechsdk = self.speech_sdk
        
        # 오디오 데이터 준비
        # 안드로이드에서 WAV 16kHz mono로 녹음되므로 변환 불필요
        # 필요 시 AMR → WAV 변환 로직 사용 가능
        wav_data = audio_data
        
        # Speech Config 설정
        speech_config = speechsdk.SpeechConfig(
            subscription=self.speech_key,
            region=self.speech_region
        )
        speech_config.speech_recognition_language = language
        
        # 발음 평가 설정
        pronunciation_config = speechsdk.PronunciationAssessmentConfig(
            reference_text=reference_text,
            grading_system=speechsdk.PronunciationAssessmentGradingSystem.HundredMark,
            granularity=speechsdk.PronunciationAssessmentGranularity.Phoneme,
            enable_miscue=True  # 잘못된 발음 감지
        )
        
        # 오디오 스트림 설정 (WAV 포맷, 16kHz, mono)
        # Azure Speech SDK가 기대하는 포맷 명시
        audio_format = speechsdk.audio.AudioStreamFormat(
            samples_per_second=16000,
            bits_per_sample=16,
            channels=1
        )
        
        audio_stream = speechsdk.audio.PushAudioInputStream(stream_format=audio_format)
        audio_stream.write(wav_data)
        audio_stream.close()
        
        audio_config = speechsdk.audio.AudioConfig(stream=audio_stream)
        
        # Speech Recognizer 생성
        recognizer = speechsdk.SpeechRecognizer(
            speech_config=speech_config,
            audio_config=audio_config
        )
        
        # 발음 평가 적용
        pronunciation_config.apply_to(recognizer)
        
        # 음성 인식 및 평가 실행
        result = recognizer.recognize_once()
        
        # 결과 처리
        if result.reason == speechsdk.ResultReason.RecognizedSpeech:
            pronunciation_result = speechsdk.PronunciationAssessmentResult(result)
            
            print(f"Azure Pronunciation Assessment:")
            print(f"  Recognized: {result.text}")
            print(f"  Reference: {reference_text}")
            print(f"  Accuracy: {pronunciation_result.accuracy_score}")
            print(f"  Pronunciation: {pronunciation_result.pronunciation_score}")
            print(f"  Completeness: {pronunciation_result.completeness_score}")
            print(f"  Fluency: {pronunciation_result.fluency_score}")
            
            # 단어별 점수 추출
            word_scores = []
            if hasattr(result, 'json') and result.json:
                import json
                result_json = json.loads(result.json)
                if "NBest" in result_json and len(result_json["NBest"]) > 0:
                    words = result_json["NBest"][0].get("Words", [])
                    word_scores = [
                        {
                            "word": w.get("Word", ""),
                            "accuracy_score": w.get("PronunciationAssessment", {}).get("AccuracyScore", 0),
                            "error_type": w.get("PronunciationAssessment", {}).get("ErrorType", "None")
                        }
                        for w in words
                    ]
            
            return {
                "accuracy_score": pronunciation_result.accuracy_score,
                "pronunciation_score": pronunciation_result.pronunciation_score,
                "completeness_score": pronunciation_result.completeness_score,
                "fluency_score": pronunciation_result.fluency_score,
                "recognized_text": result.text,
                "word_scores": word_scores
            }
        
        elif result.reason == speechsdk.ResultReason.NoMatch:
            print("Azure: No speech could be recognized")
            raise ServiceExecutionError(
                service_name="Azure Pronunciation Assessment",
                details="No speech could be recognized from the audio"
            )
        
        elif result.reason == speechsdk.ResultReason.Canceled:
            cancellation = result.cancellation_details
            print(f"Azure: Speech recognition canceled: {cancellation.reason}")
            error_details = f"Canceled: {cancellation.reason}"
            if cancellation.reason == speechsdk.CancellationReason.Error:
                error_details += f", Error: {cancellation.error_details}"
                print(f"  Error details: {cancellation.error_details}")
            raise ServiceExecutionError(
                service_name="Azure Pronunciation Assessment",
                details=error_details
            )
        
        else:
            print(f"Azure: Unexpected result reason: {result.reason}")
            raise ServiceExecutionError(
                service_name="Azure Pronunciation Assessment",
                details=f"Unexpected result reason: {result.reason}"
            )
    
    def _create_mock_pronunciation_scores(self) -> Dict[str, Any]:
        """
        Mock 발음 점수 생성 (Azure API 사용 불가 시)
        
        Returns:
            Dict: 모의 평가 결과
        """
        return {
            "accuracy_score": 88,
            "pronunciation_score": 85,
            "completeness_score": 90,
            "fluency_score": 87,
            "recognized_text": "",
            "word_scores": []
        }

