package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.service.FloatingVoiceService
import com.example.ui.MainUiState
import com.example.ui.MainViewModel
import com.example.ui.components.CallCompatibilityGuide
import com.example.ui.components.CyberMicHero
import com.example.ui.components.DspSlidersSection
import com.example.ui.components.PresetsSection
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.AmoledDark
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonActiveGreen
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonElectricBlue
import com.example.ui.theme.NeonPowerRed
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val context = LocalContext.current

                // Permission Launchers
                val micPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        viewModel.toggleEngine()
                    } else {
                        Toast.makeText(
                            context,
                            "Microphone permission is required for real-time voice changer",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                val overlaySettingsLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) {
                    viewModel.checkOverlayPermission()
                }

                LaunchedEffect(Unit) {
                    viewModel.refreshAudioRoute()
                    viewModel.checkOverlayPermission()

                    // Request notification permission on Android 13+
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CyberBlack
                ) {
                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.safeDrawing),
                        containerColor = CyberBlack,
                        topBar = {
                            CyberTopBar(
                                isOverlayActive = uiState.isOverlayEnabled,
                                hasOverlayPermission = uiState.hasOverlayPermission,
                                onToggleOverlay = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                                        val intent = Intent(
                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:${context.packageName}")
                                        )
                                        overlaySettingsLauncher.launch(intent)
                                    } else {
                                        viewModel.toggleFloatingService()
                                    }
                                }
                            )
                        }
                    ) { innerPadding ->
                        XkbMainContent(
                            uiState = uiState,
                            paddingValues = innerPadding,
                            onToggleEngine = {
                                val hasMicPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED

                                if (!hasMicPermission) {
                                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                } else {
                                    viewModel.toggleEngine()
                                }
                            },
                            onTabSelected = { viewModel.setSelectedTab(it) },
                            onUpdateParam = { viewModel.updateParam(it) },
                            onSelectPreset = { viewModel.applyPreset(it) },
                            onStartRecord = { viewModel.startTestRecording() },
                            onStopRecord = { viewModel.stopTestRecording() },
                            onPlaySample = { viewModel.playRecordedSample() },
                            onToggleFeedback = { viewModel.toggleFeedbackProtection() },
                            onRequestOverlayPermission = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                    overlaySettingsLauncher.launch(intent)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkOverlayPermission()
        viewModel.refreshAudioRoute()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CyberTopBar(
    isOverlayActive: Boolean,
    hasOverlayPermission: Boolean,
    onToggleOverlay: () -> Unit
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = CyberBlack
        ),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Cyber Badge Icon
                Image(
                    painter = painterResource(id = R.drawable.xkb_logo_badge),
                    contentDescription = "XKB-SIR Logo",
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, NeonCyan, CircleShape)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "XKB-SIR De",
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF0F3244), RoundedCornerShape(4.dp))
                                .border(0.5.dp, NeonCyan, RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "PRO DSP",
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 9.sp),
                                color = NeonCyan
                            )
                        }
                    }
                    Text(
                        text = "DEVELOPER: -XKB-SIR",
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 9.sp),
                        color = TextMuted
                    )
                }
            }
        },
        actions = {
            // Floating Overlay Button
            Button(
                onClick = onToggleOverlay,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isOverlayActive) Color(0xFF0F384C) else AmoledDark
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .height(34.dp)
                    .border(
                        width = 1.dp,
                        color = if (isOverlayActive) NeonCyan else SurfaceBorder,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .testTag("floating_overlay_toggle_btn")
            ) {
                Icon(
                    imageVector = Icons.Filled.Layers,
                    contentDescription = null,
                    tint = if (isOverlayActive) NeonCyan else TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isOverlayActive) "OVERLAY ON" else "FLOAT LOGO",
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 10.sp),
                    color = if (isOverlayActive) NeonCyan else TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XkbMainContent(
    uiState: MainUiState,
    paddingValues: PaddingValues,
    onToggleEngine: () -> Unit,
    onTabSelected: (Int) -> Unit,
    onUpdateParam: ((com.example.audio.VoiceEffectParams) -> com.example.audio.VoiceEffectParams) -> Unit,
    onSelectPreset: (com.example.audio.SoundPreset) -> Unit,
    onStartRecord: () -> Unit,
    onStopRecord: () -> Unit,
    onPlaySample: () -> Unit,
    onToggleFeedback: () -> Unit,
    onRequestOverlayPermission: () -> Unit
) {
    val tabs = listOf(
        Triple("STUDIO", Icons.Filled.Mic, 0),
        Triple("12-DSP", Icons.Filled.Tune, 1),
        Triple("PRESETS", Icons.Filled.Bolt, 2),
        Triple("CALL ROUTE", Icons.Filled.PhoneInTalk, 3)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Futuristic Cyber Navigation Tabs
        PrimaryTabRow(
            selectedTabIndex = uiState.selectedTab,
            containerColor = CyberBlack,
            contentColor = NeonCyan,
            indicator = {
                TabRowDefaults.PrimaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(uiState.selectedTab),
                    color = NeonCyan,
                    height = 2.5.dp
                )
            },
            divider = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(SurfaceBorder)
                )
            }
        ) {
            tabs.forEach { (title, icon, index) ->
                val selected = uiState.selectedTab == index
                Tab(
                    selected = selected,
                    onClick = { onTabSelected(index) },
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("tab_$title"),
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (selected) NeonCyan else TextMuted
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp),
                                color = if (selected) NeonCyan else TextSecondary,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                )
            }
        }

        // Overlay Permission Banner (if permission not yet granted)
        if (!uiState.hasOverlayPermission) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E1408))
                    .border(1.dp, Color(0xFF664411))
                    .clickable(onClick = onRequestOverlayPermission)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Enable Display Over Other Apps for Floating Logo",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                        color = Color(0xFFFFB020)
                    )
                    Text(
                        text = "GRANT",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp),
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Main Tab Content (Responsive width clamped to 600.dp for tablets/foldables)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .widthIn(max = 600.dp)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (uiState.selectedTab) {
                0 -> {
                    // TAB 0: Main Studio & Live Visualizer
                    item {
                        CyberMicHero(
                            isActive = uiState.isEngineActive,
                            micLevel = uiState.micRmsLevel,
                            isRecordingSample = uiState.isRecordingSample,
                            isPlayingSample = uiState.isPlayingSample,
                            hasRecordedSample = uiState.hasRecordedSample,
                            onToggleEngine = onToggleEngine,
                            onStartRecord = onStartRecord,
                            onStopRecord = onStopRecord,
                            onPlaySample = onPlaySample
                        )
                    }

                    item {
                        Text(
                            text = "LIVE OSCILLOSCOPE WAVEFORM",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp),
                            color = NeonCyan
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        WaveformVisualizer(
                            waveform = uiState.liveWaveform,
                            isActive = uiState.isEngineActive
                        )
                    }

                    item {
                        // Quick Active Preset Summary Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AmoledDark, RoundedCornerShape(12.dp))
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "CURRENT DSP PRESET",
                                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                                        color = TextMuted
                                    )
                                    Text(
                                        text = uiState.activePreset?.title ?: "Custom 12-Band DSP",
                                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                                        color = if (uiState.activePreset == com.example.audio.SoundPreset.BEAST_3X) NeonPowerRed else NeonCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Button(
                                    onClick = { onTabSelected(2) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF142036)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("CHANGE", fontSize = 10.sp, color = NeonCyan)
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: 12-Band DSP Studio Controls
                    item {
                        DspSlidersSection(
                            params = uiState.params,
                            onUpdateParam = onUpdateParam
                        )
                    }
                }

                2 -> {
                    // TAB 2: Signature Presets & 3X Beast Mode
                    item {
                        PresetsSection(
                            activePreset = uiState.activePreset,
                            onSelectPreset = onSelectPreset
                        )
                    }
                }

                3 -> {
                    // TAB 3: Call & Audio Routing Compatibility Guide
                    item {
                        CallCompatibilityGuide(
                            currentRoute = uiState.audioRoute,
                            isFeedbackProtectionActive = uiState.params.isFeedbackProtectionActive,
                            onToggleFeedbackProtection = onToggleFeedback
                        )
                    }
                }
            }
        }
    }
}
