package com.example.appalmacen

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.appalmacen.view.activities.SplashActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Redirigir siempre a SplashActivity al arrancar para gestionar el login
        startActivity(Intent(this, SplashActivity::class.java))
        finish()
    }
}