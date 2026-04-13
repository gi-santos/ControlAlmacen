package com.example.appalmacen.view.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sesionController: SesionController
    private lateinit var productoRepo: ProductoRepository
    private lateinit var adapter: ProductoAdapter
    private var searchJob: Job? = null

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
        setupSearch()

        // Saludo personalizado y carga inicial
        SesionController.usuarioActivo?.let { usuario ->
            binding.tvWelcome.text = "¡Hola, ${usuario.nombre}!"
            binding.fabAddProducto.visibility = View.VISIBLE
            if (usuario.esAdmin) {
                binding.btnGestionUsuarios.visibility = View.VISIBLE
            }
            cargarInteraccionesRecientes(usuario.id)
        }

        binding.fabAddProducto.setOnClickListener {
            startActivity(Intent(this, ProductoGestionActivity::class.java))
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
        adapter = ProductoAdapter(
            productos = emptyList(),
            onUpdateStock = { producto, cambio ->
                lifecycleScope.launch {
                    val nuevaCantidad = (producto.cantidad + cambio).coerceAtLeast(0)
                    val productoActualizado = producto.copy(cantidad = nuevaCantidad)
                    productoRepo.update(productoActualizado)
                    
                    // Al actualizar stock, también lo registramos como interacción
                    SesionController.usuarioActivo?.let { usuario ->
                        productoRepo.registrarInteraccion(usuario.id, producto.id)
                    }
                }
            },
            onClick = { producto ->
                SesionController.usuarioActivo?.let { usuario ->
                    lifecycleScope.launch {
                        productoRepo.registrarInteraccion(usuario.id, producto.id)
                        if (binding.etSearch.text.isEmpty()) {
                            cargarInteraccionesRecientes(usuario.id)
                        }
                    }
                }
            }
        )
        binding.rvRecientes.layoutManager = LinearLayoutManager(this)
        binding.rvRecientes.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    delay(300) // Debounce para no saturar la DB
                    if (query.isEmpty()) {
                        binding.tvInteraccionesTitulo.text = "Tus últimas interacciones"
                        SesionController.usuarioActivo?.let { cargarInteraccionesRecientes(it.id) }
                    } else {
                        binding.tvInteraccionesTitulo.text = "Resultados de búsqueda"
                        productoRepo.searchProductos(query).collectLatest { productos ->
                            adapter.updateList(productos)
                        }
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun cargarInteraccionesRecientes(usuarioId: Int) {
        lifecycleScope.launch {
            productoRepo.getUltimasInteracciones(usuarioId).collectLatest { productos ->
                // Solo actualizamos si el buscador está vacío para no pisar resultados
                if (binding.etSearch.text.isEmpty()) {
                    adapter.updateList(productos)
                    binding.tvInteraccionesTitulo.text = if (productos.isEmpty()) 
                        "Aún no tienes interacciones" else "Tus últimas interacciones"
                }
            }
        }
    }
}
