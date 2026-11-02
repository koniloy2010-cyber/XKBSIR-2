package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.VoiceEffectParams
import com.example.ui.theme.AmoledDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonElectricBlue
import com.example.ui.theme.NeonPowerRed
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun DspSlidersSection(
    params: VoiceEffectParams,
    onUpdateParam: ((VoiceEffectParams) -> VoiceEffectParams) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceCard, RoundedCornerShape(16.dp))
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "12-BAND DSP STUDIO CONTROLS",
                style = MaterialTheme.typography.titleMedium,
                color = NeonCyan
            )
            Text(
                text = "REAL-TIME ENGINE",
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 1. Master Gain
        DspSliderItem(
            title = "1. Master Gain",
            valueText = String.format(Locale.US, "%.2fx", params.masterGain),
            description = "Overall output amplification and volume drive",
            value = params.masterGain,
            valueRange = 0.0f..2.5f,
            testTag = "slider_master_gain",
            onValueChange = { newVal -> onUpdateParam { it.copy(masterGain = newVal) } }
        )

        // 2. Bass Boost
        DspSliderItem(
            title = "2. Bass Boost",
            valueText = "${(params.bassBoost * 100).toInt()}%",
            description = "120Hz low-shelf biquad filter for deep chest resonance",
            value = params.bassBoost,
            valueRange = 0.0f..1.0f,
            testTag = "slider_bass_boost",
            onValueChange = { newVal -> onUpdateParam { it.copy(bassBoost = newVal) } }
        )

        // 3. Treble Control
        DspSliderItem(
            title = "3. Treble Control",
            valueText = "${(params.trebleControl * 100).toInt()}%",
            description = "4500Hz high-shelf filter for cyber clarity and sizzle",
            value = params.trebleControl,
            valueRange = 0.0f..1.0f,
            testTag = "slider_treble_control",
            onValueChange = { newVal -> onUpdateParam { it.copy(trebleControl = newVal) } }
        )

        // 4. Voice Compressor
        DspSliderItem(
            title = "4. Voice Compressor",
            valueText = "${(params.voiceCompressor * 100).toInt()}%",
            description = "Dynamic range leveling for broadcast punch",
            value = params.voiceCompressor,
            valueRange = 0.0f..1.0f,
            testTag = "slider_voice_compressor",
            onValueChange = { newVal -> onUpdateParam { it.copy(voiceCompressor = newVal) } }
        )

        // 5. Distortion
        DspSliderItem(
            title = "5. Distortion",
            valueText = "${(params.distortion * 100).toInt()}%",
            description = "Asymmetric diode waveshaping for aggressive cyber crunch",
            value = params.distortion,
            valueRange = 0.0f..1.0f,
            testTag = "slider_distortion",
            onValueChange = { newVal -> onUpdateParam { it.copy(distortion = newVal) } }
        )

        // 6. Growling Intensity
        DspSliderItem(
            title = "6. Growling Intensity",
            valueText = "${(params.growlingIntensity * 100).toInt()}%",
            description = "Guttural throat rasp and sub-harmonic monster octaves",
            value = params.growlingIntensity,
            valueRange = 0.0f..1.0f,
            testTag = "slider_growling_intensity",
            accentColor = NeonPowerRed,
            onValueChange = { newVal -> onUpdateParam { it.copy(growlingIntensity = newVal) } }
        )

        // 7. Robot Voice
        DspSliderItem(
            title = "7. Robot Voice",
            valueText = "${(params.robotVoice * 100).toInt()}%",
            description = "110Hz carrier ring modulator & phase staircasing",
            value = params.robotVoice,
            valueRange = 0.0f..1.0f,
            testTag = "slider_robot_voice",
            onValueChange = { newVal -> onUpdateParam { it.copy(robotVoice = newVal) } }
        )

        // 8. Voice Vibration
        DspSliderItem(
            title = "8. Voice Vibration",
            valueText = "${(params.voiceVibration * 100).toInt()}%",
            description = "7.2Hz sinusoidal tremolo & harmonic vibrato modulation",
            value = params.voiceVibration,
            valueRange = 0.0f..1.0f,
            testTag = "slider_voice_vibration",
            onValueChange = { newVal -> onUpdateParam { it.copy(voiceVibration = newVal) } }
        )

        // 9. Noise Reduction
        DspSliderItem(
            title = "9. Noise Reduction",
            valueText = "${(params.noiseReduction * 100).toInt()}%",
            description = "Downward expander gate cutting room background hiss",
            value = params.noiseReduction,
            valueRange = 0.0f..1.0f,
            testTag = "slider_noise_reduction",
            onValueChange = { newVal -> onUpdateParam { it.copy(noiseReduction = newVal) } }
        )

        // 10. Voice Mixing (Clean + Growl + Robot)
        DspSliderItem(
            title = "10. Voice Mixing (Clean / FX Blend)",
            valueText = "${(params.voiceMixing * 100).toInt()}% Wet",
            description = "Simultaneous blend: Human Voice + Growling + Robotic Vibration",
            value = params.voiceMixing,
            valueRange = 0.0f..1.0f,
            testTag = "slider_voice_mixing",
            accentColor = NeonCyan,
            onValueChange = { newVal -> onUpdateParam { it.copy(voiceMixing = newVal) } }
        )

        // 11. Saturation
        DspSliderItem(
            title = "11. Saturation",
            valueText = "${(params.saturation * 100).toInt()}%",
            description = "Warm analog tape saturation and harmonic enricher",
            value = params.saturation,
            valueRange = 0.0f..1.0f,
            testTag = "slider_saturation",
            onValueChange = { newVal -> onUpdateParam { it.copy(saturation = newVal) } }
        )

        // 12. Audio Limiter
        DspSliderItem(
            title = "12. Audio Limiter",
            valueText = "${(params.audioLimiter * 100).toInt()}%",
            description = "Lookahead peak ceiling to prevent clipping and ear fatigue",
            value = params.audioLimiter,
            valueRange = 0.0f..1.0f,
            testTag = "slider_audio_limiter",
            onValueChange = { newVal -> onUpdateParam { it.copy(audioLimiter = newVal) } }
        )
    }
}

@Composable
private fun DspSliderItem(
    title: String,
    valueText: String,
    description: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    testTag: String,
    accentColor: androidx.compose.ui.graphics.Color = NeonCyan,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(AmoledDark, RoundedCornerShape(10.dp))
            .border(1.dp, SurfaceBorder.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 13.sp),
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = valueText,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                color = accentColor,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
            color = TextSecondary,
            modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
        )

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = accentColor,
                inactiveTrackColor = SurfaceBorder
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag)
        )
    }
}
