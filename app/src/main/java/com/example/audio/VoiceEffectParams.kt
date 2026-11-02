package com.example.audio

/**
 * Data class representing the 12 working real-time DSP parameters.
 * All normalized between 0.0f and 1.0f (or custom multiplier).
 */
data class VoiceEffectParams(
    val masterGain: Float = 1.0f,         // 0.0f .. 2.0f (1.0 = normal)
    val bassBoost: Float = 0.5f,          // 0.0f .. 1.0f (low shelf boost)
    val trebleControl: Float = 0.5f,      // 0.0f .. 1.0f (high shelf filter)
    val voiceCompressor: Float = 0.6f,    // 0.0f .. 1.0f (compression ratio/threshold)
    val distortion: Float = 0.35f,        // 0.0f .. 1.0f (drive/clip amount)
    val growlingIntensity: Float = 0.7f,  // 0.0f .. 1.0f (sub-octave gargle & throat rasp)
    val robotVoice: Float = 0.5f,         // 0.0f .. 1.0f (ring-mod & phase quantization)
    val voiceVibration: Float = 0.4f,     // 0.0f .. 1.0f (tremolo / vibrato LFO depth)
    val noiseReduction: Float = 0.45f,    // 0.0f .. 1.0f (downward expander gate threshold)
    val voiceMixing: Float = 0.5f,        // 0.0f (100% clean) .. 1.0f (100% processed)
    val saturation: Float = 0.4f,         // 0.0f .. 1.0f (analog warm tape saturation)
    val audioLimiter: Float = 0.85f,      // 0.0f .. 1.0f (ceiling & fast-lookahead limiter)
    val isFeedbackProtectionActive: Boolean = true
) {
    companion object {
        /**
         * 3X EFFECT INTENSITY - Beast Monster Preset:
         * Specifically engineered to deliver ~3x perceived intensity:
         * Heavy guttural growl, deep sub-bass, cyber vibration, saturated punch,
         * with robust audio limiting to prevent digital clipping while keeping the
         * original human voice understandable.
         */
        val PRESET_3X_BEAST = VoiceEffectParams(
            masterGain = 1.25f,
            bassBoost = 0.95f,
            trebleControl = 0.65f,
            voiceCompressor = 0.85f,
            distortion = 0.75f,
            growlingIntensity = 0.95f,
            robotVoice = 0.70f,
            voiceVibration = 0.65f,
            noiseReduction = 0.55f,
            voiceMixing = 0.65f, // blends strong articulate core with immense beast layer
            saturation = 0.80f,
            audioLimiter = 0.95f
        )

        val PRESET_CYBER_ROBOT = VoiceEffectParams(
            masterGain = 1.05f,
            bassBoost = 0.40f,
            trebleControl = 0.80f,
            voiceCompressor = 0.70f,
            distortion = 0.50f,
            growlingIntensity = 0.20f,
            robotVoice = 0.95f,
            voiceVibration = 0.75f,
            noiseReduction = 0.50f,
            voiceMixing = 0.75f,
            saturation = 0.60f,
            audioLimiter = 0.90f
        )

        val PRESET_GROWL_SUBBASS = VoiceEffectParams(
            masterGain = 1.10f,
            bassBoost = 1.0f,
            trebleControl = 0.35f,
            voiceCompressor = 0.80f,
            distortion = 0.60f,
            growlingIntensity = 0.90f,
            robotVoice = 0.25f,
            voiceVibration = 0.40f,
            noiseReduction = 0.40f,
            voiceMixing = 0.60f,
            saturation = 0.70f,
            audioLimiter = 0.95f
        )

        val PRESET_DEMON_OVERLORD = VoiceEffectParams(
            masterGain = 1.15f,
            bassBoost = 0.90f,
            trebleControl = 0.45f,
            voiceCompressor = 0.85f,
            distortion = 0.70f,
            growlingIntensity = 0.88f,
            robotVoice = 0.55f,
            voiceVibration = 0.50f,
            noiseReduction = 0.50f,
            voiceMixing = 0.60f,
            saturation = 0.75f,
            audioLimiter = 0.95f
        )

        val PRESET_MECHANICAL_BUZZ = VoiceEffectParams(
            masterGain = 1.0f,
            bassBoost = 0.30f,
            trebleControl = 0.85f,
            voiceCompressor = 0.75f,
            distortion = 0.80f,
            growlingIntensity = 0.30f,
            robotVoice = 0.80f,
            voiceVibration = 0.90f,
            noiseReduction = 0.60f,
            voiceMixing = 0.70f,
            saturation = 0.65f,
            audioLimiter = 0.90f
        )

        val PRESET_CLEAN = VoiceEffectParams(
            masterGain = 1.0f,
            bassBoost = 0.0f,
            trebleControl = 0.5f,
            voiceCompressor = 0.3f,
            distortion = 0.0f,
            growlingIntensity = 0.0f,
            robotVoice = 0.0f,
            voiceVibration = 0.0f,
            noiseReduction = 0.3f,
            voiceMixing = 0.0f,
            saturation = 0.0f,
            audioLimiter = 0.8f
        )
    }
}

enum class SoundPreset(val title: String, val description: String, val params: VoiceEffectParams) {
    BEAST_3X(
        title = "3X Beast Monster",
        description = "3x perceived intensity: monstrous growl, cyber vibration, deep bass",
        params = VoiceEffectParams.PRESET_3X_BEAST
    ),
    CYBER_ROBOT(
        title = "Cybernetic Robot",
        description = "Metallic ring modulation, vocoder buzz and digital synthesis",
        params = VoiceEffectParams.PRESET_CYBER_ROBOT
    ),
    GROWL_SUBBASS(
        title = "Deep Growl Sub-Bass",
        description = "Sub-harmonic octaver, maximum low-end chest rumble",
        params = VoiceEffectParams.PRESET_GROWL_SUBBASS
    ),
    DEMON_OVERLORD(
        title = "Demon Overlord",
        description = "Guttural throat rasp with pitch downshift and analog heat",
        params = VoiceEffectParams.PRESET_DEMON_OVERLORD
    ),
    MECHANICAL_BUZZ(
        title = "Mechanical Buzz Comm",
        description = "Radio communications, transformer harmonic fuzz",
        params = VoiceEffectParams.PRESET_MECHANICAL_BUZZ
    ),
    CLEAN(
        title = "Clean Mic Pass",
        description = "Original human voice with subtle noise gate and studio leveler",
        params = VoiceEffectParams.PRESET_CLEAN
    )
}
