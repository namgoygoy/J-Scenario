package com.example.j_scenario.utils

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.concurrent.thread

/**
 * Audio recording utility using AudioRecord
 * 
 * Records audio directly to WAV format (16kHz, mono, 16-bit PCM)
 * for optimal Azure Speech API compatibility.
 */
class AudioRecorder(private val context: Context) {
    
    private var audioRecord: AudioRecord? = null
    private var recordingThread: Thread? = null
    private var outputFile: File? = null
    private var isRecordingActive: Boolean = false
    
    // Audio configuration - optimized for Azure Speech API
    private val sampleRate = 16000 // 16kHz
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
    
    /**
     * Start recording audio
     * 
     * @return File where audio is being recorded, or null if failed
     */
    fun startRecording(): File? {
        if (isRecordingActive) {
            Log.w(TAG, "Recording is already active")
            return outputFile
        }
        
        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.e(TAG, "Invalid buffer size")
            return null
        }
        
        try {
            outputFile = createAudioFile()
            
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize * 2
            )
            
            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord initialization failed")
                cleanupRecorder()
                return null
            }
            
            audioRecord?.startRecording()
            isRecordingActive = true
            
            // Start recording thread
            recordingThread = thread {
                writeAudioDataToFile()
            }
            
            Log.d(TAG, "Recording started (WAV 16kHz mono): ${outputFile?.absolutePath}")
            return outputFile
            
        } catch (e: SecurityException) {
            Log.e(TAG, "Recording permission denied", e)
            cleanupRecorder()
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recording", e)
            cleanupRecorder()
            return null
        }
    }
    
    /**
     * Stop recording and return the recorded file
     * 
     * @return File containing the recorded audio, or null if failed
     */
    fun stopRecording(): File? {
        if (!isRecordingActive) {
            Log.w(TAG, "No active recording to stop")
            return null
        }
        
        return try {
            isRecordingActive = false
            
            audioRecord?.apply {
                stop()
                release()
            }
            audioRecord = null
            
            // Wait for recording thread to finish
            recordingThread?.join(1000)
            recordingThread = null
            
            val file = outputFile
            Log.d(TAG, "Recording stopped: ${file?.absolutePath}")
            file
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop recording", e)
            cleanupRecorder()
            null
        }
    }
    
    /**
     * Cancel recording and delete the file
     */
    fun cancelRecording() {
        try {
            isRecordingActive = false
            
            audioRecord?.apply {
                    stop()
                    release()
            }
            audioRecord = null
            
            recordingThread?.join(1000)
            recordingThread = null
            
            outputFile?.delete()
            outputFile = null
            
            Log.d(TAG, "Recording cancelled")
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling recording", e)
            cleanupRecorder()
        }
    }
    
    /**
     * Check if currently recording
     */
    fun isRecording(): Boolean = isRecordingActive
    
    /**
     * Create a temporary file for audio recording
     */
    private fun createAudioFile(): File {
        val fileName = "audio_${System.currentTimeMillis()}.wav"
        val storageDir = context.cacheDir
        return File(storageDir, fileName)
    }
    
    /**
     * Write audio data to file with WAV header
     */
    private fun writeAudioDataToFile() {
        val tempFile = File(context.cacheDir, "temp_${System.currentTimeMillis()}.pcm")
        
        try {
            FileOutputStream(tempFile).use { fos ->
                val buffer = ByteArray(bufferSize)
                
                while (isRecordingActive) {
                    val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    
                    if (read > 0) {
                        fos.write(buffer, 0, read)
                    } else if (read == AudioRecord.ERROR_INVALID_OPERATION) {
                        Log.e(TAG, "Invalid operation during recording")
                        break
                    } else if (read == AudioRecord.ERROR_BAD_VALUE) {
                        Log.e(TAG, "Bad value during recording")
                        break
                    }
                }
            }
            
            // Convert PCM to WAV by adding header
            if (tempFile.exists() && outputFile != null) {
                convertPcmToWav(tempFile, outputFile!!)
                tempFile.delete()
            }
            
        } catch (e: IOException) {
            Log.e(TAG, "Error writing audio data", e)
        } finally {
            tempFile.delete()
        }
    }
    
    /**
     * Convert PCM file to WAV by adding WAV header
     */
    private fun convertPcmToWav(pcmFile: File, wavFile: File) {
        try {
            val pcmData = pcmFile.readBytes()
            val wavData = ByteArray(44 + pcmData.size)
            
            writeWavHeader(wavData, pcmData.size, sampleRate, 1, 16)
            System.arraycopy(pcmData, 0, wavData, 44, pcmData.size)
            
            wavFile.writeBytes(wavData)
            
        } catch (e: IOException) {
            Log.e(TAG, "Error converting PCM to WAV", e)
        }
    }
    
    /**
     * Write WAV file header
     * 
     * @param header Byte array to write header to (must be at least 44 bytes)
     * @param dataSize Size of PCM data in bytes
     * @param sampleRate Sample rate (e.g., 16000)
     * @param channels Number of channels (1 for mono, 2 for stereo)
     * @param bitsPerSample Bits per sample (e.g., 16)
     */
    private fun writeWavHeader(
        header: ByteArray,
        dataSize: Int,
        sampleRate: Int,
        channels: Int,
        bitsPerSample: Int
    ) {
        val totalDataLen = dataSize + 36
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val blockAlign = channels * bitsPerSample / 8
        
        header[0] = 'R'.code.toByte()  // RIFF chunk descriptor
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = (totalDataLen shr 8 and 0xff).toByte()
        header[6] = (totalDataLen shr 16 and 0xff).toByte()
        header[7] = (totalDataLen shr 24 and 0xff).toByte()
        header[8] = 'W'.code.toByte()  // WAVE format
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte() // fmt subchunk
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16  // Subchunk1Size (16 for PCM)
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1   // AudioFormat (1 = PCM)
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = (sampleRate shr 8 and 0xff).toByte()
        header[26] = (sampleRate shr 16 and 0xff).toByte()
        header[27] = (sampleRate shr 24 and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = (byteRate shr 8 and 0xff).toByte()
        header[30] = (byteRate shr 16 and 0xff).toByte()
        header[31] = (byteRate shr 24 and 0xff).toByte()
        header[32] = blockAlign.toByte()
        header[33] = 0
        header[34] = bitsPerSample.toByte()
        header[35] = 0
        header[36] = 'd'.code.toByte() // data subchunk
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (dataSize and 0xff).toByte()
        header[41] = (dataSize shr 8 and 0xff).toByte()
        header[42] = (dataSize shr 16 and 0xff).toByte()
        header[43] = (dataSize shr 24 and 0xff).toByte()
    }
    
    /**
     * Cleanup recorder resources
     */
    private fun cleanupRecorder() {
        try {
            isRecordingActive = false
            audioRecord?.release()
            recordingThread?.interrupt()
            recordingThread = null
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing AudioRecord", e)
        }
        audioRecord = null
        outputFile?.delete()
        outputFile = null
    }
    
    companion object {
        private const val TAG = "AudioRecorder"
    }
}

