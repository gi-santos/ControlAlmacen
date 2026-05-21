package com.example.appalmacen

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.appalmacen.controller.SesionController
import com.example.appalmacen.databinding.ActivityMainBinding
import com.example.appalmacen.data.database.DatabaseHelper
import com.example.appalmacen.data.repository.UsuarioRepository
import com.example.appalmacen.utils.PreferencesManager
import com.example.appalmacen.ui.activities.LoginActivity
import com.example.appalmacen.ui.activities.NewProductActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sesionController: SesionController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val usuarioRepo = UsuarioRepository(DatabaseHelper.getInstance(this).usuarioDAO())
        val prefManager = PreferencesManager(this)
        sesionController = SesionController(usuarioRepo, prefManager)

        SesionController.usuarioActivo?.let {
            binding.tvWelcome.text = "¡Hola, ${it.nombre}!"
        }

        // Botón de Cerrar Sesión
        binding.btnLogout.setOnClickListener {
            sesionController.logout()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }


        binding.btnAddProduct.setOnClickListener {
            val intent = Intent(this, NewProductActivity::class.java)
            startActivity(intent)
        }

    }
}
