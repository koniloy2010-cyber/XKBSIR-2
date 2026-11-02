package com.example.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Process
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * High-performance real-time Audio DSP Engine for XKB-SIR.
 * Processes live microphone input and renders low-latency audio effects:
 * - Original human voice preservation
 * - Guttural monster growl (sub-harmonic octave + throat rasp)
 * - Cybernetic robot voice (ring modulator & phase quantizer)
 * - Voice vibration (tremolo/vibrato LFO)
 * - 12-band adjustable DSP controls
 * - Real-time visualizer waveform emission
 */
class AudioDspEngine(private val context: Context) {

    companion object {
        private const val TAG = "AudioDspEngine"
        const val SAMPLE_RATE = 44100
        private const val CHANNEL_IN = AudioFormat.CHANNEL_IN_MONO
        private const val CHANNEL_OUT = AudioFormat.CHANNEL_OUT_MONO
        private const val ENCODING = AudioFormat.ENCODING_PCM_16BIT
        private const val BUFFER_FRAMES = 512
        private const val VISUALIZER_POINTS = 64
        private const val MAX_RECORD_SECONDS = 10
    }

    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var processingThread: Thread? = null

    @Volatile
    private var isRunning = false

    @Volatile
    private var currentParams: VoiceEffectParams = VoiceEffectParams.PRESET_3X_BEAST

    // Live audio state
    private val _isEngineActive = MutableStateFlow(false)
    val isEngineActive: StateFlow<Boolean> = _isEngineActive.asStateFlow()

    private val _liveWaveform = MutableStateFlow(FloatArray(VISUALIZER_POINTS))
    val liveWaveform: StateFlow<FloatArray> = _liveWaveform.asStateFlow()

    private val _micRmsLevel = MutableStateFlow(0f)
    val micRmsLevel: StateFlow<Float> = _micRmsLevel.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isPlayingSample = MutableStateFlow(false)
    val isPlayingSample: StateFlow<Boolean> = _isPlayingSample.asStateFlow()

    private val _hasRecordedSample = MutableStateFlow(false)
    val hasRecordedSample: StateFlow<Boolean> = _hasRecordedSample.asStateFlow()

    // Test recording buffer
    private val recordedPcmBuffer = ArrayList<Short>(SAMPLE_RATE * MAX_RECORD_SECONDS)
    private var playSampleJob: Job? = null
    private val engineScope = CoroutineScope(Dispatchers.Default)

    fun updateParams(params: VoiceEffectParams) {
        currentParams = params
    }

    fun getParams(): VoiceEffectParams = currentParams

    @Synchronized
    fun startEngine(): Boolean {
        if (isRunning) return true

        val minRecBuf = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_IN, ENCODING)
        val recBufSize = (minRecBuf * 2).coerceAtLeast(BUFFER_FRAMES * 4)

