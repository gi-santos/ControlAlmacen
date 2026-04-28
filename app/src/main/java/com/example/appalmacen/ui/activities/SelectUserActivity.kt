package com.example.appalmacen.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.appalmacen.controller.SesionController
import com.example.appalmacen.databinding.ActivitySelectUserBinding
import com.example.appalmacen.data.database.DatabaseHelper
import com.example.appalmacen.data.repository.UsuarioRepository
import com.example.appalmacen.utils.PreferencesManager
import com.example.appalmacen.ui.adapters.UsuarioSelectorAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class SelectUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectUserBinding
    private lateinit var usuarioRepository: UsuarioRepository

    private lateinit var sesionController: SesionController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Inicializar la base de datos y el Repositorio PRIMERO
        val db = DatabaseHelper.getInstance(this)
        usuarioRepository = UsuarioRepository(db.usuarioDAO())

        // 2. Ahora que usuarioRepository TIENE valor, inicializamos el controlador
        val prefManager = PreferencesManager(this)
        sesionController = SesionController(usuarioRepository, prefManager)

        // 3. Configurar la UI
        binding.rvUsuarios.layoutManager = GridLayoutManager(this, 3)

        // 4. Configurar eventos de clics
        binding.btnNuevoUsuario.setOnClickListener {
            mostrarDialogoValidacionAdmin(NewUserActivity::class.java)
        }

        binding.tvContactarAdmin.setOnClickListener {
            // Tu lógica de contacto
        }

        // 5. Cargar los datos (usa el repositorio, por eso va al final)
        cargarUsuarios()

}

    private fun mostrarDialogoValidacionAdmin(destino: Class<*>) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Acceso Administrativo")
        builder.setMessage("Ingrese sus credenciales de administrador")

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 40, 60, 10)
        }

        val inputEmail = EditText(this).apply { hint = "Email de Admin" }
        val inputPass = EditText(this).apply {
            hint = "Contraseña"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        layout.addView(inputEmail)
        layout.addView(inputPass)
        builder.setView(layout)

        builder.setPositiveButton("Verificar") { _, _ ->
            val email = inputEmail.text.toString()
            val pass = inputPass.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                // Aquí pasamos el destino a tu lógica de validación
                validarYEntrar(email, pass, destino)
            } else {
                Toast.makeText(this, "Campos obligatorios", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun validarYEntrar(email: String, pass: String, destino: Class<*>) {
        lifecycleScope.launch {
            // Usamos la misma lógica que en tu LoginActivity
            val esValido = sesionController.login(email, pass)

            if (esValido) {
                // Si el login es exitoso, navegamos a la actividad de registro
                val intent = Intent(this@SelectUserActivity, destino)
                startActivity(intent)
            } else {
                Toast.makeText(this@SelectUserActivity, "Credenciales de administrador no válidas", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cargarUsuarios() {
        lifecycleScope.launch {
            usuarioRepository.habilitados.collectLatest { usuarios ->
                val adapter = UsuarioSelectorAdapter(usuarios) { usuarioSeleccionado ->

                    if (usuarioSeleccionado.esAdmin) {
                        mostrarDialogoValidacionAdmin(NewProductActivity::class.java)
                    } else {
                        // Si NO es admin, creamos el Intent explícitamente
                        val intentProduct = Intent(this@SelectUserActivity, SelectProductActivity::class.java)
                        intentProduct.putExtra("user_id", usuarioSeleccionado.id)
                        startActivity(intentProduct)
                    }
                }
                binding.rvUsuarios.adapter = adapter
            }
        }
    }
}