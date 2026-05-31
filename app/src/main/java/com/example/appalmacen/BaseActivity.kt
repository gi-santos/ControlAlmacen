package com.example.appalmacen

import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

    override fun onResume() {
        super.onResume()
        InactivityManager.enable()
    }

    override fun onPause() {
        super.onPause()
        InactivityManager.disable()
    }


    override fun dispatchTouchEvent(ev: android.view.MotionEvent?): Boolean {
        InactivityManager.reset()
        return super.dispatchTouchEvent(ev)
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        InactivityManager.reset()
        return super.dispatchKeyEvent(event)
    }
}