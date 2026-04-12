package com.example.appalmacen.view.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appalmacen.controller.SesionController
import com.example.appalmacen.databinding.ActivityLoginBinding
import com.example.appalmacen.model.database.DatabaseHelper
import com.example.appalmacen.model.entities.Usuario
import com.example.appalmacen.model.repository.UsuarioRepository
import com.example.appalmacen.utils.PreferencesManager
import com.example.appalmacen.view.adapters.UsuarioLoginAdapter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sesionController: SesionController
    private lateinit var usuarioRepo: UsuarioRepository
    private lateinit var adapter: UsuarioLoginAdapter
    private var selectedAdmin: Usuario? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar componentes
        usuarioRepo = UsuarioRepository(DatabaseHelper.getInstance(this).usuarioDAO())
        val prefManager = PreferencesManager(this)
        sesionController = SesionController(usuarioRepo, prefManager)

        setupRecyclerView()
        cargarUsuarios()

        binding.btnConfirmAdmin.setOnClickListener {
            val password = binding.etAdminPassword.text.toString()
            if (password.isEmpty()) {
                Toast.makeText(this, "Introduce la contraseña", Toast.LENGTH_SHORT).show()
            } else {
                realizarLoginAdmin(password)
            }
        }

        binding.btnCancelAdmin.setOnClickListener {
            binding.layoutAdminPass.visibility = View.GONE
            selectedAdmin = null
            binding.etAdminPassword.text.clear()
        }
    }

    private fun setupRecyclerView() {
        adapter = UsuarioLoginAdapter(emptyList()) { usuario ->
            if (usuario.esAdmin) {
                // Si es admin, pedir contraseña
                selectedAdmin = usuario
                binding.tvAdminSelected.text = "Admin: ${usuario.nombre}"
                binding.layoutAdminPass.visibility = View.VISIBLE
                binding.etAdminPassword.requestFocus()
            } else {
                // Si no es admin, login directo
                sesionController.loginDirecto(usuario)
                irAMain()
            }
        }
        binding.rvUsuarios.layoutManager = LinearLayoutManager(this)
        binding.rvUsuarios.adapter = adapter
    }

    private fun cargarUsuarios() {
        lifecycleScope.launch {
            val lista = usuarioRepo.habilitados.first()
            adapter.updateList(lista)
        }
    }

    private fun realizarLoginAdmin(password: String) {
        val admin = selectedAdmin ?: return
        lifecycleScope.launch {
            val exito = sesionController.login(admin.email, password)
            if (exito) {
                irAMain()
            } else {
                Toast.makeText(this@LoginActivity, "Contraseña incorrecta", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun irAMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
