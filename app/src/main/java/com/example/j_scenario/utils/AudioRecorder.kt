package com.example.j_scenario.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.IOException

/**
 * Audio recording utility using MediaRecorder
 * 
 * Handles recording audio to a file with proper lifecycle management.
 */
class AudioRecorder(private val context: Context) {
    
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var isRecordingActive: Boolean = false
    
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
        try {
            outputFile = createAudioFile()
            mediaRecorder = createMediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                // Google STT API가 지원하는 AMR 포맷으로 변경
                setOutputFormat(MediaRecorder.OutputFormat.AMR_WB)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB)
                // AMR-WB는 16kHz 샘플링 레이트 사용 (고정)
                setOutputFile(outputFile?.absolutePath)
                prepare()
                start()
            }
            isRecordingActive = true
            Log.d(TAG, "Recording started (AMR-WB): ${outputFile?.absolutePath}")
            return outputFile
        } catch (e: IOException) {
            Log.e(TAG, "Failed to start recording", e)
            cleanupRecorder()
            return null
        } catch (e: IllegalStateException) {
            Log.e(TAG, "MediaRecorder in illegal state", e)
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
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecordingActive = false
            val file = outputFile
            Log.d(TAG, "Recording stopped: ${file?.absolutePath}")
            file
        } catch (e: IllegalStateException) {
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
            if (isRecordingActive) {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
                mediaRecorder = null
                isRecordingActive = false
            }
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
        // AMR-WB 포맷으로 변경 (Google STT 지원)
        val fileName = "audio_${System.currentTimeMillis()}.amr"
        val storageDir = context.cacheDir
        return File(storageDir, fileName)
    }
    
    /**
     * Create MediaRecorder instance based on Android version
     */
    private fun createMediaRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
    }
    
    /**
     * Cleanup recorder resources
     */
    private fun cleanupRecorder() {
        try {
            mediaRecorder?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing MediaRecorder", e)
        }
        mediaRecorder = null
        isRecordingActive = false
        outputFile?.delete()
        outputFile = null
    }
    
    companion object {
        private const val TAG = "AudioRecorder"
    }
}

