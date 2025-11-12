package com.example.j_scenario.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.j_scenario.ui.theme.JScenarioTheme
import com.example.j_scenario.ui.theme.PrimaryGreen
import com.example.j_scenario.ui.theme.ProgressBg

@Composable
fun CustomProgressBar(
    progress: Float, // 0.0 ~ 1.0
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    trackColor: Color = ProgressBg,
    progressColor: Color = PrimaryGreen,
    indicatorSize: Dp = 12.dp,
    indicatorInnerSize: Dp = 6.dp
) {
    val density = LocalDensity.current
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(10.dp))
            .background(trackColor)
            .onSizeChanged { size ->
                boxSize = size
            }
    ) {
        // Progress fill
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .clip(RoundedCornerShape(10.dp))
                .background(progressColor)
        )
        
        // Circular indicator at the end of progress
        if (progress > 0f && boxSize.width > 0) {
            val progressWidth = boxSize.width * progress.coerceIn(0f, 1f)
            val indicatorOffset = with(density) {
                (progressWidth - indicatorSize.toPx() / 2).toDp()
            }
            
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = indicatorOffset)
                    .size(indicatorSize)
                    .clip(CircleShape)
                    .background(trackColor),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(indicatorInnerSize)
                        .clip(CircleShape)
                        .background(progressColor)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F251A)
@Composable
fun CustomProgressBarPreview() {
    JScenarioTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            CustomProgressBar(progress = 0.95f)
            CustomProgressBar(progress = 0.88f)
            CustomProgressBar(progress = 0.93f)
            CustomProgressBar(progress = 0.6f)
        }
    }
}
