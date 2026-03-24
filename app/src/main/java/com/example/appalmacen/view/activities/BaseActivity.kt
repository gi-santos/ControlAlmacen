package com.example.appalmacen.view.activities

import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

//todas las opantallas que funcionen con timer tienen que heredar directamente de esta
// NN de de AppCompatActivity
open class BaseActivity : AppCompatActivity() {

    // 10 minutos en milisegundos
    private val logoutTime: Long = 10 * 60 * 1000
    private val handler = Handler(Looper.getMainLooper())

    private val logoutRunnable = Runnable {
        cerrarSesion()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        resetTimer()
    }

    private fun resetTimer() {
        handler.removeCallbacks(logoutRunnable)
        handler.postDelayed(logoutRunnable, logoutTime)
    }

    //Cambiar el MainActivity por  la de seleccionar perfil
    private fun cerrarSesion() {

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        resetTimer()
    }

    override fun onStop() {
        super.onStop()

        handler.removeCallbacks(logoutRunnable)
    }
}