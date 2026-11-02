package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmoledDark
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.NeonActiveGreen
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonElectricBlue
import com.example.ui.theme.NeonPowerRed
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CyberMicHero(
    isActive: Boolean,
    micLevel: Float,
    isRecordingSample: Boolean,
    isPlayingSample: Boolean,
    hasRecordedSample: Boolean,
    onToggleEngine: () -> Unit,
    onStartRecord: () -> Unit,
    onStopRecord: () -> Unit,
    onPlaySample: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mic_rings")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val haloAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_alpha"
    )

    val ringColor by animateColorAsState(
        targetValue = if (isActive) NeonCyan else Color(0xFF263852),
        animationSpec = tween(400),
        label = "ring_color"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceCard, RoundedCornerShape(18.dp))
            .border(1.5.dp, if (isActive) NeonCyan.copy(alpha = 0.4f) else SurfaceBorder, RoundedCornerShape(18.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Status Badge Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            if (isActive) NeonActiveGreen else NeonPowerRed,
                            CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isActive) "LIVE MIC ACTIVE" else "DSP ENGINE STANDBY",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isActive) NeonActiveGreen else TextMuted
                )
            }

            // Real-time Audio Level Peak meter
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "LEVEL",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height(8.dp)
                        .background(AmoledDark, RoundedCornerShape(4.dp))
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(4.dp))
                ) {
                    val fillPercent = if (isActive) (micLevel * 3.5f).coerceIn(0f, 1f) else 0f
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fillPercent)
                            .height(8.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(NeonElectricBlue, NeonCyan, NeonPowerRed)
                                ),
                                RoundedCornerShape(4.dp)
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Large Animated Circular Microphone Hero
        Box(
            modifier = Modifier
                .size(170.dp),
            contentAlignment = Alignment.Center
        ) {
            // Live Pulsing Halo Canvas
            Canvas(modifier = Modifier.size(170.dp)) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val baseRadius = size.width / 2.7f

                if (isActive) {
                    val audioBoost = (micLevel * 40f).coerceAtMost(30f)
                    val r1 = (baseRadius * pulseScale) + audioBoost
                    drawCircle(
                        color = NeonCyan.copy(alpha = (haloAlpha * 0.4f).coerceIn(0f, 1f)),
                        radius = r1,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    drawCircle(
                        color = NeonElectricBlue.copy(alpha = (haloAlpha * 0.2f).coerceIn(0f, 1f)),
                        radius = r1 + 12.dp.toPx(),
                        center = center,
                        style = Stroke(width = 1.dp.toPx())
                    )
                } else {
                    drawCircle(
                        color = Color(0x33263852),
                        radius = baseRadius,
                        center = center,
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            }

            // Core Interactive Mic Circle
            Box(
                modifier = Modifier
                    .size(114.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = if (isActive) {
                                listOf(Color(0xFF0F2636), Color(0xFF050B12), CyberBlack)
                            } else {
                                listOf(Color(0xFF141A26), Color(0xFF090D14), CyberBlack)
                            }
                        )
                    )
                    .border(
                        width = if (isActive) 3.dp else 2.dp,
                        brush = if (isActive) {
                            Brush.linearGradient(listOf(NeonCyan, NeonElectricBlue))
                        } else {
                            Brush.linearGradient(listOf(SurfaceBorder, Color(0xFF152238)))
                        },
                        shape = CircleShape
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onToggleEngine
                    )
                    .testTag("microphone_hero_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isActive) Icons.Filled.Mic else Icons.Filled.MicOff,
                    contentDescription = "Microphone Toggle",
                    modifier = Modifier.size(54.dp),
                    tint = if (isActive) NeonCyan else TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Large Red / Cyan Power ON-OFF Button
        Button(
            onClick = onToggleEngine,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .shadow(
                    elevation = if (isActive) 12.dp else 4.dp,
                    shape = RoundedCornerShape(12.dp),
                    ambientColor = if (isActive) NeonPowerRed else NeonCyan,
                    spotColor = if (isActive) NeonPowerRed else NeonCyan
                )
                .testTag("power_toggle_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isActive) NeonPowerRed else NeonCyan,
                contentColor = if (isActive) Color.White else CyberBlack
            )
        ) {
            Icon(
                imageVector = Icons.Filled.PowerSettingsNew,
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (isActive) "STOP VOICE ENGINE (OFF)" else "ACTIVATE VOICE ENGINE (ON)",
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
                color = if (isActive) Color.White else CyberBlack,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Audition / Test Recording Bar (Record 5s & Playback with effects applied)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AmoledDark, RoundedCornerShape(10.dp))
                .border(1.dp, SurfaceBorder, RoundedCornerShape(10.dp))
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "VOICE AUDITION",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp),
                        color = NeonCyan
                    )
                    Text(
                        text = when {
                            isRecordingSample -> "Recording your voice sample..."
                            isPlayingSample -> "Playing processed voice..."
                            hasRecordedSample -> "Voice sample ready to audition"
                            else -> "Record to hear effects preview"
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                        color = TextSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Record / Stop Record Button
                    if (isRecordingSample) {
                        Button(
                            onClick = onStopRecord,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPowerRed),
                            modifier = Modifier.height(36.dp).testTag("stop_recording_button")
                        ) {
                            Icon(Icons.Filled.Stop, contentDescription = "Stop", modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("DONE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        OutlinedButton(
                            onClick = onStartRecord,
                            enabled = isActive,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonPowerRed),
                            modifier = Modifier.height(36.dp).testTag("start_recording_button")
                        ) {
                            Icon(
                                Icons.Filled.FiberManualRecord,
                                contentDescription = "Record",
                                tint = NeonPowerRed,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("RECORD", fontSize = 11.sp, color = if (isActive) TextPrimary else TextMuted)
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    // Play Sample Button
                    Button(
                        onClick = onPlaySample,
                        enabled = hasRecordedSample && !isRecordingSample && !isPlayingSample,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (hasRecordedSample) NeonElectricBlue else Color(0xFF1E283A)
                        ),
                        modifier = Modifier.height(36.dp).testTag("play_sample_button")
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = "Play", modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (isPlayingSample) "PLAYING" else "LISTEN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
