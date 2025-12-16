package com.example.j_scenario.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.example.j_scenario.ui.theme.ScoreGreen
import com.example.j_scenario.ui.theme.ScoreOrange
import com.example.j_scenario.ui.theme.ScoreRed
import com.example.j_scenario.ui.theme.ScoreYellow

/**
 * 점수 기반 유틸리티 함수
 */
object ScoreUtils {
    
    /**
     * 점수에 따른 색상 계산 (그라데이션)
     * 
     * @param score 점수 (0-100)
     * @return 점수에 맞는 색상
     */
    fun getScoreColor(score: Int): Color {
        return when {
            score < 40 -> {
                // 0-39점: 빨강
                ScoreRed
            }
            score < 55 -> {
                // 40-54점: 빨강 → 주황 그라데이션
                val progress = (score - 40) / 15f
                lerp(ScoreRed, ScoreOrange, progress)
            }
            score < 70 -> {
                // 55-69점: 주황 → 노랑 그라데이션
                val progress = (score - 55) / 15f
                lerp(ScoreOrange, ScoreYellow, progress)
            }
            score < 85 -> {
                // 70-84점: 노랑 → 초록 그라데이션
                val progress = (score - 70) / 15f
                lerp(ScoreYellow, ScoreGreen, progress)
            }
            else -> {
                // 85-100점: 초록
                ScoreGreen
            }
        }
    }
    
    /**
     * 점수에 따른 평가 메시지
     */
    fun getScoreMessage(score: Int): String {
        return when {
            score < 40 -> "더 연습이 필요합니다"
            score < 55 -> "조금 더 노력해보세요"
            score < 70 -> "괜찮아요, 계속 해보세요"
            score < 85 -> "잘하고 있어요!"
            score < 95 -> "훌륭합니다!"
            else -> "완벽해요!"
        }
    }
    
    /**
     * 점수에 따른 이모지
     */
    fun getScoreEmoji(score: Int): String {
        return when {
            score < 40 -> "😰"
            score < 55 -> "😐"
            score < 70 -> "🙂"
            score < 85 -> "😊"
            score < 95 -> "🎉"
            else -> "🌟"
        }
    }
}

