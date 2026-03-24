package com.example.appalmacen.view.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.appalmacen.controller.SesionController
import com.example.appalmacen.databinding.ActivityMainBinding
import com.example.appalmacen.model.database.DatabaseHelper
import com.example.appalmacen.model.repository.UsuarioRepository
import com.example.appalmacen.utils.PreferencesManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sesionController: SesionController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar controlador para poder cerrar sesión
        val usuarioRepo = UsuarioRepository(DatabaseHelper.getInstance(this).usuarioDAO())
        val prefManager = PreferencesManager(this)
        sesionController = SesionController(usuarioRepo, prefManager)

        // Saludo personalizado si hay un usuario activo
        SesionController.usuarioActivo?.let {
            binding.tvWelcome.text = "¡Hola, ${it.nombre}!"
        }

        // Botón de Cerrar Sesión
        binding.btnLogout.setOnClickListener {
            sesionController.logout()
            // Ir al Login y limpiar el historial de pantallas
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
