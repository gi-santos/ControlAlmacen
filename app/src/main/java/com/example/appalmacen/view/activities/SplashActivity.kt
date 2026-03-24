package com.example.appalmacen.view.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.appalmacen.controller.SesionController
import com.example.appalmacen.model.database.DatabaseHelper
import com.example.appalmacen.model.entities.Usuario
import com.example.appalmacen.model.repository.UsuarioRepository
import com.example.appalmacen.utils.PreferencesManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Podrías poner un layout con un logo aquí si quieres
        // setContentView(R.layout.activity_splash)

        val db = DatabaseHelper.getInstance(this)
        val usuarioRepo = UsuarioRepository(db.usuarioDAO())
        val prefManager = PreferencesManager(this)
        val sesionController = SesionController(usuarioRepo, prefManager)

        lifecycleScope.launch {
            // 1. Crear usuario por defecto si la base de datos está vacía
            crearUsuarioAdminSiNoExiste(usuarioRepo)

            // 2. Pequeña pausa para que se vea el logo (opcional)
            delay(1500)

            // 3. Decidir a qué pantalla ir
            if (sesionController.restaurarSesion()) {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            } else {
                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            }
            finish()
        }
    }

    private suspend fun crearUsuarioAdminSiNoExiste(repository: UsuarioRepository) {
        val usuarios = repository.allUsuarios.first()
        if (usuarios.isEmpty()) {
            val admin = Usuario(
                nombre = "Administrador",
                email = "admin@almacen.com",
                password = "admin123",
                esAdmin = true,
                foto = null
            )
            repository.insert(admin)
        }
    }
}
