package com.example.appalmacen.ui.activities

import android.content.Intent // <--- Asegúrate de tener esta importación
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.appalmacen.databinding.ActivityRegisterBinding
import com.example.appalmacen.data.database.DatabaseHelper
import com.example.appalmacen.model.entities.Usuario
import com.example.appalmacen.data.repository.UsuarioRepository
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var usuarioRepository: UsuarioRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = DatabaseHelper.getInstance(this)
        usuarioRepository = UsuarioRepository(db.usuarioDAO())

        binding.btnRegister.setOnClickListener {
            registrarUsuario()
        }

        binding.tvBackToLogin.setOnClickListener {
            lifecycleScope.launch {
                val admins = usuarioRepository.getAdmins()
                if (admins.isNotEmpty()) {
                    val intent = Intent(this@RegisterActivity, SelectUserActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Debes registrar un administrador primero",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun registrarUsuario() {
        val nombre = binding.etRegNombre.text.toString().trim()
        val email = binding.etRegEmail.text.toString().trim()
        val password = binding.etRegPassword.text.toString()
        val confirmPassword = binding.etRegConfirmPassword.text.toString()

        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val nuevoUsuario = Usuario(
                nombre = nombre,
                email = email,
                password = password,
                foto = null,
                esAdmin = true
            )

            try {
                usuarioRepository.insert(nuevoUsuario)
                Toast.makeText(this@RegisterActivity, "Usuario registrado con éxito", Toast.LENGTH_SHORT).show()

                // --- CAMBIO AQUÍ: Navegar a SelectUserActivity ---
                val intent = Intent(this@RegisterActivity, SelectUserActivity::class.java)
                startActivity(intent)

                // Opcional: cerramos la pantalla de registro para que no pueda volver atrás con el botón físico
                finish()

            } catch (e: Exception) {
                Toast.makeText(this@RegisterActivity, "Error al registrar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}