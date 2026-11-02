package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.XkbApplication
import com.example.audio.AudioDspEngine
import com.example.audio.SoundPreset
import com.example.audio.VoiceEffectParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.abs

class FloatingVoiceService : Service() {

    companion object {
        const val ACTION_START_ENGINE = "ACTION_START_ENGINE"
        const val ACTION_STOP_ENGINE = "ACTION_STOP_ENGINE"
        const val ACTION_TOGGLE_OVERLAY = "ACTION_TOGGLE_OVERLAY"
        const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
        private const val NOTIFICATION_ID = 1001

        @Volatile
        var isServiceRunning = false
            private set

        @Volatile
        var isOverlayShowing = false
            private set
    }

    private var windowManager: WindowManager? = null
    private var floatingContainer: View? = null
    private var windowParams: WindowManager.LayoutParams? = null

    private lateinit var dspEngine: AudioDspEngine
    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private var engineStateJob: Job? = null

    // Dragging physics state
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false
    private var isExpanded = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        isServiceRunning = true
        dspEngine = XkbApplication.instance.dspEngine
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val notification = createNotification("Engine Standby")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        observeEngine()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_ENGINE -> {
                dspEngine.startEngine()
                updateNotification("Live Voice Changer Active")
            }
            ACTION_STOP_ENGINE -> {
                dspEngine.stopEngine()
                updateNotification("Engine Standby")
            }
            ACTION_TOGGLE_OVERLAY -> {
                if (isOverlayShowing) {
                    removeFloatingWindow()
                } else {
                    showFloatingWindow()
                }
            }
            ACTION_STOP_SERVICE -> {
                dspEngine.stopEngine()
                removeFloatingWindow()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            else -> {
                // Default: show floating window if overlay enabled
                showFloatingWindow()
            }
        }
        return START_STICKY
    }

    private fun observeEngine() {
        engineStateJob = serviceScope.launch {
            dspEngine.isEngineActive.collectLatest { active ->
                updateNotification(if (active) "Live Voice Changer Active" else "Engine Standby")
                updateFloatingViewContent()
            }
        }
    }

    private fun createNotification(statusText: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingOpenApp = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopEngineIntent = Intent(this, FloatingVoiceService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        val pendingStop = PendingIntent.getService(
            this, 1, stopEngineIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val toggleMicIntent = Intent(this, FloatingVoiceService::class.java).apply {
            action = if (dspEngine.isEngineActive.value) ACTION_STOP_ENGINE else ACTION_START_ENGINE
        }
        val pendingToggleMic = PendingIntent.getService(
            this, 2, toggleMicIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val micActionLabel = if (dspEngine.isEngineActive.value) "MUTE MIC" else "START MIC"

        return NotificationCompat.Builder(this, XkbApplication.NOTIFICATION_CHANNEL_ID)
            .setContentTitle("XKB-SIR Voice Engine")
            .setContentText(statusText)
            .setSmallIcon(R.drawable.ic_launcher_generated)
            .setOngoing(true)
            .setContentIntent(pendingOpenApp)
            .addAction(android.R.drawable.ic_media_pause, micActionLabel, pendingToggleMic)
            .addAction(android.R.drawable.ic_delete, "EXIT", pendingStop)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNotification(statusText: String) {
        val notification = createNotification(statusText)
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        nm.notify(NOTIFICATION_ID, notification)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showFloatingWindow() {
        if (floatingContainer != null || windowManager == null) return

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        windowParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 30
            y = 200
        }

        val root = createFloatingLayout()
        floatingContainer = root

        try {
            windowManager?.addView(root, windowParams)
            isOverlayShowing = true
        } catch (e: Exception) {
            e.printStackTrace()
            isOverlayShowing = false
        }
    }

    private fun removeFloatingWindow() {
        if (floatingContainer != null && windowManager != null) {
            try {
                windowManager?.removeView(floatingContainer)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            floatingContainer = null
            isOverlayShowing = false
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createFloatingLayout(): View {
        val root = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // 1. Collapsed Bubble View
        val bubble = FrameLayout(this).apply {
            id = View.generateViewId()
            layoutParams = FrameLayout.LayoutParams(dpToPx(64), dpToPx(64))
            setBackgroundResource(R.drawable.bg_floating_bubble)
            elevation = 16f
        }

        val logoImg = ImageView(this).apply {
            layoutParams = FrameLayout.LayoutParams(dpToPx(56), dpToPx(56)).apply {
                gravity = Gravity.CENTER
            }
            setImageResource(R.drawable.xkb_logo_badge)
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        bubble.addView(logoImg)

        // 2. Expanded Panel View
        val panel = createExpandedPanel()
        panel.visibility = View.GONE

        root.addView(bubble)
        root.addView(panel)

        // Drag & Touch Listener on Bubble
        bubble.setOnTouchListener { _, event ->
            val params = windowParams ?: return@setOnTouchListener false
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()
                    if (abs(dx) > 10 || abs(dy) > 10) {
                        isDragging = true
                    }
                    if (isDragging) {
                        params.x = initialX + dx
                        params.y = initialY + dy
                        windowManager?.updateViewLayout(root, params)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        // Click detected: Toggle Expand
                        isExpanded = true
                        bubble.visibility = View.GONE
                        panel.visibility = View.VISIBLE
                        updateFloatingViewContent()
                    } else {
                        // Snap to nearest screen edge
                        val displayMetrics = resources.displayMetrics
                        val screenWidth = displayMetrics.widthPixels
                        val finalX = if (params.x + dpToPx(32) < screenWidth / 2) 20 else screenWidth - dpToPx(76)
                        params.x = finalX
                        windowManager?.updateViewLayout(root, params)
                    }
                    true
                }
                else -> false
            }
        }

        return root
    }

    private fun createExpandedPanel(): LinearLayout {
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.bg_floating_panel)
            setPadding(dpToPx(14), dpToPx(12), dpToPx(14), dpToPx(12))
            layoutParams = FrameLayout.LayoutParams(dpToPx(280), ViewGroup.LayoutParams.WRAP_CONTENT)
            elevation = 20f
        }

        // Title row
        val headerRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        val titleTv = TextView(this).apply {
            text = "XKB-SIR De"
            setTextColor(0xFF00F0FF.toInt())
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        val minimizeBtn = TextView(this).apply {
            text = "—"
            setTextColor(0xFF00F0FF.toInt())
            textSize = 18f
            setPadding(dpToPx(8), 0, dpToPx(8), 0)
            setOnClickListener {
                isExpanded = false
                floatingContainer?.let { root ->
                    val bubble = (root as ViewGroup).getChildAt(0)
                    val pnl = root.getChildAt(1)
                    pnl.visibility = View.GONE
                    bubble.visibility = View.VISIBLE
                }
            }
        }

        val closeBtn = TextView(this).apply {
            text = "✕"
            setTextColor(0xFFFF2A55.toInt())
            textSize = 16f
            setPadding(dpToPx(8), 0, dpToPx(4), 0)
            setOnClickListener {
                removeFloatingWindow()
            }
        }

        headerRow.addView(titleTv)
        headerRow.addView(minimizeBtn)
        headerRow.addView(closeBtn)
        panel.addView(headerRow)

        // Mic Power Button & Status
        val powerBtn = Button(this).apply {
            id = R.id.btn_floating_power
            text = if (dspEngine.isEngineActive.value) "STOP VOICE ENGINE" else "START VOICE ENGINE"
            setBackgroundResource(if (dspEngine.isEngineActive.value) R.drawable.btn_glow_red else R.drawable.btn_glow_cyan)
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 12f
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dpToPx(40)
            ).apply {
                topMargin = dpToPx(8)
                bottomMargin = dpToPx(8)
            }
            setOnClickListener {
                if (dspEngine.isEngineActive.value) {
                    dspEngine.stopEngine()
                } else {
                    dspEngine.startEngine()
                }
                updateFloatingViewContent()
            }
        }
        panel.addView(powerBtn)

        // Quick Preset Selector Row
        val presetLabel = TextView(this).apply {
            text = "QUICK PRESET:"
            setTextColor(0xFF8A9DBA.toInt())
            textSize = 10f
        }
        panel.addView(presetLabel)

        val presetRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                topMargin = dpToPx(4)
                bottomMargin = dpToPx(8)
            }
        }

        val btn3x = Button(this).apply {
            text = "3X BEAST"
            textSize = 10f
            setBackgroundResource(R.drawable.bg_preset_chip_active)
            setTextColor(0xFF00F0FF.toInt())
            layoutParams = LinearLayout.LayoutParams(0, dpToPx(32), 1f).apply { marginEnd = dpToPx(4) }
            setOnClickListener {
                dspEngine.updateParams(VoiceEffectParams.PRESET_3X_BEAST)
            }
        }

        val btnRobot = Button(this).apply {
            text = "ROBOT"
            textSize = 10f
            setBackgroundResource(R.drawable.bg_preset_chip)
            setTextColor(0xFFE6F8FF.toInt())
            layoutParams = LinearLayout.LayoutParams(0, dpToPx(32), 1f).apply { marginEnd = dpToPx(4) }
            setOnClickListener {
                dspEngine.updateParams(VoiceEffectParams.PRESET_CYBER_ROBOT)
            }
        }

        val btnClean = Button(this).apply {
            text = "CLEAN"
            textSize = 10f
            setBackgroundResource(R.drawable.bg_preset_chip)
            setTextColor(0xFFE6F8FF.toInt())
            layoutParams = LinearLayout.LayoutParams(0, dpToPx(32), 1f)
            setOnClickListener {
                dspEngine.updateParams(VoiceEffectParams.PRESET_CLEAN)
            }
        }

        presetRow.addView(btn3x)
        presetRow.addView(btnRobot)
        presetRow.addView(btnClean)
        panel.addView(presetRow)

        // Key Quick Sliders: Growl Intensity & Voice Mixing
        val growlLabel = TextView(this).apply {
            text = "Growl Intensity"
            setTextColor(0xFF8A9DBA.toInt())
            textSize = 11f
        }
        panel.addView(growlLabel)

        val growlSeek = SeekBar(this).apply {
            max = 100
            progress = (dspEngine.getParams().growlingIntensity * 100).toInt()
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        val current = dspEngine.getParams()
                        dspEngine.updateParams(current.copy(growlingIntensity = progress / 100f))
                    }
                }
                override fun onStartTrackingTouch(p0: SeekBar?) {}
                override fun onStopTrackingTouch(p0: SeekBar?) {}
            })
        }
        panel.addView(growlSeek)

        val mixLabel = TextView(this).apply {
            text = "Voice Mixing (Clean / Growl / Robot)"
            setTextColor(0xFF8A9DBA.toInt())
            textSize = 11f
        }
        panel.addView(mixLabel)

        val mixSeek = SeekBar(this).apply {
            max = 100
            progress = (dspEngine.getParams().voiceMixing * 100).toInt()
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        val current = dspEngine.getParams()
                        dspEngine.updateParams(current.copy(voiceMixing = progress / 100f))
                    }
                }
                override fun onStartTrackingTouch(p0: SeekBar?) {}
                override fun onStopTrackingTouch(p0: SeekBar?) {}
            })
        }
        panel.addView(mixSeek)

        // Open App Button
        val openAppBtn = Button(this).apply {
            text = "OPEN FULL DASHBOARD"
            setTextColor(0xFF00F0FF.toInt())
            textSize = 11f
            setBackgroundResource(R.drawable.bg_preset_chip)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dpToPx(36)
            ).apply {
                topMargin = dpToPx(8)
            }
            setOnClickListener {
                val intent = Intent(this@FloatingVoiceService, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                startActivity(intent)
            }
        }
        panel.addView(openAppBtn)

        return panel
    }

    private fun updateFloatingViewContent() {
        floatingContainer?.let { root ->
            val panel = (root as ViewGroup).findViewById<View>(R.id.btn_floating_power) as? Button
            val active = dspEngine.isEngineActive.value
            panel?.text = if (active) "STOP VOICE ENGINE" else "START VOICE ENGINE"
            panel?.setBackgroundResource(if (active) R.drawable.btn_glow_red else R.drawable.btn_glow_cyan)
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        engineStateJob?.cancel()
        removeFloatingWindow()
    }
}
