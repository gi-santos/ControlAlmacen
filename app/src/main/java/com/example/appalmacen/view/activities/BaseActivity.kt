package com.example.appalmacen.view.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.appalmacen.controller.SesionController
import com.example.appalmacen.model.database.DatabaseHelper
import com.example.appalmacen.model.repository.UsuarioRepository
import com.example.appalmacen.utils.PreferencesManager

//todas las pantallas que funcionen con timer tienen que heredar directamente de esta
open class BaseActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private var logoutTime: Long = 10 * 60 * 1000

    private val logoutRunnable = Runnable {
        cerrarSesion()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefManager = PreferencesManager(this)
        logoutTime = prefManager.getLogoutTimeout()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        resetTimer()
    }

    private fun resetTimer() {
        handler.removeCallbacks(logoutRunnable)
        handler.postDelayed(logoutRunnable, logoutTime)
    }

    private fun cerrarSesion() {
        // Limpiar sesión
        val usuarioRepo = UsuarioRepository(DatabaseHelper.getInstance(this).usuarioDAO())
        val prefManager = PreferencesManager(this)
        val sesionController = SesionController(usuarioRepo, prefManager)
        sesionController.logout()

        val intent = Intent(this, LoginActivity::class.java)
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