package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.audio.SoundPreset
import com.example.audio.VoiceEffectParams
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun testAppName() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("XKB-SIR", appName)
    }

    @Test
    fun testBeast3xPresetParameters() {
        val beast = VoiceEffectParams.PRESET_3X_BEAST
        assertTrue("Beast growling must be high intensity", beast.growlingIntensity >= 0.9f)
        assertTrue("Beast bass boost must be powerful", beast.bassBoost >= 0.9f)
        assertTrue("Voice mixing must preserve human voice", beast.voiceMixing in 0.4f..0.8f)
        assertTrue("Limiter must be active to prevent clipping", beast.audioLimiter >= 0.9f)
    }

    @Test
    fun testAllPresetsDefined() {
        val presets = SoundPreset.values()
        assertTrue("At least 5 presets should be available", presets.size >= 5)
        assertNotNull(SoundPreset.valueOf("BEAST_3X"))
        assertNotNull(SoundPreset.valueOf("CYBER_ROBOT"))
    }
}
