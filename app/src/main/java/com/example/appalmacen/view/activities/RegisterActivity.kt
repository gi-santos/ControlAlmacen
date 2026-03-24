package com.example.appalmacen.view.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.appalmacen.databinding.ActivityRegisterBinding
import com.example.appalmacen.model.database.DatabaseHelper
import com.example.appalmacen.model.entities.Usuario
import com.example.appalmacen.model.repository.UsuarioRepository
import kotlinx.coroutines.launch


// Hay que quitar BaseActivity y volver a poner AppCompativity en donde no queramos
// que el timer funcione
class RegisterActivity : BaseActivity() {

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
            finish()
        }
    }

    private fun registrarUsuario() {
        val nombre = binding.etRegNombre.text.toString()
        val email = binding.etRegEmail.text.toString()
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
                esAdmin = false // Por defecto los registrados no son admin
            )

            try {
                usuarioRepository.insert(nuevoUsuario)
                Toast.makeText(this@RegisterActivity, "Usuario registrado con éxito", Toast.LENGTH_SHORT).show()
                finish() // Volver al login
            } catch (e: Exception) {
                Toast.makeText(this@RegisterActivity, "Error al registrar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
