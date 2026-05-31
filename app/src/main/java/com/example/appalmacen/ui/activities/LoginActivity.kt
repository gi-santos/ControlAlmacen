package com.example.appalmacen.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.appalmacen.controller.SesionController
import com.example.appalmacen.databinding.ActivityLoginBinding
import com.example.appalmacen.data.database.DatabaseHelper
import com.example.appalmacen.data.repository.UsuarioRepository
import com.example.appalmacen.utils.PreferencesManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sesionController: SesionController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val usuarioRepo = UsuarioRepository(DatabaseHelper.getInstance(this).usuarioDAO())
        val prefManager = PreferencesManager(this)
        sesionController = SesionController(usuarioRepo, prefManager)


        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
            } else {
                realizarLogin(email, password)
            }
        }


        binding.tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun realizarLogin(email: String, password: String) {
        lifecycleScope.launch {
            val exito = sesionController.login(email, password)
            if (exito) {

                val intent = Intent(this@LoginActivity, SelectUserActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this@LoginActivity, "Email o contraseña incorrectos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
