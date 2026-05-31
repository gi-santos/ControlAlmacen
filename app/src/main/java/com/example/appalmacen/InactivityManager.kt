package com.example.appalmacen

import android.os.Handler
import android.os.Looper

object InactivityManager {

    private const val TIMEOUT_MS = 10 * 60 * 1000L  // 10 minutos
    private val handler = Handler(Looper.getMainLooper())
    private var onTimeout: (() -> Unit)? = null
    private var isEnabled = false

    private val timeoutRunnable = Runnable {
        onTimeout?.invoke()
    }

    fun init(callback: () -> Unit) {
        onTimeout = callback
    }

    fun enable() {
        isEnabled = true
        reset()
    }

    fun disable() {
        isEnabled = false
        handler.removeCallbacks(timeoutRunnable)
    }

    fun reset() {
        if (!isEnabled) return
        handler.removeCallbacks(timeoutRunnable)
        handler.postDelayed(timeoutRunnable, TIMEOUT_MS)
    }
}