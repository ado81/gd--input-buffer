package com.ado81.gdinputbuffer

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast

class OverlayService : Service() {
    private lateinit var windowManager: WindowManager
    private var overlayButton: Button? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val button = Button(this).apply {
            text = "GD TAP"
            setOnClickListener {
                isEnabled = false
                handler.postDelayed({
                    Toast.makeText(this@OverlayService, "Entrada procesada (demo)", Toast.LENGTH_SHORT).show()
                    isEnabled = true
                }, 100L)
            }
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 40
            y = 180
        }
        button.setOnTouchListener(object : android.view.View.OnTouchListener {
            private var downX = 0f
            private var downY = 0f
            private var startX = 0
            private var startY = 0
            private var dragging = false

            override fun onTouch(v: android.view.View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        downX = event.rawX
                        downY = event.rawY
                        startX = params.x
                        startY = params.y
                        dragging = false
                        return false
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - downX).toInt()
                        val dy = (event.rawY - downY).toInt()
                        if (kotlin.math.abs(dx) > 8 || kotlin.math.abs(dy) > 8) dragging = true
                        if (dragging) {
                            params.x = startX + dx
                            params.y = startY + dy
                            windowManager.updateViewLayout(button, params)
                        }
                    }
                    MotionEvent.ACTION_UP -> if (dragging) return true
                }
                return dragging
            }
        })
        windowManager.addView(button, params)
        overlayButton = button
    }

    override fun onDestroy() {
        overlayButton?.let { windowManager.removeView(it) }
        overlayButton = null
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
