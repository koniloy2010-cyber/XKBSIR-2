package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.audio.AudioDspEngine
import com.example.audio.AudioRouteManager

class XkbApplication : Application() {

    lateinit var dspEngine: AudioDspEngine
        private set

    lateinit var routeManager: AudioRouteManager
        private set

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "xkb_voice_engine_channel"
        lateinit var instance: XkbApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        dspEngine = AudioDspEngine(this)
        routeManager = AudioRouteManager(this)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notif_channel_name)
            val descriptionText = getString(R.string.notif_channel_desc)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
