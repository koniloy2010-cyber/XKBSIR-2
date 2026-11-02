package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundPreset
import com.example.ui.theme.AmoledDark
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonElectricBlue
import com.example.ui.theme.NeonPowerRed
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun PresetsSection(
    activePreset: SoundPreset?,
    onSelectPreset: (SoundPreset) -> Unit,
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
                text = "SIGNATURE SOUND PRESETS",
                style = MaterialTheme.typography.titleMedium,
                color = NeonCyan
            )
            Text(
                text = "DSP MODES",
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3X BEAST MONSTER - Featured Hero Preset Card
        val is3xActive = activePreset == SoundPreset.BEAST_3X
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            if (is3xActive) Color(0xFF2A0915) else Color(0xFF14070B),
                            AmoledDark
                        )
                    )
                )
                .border(
                    width = if (is3xActive) 2.dp else 1.dp,
                    color = if (is3xActive) NeonPowerRed else Color(0xFF6B1728),
                    shape = RoundedCornerShape(14.dp)
                )
                .clickable { onSelectPreset(SoundPreset.BEAST_3X) }
                .padding(16.dp)
                .testTag("preset_3x_beast_card")
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF470C1B), RoundedCornerShape(8.dp))
                                .border(1.dp, NeonPowerRed, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Bolt,
                                contentDescription = null,
                                tint = NeonPowerRed,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "3X BEAST MONSTER",
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                                color = Color(0xFFFF4D79),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "3X PERCEIVED INTENSITY ENGINE",
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                                color = NeonPowerRed
                            )
                        }
                    }

                    if (is3xActive) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = "Active",
                                tint = NeonPowerRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "ENGAGED",
                                style = MaterialTheme.typography.labelMedium,
                                color = NeonPowerRed,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Engineered with triple perceived impact: Maximum guttural growl, sub-harmonic bass thrust, cyber robotic vibration, and tape saturation with zero clipping. The original human voice stays crisp and articulate.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { onSelectPreset(SoundPreset.BEAST_3X) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("engage_3x_beast_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (is3xActive) NeonPowerRed else Color(0xFF6B1728),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (is3xActive) "3X BEAST ACTIVE" else "ENGAGE 3X INTENSITY PRESET",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Secondary Presets
        val presets = listOf(
            SoundPreset.CYBER_ROBOT to Icons.Filled.SmartToy,
            SoundPreset.GROWL_SUBBASS to Icons.Filled.VolumeUp,
            SoundPreset.DEMON_OVERLORD to Icons.Filled.RecordVoiceOver,
            SoundPreset.MECHANICAL_BUZZ to Icons.Filled.GraphicEq,
            SoundPreset.CLEAN to Icons.Filled.RecordVoiceOver
        )

        presets.forEach { (preset, icon) ->
            PresetRowItem(
                preset = preset,
                icon = icon,
                isSelected = activePreset == preset,
                onSelect = { onSelectPreset(preset) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PresetRowItem(
    preset: SoundPreset,
    icon: ImageVector,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) Color(0xFF0C2434) else AmoledDark)
            .border(
                width = 1.dp,
                color = if (isSelected) NeonCyan else SurfaceBorder,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onSelect)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag("preset_${preset.name.lowercase()}_item"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) NeonCyan else TextSecondary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = preset.title,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 13.sp),
                color = if (isSelected) NeonCyan else TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = preset.description,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                color = TextSecondary,
                maxLines = 2
            )
        }
        if (isSelected) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = "Active",
                tint = NeonCyan,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
