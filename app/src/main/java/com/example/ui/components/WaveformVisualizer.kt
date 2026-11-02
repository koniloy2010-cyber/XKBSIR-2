package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AmoledDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonElectricBlue
import com.example.ui.theme.SurfaceBorder
import kotlin.math.sin

@Composable
fun WaveformVisualizer(
    waveform: FloatArray,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_idle_anim")
    val idlePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "idle_phase"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .background(AmoledDark, RoundedCornerShape(12.dp))
            .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
            .padding(8.dp)
            .testTag("waveform_visualizer")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerY = height / 2f

            // Draw cyber grid background lines
            val gridColor = Color(0x1A00F0FF)
            drawLine(
                color = gridColor,
                start = Offset(0f, centerY),
                end = Offset(width, centerY),
                strokeWidth = 1f
            )
            val vLines = 8
            for (i in 1 until vLines) {
                val x = (width / vLines) * i
                drawLine(
                    color = gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 1f
                )
            }

            // Draw oscilloscope audio wave
            val path = Path()
            val glowPath = Path()
            val pointsCount = waveform.size

            if (pointsCount > 1) {
                val dx = width / (pointsCount - 1)

                for (i in 0 until pointsCount) {
                    val rawSample = if (isActive) {
                        waveform[i]
                    } else {
                        // Gentle idle sine wave breathing
                        (sin((i * 0.25f) + idlePhase) * 0.08f)
                    }

                    // Symmetrical peak height clamp
                    val amplitude = (rawSample * (centerY * 0.85f)).coerceIn(-centerY * 0.95f, centerY * 0.95f)
                    val x = i * dx
                    val y = centerY - amplitude

                    if (i == 0) {
                        path.moveTo(x, y)
                        glowPath.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                        glowPath.lineTo(x, y)
                    }
                }

                // Wide soft glow stroke
                drawPath(
                    path = glowPath,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            NeonElectricBlue.copy(alpha = 0.35f),
                            NeonCyan.copy(alpha = 0.6f),
                            NeonElectricBlue.copy(alpha = 0.35f)
                        )
                    ),
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                )

                // Crisp sharp neon center line
                drawPath(
                    path = path,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            NeonElectricBlue,
                            Color(0xFFE8F6FF),
                            NeonCyan
                        )
                    ),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
    }
}
