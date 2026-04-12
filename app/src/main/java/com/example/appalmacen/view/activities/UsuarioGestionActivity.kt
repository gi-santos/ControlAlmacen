package com.example.appalmacen.view.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appalmacen.databinding.ActivityUsuariosGestionBinding
import com.example.appalmacen.model.database.DatabaseHelper
import com.example.appalmacen.model.entities.Usuario
import com.example.appalmacen.model.repository.UsuarioRepository
import com.example.appalmacen.view.adapters.UsuarioGestionAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class UsuarioGestionActivity : BaseActivity() {

    private lateinit var binding: ActivityUsuariosGestionBinding
    private lateinit var usuarioRepo: UsuarioRepository
    private lateinit var adapter: UsuarioGestionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsuariosGestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        usuarioRepo = UsuarioRepository(DatabaseHelper.getInstance(this).usuarioDAO())
        setupRecyclerView()
        observeUsuarios()

        binding.fabAddUsuario.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.toolbar.setNavigationIcon(android.R.drawable.ic_menu_revert)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = UsuarioGestionAdapter(
            emptyList(),
            onEdit = { usuario ->
                val intent = Intent(this, RegisterActivity::class.java).apply {
                    putExtra("USUARIO_ID", usuario.id)
                }
                startActivity(intent)
            },
            onDelete = { usuario ->
                confirmarBorrado(usuario)
            }
        )
        binding.rvUsuariosGestion.layoutManager = LinearLayoutManager(this)
        binding.rvUsuariosGestion.adapter = adapter
    }

    private fun observeUsuarios() {
        lifecycleScope.launch {
            usuarioRepo.allUsuarios.collectLatest { lista ->
                adapter.updateList(lista)
            }
        }
    }

    private fun confirmarBorrado(usuario: Usuario) {
        if (usuario.esAdmin && usuario.nombre == "Administrador") {
            Toast.makeText(this, "No se puede eliminar al administrador principal", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Eliminar Usuario")
            .setMessage("¿Estás seguro de que deseas eliminar a ${usuario.nombre}?")
            .setPositiveButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    usuarioRepo.delete(usuario)
                    Toast.makeText(this@UsuarioGestionActivity, "Usuario eliminado", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
