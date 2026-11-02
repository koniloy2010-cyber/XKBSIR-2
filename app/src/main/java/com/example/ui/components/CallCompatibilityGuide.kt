package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioRouteInfo
import com.example.ui.theme.AmoledDark
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
fun CallCompatibilityGuide(
    currentRoute: AudioRouteInfo?,
    isFeedbackProtectionActive: Boolean,
    onToggleFeedbackProtection: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceCard, RoundedCornerShape(16.dp))
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CALL & ROUTING COMPATIBILITY",
                style = MaterialTheme.typography.titleMedium,
                color = NeonCyan
            )
            Icon(
                imageVector = Icons.Filled.Security,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Current Hardware Audio Route Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AmoledDark, RoundedCornerShape(12.dp))
                .border(1.dp, if (currentRoute?.isSafeFromAcousticFeedback == true) NeonActiveGreen.copy(alpha = 0.5f) else Color(0xFF8A5A1A), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (currentRoute?.isSafeFromAcousticFeedback == true) Icons.Filled.Headphones else Icons.Filled.VolumeUp,
                        contentDescription = null,
                        tint = if (currentRoute?.isSafeFromAcousticFeedback == true) NeonActiveGreen else Color(0xFFFFB020),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DETECTED OUTPUT: ${currentRoute?.title ?: "Speaker"}",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 12.sp),
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = currentRoute?.recommendation ?: "Keep volume moderate to prevent feedback screeching.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Acoustic Feedback Protection Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Acoustic Feedback Protection",
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 12.sp),
                            color = TextPrimary
                        )
                        Text(
                            text = "Auto-limits feedback screeching when using speaker",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 10.sp),
                            color = TextMuted
                        )
                    }
                    Switch(
                        checked = isFeedbackProtectionActive,
                        onCheckedChange = { onToggleFeedbackProtection() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonCyan,
                            checkedTrackColor = Color(0xFF0F3242),
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = AmoledDark
                        ),
                        modifier = Modifier.testTag("feedback_protection_switch")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Android Audio Sandboxing Transparency
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0B1220), RoundedCornerShape(10.dp))
                .border(1.dp, Color(0xFF1B2E4F), RoundedCornerShape(10.dp))
                .padding(12.dp)
        ) {
            Row {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = NeonElectricBlue,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Android Audio Isolation Notice",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 12.sp),
                        color = NeonElectricBlue,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Android's operating system strictly sandboxes microphone input. The floating overlay gives you instant on-screen control over your voice engine while using any app, but cannot secretly hijack hardware mic drivers for external phone calls without audio routing accessories.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "SUPPORTED WORKFLOWS",
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp),
            color = NeonActiveGreen
        )
        Spacer(modifier = Modifier.height(6.dp))

        CompatibilityItem(
            isSupported = true,
            title = "Floating Control Over Games & Chats",
            description = "Keep XKB-SIR De floating logo over Discord, Free Fire, PUBG, or WhatsApp while adjusting effects on the fly.",
            icon = Icons.Filled.Shield
        )
        CompatibilityItem(
            isSupported = true,
            title = "Live Streaming & Screen Recording",
            description = "Twitch, YouTube Gaming, Omlet Arcade, and Screen Recorders capture all processed monster/robot audio cleanly.",
            icon = Icons.Filled.Videocam
        )
        CompatibilityItem(
            isSupported = true,
            title = "Hardware Loopback (TRRS / AUX / USB-C)",
            description = "Connect an AUX loopback cable or USB-C audio splitter to feed processed voice into a PC, gaming console, or call device.",
            icon = Icons.Filled.Headphones
        )
        CompatibilityItem(
            isSupported = true,
            title = "Voice Notes & Audio Audition",
            description = "Record real-time processed audio clips and listen or share them anywhere.",
            icon = Icons.Filled.VolumeUp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "RESTRICTED WITHOUT HARDWARE ROUTE",
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp),
            color = NeonPowerRed
        )
        Spacer(modifier = Modifier.height(6.dp))

        CompatibilityItem(
            isSupported = false,
            title = "Standard Cellular & WhatsApp In-Call Mic Injection",
            description = "Native Android security restricts background apps from injecting audio into active telephony calls without an external splitter or speaker-to-mic route.",
            icon = Icons.Filled.PhoneInTalk
        )
    }
}

@Composable
private fun CompatibilityItem(
    isSupported: Boolean,
    title: String,
    description: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(AmoledDark, RoundedCornerShape(8.dp))
            .border(1.dp, SurfaceBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    if (isSupported) Color(0x2600FF88) else Color(0x26FF2A55),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSupported) Icons.Filled.Check else Icons.Filled.Close,
                contentDescription = null,
                tint = if (isSupported) NeonActiveGreen else NeonPowerRed,
                modifier = Modifier.size(14.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 12.sp),
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 10.sp),
                color = TextSecondary,
                lineHeight = 14.sp
            )
        }
    }
}
