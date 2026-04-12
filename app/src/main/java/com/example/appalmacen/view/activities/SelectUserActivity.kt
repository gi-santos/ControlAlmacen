package com.example.appalmacen.view.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.appalmacen.databinding.ActivitySelectUserBinding
import com.example.appalmacen.model.database.DatabaseHelper
import com.example.appalmacen.model.repository.UsuarioRepository
import com.example.appalmacen.view.adapters.UsuarioSelectorAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SelectUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectUserBinding
    private lateinit var usuarioRepository: UsuarioRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = DatabaseHelper.getInstance(this)
        usuarioRepository = UsuarioRepository(db.usuarioDAO())

        // Configurar el grid de 3 columnas ya aquí, antes de cargar datos
        binding.rvUsuarios.layoutManager = GridLayoutManager(this, 3)

        cargarUsuarios()

        binding.tvContactarAdmin.setOnClickListener {
            // Aquí puedes abrir un diálogo o mandar un email
        }
    }

    private fun cargarUsuarios() {
        lifecycleScope.launch {
            // Usamos habilitados para que solo aparezcan usuarios activos
            usuarioRepository.habilitados.collectLatest { usuarios ->
                val adapter = UsuarioSelectorAdapter(usuarios) { usuarioSeleccionado ->
                    val intent = Intent(this@SelectUserActivity, LoginActivity::class.java)
                    intent.putExtra("usuario_id", usuarioSeleccionado.id)
                    startActivity(intent)
                }
                binding.rvUsuarios.adapter = adapter
            }
        }
    }
}