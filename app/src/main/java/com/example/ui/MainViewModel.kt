package com.example.ui

import android.app.Application
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.XkbApplication
import com.example.audio.AudioDspEngine
import com.example.audio.AudioRouteInfo
import com.example.audio.SoundPreset
import com.example.audio.VoiceEffectParams
import com.example.service.FloatingVoiceService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class MainUiState(
    val isEngineActive: Boolean = false,
    val micRmsLevel: Float = 0f,
    val liveWaveform: FloatArray = FloatArray(64),
    val params: VoiceEffectParams = VoiceEffectParams.PRESET_3X_BEAST,
    val activePreset: SoundPreset? = SoundPreset.BEAST_3X,
    val isRecordingSample: Boolean = false,
    val isPlayingSample: Boolean = false,
    val hasRecordedSample: Boolean = false,
    val isOverlayEnabled: Boolean = false,
    val hasOverlayPermission: Boolean = false,
    val audioRoute: AudioRouteInfo? = null,
    val selectedTab: Int = 0 // 0: Main & Waveform, 1: 12-Band DSP, 2: Presets, 3: Call Guide
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as MainUiState
        return isEngineActive == other.isEngineActive &&
                micRmsLevel == other.micRmsLevel &&
                liveWaveform.contentEquals(other.liveWaveform) &&
                params == other.params &&
                activePreset == other.activePreset &&
                isRecordingSample == other.isRecordingSample &&
                isPlayingSample == other.isPlayingSample &&
                hasRecordedSample == other.hasRecordedSample &&
                isOverlayEnabled == other.isOverlayEnabled &&
                hasOverlayPermission == other.hasOverlayPermission &&
                audioRoute == other.audioRoute &&
                selectedTab == other.selectedTab
    }

    override fun hashCode(): Int {
        var result = isEngineActive.hashCode()
        result = 31 * result + micRmsLevel.hashCode()
        result = 31 * result + liveWaveform.contentHashCode()
        result = 31 * result + params.hashCode()
        result = 31 * result + (activePreset?.hashCode() ?: 0)
        result = 31 * result + isRecordingSample.hashCode()
        result = 31 * result + isPlayingSample.hashCode()
        result = 31 * result + hasRecordedSample.hashCode()
        result = 31 * result + isOverlayEnabled.hashCode()
        result = 31 * result + hasOverlayPermission.hashCode()
        result = 31 * result + (audioRoute?.hashCode() ?: 0)
        result = 31 * result + selectedTab
        return result
    }
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val dspEngine: AudioDspEngine = XkbApplication.instance.dspEngine
    private val routeManager = XkbApplication.instance.routeManager

    private val _uiState = MutableStateFlow(
        MainUiState(
            params = dspEngine.getParams(),
            audioRoute = routeManager.getCurrentRoute()
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            dspEngine.isEngineActive.collectLatest { active ->
                _uiState.value = _uiState.value.copy(isEngineActive = active)
            }
        }

        viewModelScope.launch {
            dspEngine.micRmsLevel.collectLatest { level ->
                _uiState.value = _uiState.value.copy(micRmsLevel = level)
            }
        }

        viewModelScope.launch {
            dspEngine.liveWaveform.collectLatest { wave ->
                _uiState.value = _uiState.value.copy(liveWaveform = wave)
            }
        }

        viewModelScope.launch {
            dspEngine.isRecording.collectLatest { rec ->
                _uiState.value = _uiState.value.copy(isRecordingSample = rec)
            }
        }

        viewModelScope.launch {
            dspEngine.isPlayingSample.collectLatest { playing ->
                _uiState.value = _uiState.value.copy(isPlayingSample = playing)
            }
        }

        viewModelScope.launch {
            dspEngine.hasRecordedSample.collectLatest { hasSample ->
                _uiState.value = _uiState.value.copy(hasRecordedSample = hasSample)
            }
        }

        checkOverlayPermission()
    }

    fun checkOverlayPermission() {
        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(getApplication())
        } else {
            true
        }
        _uiState.value = _uiState.value.copy(
            hasOverlayPermission = hasPermission,
            isOverlayEnabled = FloatingVoiceService.isServiceRunning
        )
    }

    fun refreshAudioRoute() {
        _uiState.value = _uiState.value.copy(
            audioRoute = routeManager.getCurrentRoute()
        )
    }

    fun setSelectedTab(tab: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun toggleEngine(): Boolean {
        return if (_uiState.value.isEngineActive) {
            dspEngine.stopEngine()
            true
        } else {
            dspEngine.startEngine()
        }
    }

    fun applyPreset(preset: SoundPreset) {
        val newParams = preset.params
        dspEngine.updateParams(newParams)
        _uiState.value = _uiState.value.copy(
            params = newParams,
            activePreset = preset
        )
    }

    fun updateParam(transform: (VoiceEffectParams) -> VoiceEffectParams) {
        val updated = transform(_uiState.value.params)
        dspEngine.updateParams(updated)
        _uiState.value = _uiState.value.copy(
            params = updated,
            activePreset = null // Custom modification
        )
    }

    fun toggleFloatingService(): Boolean {
        val context = getApplication<Application>()
        if (FloatingVoiceService.isServiceRunning) {
            val stopIntent = Intent(context, FloatingVoiceService::class.java).apply {
                action = FloatingVoiceService.ACTION_STOP_SERVICE
            }
            context.startService(stopIntent)
            _uiState.value = _uiState.value.copy(isOverlayEnabled = false)
            return false
        } else {
            val startIntent = Intent(context, FloatingVoiceService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(startIntent)
            } else {
                context.startService(startIntent)
            }
            _uiState.value = _uiState.value.copy(isOverlayEnabled = true)
            return true
        }
    }

    fun startTestRecording() {
        dspEngine.startTestRecording()
    }

    fun stopTestRecording() {
        dspEngine.stopTestRecording()
    }

    fun playRecordedSample() {
        dspEngine.playRecordedSample()
    }

    fun toggleFeedbackProtection() {
        updateParam { it.copy(isFeedbackProtectionActive = !it.isFeedbackProtectionActive) }
    }
}
