package com.example.appalmacen.view.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appalmacen.controller.SesionController
import com.example.appalmacen.databinding.ActivityMainBinding
import com.example.appalmacen.model.database.DatabaseHelper
import com.example.appalmacen.model.entities.Producto
import com.example.appalmacen.model.repository.ProductoRepository
import com.example.appalmacen.model.repository.UsuarioRepository
import com.example.appalmacen.utils.PreferencesManager
import com.example.appalmacen.view.adapters.ProductoAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sesionController: SesionController
    private lateinit var productoRepo: ProductoRepository
    private lateinit var adapter: ProductoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = DatabaseHelper.getInstance(this)
        val usuarioRepo = UsuarioRepository(db.usuarioDAO())
        productoRepo = ProductoRepository(db.productoDAO(), db.interaccionDAO())
        
        val prefManager = PreferencesManager(this)
        sesionController = SesionController(usuarioRepo, prefManager)

        setupRecyclerView()

        // Saludo personalizado y carga de interacciones
        SesionController.usuarioActivo?.let { usuario ->
            binding.tvWelcome.text = "¡Hola, ${usuario.nombre}!"
            if (usuario.esAdmin) {
                binding.btnGestionUsuarios.visibility = View.VISIBLE
            }
            cargarInteraccionesRecientes(usuario.id)
        }

        binding.btnGestionUsuarios.setOnClickListener {
            startActivity(Intent(this, UsuarioGestionActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            sesionController.logout()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        adapter = ProductoAdapter(emptyList()) { producto ->
            // Al pulsar un producto, registramos la interacción
            SesionController.usuarioActivo?.let { usuario ->
                lifecycleScope.launch {
                    productoRepo.registrarInteraccion(usuario.id, producto.id)
                    // Aquí podrías ir al detalle del producto si existiera
                }
            }
        }
        binding.rvRecientes.layoutManager = LinearLayoutManager(this)
        binding.rvRecientes.adapter = adapter
    }

    private fun cargarInteraccionesRecientes(usuarioId: Int) {
        lifecycleScope.launch {
            productoRepo.getUltimasInteracciones(usuarioId).collectLatest { productos ->
                adapter.updateList(productos)
                if (productos.isEmpty()) {
                    binding.tvInteraccionesTitulo.text = "Aún no tienes interacciones"
                } else {
                    binding.tvInteraccionesTitulo.text = "Tus últimas interacciones"
                }
            }
        }
    }
}
