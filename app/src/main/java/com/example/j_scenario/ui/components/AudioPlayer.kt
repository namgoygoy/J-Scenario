package com.example.j_scenario.ui.components

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.audio.AudioAttributes as ExoAudioAttributes

/**
 * 네트워크 오디오 재생 컴포저블
 * 
 * @param audioUrl 재생할 오디오 파일의 URL (네트워크 또는 로컬)
 * @param autoPlay 자동 재생 여부 (기본값: false)
 * @param onPlaybackStateChanged 재생 상태 변경 콜백 (true: 재생 중, false: 정지)
 */
@Composable
fun AudioPlayer(
    audioUrl: String?,
    autoPlay: Boolean = false,
    modifier: Modifier = Modifier,
    onPlaybackStateChanged: ((Boolean) -> Unit)? = null
) {
    val context = LocalContext.current
    
    // ExoPlayer 인스턴스 생성 및 관리
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            // 오디오 속성 설정 (볼륨 및 오디오 포커스)
            setAudioAttributes(
                ExoAudioAttributes.Builder()
                    .setContentType(com.google.android.exoplayer2.C.CONTENT_TYPE_SPEECH)
                    .setUsage(com.google.android.exoplayer2.C.USAGE_MEDIA)
                    .build(),
                true // handleAudioFocus = true
            )
            
            // 볼륨 설정 (1.0f = 최대 볼륨)
            volume = 1.0f
            
            Log.d("AudioPlayer", "ExoPlayer created with volume: $volume")
            
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    val stateName = when (playbackState) {
                        Player.STATE_IDLE -> "IDLE"
                        Player.STATE_BUFFERING -> "BUFFERING"
                        Player.STATE_READY -> "READY"
                        Player.STATE_ENDED -> "ENDED"
                        else -> "UNKNOWN"
                    }
                    Log.d("AudioPlayer", "Playback state changed: $stateName")
                    
                    val isPlayingNow = playbackState == Player.STATE_READY && isPlaying
                    onPlaybackStateChanged?.invoke(isPlayingNow)
                }
                
                override fun onPlayerError(error: com.google.android.exoplayer2.PlaybackException) {
                    Log.e("AudioPlayer", "Player error: ${error.message}", error)
                    Log.e("AudioPlayer", "Error type: ${error.errorCode}")
                    Log.e("AudioPlayer", "Error cause: ${error.cause?.message}")
                }
                
                override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                    Log.d("AudioPlayer", "Is playing changed: $isPlayingNow")
                }
            })
        }
    }
    
    // 재생 상태 관리
    var isPlaying by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    // 오디오 URL이 변경되면 미디어 아이템 설정
    LaunchedEffect(audioUrl) {
        if (audioUrl != null && audioUrl.isNotBlank()) {
            Log.d("AudioPlayer", "Loading audio URL: $audioUrl")
            isLoading = true
            try {
                val uri = Uri.parse(audioUrl)
                Log.d("AudioPlayer", "Parsed URI: $uri")
                
                val mediaItem = MediaItem.fromUri(uri)
                Log.d("AudioPlayer", "Created MediaItem: ${mediaItem.mediaId}")
                
                player.setMediaItem(mediaItem)
                player.prepare()
                Log.d("AudioPlayer", "Player prepared, autoPlay=$autoPlay")
                
                // 자동 재생이 활성화되어 있으면 재생 시작
                // ExoPlayer가 이미 handleAudioFocus=true로 설정되어 있어 자동으로 오디오 포커스를 처리함
                if (autoPlay) {
                    Log.d("AudioPlayer", "Starting playback...")
                    Log.d("AudioPlayer", "Current volume: ${player.volume}")
                    Log.d("AudioPlayer", "Audio attributes: ${player.audioAttributes}")
                    
                    // ExoPlayer가 자동으로 오디오 포커스를 처리하므로 수동 요청 불필요
                    player.play()
                    isPlaying = true
                }
            } catch (e: Exception) {
                Log.e("AudioPlayer", "Error loading audio: ${e.message}", e)
                e.printStackTrace()
            } finally {
                isLoading = false
                Log.d("AudioPlayer", "Loading finished, isLoading=false")
            }
        } else {
            Log.w("AudioPlayer", "Audio URL is null or blank")
        }
    }
    
    // 재생 상태 업데이트
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                isPlaying = isPlayingNow
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
        }
    }
    
    // 컴포저블이 dispose될 때 플레이어 해제
    // ExoPlayer가 자동으로 오디오 포커스를 해제하므로 수동 해제 불필요
    DisposableEffect(Unit) {
        onDispose {
            player.release()
            Log.d("AudioPlayer", "Player released")
        }
    }
    
    // UI 렌더링
    if (audioUrl != null && audioUrl.isNotBlank()) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = {
                    if (isPlaying) {
                        Log.d("AudioPlayer", "Pausing playback")
                        player.pause()
                        isPlaying = false
                    } else {
                        Log.d("AudioPlayer", "Starting playback manually")
                        Log.d("AudioPlayer", "Current volume: ${player.volume}")
                        
                        // ExoPlayer가 자동으로 오디오 포커스를 처리하므로 수동 요청 불필요
                        player.play()
                        isPlaying = true
                    }
                },
                enabled = !isLoading
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "일시정지" else "재생",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

