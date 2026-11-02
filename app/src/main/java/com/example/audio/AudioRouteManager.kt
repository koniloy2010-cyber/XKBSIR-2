package com.example.audio

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build

enum class AudioRouteType {
    HEADSET_WIRED,
    BLUETOOTH,
    USB_AUDIO,
    BUILT_IN_SPEAKER
}

data class AudioRouteInfo(
    val type: AudioRouteType,
    val title: String,
    val isSafeFromAcousticFeedback: Boolean,
    val recommendation: String
)

class AudioRouteManager(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun getCurrentRoute(): AudioRouteInfo {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            for (device in devices) {
                when (device.type) {
                    AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                    AudioDeviceInfo.TYPE_WIRED_HEADSET -> {
                        return AudioRouteInfo(
                            type = AudioRouteType.HEADSET_WIRED,
                            title = "Wired Headphones / 3.5mm AUX",
                            isSafeFromAcousticFeedback = true,
                            recommendation = "Optimal for zero acoustic feedback and pristine real-time voice monitoring."
                        )
                    }
                    AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                    AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
                    AudioDeviceInfo.TYPE_BLE_HEADSET -> {
                        return AudioRouteInfo(
                            type = AudioRouteType.BLUETOOTH,
                            title = "Bluetooth Headset / Earbuds",
                            isSafeFromAcousticFeedback = true,
                            recommendation = "Safe from acoustic feedback loop. Latency depends on Bluetooth codec."
                        )
                    }
                    AudioDeviceInfo.TYPE_USB_DEVICE,
                    AudioDeviceInfo.TYPE_USB_HEADSET -> {
                        return AudioRouteInfo(
                            type = AudioRouteType.USB_AUDIO,
                            title = "USB-C Audio / DAC Interface",
                            isSafeFromAcousticFeedback = true,
                            recommendation = "Pro-grade studio interface route with ultra-low latency."
                        )
                    }
                }
            }
        }

        return AudioRouteInfo(
            type = AudioRouteType.BUILT_IN_SPEAKER,
            title = "Device Speaker",
            isSafeFromAcousticFeedback = false,
            recommendation = "Use headphones or keep volume moderate to avoid speaker-to-microphone feedback screeching."
        )
    }
}