        val minTrackBuf = AudioTrack.getMinBufferSize(SAMPLE_RATE, CHANNEL_OUT, ENCODING)
        val trackBufSize = (minTrackBuf * 2).coerceAtLeast(BUFFER_FRAMES * 4)

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                SAMPLE_RATE,
                CHANNEL_IN,
                ENCODING,
                recBufSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                // Fallback to MIC if VOICE_COMMUNICATION is restricted
                audioRecord?.release()
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    CHANNEL_IN,
                    ENCODING,
                    recBufSize
                )
            }

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord initialization failed")
                return false
            }

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()

            val audioFormat = AudioFormat.Builder()
                .setSampleRate(SAMPLE_RATE)
                .setChannelMask(CHANNEL_OUT)
                .setEncoding(ENCODING)
                .build()

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(trackBufSize)
                .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioRecord?.startRecording()
            audioTrack?.play()

            isRunning = true
            _isEngineActive.value = true

            startProcessingLoop()
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start audio engine", e)
            stopEngine()
            return false
        }
    }

    @Synchronized
    fun stopEngine() {
        isRunning = false
        _isEngineActive.value = false
        _micRmsLevel.value = 0f

        try {
            processingThread?.interrupt()
            processingThread = null

            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null

            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping audio engine", e)
        }
    }

    fun startTestRecording() {
        synchronized(recordedPcmBuffer) {
            recordedPcmBuffer.clear()
        }
        _isRecording.value = true
        _hasRecordedSample.value = false
    }

    fun stopTestRecording() {
        _isRecording.value = false
        synchronized(recordedPcmBuffer) {
            _hasRecordedSample.value = recordedPcmBuffer.isNotEmpty()
        }
    }

    fun playRecordedSample() {
        if (_isPlayingSample.value) return
        val samplesToPlay: ShortArray
        synchronized(recordedPcmBuffer) {
            if (recordedPcmBuffer.isEmpty()) return
            samplesToPlay = recordedPcmBuffer.toShortArray()
        }

        playSampleJob = engineScope.launch {
            _isPlayingSample.value = true
            try {
                val tempTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(CHANNEL_OUT)
                            .setEncoding(ENCODING)
                            .build()
                    )
                    .setBufferSizeInBytes(samplesToPlay.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                tempTrack.write(samplesToPlay, 0, samplesToPlay.size)
                tempTrack.play()

                val durationMs = (samplesToPlay.size.toLong() * 1000L) / SAMPLE_RATE
                kotlinx.coroutines.delay(durationMs + 200L)

                tempTrack.stop()
                tempTrack.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error playing recorded sample", e)
            } finally {
                _isPlayingSample.value = false
            }
        }
    }

    private fun startProcessingLoop() {
        processingThread = Thread {
            Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO)

            val inShorts = ShortArray(BUFFER_FRAMES)
            val outShorts = ShortArray(BUFFER_FRAMES)
            val floatIn = FloatArray(BUFFER_FRAMES)
            val floatOut = FloatArray(BUFFER_FRAMES)
            val cleanFloat = FloatArray(BUFFER_FRAMES)
            val waveSnapshot = FloatArray(VISUALIZER_POINTS)

            // DSP Persistent state
            var noiseGateEnv = 0.0f
            var compressorEnv = 0.0f
            var robotPhase = 0.0
            var vibrationPhase = 0.0
            var growlLfoPhase = 0.0
            var subOctaveFlip = 1.0f
            var lastSign = false

            // Biquad Bass state (Direct Form 1)
            var bX1 = 0f; var bX2 = 0f; var bY1 = 0f; var bY2 = 0f
            // Biquad Treble state
            var tX1 = 0f; var tX2 = 0f; var tY1 = 0f; var tY2 = 0f

            val stepVis = (BUFFER_FRAMES / VISUALIZER_POINTS).coerceAtLeast(1)

            while (isRunning && !Thread.currentThread().isInterrupted) {
                val readCount = audioRecord?.read(inShorts, 0, BUFFER_FRAMES) ?: -1
                if (readCount <= 0) continue

                val p = currentParams

                // Convert to normalized floats [-1.0 .. 1.0]
                var sumSquares = 0.0
                for (i in 0 until readCount) {
                    val s = inShorts[i] / 32768.0f
                    floatIn[i] = s
                    cleanFloat[i] = s
                    sumSquares += (s * s)
                }

                val rms = sqrt(sumSquares / readCount).toFloat()

                // Calculate Biquad Coefficients for Bass and Treble based on currentParams
                // Bass Shelf ~ 120Hz
                val bassGainDb = (p.bassBoost * 24.0f) - 6.0f // -6dB to +18dB
                val bassA = java.lang.Math.pow(10.0, (bassGainDb / 40.0).toDouble()).toFloat()
                val w0Bass = (2.0 * PI * 120.0 / SAMPLE_RATE).toFloat()
                val cosW0B = cos(w0Bass)
                val sinW0B = sin(w0Bass)
                val alphaB = sinW0B / 2.0f * sqrt((bassA + 1f / bassA) * (1f / 0.707f - 1f) + 2f)

                val bB0 = bassA * ((bassA + 1f) - (bassA - 1f) * cosW0B + 2f * sqrt(bassA) * alphaB)
                val bB1 = 2f * bassA * ((bassA - 1f) - (bassA + 1f) * cosW0B)
                val bB2 = bassA * ((bassA + 1f) - (bassA - 1f) * cosW0B - 2f * sqrt(bassA) * alphaB)
                val bA0 = (bassA + 1f) + (bassA - 1f) * cosW0B + 2f * sqrt(bassA) * alphaB
                val bA1 = -2f * ((bassA - 1f) + (bassA + 1f) * cosW0B)
                val bA2 = (bassA + 1f) + (bassA - 1f) * cosW0B - 2f * sqrt(bassA) * alphaB

                // Treble Shelf ~ 4500Hz
                val trebleGainDb = (p.trebleControl * 20.0f) - 10.0f
                val trebleA = java.lang.Math.pow(10.0, (trebleGainDb / 40.0).toDouble()).toFloat()
                val w0Treb = (2.0 * PI * 4500.0 / SAMPLE_RATE).toFloat()
                val cosW0T = cos(w0Treb)
                val sinW0T = sin(w0Treb)
                val alphaT = sinW0T / 2.0f * sqrt((trebleA + 1f / trebleA) * (1f / 0.707f - 1f) + 2f)

                val tB0 = trebleA * ((trebleA + 1f) + (trebleA - 1f) * cosW0T + 2f * sqrt(trebleA) * alphaT)
                val tB1 = -2f * trebleA * ((trebleA - 1f) + (trebleA + 1f) * cosW0T)
                val tB2 = trebleA * ((trebleA + 1f) + (trebleA - 1f) * cosW0T - 2f * sqrt(trebleA) * alphaT)
                val tA0 = (trebleA + 1f) - (trebleA - 1f) * cosW0T + 2f * sqrt(trebleA) * alphaT
                val tA1 = 2f * ((trebleA - 1f) - (trebleA + 1f) * cosW0T)
                val tA2 = (trebleA + 1f) - (trebleA - 1f) * cosW0T - 2f * sqrt(trebleA) * alphaT

                // Noise Gate Threshold
                val gateThreshold = (p.noiseReduction * 0.08f) + 0.002f
                val compThreshold = 0.25f / (1.0f + p.voiceCompressor * 2.0f)

                for (i in 0 until readCount) {
                    var x = floatIn[i]
                    val clean = cleanFloat[i]

                    // 1. Noise Reduction / Downward Expander
                    val absX = abs(x)
                    val targetGate = if (absX > gateThreshold) 1.0f else (absX / gateThreshold) * 0.2f
                    noiseGateEnv += 0.01f * (targetGate - noiseGateEnv)
                    x *= noiseGateEnv

                    // 2. Voice Compressor
                    val envTarget = abs(x)
                    compressorEnv += if (envTarget > compressorEnv) 0.05f * (envTarget - compressorEnv) else 0.005f * (envTarget - compressorEnv)
                    if (compressorEnv > compThreshold && p.voiceCompressor > 0.05f) {
                        val over = compressorEnv - compThreshold
                        val compRatio = 1.0f + (p.voiceCompressor * 5.0f)
                        val compFactor = 1.0f / (1.0f + over * compRatio)
                        x *= compFactor
                    }

                    // 3. Bass Boost Filter (Biquad DF1)
                    val bassFiltered = (bB0 / bA0) * x + (bB1 / bA0) * bX1 + (bB2 / bA0) * bX2 - (bA1 / bA0) * bY1 - (bA2 / bA0) * bY2
                    bX2 = bX1; bX1 = x; bY2 = bY1; bY1 = bassFiltered

                    // 4. Treble Control Filter (Biquad DF1)
                    val trebFiltered = (tB0 / tA0) * bassFiltered + (tB1 / tA0) * tX1 + (tB2 / tA0) * tX2 - (tA1 / tA0) * tY1 - (tA2 / tA0) * tY2
                    tX2 = tX1; tX1 = bassFiltered; tY2 = tY1; tY1 = trebFiltered

                    var toneSample = trebFiltered

                    // 5. Growling Effect Engine (Monstrous guttural rasp + sub-octave generation)
                    val currentSign = toneSample >= 0f
                    if (currentSign != lastSign && currentSign) {
                        subOctaveFlip = -subOctaveFlip
                    }
                    lastSign = currentSign

                    // Sub-octave undertone
                    val subOctave = toneSample * subOctaveFlip

                    // Guttural throat rasp: LFO modulated non-linear waveshaping
                    growlLfoPhase += (2.0 * PI * 34.0) / SAMPLE_RATE
                    if (growlLfoPhase > 2.0 * PI) growlLfoPhase -= 2.0 * PI
                    val growlLfo = sin(growlLfoPhase).toFloat()
                    val throatDrive = (1.0f + p.growlingIntensity * 2.5f)
                    var growlSample = (toneSample * 0.5f + subOctave * 0.7f) * throatDrive * (1.0f + 0.35f * growlLfo)
                    // Cubic soft laryngeal saturation
                    growlSample = (growlSample - 0.25f * growlSample * growlSample * growlSample).coerceIn(-1.5f, 1.5f)

                    // 6. Robot Voice Engine (Ring Modulator & Phase Quantization)
                    val robotCarrierFreq = 105.0 + (p.robotVoice * 35.0) // 105 Hz - 140 Hz carrier
                    robotPhase += (2.0 * PI * robotCarrierFreq) / SAMPLE_RATE
                    if (robotPhase > 2.0 * PI) robotPhase -= 2.0 * PI
                    val carrierSine = sin(robotPhase).toFloat()
                    // Phase quantization for mechanical transformer buzz
                    val quantSteps = 16.0f
                    val quantizedTone = kotlin.math.floor(toneSample * quantSteps) / quantSteps
                    val robotSample = (0.7f * (toneSample * carrierSine) + 0.3f * quantizedTone) * (1.0f + p.robotVoice)

                    // 7. Voice Vibration (Tremolo & Vibrato LFO)
                    vibrationPhase += (2.0 * PI * 7.2) / SAMPLE_RATE
                    if (vibrationPhase > 2.0 * PI) vibrationPhase -= 2.0 * PI
                    val vibLfo = sin(vibrationPhase).toFloat()
                    val tremoloMod = 1.0f - (p.voiceVibration * 0.45f * (1.0f + vibLfo))
                    val vibratedSample = toneSample * tremoloMod

                    // 8. Electronic Distortion & Saturation
                    var fxSample = 0.45f * growlSample * p.growlingIntensity +
                            0.35f * robotSample * p.robotVoice +
                            0.20f * vibratedSample * (1.0f + p.voiceVibration)

                    if (p.distortion > 0.05f) {
                        val distDrive = 1.0f + p.distortion * 4.0f
                        val driven = fxSample * distDrive
                        // Asymmetrical diode clipper
                        fxSample = if (driven > 0f) {
                            driven / (1.0f + driven)
                        } else {
                            -abs(driven) / (1.0f + 0.7f * abs(driven))
                        }
                    }

                    if (p.saturation > 0.05f) {
                        val satDrive = 1.0f + p.saturation * 2.0f
                        val satInput = fxSample * satDrive
                        // Fast tanh approximation
                        fxSample = (satInput / (1.0f + abs(satInput))) * 1.1f
                    }

                    // 9. Voice Mixing:
                    // MUST contain: Original Human Voice + Growling Voice + Robotic Vibration
                    // The original voice must remain articulate and understandable!
                    val wetMix = p.voiceMixing
                    val dryMix = 1.0f - (wetMix * 0.65f) // Keeps at least 35% clean articulation even at max wet!
                    var combined = (clean * dryMix) + (fxSample * wetMix * 1.3f)

                    // 10. Master Gain
                    combined *= p.masterGain

                    // 11. Audio Limiter (Lookahead / soft peak limiter)
                    val limitCeiling = (0.70f + p.audioLimiter * 0.28f).coerceIn(0.6f, 0.98f)
                    if (abs(combined) > limitCeiling) {
                        val sign = if (combined > 0f) 1.0f else -1.0f
                        val excess = abs(combined) - limitCeiling
                        combined = sign * (limitCeiling + (excess / (1.0f + excess * 2.0f)))
                    }

                    floatOut[i] = combined.coerceIn(-1.0f, 1.0f)
                }

                // Convert back to PCM 16-bit
                for (i in 0 until readCount) {
                    val s = (floatOut[i] * 32767.0f).toInt().coerceIn(-32768, 32767).toShort()
                    outShorts[i] = s
                }

                // Write to live audio output (speaker / headphone)
                audioTrack?.write(outShorts, 0, readCount)

                // If user is recording a test sample, save to memory
                if (_isRecording.value) {
                    synchronized(recordedPcmBuffer) {
                        if (recordedPcmBuffer.size < SAMPLE_RATE * MAX_RECORD_SECONDS) {
                            for (i in 0 until readCount) {
                                recordedPcmBuffer.add(outShorts[i])
                            }
                        }
                    }
                }

                // Populate visualizer waveform snapshot
                for (k in 0 until VISUALIZER_POINTS) {
                    val idx = (k * stepVis).coerceIn(0, readCount - 1)
                    waveSnapshot[k] = floatOut[idx]
                }
                _liveWaveform.value = waveSnapshot.copyOf()
                _micRmsLevel.value = rms
            }
        }.apply {
            name = "XkbDspAudioThread"
            start()
        }
    }
}
